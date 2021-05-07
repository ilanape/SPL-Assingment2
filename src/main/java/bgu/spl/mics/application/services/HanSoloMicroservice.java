package bgu.spl.mics.application.services;


import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Main;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.Finish;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HanSoloMicroservices is in charge of the handling {@link AttackEvent}.
 */
public class HanSoloMicroservice extends MicroService {

    public HanSoloMicroservice() {
        super("Han");
    }

    @Override
    protected void initialize() {
        subscribeEvent(AttackEvent.class, (AttackEvent event) -> {
            //callback
            //acquire resources depend on the attack requirements
            List<Integer> ewokSerials = event.getSerials();
            Ewoks.getInstance().acquire(ewokSerials);

            int duration = event.getDuration();
            try {
                Thread.sleep(duration);

                //set to diary total attacks
                AtomicInteger overallAttacksCounter = Diary.getInstance().getTotalAttacks();
                Diary.getInstance().setTotalAttacks(overallAttacksCounter.incrementAndGet());

                complete(event, true);

                //set to diary finish (will replace current each time)
                Diary.getInstance().setHanSoloFinish(System.currentTimeMillis());
                Ewoks.getInstance().release(ewokSerials);
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
