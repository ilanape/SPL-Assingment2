package bgu.spl.mics.application.passiveObjects;


import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.services.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive data-object representing a Diary - in which the flow of the battle is recorded.
 */
public class Diary {

    //singleton
    private static class DiaryHolder {
        private static Diary instance = new Diary();
    }

    AtomicInteger totalAttacks;
    private long HanSoloFinish;
    private long C3POFinish;
    private long R2D2Deactivate;
    private long LeiaTerminate;
    private long HanSoloTerminate;
    private long C3POTerminate;
    private long R2D2Terminate;
    private long LandoTerminate;

    public Diary() {
        totalAttacks = new AtomicInteger(0);
        HanSoloFinish = 0;
        C3POFinish = 0;
        R2D2Deactivate = 0;
        LeiaTerminate = 0;
        HanSoloTerminate = 0;
        C3POTerminate = 0;
        R2D2Terminate = 0;
        LandoTerminate = 0;
    }

    //getters
    public static Diary getInstance() {
        return DiaryHolder.instance;
    }
    public AtomicInteger getTotalAttacks() {
        return totalAttacks;
    }
    public long getC3POFinish() {
        return C3POFinish;
    }
    public long getHanSoloFinish() {
        return HanSoloFinish;
    }
    public long getR2D2Deactivate() {
        return R2D2Deactivate;
    }
    public long getLeiaTerminate() {
        return LeiaTerminate;
    }
    public long getHanSoloTerminate() {
        return HanSoloTerminate;
    }
    public long getC3POTerminate() {
        return C3POTerminate;
    }
    public long getR2D2Terminate() {
        return R2D2Terminate;
    }
    public long getLandoTerminate() {
        return LandoTerminate;
    }

    //setters
    public void setTotalAttacks(int i) {
        totalAttacks.compareAndSet(totalAttacks.get(), i);
    }
    public void setTerminate(MicroService m, long timestamp) {
        if (m instanceof LeiaMicroservice)
            LeiaTerminate = timestamp;
        if (m instanceof HanSoloMicroservice)
            HanSoloTerminate = timestamp;
        if (m instanceof C3POMicroservice)
            C3POTerminate = timestamp;
        if (m instanceof R2D2Microservice)
            R2D2Terminate = timestamp;
        if (m instanceof LandoMicroservice)
            LandoTerminate = timestamp;
    }
    public void setR2D2Deactivate(long timeStamp) {
        R2D2Deactivate = timeStamp;
    }
    public void setHanSoloFinish(long timeStamp) {
        HanSoloFinish = timeStamp;
    }
    public void setC3POFinish(long timeStamp) {
        C3POFinish = timeStamp;
    }

    //for tester
    public void resetNumberAttacks() {
        totalAttacks.getAndSet(0);
    }

}
