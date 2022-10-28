package io.v4guard.plugin.core.accounts.messaging;

public abstract class MessageReceiver {

//    This is a bit of a "dirty" solution.
//    If we place a custom channel name the message does not reach BungeeCord/Velocity.
//    We will temporarily use the BungeeCord channel until we find a solution to the problem.
//    protected static final String CHANNEL = "v4guard:accountshield";

    protected static final String CHANNEL = "BungeeCord";
    protected static final String VELOCITY_CHANNEL = "bungeecord:main";

}
