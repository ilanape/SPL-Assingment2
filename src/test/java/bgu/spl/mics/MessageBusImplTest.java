package bgu.spl.mics;

import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TestEvent;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.services.TestMS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageBusImplTest {

    private MessageBusImpl MB; //changed to MessageBusImpl instance

    @BeforeEach
    void setUp() {
        MB= MessageBusImpl.getInstance(); //added after implementation as singleton
    }

    /**
     * Being checked in sendEvent test
     * because in order to receive a message, it should be sent first
     * and then wait in the receiver's queue which is checked by awaitMessage()
     */
    @Test
    void subscribeEvent() {}

    /**
     * Being checked in sendBroadcast test
     * because in order to receive a message, it should be sent first
     * and then wait in the receiver's queue which is checked by awaitMessage()
     */
    @Test
    void subscribeBroadcast() {}

    /**
     * Checks if the result of the event thats being completed is the result that future object received.
     */
    @Test
    void complete() {
        //added a ms that is registered
        //as the sendEvent function needs someone to be registered to deliver the message
        MicroService receiver = new TestMS();
        MB.register(receiver);
        MB.subscribeEvent(TestEvent.class, receiver);
        Event<Boolean> event= new TestEvent(); //added dummy event
        Future future= MB.sendEvent(event);
        MB.complete(event, true);
        assertTrue(future.isDone());
        assertEquals(true,future.get());
    }

    /**
     * Checks MB sendBroadcast() by:
     * Calling MS sendBroadcast() which should use MB's.
     * Asserting awaitMessage() returns the message that was sent.
     *
     * Checks MB subscribeBroadcast() by:
     * Calling MS subscribeBroadcast() which should use MB's.
     * Asserting awaitMessage() returns the message that was sent.
     *
     * Checks awaitMessage() by:
     * Comparison of the received message and the one that was sent.
     */
    @Test
    void sendBroadcast() {
        MicroService sender = new TestMS();
        MicroService receiver1 = new TestMS();
        MicroService receiver2 = new TestMS();
        Broadcast someMessage= new Broadcast() {};

        MB.register(receiver1);
        MB.register(receiver2);
        receiver1.subscribeBroadcast(someMessage.getClass(),c -> {});
        receiver2.subscribeBroadcast(someMessage.getClass(),c -> {});
        sender.sendBroadcast(someMessage);
        try{
            Message receivedMessage1= MB.awaitMessage(receiver1);
            Message receivedMessage2= MB.awaitMessage(receiver2);
            assertEquals(someMessage,receivedMessage1);
            assertEquals(someMessage,receivedMessage2);
        } catch (InterruptedException e){ fail(); }
    }

    /**
     * Checks MB sendEvent() by:
     * Calling MS sendEvent() which should use MB's.
     * Asserting awaitMessage() returns the message that was sent.
     *
     * Checks MB subscribeEvent() by:
     * Calling MS subscribeEvent() which should use MB's.
     * Asserting awaitMessage() returns the message that was sent.
     *
     * Checks awaitMessage() by:
     * Comparison of the received message and the one that was sent.
     */
    @Test
    void sendEvent() {
        MicroService sender = new TestMS();
        MicroService receiver1 = new TestMS();
        Event<AttackEvent> event= new Event<AttackEvent>() {};

        MB.register(receiver1);
        receiver1.subscribeEvent(event.getClass(), c -> {});
        sender.sendEvent(event);

        try{
            Message receivedMessage1= MB.awaitMessage(receiver1);
            assertEquals(event,receivedMessage1);
        } catch (InterruptedException e){ fail(); }
    }

    /**
     * Being checked in send tests because in order to check if the reciever got the message
     * it should register and then subscribe to the message type.
     */
    @Test
    void register() {
    }

    @Test //no need to test
    void unregister() {}

    /**
     * changed to:
     * Empty scenario deleted
     * Checked only in the sendEvent scenario
     * @throws InterruptedException
     */
    @Test
    void awaitMessage() throws InterruptedException {
    }

}