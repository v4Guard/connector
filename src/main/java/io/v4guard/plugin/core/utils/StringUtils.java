package io.v4guard.plugin.core.utils;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import org.bson.Document;

import java.nio.charset.Charset;
import java.util.List;
import java.util.StringJoiner;

public class StringUtils {

    public static String replacePlaceholders(String message, Document placeholders){
        if(placeholders == null) return message;
        for(String var : placeholders.keySet()){
            message = message.replace("{" + var + "}", placeholders.get(var).toString());
        }
        return message;
    }

    public static String buildMultilineString(List<String> lines) {
        StringJoiner message = new StringJoiner("\n");

        for (String line : lines) {
            message.add(line);
        }

        return message.toString();
    }

    /**
     * Reads a varint from the given byte buffer
     *
     * @param buf the byte buffer the varint should be read from
     * @return the int read
     */
    public static int readVarIntFromBuffer( ByteBuf buf ) {
        int i = 0;
        int j = 0;

        byte b0;
        do {
            b0 = buf.readByte();
            i |= (b0 & 127) << j++ * 7;
            if ( j > 5 ) {
                throw new RuntimeException( "VarInt too big" );
            }
        } while ( (b0 & 128) == 128 );

        return i;
    }

    /**
     * Reads a string from the given byte buffer
     *
     * @param buf       the byte buffer the string should be read from
     * @param maxLength the string's max-length
     * @return the string read
     */
    public static String readString(ByteBuf buf, int maxLength ) {
        int i = readVarIntFromBuffer( buf );

        if ( i > maxLength * 4 ) {
            throw new DecoderException( "The received encoded string buffer length is longer than maximum allowed (" + i + " > " + maxLength * 4 + ")" );
        } else if ( i < 0 ) {
            throw new DecoderException( "The received encoded string buffer length is less than zero! Weird string!" );
        } else {
            byte[] bytes = new byte[i];
            buf.readBytes( bytes );

            String s = new String( bytes, Charset.forName( "UTF-8" ) );
            if ( s.length() > maxLength ) {
                throw new DecoderException( "The received string length is longer than maximum allowed (" + i + " > " + maxLength + ")" );
            } else {
                return s;
            }
        }
    }


}
