package com.samratinfosys.myecollege.json_classes;

/**
 * Created by iAmMegamohan on 23-04-2015.
 */
public class ChatHead {
    public long fromId;
    public long toId;
    public String fromName=null;
    public String toName=null;
    public String messageLine=null;
    public Chat.MessageType messageType= Chat.MessageType.None;
    public long newCount=0;
}
