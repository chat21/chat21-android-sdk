package chat21.android.core.groups.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by stefanodp91 on 16/01/17.
 */

public class ChatGroup implements Serializable {
    @Exclude
    private String groupId;
    private Long createdOn;
    private String iconURL = "NOICON";
    private Map<String, Integer> members;
    private String name;
    private String owner;

    public ChatGroup() {

    }

    public ChatGroup(String name, String owner) {
        this.name = name;
        this.owner = owner;
    }

    @Exclude
    public String getGroupId() {
        return groupId;
    }

    @Exclude
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public java.util.Map<String, String> getCreatedOn() {
        return ServerValue.TIMESTAMP;
    }

    public void setTimestamp(Long createdOn) {
        this.createdOn = createdOn;
    }

    @Exclude
    public Long getCreatedOnLong() {
        return createdOn;
    }

    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    public Map<String, Integer> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Integer> members) {
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "ChatGroup{" +
                "groupId='" + groupId + '\'' +
                ", createdOn=" + createdOn +
                ", iconURL='" + iconURL + '\'' +
                ", members=" + members +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }
}
