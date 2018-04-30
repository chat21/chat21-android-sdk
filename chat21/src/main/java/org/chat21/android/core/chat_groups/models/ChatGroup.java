package org.chat21.android.core.chat_groups.models;

import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
        members = new HashMap<>();
    }

//    public ChatGroup(String name, String owner) {
//        this.name = name;
//        this.owner = owner;
//        members = new HashMap<>();
//    }

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

    public void addMembers(Map<String, Integer> members) {
        this.members.putAll(members);
    }

    public void addMember(String member) {
        this.members.put(member, 1);
    }

    @Exclude
    public List<IChatUser> getMembersList() {

        return patchMembers(members);
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

    private List<IChatUser> patchMembers(Map<String, Integer> members) {
        List<IChatUser> patchedMembers = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : members.entrySet()) {
            IChatUser contact = ChatManager.getInstance().getContactsSynchronizer().findById(entry.getKey());
            if (contact != null) {
                patchedMembers.add(contact);
            } else {
                // add user id
                // TODO: 30/04/18 hardcoded username "system"
//                Log.d("entry", entry.toString());
                if(!entry.getKey().equals("system")) {
                    patchedMembers.add(new ChatUser(entry.getKey(), ""));
                }
            }

            // TODO: 07/02/18 check for this
            if (entry.getKey().equals(ChatManager.getInstance().getLoggedUser().getId())) {
                if (!patchedMembers.contains(ChatManager.getInstance().getLoggedUser())) {
                    patchedMembers.add(ChatManager.getInstance().getLoggedUser());
                }
            }
        }


        return patchedMembers;
    }

    public String printMembersListWithSeparator(String separator) {
        String delimitedList = "";
        String usersWithoutDisplayName = "";

        if (getMembersList() != null && getMembersList().size() > 0) {
            // append chat users
            Iterator<IChatUser> it = getMembersList().iterator();

            while (it.hasNext()) {
                IChatUser usr = it.next();
                String userId = usr.getId();
                String fullName = usr.getFullName();

                if (StringUtils.isValid(fullName)) {
                    delimitedList += separator + fullName;
                } else {
                    usersWithoutDisplayName += separator + userId;
                }
            }

            // append the list of the user without the fullName to the list of the user with a
            // valid fullName
            delimitedList = delimitedList + usersWithoutDisplayName;

            // if the string starts with separator remove it
            if (delimitedList.startsWith(separator)) {
                delimitedList = delimitedList.replaceFirst("^" + separator, "");
            }
        }

        return delimitedList;
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
