package bgu.spl.net.srv;

import bgu.spl.net.impl.stomp.StompFrame;

import java.io.IOException;

public interface Connections<T> {
    /**
     * this function sends a msg Frame to a client
     * @param connectionId the id of the client to send the frame
     * @param msg the frame to send
     * @return
     */
    boolean send(int connectionId, T msg);

    /**
     * this functions activate the send method to every client registered to a specific topic
     * @param channel the topic to send to
     * @param msg the msg frame to send
     */

    void send(String channel, T msg);
    /**
     * this function removes a client from the connections
     * @param connectionId the id of the client to remove
     */
    void disconnect(int connectionId);

    /**
     * this function adds an connection handler to the connections
     * @param id the id of the client to add his handler
     * @param connectionHandler the handler to add of the client
     */
    void addHandler(int id, ConnectionHandler<StompFrame> connectionHandler);

    /**
     * this function checks if a client is logged In by his id
     * @param userId the id of the client to check if he is logged in
     * @return true if the client is logged in and false if not
     */
    Boolean isLoggedIn(int userId);


    /**
     * this function checks if a client is logged in by his user name
     * @param userName the username of the client to check
     * @return true if the client is logged in and false if not
     */
    Boolean isLoggedInByUserName(String userName);

    /**
     * this functions checks if the passcode of some client is correct
     * @param userName the userName of the client to check his passcode
     * @param passcode the passcode to check
     * @return
     */
    Boolean checkPasscode(String userName, String passcode);

    /**
     * this function adds a logged in client to the list of logged in users
     * @param connectionId the id of the client to add to the list
     * @param userName the user name of the client to add
     */
    void addLoggedInUser(int connectionId, String userName);
}
