package chat21.android.presence;

import android.content.Context;
import android.util.Log;

import static chat21.android.utils.DebugConstants.DEBUG_PRESENCE;

/**
 * Created by stefanodp91 on 03/08/17.
 * <p>
 * bugfix Issue #16
 */
public class PresenceHandler {

    public static void observeUserPresenceChanges(Context context, String userId,
                                                  final OnPresenceChangesListener onPresenceChangesListener) {
        Log.i(DEBUG_PRESENCE, "PresenceHandler.observeUserPresenceChanges");

        UserHandler userHandler = new UserHandler();
        userHandler.observeUserPresenceChanges(context, userId, onPresenceChangesListener);
    }
}