package bgu.spl.mics;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 */
public class MessageBusImpl implements MessageBus {//singleton

    //Singleton
    private static class MessageBusHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }

    private ConcurrentHashMap<MicroService, ConcurrentLinkedQueue<Message>> MessageQueue;
    private ConcurrentHashMap<Class<? extends Message>, ConcurrentLinkedQueue<MicroService>> subscribers;
    private ConcurrentHashMap<Event<?>, Future> results;

    private MessageBusImpl() {
        MessageQueue = new ConcurrentHashMap<>();
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
    public <T> void complete(Event<T> e, T result) {
        if (results.containsKey(e)) {
            results.get(e).resolve(result);
        }
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        if (subscribers.containsKey(b.getClass())) {
            Queue<MicroService> subsTob = subscribers.get(b.getClass());
            synchronized (subsTob) { //so no changes would occur by other thread while using it
                for (MicroService m : subsTob) { //if there is no subscribers - nothing happens
                    synchronized (MessageQueue.get(m)) { //for waking up who's waiting on this key in awaitMessage
                        MessageQueue.get(m).add(b);
                        MessageQueue.get(m).notifyAll();
                    }
                }
            }
        }
    }

    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        //event does not exist means no microService has subscribed
        if (!subscribers.containsKey(e.getClass())) return null;

        Future<T> future = new Future<>();
        synchronized (subscribers.get(e.getClass())) { //so no changes would occur by other thread while using it
            if (subscribers.get(e.getClass()).isEmpty()) return null;

            results.put(e, future); //add to event-results data structure
            //first MS in this event subscribers = round robin manner
            MicroService m = subscribers.get(e.getClass()).poll();
            synchronized (MessageQueue.get(m)) { //for waking up who's waiting on this key in awaitMessage
                MessageQueue.get(m).add(e);
                MessageQueue.get(m).notifyAll();
            }
            subscribers.get(e.getClass()).add(m); //back to subscribers = round robin manner
        }
        return future;
    }

    @Override
    public void register(MicroService m) {
        MessageQueue.putIfAbsent(m, new ConcurrentLinkedQueue<>());
    }

    @Override
    public void unregister(MicroService m) {
        if (MessageQueue.containsKey(m)) {
            MessageQueue.remove(m);
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
        if (!MessageQueue.containsKey(m))
            throw new IllegalStateException("this MS did not register yet");

        //is registered
        if (MessageQueue.get(m).isEmpty()) {
            synchronized (MessageQueue.get(m)) { //waiting on m's message queue monitor
                while (MessageQueue.get(m).isEmpty()) {
                    try {
                        MessageQueue.get(m).wait();
                    } catch (InterruptedException e) {}
                }
            }
        }
        return MessageQueue.get(m).poll();
    }
}
