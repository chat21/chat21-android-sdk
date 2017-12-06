package chat21.android.core.messages.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;

/**
 * Created by stefano on 06/10/2015.
 */
public class Message implements Serializable {
    private static final String TAG = Message.class.getName();

    // message status
    public static final int STATUS_SENT = 0; // message sent (pending)
    public static final int STATUS_RECEIVED = 1; // message received from the server
    public static final int STATUS_READ = 2; // message read from contact

    // message type
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_FILE = "file";

    private String sender, recipient, text, conversationId;
    private int status;
    private Long timestamp;
    private String type;
    private String sender_fullname;
    private String recipientGroupId;

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    public Message() {
        this.status = STATUS_SENT;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender_fullname() {
        return sender_fullname;
    }

    public void setSender_fullname(String sender_fullname) {
        this.sender_fullname = sender_fullname;
    }

    public String getRecipientGroupId() {
        return recipientGroupId;
    }

    public void setRecipientGroupId(String recipientGroupId) {
        this.recipientGroupId = recipientGroupId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", text='" + text + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", status=" + status +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", sender_fullname='" + sender_fullname + '\'' +
                ", recipientGroupId='" + recipientGroupId + '\'' +
                '}';
    }
}
