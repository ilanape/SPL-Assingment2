package bgu.spl.mics.application.passiveObjects;

/**
 * Passive data-object representing a forest creature summoned when HanSolo and C3PO receive AttackEvents.
 */
public class Ewok {
    int serialNumber;
    boolean available;

    public Ewok(int serialNumber) {
        this.serialNumber = serialNumber;
        available = true;
    }

    /**
     * Acquires an Ewok
     */
    public void acquire() {
        if (available)
            available = false;
    }

    /**
     * release an Ewok
     */
    public void release() {
        if (!available)
            available = true;
    }

    public boolean isAvailable(){
        return available;
    }
}
