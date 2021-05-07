package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Main;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.Finish;

/**
 * LandoMicroservice
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
            //callback
            try {
                Thread.sleep(duration);
                complete(event, true);
            } catch (InterruptedException e) {}
        });

        subscribeBroadcast(Finish.class, (Finish broadcast) -> {
            //callback
            //receives only Finish type of broadcast
            terminate();
            Main.TerminateLatch.countDown();
        });

        //initialize complete
        Main.subscribeLatch.countDown();
    }
}
