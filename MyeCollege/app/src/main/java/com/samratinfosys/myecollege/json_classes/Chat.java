package com.samratinfosys.myecollege.json_classes;

import android.database.Cursor;

import com.samratinfosys.myecollege.utils.Helper;

/**
 * Created by iAmMegamohan on 12-04-2015.
 */
public class Chat {

    public Chat(MyJSON json) {
        ready=false;
        try {
            if(json.isReady()) {
                id=json.getLong("chat_id",0);
                propagationType=fromPropagationInt(json.getInt("chat_propagation_type", 0));
                from_id=json.getInt("chat_from_id", 0);
                to_id=json.getInt("chat_to_id", 0);
                timestamp =json.getString("chat_timestamp");
                security_data=json.getString("chat_security_data");
                message_type =fromMessageInt(json.getInt("chat_message_type", 0));
                String messageJson=json.getString("chat_message");
                if(messageJson!=null) {
                    message = new Message(messageJson);
                }
                status=fromStatusInt(json.getInt("chat_status",0));
                from_name=json.getString("chat_from_name");
                to_name=json.getString("chat_to_name");
                ready=true;
            }
        } catch (Exception ex) {
            Helper.Log(ex,"initMessage: from MyJSON: "+json);
        }
    }

    public Chat(Cursor cursor) {
        ready=false;
        try {
            id=cursor.getLong(cursor.getColumnIndex("chat_id"));
            propagationType=fromPropagationInt(cursor.getInt(cursor.getColumnIndex("chat_propagation_type")));
            from_id=cursor.getInt(cursor.getColumnIndex("chat_from_id"));
            to_id=cursor.getInt(cursor.getColumnIndex("chat_to_id"));
            timestamp =cursor.getString(cursor.getColumnIndex("chat_timestamp"));
            security_data=cursor.getString(cursor.getColumnIndex("chat_security_data"));
            message_type =fromMessageInt(cursor.getInt(cursor.getColumnIndex("chat_message_type")));
            String messageJson=cursor.getString(cursor.getColumnIndex("chat_message"));
            if(messageJson!=null) {
                message = new Message(messageJson);
            }
            status=fromStatusInt(cursor.getInt(cursor.getColumnIndex("chat_status")));
            from_name=cursor.getString(cursor.getColumnIndex("chat_from_name"));
            to_name=cursor.getString(cursor.getColumnIndex("chat_to_name"));
            ready=true;
        } catch (Exception ex) {
            Helper.Log(ex,"initMessage: from MyCursor: "+cursor);
        }
    }

    public enum MessageType {
        Text, //1
        Image, //2
        Audio, //3
        Video, //4
        None//0
    }

    public enum PropagationType {
        Unicast, //1
        Multicast, //2
        Broadcast, //3
        None //0
    }

    public enum Status {
        Old, //-1
        New, //1
        None //0
    }

    public boolean ready=false;

    public long id;
    public PropagationType propagationType= PropagationType.None;
    public long from_id;
    public long to_id;
    public String timestamp =null;
    public String security_data=null;
    public MessageType message_type = MessageType.None;
    public Message message=null;
    public Status status=Status.None;
    public String from_name=null, to_name=null;
    public boolean isReceived=false;

    public class Message extends MyJSON {
        public String text=null;
        public String image=null;
        public String audio=null;
        public String video=null;

        public Message(String json) {
            super(json);
            text=getString("text");
            image=getString("image");
            audio=getString("audio");
            video=getString("video");
        }

        public boolean verify() {
            return text!=null || image!=null || audio!=null || video!=null;
        }
    }

    public static int fromEnum(MessageType value) {
        switch (value) {
            case Text: return 1;
            case Image: return 2;
            case Audio: return 3;
            case Video: return 4;
        }
        return 0;
    }

    public static int fromEnum(PropagationType value) {
        switch (value) {
            case Unicast: return 1;
            case Multicast: return 2;
            case Broadcast: return 3;
        }
        return 0;
    }

    public static int fromEnum(Status value) {
        switch (value) {
            case Old: return 1;
            case New: return 2;
        }
        return 0;
    }

    public static MessageType fromMessageInt(int value) {
        if(value==1)
            return MessageType.Text;
        else if(value==2)
            return MessageType.Image;
        else if(value==3)
            return MessageType.Audio;
        else if(value==4)
            return MessageType.Video;

        return MessageType.None;
    }

    public static Status fromStatusInt(int value) {
        if(value==1)
            return Status.Old;
        else if(value==2)
            return Status.New;

        return Status.None;
    }

    public static PropagationType fromPropagationInt(int value) {
        if(value==1) return PropagationType.Unicast;
        else if(value==2) return PropagationType.Multicast;
        else if(value==3) return PropagationType.Broadcast;

        return PropagationType.None;
    }


}
