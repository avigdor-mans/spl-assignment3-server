package bgu.spl.net.impl.stomp;




import java.util.HashMap;


import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BookClub {


    private ConcurrentHashMap<String, HashMap<Integer,Integer>> topics; //Map of topic <-> map of (user id, sub id)

    private ConcurrentHashMap<Integer, List<String>> topicsForClientId; //Map of topics registered for every client

    private static class SingletonHolder {
        private static BookClub instance = new BookClub();

    }

    public static BookClub getInstance() {

        return BookClub.SingletonHolder.instance;

    }
    private BookClub()
    {
        topics = new ConcurrentHashMap<>();
        topicsForClientId = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, HashMap<Integer,Integer>> getTopics()
    {
        return topics;
    }

    /**
     * this function stores the subscription of some client in the book club
     * @param clientId is the id of the user
     * @param subId is the subscription id
     * @param topic is the topic to register
     */
    public void Subscribe(Integer clientId, Integer subId, String topic){
        topicsForClientId.putIfAbsent(clientId,new LinkedList<>());
        topicsForClientId.get(clientId).add(topic);
        topics.putIfAbsent(topic,new HashMap<>());
        synchronized (topics.get(topic)) {
            topics.get(topic).put(clientId,subId);
            System.out.println("client id : "+ clientId + " joined to topic : " + topic);
        }


    }
    /**
     * this function removes subscription of some client in the book club
     * @param clientId is the id of the user
     * @param topic is the topic to register
     */
    public boolean Unsubscribe(Integer clientId, String topic){
        topicsForClientId.get(clientId).remove(topic);
        if (topics.get(topic) != null){
            synchronized (topics.get(topic)) {
                if (topics.get(topic).containsKey(clientId)) {
                    topics.get(topic).remove(clientId);
                    System.out.println("client id : "+ clientId + " left topic : " + topic);
                    return true;
                }
            }
        }
        return false;

    }
    public int getSubscriptionNum (String topic, int cliendId)
    {
        if (topics.get(topic) != null) {
            if (topics.get(topic).get(cliendId) != null)
            {
                return topics.get(topic).get(cliendId);
            }
        }
        return  -1;
    }

    /**
     * this function returns a Map of all the users registered to some topic and their subscription id
     * @param topic is the topic to return the details for
     * @return a Map of all the users registered to some topic and their subscription id
     */
    public HashMap<Integer,Integer> getSubForTopic(String topic){return topics.get(topic);}

    /**
     * this function removes a client from the books club, removing his subscriptions
     * @param clientId is the id of the client to remove
     */
    public void removeClient(int clientId)
    {
        if (topicsForClientId.get(clientId) != null) {
            for (String topic : topicsForClientId.get(clientId)) {
                synchronized (topics.get(topic)) {
                    topics.get(topic).remove(clientId);
                }
            }
            topicsForClientId.remove(clientId);
        }
    }

}
