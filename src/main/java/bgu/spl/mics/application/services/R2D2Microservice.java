package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Main;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.Finish;
import bgu.spl.mics.application.passiveObjects.Diary;

/**
 * R2D2Microservices is in charge of the handling {@link DeactivationEvent}.
 */
public class R2D2Microservice extends MicroService {
    private long duration;

    public R2D2Microservice(long duration) {
        super("R2D2");
        this.duration = duration;
    }

    @Override
    protected void initialize() {
        subscribeEvent(DeactivationEvent.class, (DeactivationEvent event) -> {
            //callback
            try {
                Thread.sleep(duration);
                complete(event, true);

                //set to diary deactivate
                Diary.getInstance().setR2D2Deactivate(System.currentTimeMillis());
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
