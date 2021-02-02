package bgu.spl.mics.application.passiveObjects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EwokTest {

    private Ewok ewok;

    @BeforeEach
    void setUp() {
        ewok= new Ewok(1);
    }

    /**
     * Checks if before acquire() the ewok is available
     * and after aquire() it is unavailable.
     */
    @Test
    void acquire() {
        ewok.release();
        assertTrue(ewok.available);
        ewok.acquire();
        assertFalse(ewok.available);
    }


    /**
     * Checks if before release() the ewok is unavailable
     * and after release() it is available.
     */
    @Test
    void release() {
        ewok.acquire();
        assertFalse(ewok.available);
        ewok.release();
        assertTrue(ewok.available);
    }
}