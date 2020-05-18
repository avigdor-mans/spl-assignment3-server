package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionsImpl;



import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StompMessagingProtocolImpl implements StompMessagingProtocol {
    Boolean terminate = false;
    Connections connections;
    int connectionId;
    Map<String,Integer> myTopicSub;


    @Override
    public void start(int connectionId, Connections connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        myTopicSub = new HashMap<>();

    }

    /**
     * this method process the appropriate actions due to the frame received
     * first it checks the type of the frame Command (Connect,Disconnect,Send,Subscribe,Unsubscribe)
     * after it checks if the frame is legal, and then its content, and reacts appropriately.
     *
     * @param frame is the message the server received from the client
     */
    @Override
    public void process(StompFrame frame) {
        switch (frame.getCommand()) {
            case "CONNECT": {
                boolean acceptVersion=false;
                boolean login = false;
                boolean passcode = false;
                String passcodeS="";
                String loginName="";
                String version ="";

                for (int i = 0; i < frame.getHeaders().size(); i++)
                {
                    if (frame.getHeaders().get(i).contains("accept-version")) {
                        version  = frame.getHeaders().get(i).substring  // to check if version must be 1.2
                                (frame.getHeaders().get(i).indexOf(":")+1);
                        acceptVersion = true;
                    }

                    if (frame.getHeaders().get(i).contains("passcode")) {
                        passcode = true;
                        passcodeS = frame.getHeaders().get(i).substring
                                (frame.getHeaders().get(i).indexOf(":")+1);
                    }
                    if (frame.getHeaders().get(i).contains("login")) {
                        login = true;
                        loginName = frame.getHeaders().get(i).substring
                                (frame.getHeaders().get(i).indexOf(":")+1);
                    }
                }

                if (acceptVersion  & passcode & login)
                {
                    if (connections.isLoggedInByUserName(loginName))
                    {
                        String command = "ERROR";
                        LinkedList<String> headers = new LinkedList<>();
                        headers.add("login: User already logged in");
                        String body = "The user " + loginName + " tried to connect but was already logged in";
                        StompFrame errorFrame = new StompFrame(command,headers,body);
                        connections.send(connectionId, errorFrame);
                        connections.disconnect(connectionId);
                        terminate = true;
                    }
                    else
                    {
                        if (connections.checkPasscode(loginName, passcodeS))
                        {
                            System.out.println("Correct Password");

                            String command = "CONNECTED";
                            LinkedList<String> headers = new LinkedList<>();
                            headers.add("version:" + version);
                            String body = "";
                            StompFrame returnedFrame = new StompFrame(command, headers, body);

                            //  connections.add Connected ...
                            connections.addLoggedInUser(connectionId,loginName);
                            connections.send(connectionId, returnedFrame);
                        }
                        else
                        {
                            System.out.println("Wrong Password");

                            String command = "ERROR";
                            LinkedList<String> headers = new LinkedList<>();
                            headers.add("login: Wrong password");
                            StompFrame errorFrame = new StompFrame(command,headers,"");
                            connections.send(connectionId, errorFrame);
                            connections.disconnect(connectionId);
                            terminate = true;
                        }
                    }
                }
                else
                {
                    //error
                    System.out.println("HEADER MISSING");
                    String command = "ERROR";
                    LinkedList<String> headers = new LinkedList<>();
                    headers.add("message: malformed frame received");
                    String body = "The message:\n-----\n" + frame.toString() + "\n-----\nDid not contain a";
                    if (!login)
                        body += "login header, ";
                    if (!passcode)
                        body += "passcode header, ";

                    body += "which is REQUIRED for message propagation.";

                    StompFrame errorFrame = new StompFrame(command,headers,body);
                    connections.send(connectionId, errorFrame);
                    connections.disconnect(connectionId);
                    terminate = true;
                }
                break;
            }
            case "DISCONNECT" :{
                if (connections.isLoggedIn(connectionId)) {
                    if (frame.getHeaders().size() > 0) {
                        String receipt = frame.getHeaders().get(0).substring
                                (frame.getHeaders().get(0).indexOf(":") + 1);
                        List<String> headers = new LinkedList<>();
                        headers.add("receipt-id:" + receipt);
                        StompFrame returnStomp = new StompFrame("RECEIPT", headers, "");
                        connections.send(connectionId,returnStomp);
                    }
                    connections.disconnect(connectionId);
                    terminate = true;
                }
                break;
            }
            case "SEND": {
                boolean destination = false;
                String destinationS="";
                for (int i =0; i<frame.getHeaders().size(); i++){
                    if (frame.getHeaders().get(i).contains("destination")) {
                        destinationS = frame.getHeaders().get(i).substring
                                (frame.getHeaders().get(i).indexOf(":") + 1);
                        destination = true;
                    }
                }
                if (destination && !frame.getBody().equals("")){
                    String body = frame.getBody();
                    boolean hasAddedtheBook = false; // to decide between "added the book" and "has the book"
                    if (body.contains("added the book"))
                    {
                        hasAddedtheBook = true;
                        LinkedList<String> headers = new LinkedList<>();
                        headers.add("destination:"+destinationS);
                        StompFrame addBookFrame = new StompFrame("MESSAGE",headers,body);
                        connections.send(destinationS,addBookFrame);
                    }
                    if (body.contains("wish to borrow"))
                    {
                        LinkedList<String> headers = new LinkedList<>();
                        headers.add("destination:"+destinationS);
                        StompFrame wishFrame = new StompFrame("MESSAGE",headers,body);
                        connections.send(destinationS,wishFrame);
                    }
                    if (!hasAddedtheBook && body.contains("has"))
                    {
                        LinkedList<String> headers = new LinkedList<>();
                        headers.add("destination:"+destinationS);
                        StompFrame hasFrame = new StompFrame("MESSAGE",headers,body);
                        connections.send(destinationS,hasFrame);
                    }
                    if (body.contains("Taking"))
                    {

                        LinkedList<String> headers = new LinkedList<>();
                        headers.add("destination:"+destinationS);
                        StompFrame toTake = new StompFrame("MESSAGE",headers,body);
                        connections.send(destinationS,toTake);
                    }
                    if (body.contains("Returning"))
                    {
                        LinkedList<String> headers = new LinkedList<>();
                        headers.add("destination:"+destinationS);
                        StompFrame toReturn = new StompFrame("MESSAGE",headers,body);
                        connections.send(destinationS,toReturn);
                    }
                    if (body.contains("book status"))
                    {
                        LinkedList<String> headers = new LinkedList<>();
                        headers.add("destination:"+destinationS);
                        StompFrame getStatus = new StompFrame("MESSAGE",headers,body);
                        connections.send(destinationS,getStatus);
                    }
                    if (body.contains(":"))
                    {
                        LinkedList<String> headers = new LinkedList<>();
                        headers.add("destination:"+destinationS);
                        StompFrame getStatus = new StompFrame("MESSAGE",headers,body);
                        connections.send(destinationS,getStatus);
                    }
                }
                else
                {
                    System.out.println("HEADER MISSING");
                    String command = "ERROR";
                    LinkedList<String> headers = new LinkedList<>();
                    headers.add("message: malformed frame received");
                    String body = "The message:\n-----\n" + frame.toString() + "\n-----\nDid not contain a destination header, ";

                    body += "which is REQUIRED for message propagation.";

                    StompFrame errorFrame = new StompFrame(command,headers,body);
                    connections.send(connectionId, errorFrame);
                    connections.disconnect(connectionId);
                    terminate = true;

                }
                break;
            }



            case "SUBSCRIBE": {
                boolean destination = false;
                boolean id = false;
                boolean receipt = false;
                String destinationS = "";
                String idS = "";
                String receiptS = "";
                for (int i = 0; i < frame.getHeaders().size(); i++) {
                    if (frame.getHeaders().get(i).contains("destination")) {
                        destinationS = frame.getHeaders().get(i).substring
                                (frame.getHeaders().get(i).indexOf(":") + 1);
                        destination = true;
                    }
                    if (frame.getHeaders().get(i).contains("receipt")) {
                        receipt = true;
                        receiptS = frame.getHeaders().get(i).substring
                                (frame.getHeaders().get(i).indexOf(":") + 1);
                    }
                    if (frame.getHeaders().get(i).contains("id")) {
                        id = true;
                        idS = frame.getHeaders().get(i).substring
                                (frame.getHeaders().get(i).indexOf(":") + 1);
                    }
                }
                if (destination & id & receipt) {

                    if (!myTopicSub.containsKey(destinationS)) {
                        int idForSub = Integer.parseInt(idS);
                        boolean isSubIdExist=false;
                       // synchronized (my)
                        for (String top : myTopicSub.keySet()) {
                            if (myTopicSub.get(top) == idForSub)
                                isSubIdExist = true;
                        }
                        if (!isSubIdExist) {
                                BookClub.getInstance().Subscribe(connectionId, idForSub, destinationS);
                                LinkedList<String> headers = new LinkedList<>();
                                headers.add("receipt-id:" + receiptS);
                                StompFrame subFrame = new StompFrame("RECEIPT", headers, "");
                                connections.send(connectionId, subFrame);
                                myTopicSub.put(destinationS,idForSub);
                                System.out.println(subFrame.toString());
                                System.out.println("sent true frame back to SUBSCRIBE");

                        }
                        else
                        {

                            // id exist
                            //error
                            System.out.println("SUB FAILED SUB ID NOT UNIQUE");

                            String command = "ERROR";
                            LinkedList<String> headers = new LinkedList<>();
                            headers.add("subscription id: subscription id already exist");
                            String body = "User tried to subscribe to a topic with subscription id already being used";

                            StompFrame errorFrame = new StompFrame(command,headers,body);
                            connections.send(connectionId, errorFrame);
                            connections.disconnect(connectionId);
                            terminate = true;

                        }
                    }
                }
                else
                {
                    //error
                    System.out.println("HEADER MISSING");
                    String command = "ERROR";
                    LinkedList<String> headers = new LinkedList<>();
                    headers.add("message: malformed frame received");
                    String body = "The message:\n-----\n" + frame.toString() + "\n-----\nDid not contain a";
                    if (!receipt)
                        body += "receipt header, ";
                    if (!destination)
                        body += "destination header, ";
                    if (!id)
                        body += "id header, ";

                    body += "which is REQUIRED for message propagation.";

                    StompFrame errorFrame = new StompFrame(command,headers,body);
                    connections.send(connectionId, errorFrame);
                    connections.disconnect(connectionId);
                    terminate = true;
                }
                break;
            }
            case "UNSUBSCRIBE": {
                boolean id = false;
                boolean receipt = false;
                int idS = 0;
                String receiptS = "";
                for (int i = 0; i < frame.getHeaders().size(); i++) {
                    if (frame.getHeaders().get(i).contains("receipt")) {
                        receipt = true;
                        receiptS = frame.getHeaders().get(i).substring
                                (frame.getHeaders().get(i).indexOf(":") + 1);
                    }
                    if (frame.getHeaders().get(i).contains("id")) {
                        id = true;
                        idS = Integer.parseInt(frame.getHeaders().get(i).substring
                                (frame.getHeaders().get(i).indexOf(":") + 1));
                    }
                }
                if (id & receipt) {
                    String destinationS="";
                    for (String des : myTopicSub.keySet())
                    {
                        if (myTopicSub.get(des).equals(idS))
                        {
                            destinationS = des;
                        }

                    }
                    if (BookClub.getInstance().Unsubscribe(connectionId, destinationS)) {
                        LinkedList<String> headers = new LinkedList<>();
                        headers.add("receipt-id:" + receiptS);
                        StompFrame subFrame = new StompFrame("RECEIPT", headers, "");
                        connections.send(connectionId, subFrame);
                        myTopicSub.remove(destinationS);
                    }
                }
                else
                {
                    System.out.println("HEADER MISSING");
                    String command = "ERROR";
                    LinkedList<String> headers = new LinkedList<>();
                    headers.add("message: malformed frame received");
                    String body = "The message:\n-----\n" + frame.toString() + "\n-----\nDid not contain a";
                    if (!receipt)
                        body += "receipt header, ";
                    if (!id)
                        body += "subscription id header, ";

                    body += "which is REQUIRED for message propagation.";

                    StompFrame errorFrame = new StompFrame(command,headers,body);
                    connections.send(connectionId, errorFrame);
                    connections.disconnect(connectionId);
                    terminate = true;
                }
                break;
            }

        }
    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }
}
