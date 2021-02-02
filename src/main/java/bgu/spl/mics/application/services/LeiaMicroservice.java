package bgu.spl.mics.application.services;

import java.util.Vector;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Main;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.Finish;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.messages.AttackEvent;

/**
 * LeiaMicroservices Initialized with Attack objects, and sends them as  {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LeiaMicroservice extends MicroService {
    private Attack[] attacks;
    private Vector<Future<Boolean>> results;

    public LeiaMicroservice(Attack[] attacks) {
        super("Leia");
        this.attacks = attacks;
        results = new Vector<>();
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(Finish.class, (Finish broadcast) -> {
            terminate();
            Main.terLatch.countDown();
        });

        try {
            Main.latch.await(); //waits until everybody has subscribed
        } catch (InterruptedException e) {}

        //send attack events
        for (int i = 0; i < attacks.length; i++) {
            results.add(sendEvent(new AttackEvent(attacks[i])));
        }
        for (Future<?> result : results) {
            result.get(); //waits until resolved
        }

        //send deactivation event
        Future<?> deacResult = sendEvent(new DeactivationEvent());
        deacResult.get(); //waits until resolved

        //send bomb destroyer event
        Future<?> bombResult = sendEvent(new BombDestroyerEvent());
        bombResult.get(); //waits until resolved

        sendBroadcast(new Finish());
    }
}
