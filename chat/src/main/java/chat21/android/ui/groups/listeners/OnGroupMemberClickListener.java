package chat21.android.ui.groups.listeners;

import chat21.android.core.users.models.IChatUser;

/**
 * Created by stefanodp91 on 07/12/17.
 */

public interface OnGroupMemberClickListener {
    void onGroupMemberClicked(IChatUser groupMember, int position);
}
