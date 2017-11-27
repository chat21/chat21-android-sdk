package chat21.android.user.presence;


import android.content.Context;
import android.util.Log;

import chat21.android.presence.OnPresenceChangesListener;

import static chat21.android.utils.DebugConstants.DEBUG_MY_PRESENCE;


/**
 * Created by stefanodp91 on 03/08/17.
 * <p>
 * bugfix Issue #16
 */
public class MyPresenceHandler {

    public static void observeMyPresenceChanges(
            Context context,
            String userId,
            final OnMyPresenceChangesListener onMyPresenceChangesListener) {
        Log.i(DEBUG_MY_PRESENCE, "MyPresenceHandler.observeMyPresenceChanges");

        LoggedUserHandler loggedUserHandler = new LoggedUserHandler();
        loggedUserHandler.observeMyPresenceChanges(context, userId, onMyPresenceChangesListener);
    }

    public static void signOut(Context context, String userId,
                               String presenceDeviceInstanceId,
                               final OnPresenceChangesListener onPresenceChangesListener) {
        Log.i(DEBUG_MY_PRESENCE, "MyPresenceHandler.signOut");

        LogoutHandler logoutHandler = new LogoutHandler();
        logoutHandler.signOut(context, userId, presenceDeviceInstanceId, onPresenceChangesListener);
    }
}