package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class StompFrameEncoderDecoder implements MessageEncoderDecoder<StompFrame> {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;

    /**
     *
     * @param nextByte the next byte to consider for the currently decoded
     * message
     * @return a StompFrame
     */
    @Override
    public StompFrame decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if (nextByte == '\u0000') {
            if (len > 0) {
                StompFrame newFrame = new StompFrame(popString());
                return newFrame;
            }
        }
        else {
            pushByte(nextByte);
        }
        return null; //not a StompFrame yet
    }

    /**
     *
     * @param frame the frame to encode
     * @return
     */
    @Override
    public byte[] encode(StompFrame frame) {
         String strOfFrame = "";
         strOfFrame += frame.getCommand() + "\n";
        for (String header : frame.getHeaders()) {
            strOfFrame += header + "\n";
        }
        strOfFrame += "\n";
        strOfFrame += frame.getBody() + "\n";
        strOfFrame += '\u0000';

        return strOfFrame.getBytes(); //uses utf8 by default
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        return result;
    }
}
