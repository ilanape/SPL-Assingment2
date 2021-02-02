package bgu.spl.mics.application.passiveObjects;


import java.util.Collections;
import java.util.List;

/**
 * Passive object representing the resource manager.
 * <p>
 * This class must be implemented as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class Ewoks { //singleton
    private static class EwoksHolder {
        private static Ewoks instance = new Ewoks();
    }

    private Ewok[] collection;

    private Ewoks() {
        collection = new Ewok[0];
    }

    public static Ewoks getInstance() {
        return EwoksHolder.instance;
    }

    public void setEwoks(Ewok[] toSet) {
        collection=toSet;
    }

    public void acquire(List<Integer> serials) {
        Collections.sort(serials); //sort the serials in descending order
        for (Integer serial : serials) {
            synchronized (collection[serial]) {
                while (!(collection[serial]).isAvailable()){
                    try {
                        collection[serial].wait();
                    }catch (InterruptedException e){}
                }
                collection[serial].acquire();
            }
        }
    }

    public void release(List<Integer> serials) {
        for (Integer serial : serials) {
            synchronized (collection[serial]) {
                collection[serial].release();
                collection[serial].notifyAll();
            }
        }
    }

}
