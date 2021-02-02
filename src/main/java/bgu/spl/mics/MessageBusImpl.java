package bgu.spl.mics;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {//singleton

    private static class MessageBusHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }

    //should be concurrent because:
    //it is possible (in the framework) that someone else unregisters the microservice
    //while ms is chosen in round robin
    private ConcurrentHashMap<MicroService, ConcurrentLinkedQueue<Message>> MQ;
    //acts as a shared resource: send event and send broadcast at the same time
    private ConcurrentHashMap<Class<? extends Message>, ConcurrentLinkedQueue<MicroService>> subscribers;
    //it is possible (in the framework) that two ms complete the same event at the same time
    private ConcurrentHashMap<Event<?>, Future> results;


    private MessageBusImpl() {
        MQ = new ConcurrentHashMap<>();
        subscribers = new ConcurrentHashMap<>();
        results = new ConcurrentHashMap<>();

    }

    public static MessageBusImpl getInstance() {
        return MessageBusHolder.instance;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        subscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
        subscribers.get(type).add(m);
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        subscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
        subscribers.get(type).add(m);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void complete(Event<T> e, T result) {
        if (results.containsKey(e)) {
            results.get(e).resolve(result);
        }
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        if (subscribers.containsKey(b.getClass())) {
            Queue<MicroService> subs = subscribers.get(b.getClass());
            synchronized (subs) { //so no changes would occur by other thread while using it
                for (MicroService m : subs) { //if there is no subscribers - nothing happens
                    synchronized (MQ.get(m)) { //for waking up who's waiting on this key in awaitMessage
                        MQ.get(m).add(b);
                        MQ.get(m).notifyAll();
                    }
                }
            }
        }
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        //event does not exist=no microService has subscribed
        if (!subscribers.containsKey(e.getClass())) return null;
        Future<T> future = new Future<>();
        synchronized (subscribers.get(e.getClass())) { //so no changes would occur by other thread while using it
            //no microService has subscribed to this type of messages
            if (subscribers.get(e.getClass()).isEmpty()) return null;
            results.put(e, future); //add to event-results data structure
            MicroService m = subscribers.get(e.getClass()).poll(); //first ms in this event subscribers = round robin
            synchronized (MQ.get(m)) { //for waking up who's waiting on this key in awaitMessage
                MQ.get(m).add(e);
                MQ.get(m).notifyAll();
            }
            subscribers.get(e.getClass()).add(m); //back to subscribers = round robin
        }
        return future;
    }


    @Override
    public void register(MicroService m) {
        MQ.putIfAbsent(m, new ConcurrentLinkedQueue<>());
    }

    @Override
    public void unregister(MicroService m) {
        if (MQ.containsKey(m)) {
            MQ.remove(m);
            //removal from subscribers
            for (Queue q : subscribers.values()) {
                if (q.contains(m))
                    q.remove(m);
            }
        }
    }


    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        //m was never registered
        if (!MQ.containsKey(m))
            throw new IllegalStateException("ms did not register yet");
        //is registered
        if (MQ.get(m).isEmpty()) {
            synchronized (MQ.get(m)) { //waiting on m's message queue monitor
                while (MQ.get(m).isEmpty()) {
                    try {
                        MQ.get(m).wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        return MQ.get(m).poll();
    }
}
