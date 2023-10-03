/*
 * Copyright (C) 2018-2023 Velocity Contributors
 */

package io.v4guard.plugin.core.utils;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

import java.nio.charset.StandardCharsets;

public class ProtocolUtils {

    public static String readString(ByteBuf buf) {
        return readString(buf, Short.MAX_VALUE);
    }

    /**
     * Reads a VarInt length-prefixed UTF-8 string from the {@code buf}, making sure to not go over
     * {@code cap} size.
     *
     * @param buf the buffer to read from
     * @param cap the maximum size of the string, in UTF-8 character length
     * @return the decoded string
     */
    public static String readString(ByteBuf buf, int cap) {
        int length = readVarInt(buf);
        return readString(buf, cap, length);
    }

    private static String readString(ByteBuf buf, int cap, int length) {
        checkFrame(length >= 0, "Got a negative-length string (%s)", length);
        checkFrame(length <= cap * 3, "Bad string size (got %s, maximum is %s)", length, cap);
        checkFrame(buf.isReadable(length),
                "Trying to read a string that is too long (wanted %s, only have %s)", length,
                buf.readableBytes());
        String str = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8);
        buf.skipBytes(length);
        checkFrame(str.length() <= cap, "Got a too-long string (got %s, max %s)",
                str.length(), cap);
        return str;
    }

    /**
     * Reads a Minecraft-style VarInt from the specified {@code buf}.
     *
     * @param buf the buffer to read from
     * @return the decoded VarInt
     */
    public static int readVarInt(ByteBuf buf) {
        int read = readVarIntSafely(buf);

        if (read == Integer.MIN_VALUE) {
            return -1;
        }

        return read;
    }

    /**
     * Reads a Minecraft-style VarInt from the specified {@code buf}. The difference between this
     * method and {@link #readVarInt(ByteBuf)} is that this function returns a sentinel value if the
     * varint is invalid.
     *
     * @param buf the buffer to read from
     * @return the decoded VarInt, or {@code Integer.MIN_VALUE} if the varint is invalid
     */
    public static int readVarIntSafely(ByteBuf buf) {
        int i = 0;
        int maxRead = Math.min(5, buf.readableBytes());
        for (int j = 0; j < maxRead; j++) {
            int k = buf.readByte();
            i |= (k & 0x7F) << j * 7;
            if ((k & 0x80) != 128) {
                return i;
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Throws {@link CorruptedFrameException} if {@code b} is false.
     *
     * @param b       the expression to check
     * @param message the message to include in the thrown {@link CorruptedFrameException}, formatted
     *                like {@link com.google.common.base.Preconditions#checkArgument(boolean)} and
     *                friends
     * @param args    the arguments to format the message with-
     */
    public static void checkFrame(boolean b, String message, Object... args) {
        if (!b) {
            throw new CorruptedFrameException(String.format(message, args));
        }
    }

}
