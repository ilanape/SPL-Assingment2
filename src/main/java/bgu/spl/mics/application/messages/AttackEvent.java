package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Attack;
import java.util.List;

public class AttackEvent implements Event<Boolean> {
    private Attack attack;

    public AttackEvent(Attack attack) {
        this.attack = attack;
    }

    public int getDuration() {
        return attack.getDuration();
    }

    public List<Integer> getSerials() {
        return attack.getSerials();
    }
}
