package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Main;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.Finish;

/**
 * LandoMicroservice
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LandoMicroservice extends MicroService {
    private long duration;

    public LandoMicroservice(long duration) {
        super("Lando");
        this.duration = duration;
    }

    @Override
    protected void initialize() {
        subscribeEvent(BombDestroyerEvent.class, (BombDestroyerEvent event) -> {
            try {
                Thread.sleep(duration);
                complete(event, true);
            } catch (InterruptedException e) {
            }
        });

        subscribeBroadcast(Finish.class, (Finish broadcast) -> {
            terminate();
            Main.terLatch.countDown();
        });

        Main.latch.countDown();
    }
}
