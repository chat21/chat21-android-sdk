package chat21.android.core.messages.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by stefano on 06/10/2015.
 */
public class Message implements Serializable {
    private static final String TAG = Message.class.getName();

    // message status
    public static final int STATUS_FAILED = -100;
    public static final int STATUS_SENDING = 0;
    public static final int STATUS_SENT = 100; //(SALVATO SULLA TIMELINE DEL MITTENTE)
    public static final int STATUS_DELIVERED_TO_RECIPIENT_TIMELINE = 150; //(SALVATO SULLA TIMELINE DEL DESTINATARIO)

    public static final int STATUS_RECEIVED_FROM_RECIPIENT_CLIENT = 200;
    public static final int STATUS_RETURN_RECEIPT = 250;  // from the recipient client app)
    public static final int STATUS_SEEN = 300; // message read from contact


    // message type
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_FILE = "file";

    @Exclude
    private String id;
    private String sender, recipient, text;
    private int status;
    private Long timestamp;
    private String type;
    private String sender_fullname;
    private String recipient_fullname;


    public Message() {

        this.status = STATUS_SENDING;
        this.timestamp = new Date().getTime();
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
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

    public String getRecipient_fullname() {
        return recipient_fullname;
    }

    public void setRecipient_fullname(String recipient_fullname) {
        this.recipient_fullname = recipient_fullname;
    }


    @Override
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

    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", text='" + text + '\'' +
                ", status=" + status +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", sender_fullname='" + sender_fullname + '\'' +
                '}';
    }
}
