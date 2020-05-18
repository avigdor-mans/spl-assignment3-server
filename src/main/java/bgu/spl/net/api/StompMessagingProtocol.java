package bgu.spl.net.api;

import bgu.spl.net.impl.stomp.StompFrame;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionsImpl;

public interface StompMessagingProtocol  {
	/**
	 * Used to initiate the current client protocol with it's personal connection ID and the connections implementation
	**/
    void start(int connectionId, Connections connections); // to ask if it ok *************
    
    void process(StompFrame frame);
	
	/**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
}
