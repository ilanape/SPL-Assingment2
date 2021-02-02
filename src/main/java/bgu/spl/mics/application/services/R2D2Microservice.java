package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Main;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.Finish;
import bgu.spl.mics.application.passiveObjects.Diary;

/**
 * R2D2Microservices is in charge of the handling {@link DeactivationEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link DeactivationEvent}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
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
            try {
                Thread.sleep(duration);
                complete(event, true);
                Diary.getInstance().setR2D2Deactivate(System.currentTimeMillis()); //set to diary deactivate
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
