package org.chat21.android.core.users.models;

import java.io.Serializable;

import org.chat21.android.core.users.models.exception.ChatUserIdException;

/**
 * Created by stefano on 21/09/2015.
 */
public class ChatUser implements IChatUser, Serializable, Comparable<IChatUser> {
    private String fullName;
    private String email;
    private String id;
    private String profilePictureUrl;
//    private String auth;
//    private String password;


    public ChatUser() {

    }

    public ChatUser(String id, String fullname) {

        if (id.contains(".")){
            throw new ChatUserIdException("Id Field contains invalid char");
        }

        this.id = id;
        this.fullName = fullname;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public void setId(String id) {
        if (id.contains(".")){
            throw new ChatUserIdException("Id Field contains invalid char");
        }
        this.id = id;
    }

    @Override
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    @Override
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }


    private static int compare(String x, String y) {
        return x.compareTo(y) < 0 ? -1 : x.compareTo(y) > 0 ? 1 : 0;
    }

    @Override
    public int compareTo(IChatUser another) {
        return compare(this.getFullName(), another.getFullName());
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;

        if (obj instanceof IChatUser) {
            IChatUser user = (IChatUser) obj;

            isEqual = user.getId().compareTo(this.getId()) == 0;
        }

        return isEqual;
    }

    @Override
    public String toString() {
        return "ChatUser{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", profilePictureUrl='" + profilePictureUrl + '\'' +
                '}';
    }
}
