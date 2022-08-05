package io.v4guard.plugin.core.kick;

public class KickReason {

    private String name;
    private String message;

    public KickReason(String name, String message) {
        this.name = name;
        this.message = message.replace("&", "§");
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }
}
