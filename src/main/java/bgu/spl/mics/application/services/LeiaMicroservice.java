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
 * LeiaMicroservices Initialized with Attack objects, and sends them as {@link AttackEvent}.
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
            //callback
            //receives only Finish type of broadcast
            terminate();
            Main.TerminateLatch.countDown();
        });

        try {
            //waits until everybody has subscribed
            Main.subscribeLatch.await();
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

        //notifies all to finish
        sendBroadcast(new Finish());
    }
}
