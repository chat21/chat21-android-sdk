package org.chat21.android.core.messages.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stefano on 06/10/2015.
 */
public class Message implements Serializable, Cloneable {
    private static final String TAG = Message.class.getName();

    public static final String STATUS_FIELD_KEY = "status";
    public static final String TIMESTAMP_FIELD_KEY = "timestamp";

    // message status
    public static final long STATUS_FAILED = -100;
    public static final long STATUS_SENDING = 0;
    public static final long STATUS_SENT = 100; //(SALVATO SULLA TIMELINE DEL MITTENTE)
    public static final long STATUS_DELIVERED_TO_RECIPIENT_TIMELINE = 150; //(SALVATO SULLA TIMELINE DEL DESTINATARIO)

    public static final long STATUS_RECEIVED_FROM_RECIPIENT_CLIENT = 200;
    public static final long STATUS_RETURN_RECEIPT = 250;  // from the recipient client app)
    public static final long STATUS_SEEN = 300; // message read from contact

    public static final String DIRECT_CHANNEL_TYPE = "direct";
    public static final String GROUP_CHANNEL_TYPE = "group";

    // message type
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_FILE = "file";

    @Exclude
    String id;

    @PropertyName("sender")
    String sender;

    @PropertyName("sender_fullname")
    String senderFullname;

    @PropertyName("recipient")
    String recipient;

    @PropertyName("recipient_fullname")
    String recipientFullname;

    @PropertyName("text")
    String text;

    @PropertyName("status")
    Long status; //could be null

    @PropertyName("timestamp")
    Long timestamp;

    @PropertyName("type")
    String type;

    @PropertyName("channel_type")
    String channelType;

    @PropertyName("metadata")
    Map<String, Object> metadata;

    @PropertyName("attributes")
    Map<String, Object> attributes;


    public Message() {

        // this.status = STATUS_SENDING;
        //this.timestamp = new Date().getTime();
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("sender")
    public String getSender() {
        return sender;
    }

    @PropertyName("sender")
    public void setSender(String sender) {
        this.sender = sender;
    }

    @PropertyName("recipient")
    public String getRecipient() {
        return recipient;
    }

    @PropertyName("recipient")
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    @PropertyName("sender_fullname")
    public String getSenderFullname() {
        return senderFullname;
    }

    @PropertyName("sender_fullname")
    public void setSenderFullname(String senderFullname) {
        this.senderFullname = senderFullname;
    }

    @PropertyName("recipient_fullname")
    public String getRecipientFullname() {
        return recipientFullname;
    }


    @PropertyName("recipient_fullname")
    public void setRecipientFullname(String recipientFullname) {
        this.recipientFullname = recipientFullname;
    }

    @PropertyName("channel_type")
    public String getChannelType() {
        return channelType;
    }

    @PropertyName("channel_type")
    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    @PropertyName("text")
    public String getText() {
        return text;
    }

    @PropertyName("text")
    public void setText(String text) {
        this.text = text;
    }

    @PropertyName("status")
    public Long getStatus() {
        return status;
    }

    @PropertyName("status")
    public void setStatus(Long status) {
        this.status = status;
    }

    @PropertyName("timestamp")
    public Long getTimestamp() {
        return this.timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }



    @PropertyName("type")
    public String getType() {
        return type;
    }

    @PropertyName("type")
    public void setType(String type) {
        this.type = type;
    }


    @Exclude
    public boolean isDirectChannel() {
        if (this.channelType == null || this.channelType.equals(Message.DIRECT_CHANNEL_TYPE)) {
            return true;
        } else {
            return false;
        }
    }

    @Exclude
    public boolean isGroupChannel() {
        if (this.channelType != null && this.channelType.equals(Message.GROUP_CHANNEL_TYPE)) {
            return true;
        } else {
            return false;
        }
    }

    @PropertyName("metadata")
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @PropertyName("metadata")
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }


    @PropertyName("attributes")
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @PropertyName("attributes")
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    @Exclude
    public boolean equals(Object object) {
        if (object instanceof Message) {
            Message message = (Message) object;
            if (this.getId().equals(message.getId())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Exclude
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnse) {
            return null;
        }
    }

    @Exclude
    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", sender='" + sender + '\'' +
                ", senderFullname='" + senderFullname + '\'' +
                ", recipient='" + recipient + '\'' +
                ", recipientFullname='" + recipientFullname + '\'' +
                ", text='" + text + '\'' +
                ", status=" + status +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", channelType='" + channelType + '\'' +
                ", metadata=" + metadata +
                ", attributes=" + attributes +
                '}';
    }

//    String id;
//    @PropertyName("sender")
//    @PropertyName("sender_fullname")
//    @PropertyName("recipient")
//    @PropertyName("recipient_fullname")
//    @PropertyName("text")
//    @PropertyName("status")
//    @PropertyName("timestamp")
//    @PropertyName("type")
//    @PropertyName("channel_type")
//    @PropertyName("metadata")
//    @PropertyName("attributes")

    public Map asFirebaseMap() {
        Map map = new HashMap();
        map.put("sender",null);
        map.put("sender_fullname",senderFullname);
        map.put("recipient",null);
        map.put("recipient_fullname",recipientFullname);
        map.put("text",text);
        map.put("status",null);
        map.put("timestamp", ServerValue.TIMESTAMP);
        map.put("type",type);
        map.put("channel_type",channelType);
        map.put("metadata",metadata);
        map.put("attributes",attributes);

        return map;

    }


}
