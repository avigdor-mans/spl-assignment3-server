package bgu.spl.net.impl.stomp;

import java.util.LinkedList;
import java.util.List;

public class                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    StompFrame {
   private String command;
   private List<String> headers;

   private   String body;

    /**
     * a constructor of StopFrame from a given legal string
     * @param details the string to build the frame from
     */
    public StompFrame(String details)
    {
        command = details.substring(0,details.indexOf('\n'));
        headers = new LinkedList<>();
        String header ="";
        details = details.substring(details.indexOf('\n')+1);
        while (details.indexOf('\n')!=0){
            header = details.substring(0,details.indexOf('\n'));
            headers.add(header);
            details = details.substring(details.indexOf('\n')+1);
        }
        details = details.substring(details.indexOf('\n')+1);
        body = details.substring(0,details.indexOf('\n'));

    }
    /**
     * a constructor of StopFrame from a given legal frame parts
     * @param Command is the command of the frame
     * @param headers are the headers of the frame
     * @param body is the body of the frame
     */
    public StompFrame(String Command, List<String> headers, String body)
    {
        this.command = Command;
        this.headers = headers;
        this.body = body;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        command = command;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
    public List<String> getHeaders()
    {
        return headers;
    }

    /**
     * return a string representing the frame
     * @return
     */
    public String toString()
    {
        String strOfFrame = "";
        strOfFrame += command + "\n";
        for (String header : headers) {
            strOfFrame += header + "\n";
        }
        strOfFrame += "\n";
        strOfFrame += body;

        return strOfFrame;
    }
    public void addHeader(String header)
    {
        headers.add(header);
    }
}
