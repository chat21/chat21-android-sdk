package chat21.android.core.groups;

import chat21.android.core.groups.models.ChatGroup;
import chat21.android.utils.StringUtils;

/**
 * Created by stefanodp91 on 14/07/17.
 */

public class GroupUtils {

    public static boolean isValidGroup(ChatGroup chatGroup) {
        if (chatGroup != null) {

            boolean isValidName = StringUtils.isValid(chatGroup.getName());
            boolean isValidMembers = chatGroup.getMembers() != null && chatGroup.getMembers().size() > 0;
            boolean isValidOwner = StringUtils.isValid(chatGroup.getOwner());
            boolean isValidTimestamp = chatGroup.getCreatedOnLong() != 0;

            if (isValidName && isValidMembers && isValidOwner && isValidTimestamp)
                return true;
            else
                return false;
        } else {
            return false;
        }
    }

    public interface OnGroupCreatedListener {
        void onGroupCreatedSuccess(String groupId, ChatGroup chatGroup);

        void onGroupCreatedError(String errorMessage);
    }

    public interface OnGroupUpdatedListener {
        void onGroupUpdatedSuccess(String groupId, ChatGroup chatGroup);

        void onGroupUpdatedError(String errorMessage);
    }
}