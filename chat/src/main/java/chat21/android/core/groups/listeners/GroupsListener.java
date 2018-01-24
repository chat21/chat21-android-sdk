package chat21.android.core.groups.listeners;

import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.core.groups.models.ChatGroup;

/**
 * Created by stefanodp91 on 24/01/18.
 */

public interface GroupsListener {

    void onGroupAdded(ChatGroup chatGroup, ChatRuntimeException e);

    void onGroupChanged(ChatGroup chatGroup, ChatRuntimeException e);

    void onGroupRemoved(ChatRuntimeException e);
}
