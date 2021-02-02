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
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class HanSoloMicroservice extends MicroService {

    public HanSoloMicroservice() {
        super("Han");
    }


    @Override
    protected void initialize() {
        subscribeEvent(AttackEvent.class, (AttackEvent event) -> {
            List<Integer> ewokSerials = event.getSerials();
            int duration = event.getDuration();
            Ewoks.getInstance().acquire(ewokSerials);
            try {
                Thread.sleep(duration);
                AtomicInteger i = Diary.getInstance().getTotalAttacks();
                Diary.getInstance().setTotalAttacks(i.incrementAndGet()); //set to diary
                complete(event, true);
                Diary.getInstance().setHanSoloFinish(System.currentTimeMillis()); //set to diary finish (will replace current each time)
                Ewoks.getInstance().release(ewokSerials);
            } catch (InterruptedException e) {}
        });

        subscribeBroadcast(Finish.class, (Finish broadcast) -> {
            terminate();
            Main.terLatch.countDown();
        });

        Main.latch.countDown();
    }
}
