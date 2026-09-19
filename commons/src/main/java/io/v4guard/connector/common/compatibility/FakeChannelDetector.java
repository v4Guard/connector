package io.v4guard.connector.common.compatibility;

/**
 * <a href="https://github.com/retrooper/packetevents/blob/2.0/api/src/main/java/com/github/retrooper/packetevents/util/FakeChannelUtil.java"> Original code snippet from PacketEvents. </a>
 */
public class FakeChannelDetector {

    public static boolean isFakeChannel(Object channel) {
        return channel.getClass().getSimpleName().equals("FakeChannel")
                || channel.getClass().getSimpleName().equals("SpoofedChannel")
                || channel.getClass().getSimpleName().equals("EmbeddedChannel");
    }

}
