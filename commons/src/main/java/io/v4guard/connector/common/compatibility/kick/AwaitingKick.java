package io.v4guard.connector.common.compatibility.kick;

import java.util.List;

public class AwaitingKick<PC> {

    private PC player;
    private List<String> reason;


    public AwaitingKick(PC player, List<String> reason) {
        this.player = player;
        this.reason = reason;
    }

    public PC getPlayer() {
        return player;
    }

    public List<String> getReason() {
        return reason;
    }
}
