package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl.net.impl.rci.RemoteCommandInvocationProtocol;
import bgu.spl.net.srv.Server;

import java.util.function.Supplier;

public class StompServer {

    public static void main(String[] args) {
        if (args[1].equals("tpc")) {
            Server.threadPerClient(
                    Integer.parseInt(args[0]), //port
                    () -> new StompMessagingProtocolImpl(), //protocol factory
                    StompFrameEncoderDecoder::new //message encoder decoder factory
            ).serve();
        }
        if (args[1].equals("reactor")) {
        Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                Integer.parseInt(args[0]), //port
                () ->  new StompMessagingProtocolImpl(), //protocol factory
                StompFrameEncoderDecoder::new //message encoder decoder factory
        ).serve();
        }
    }


}
