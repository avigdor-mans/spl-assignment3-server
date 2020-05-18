package bgu.spl.net.srv;

import bgu.spl.net.impl.stomp.BookClub;
import bgu.spl.net.impl.stomp.StompFrame;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class                                                                                                                                                                                                                ConnectionsImpl implements Connections<StompFrame> {

    private AtomicInteger messageCounter = new AtomicInteger(0);
    private ConcurrentHashMap<Integer,String> loggedInUsers= new ConcurrentHashMap(); // user id , user name

    private ConcurrentHashMap<Integer,ConnectionHandler> connectionHandlers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,String> users = new ConcurrentHashMap<>(); // user name , user password


    @Override
    public boolean send(int connectionId, StompFrame msg) {
        ConnectionHandler nBc = connectionHandlers.get(connectionId);
        System.out.println("private msg " + msg.toString());
        if (nBc != null) {
            connectionHandlers.get(connectionId).send(msg);
            return true;
        }
        return false;
    }


    @Override
    public void send(String channel, StompFrame msg) {
       int num = messageCounter.incrementAndGet();
       msg.addHeader("Message-id:" + num);
           HashMap<Integer, Integer> map = BookClub.getInstance().getSubForTopic(channel);
           if (map != null){
                synchronized (map) {
                   for (Integer id : map.keySet()) {
                       LinkedList<String> headers = new LinkedList<>();
                       for (String s: msg.getHeaders()) {
                           headers.add(s);
                       }
                       StompFrame toSend = new StompFrame(msg.getCommand(),headers,msg.getBody());

                       toSend.addHeader("subscription:" + map.get(id));
                       send(id, toSend);
                   }
               }
           }

    }


    @Override
    public void disconnect(int connectionId) {
        synchronized (loggedInUsers) {
            loggedInUsers.remove(connectionId);
        }
        connectionHandlers.remove(connectionId);
        BookClub.getInstance().removeClient(connectionId);

    }


    public void addHandler(int connectionId, ConnectionHandler<StompFrame> connectionHandler){
        connectionHandlers.putIfAbsent(connectionId,connectionHandler);
    }


    public Boolean checkPasscode(String userName, String passcode)
    {
        if (users.keySet().contains(userName))
        {
            if (users.get(userName).equals(passcode))
                return true;
            return false;
        }
        else
        {
            users.putIfAbsent(userName,passcode);
            return true;
        }
    }


    public Boolean isLoggedIn(int userId)
    {
        //synchronized
        synchronized (loggedInUsers) {
            return loggedInUsers.keySet().contains(userId);
        }
    }


    public void addLoggedInUser(int userId, String userName)
    {
        //syn
        synchronized (loggedInUsers) {
            loggedInUsers.put(userId, userName);
        }
    }


    public Boolean isLoggedInByUserName(String userName)
    {
        //synchronized
        synchronized (loggedInUsers) {
            return loggedInUsers.contains(userName);
        }
    }
}


