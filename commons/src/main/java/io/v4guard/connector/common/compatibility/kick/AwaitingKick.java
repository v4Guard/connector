package io.v4guard.connector.common.compatibility.kick;

public class AwaitingKick<PC> {

    private PC player;
    private String reason;


    public AwaitingKick(PC player, String reason) {
        this.player = player;
        this.reason = reason;
    }

    public PC getPlayer() {
        return player;
    }

    public String getReason() {
        return reason;
    }
}
