//package chat21.android.core.session;
//
//
//import android.content.Context;
//import android.util.Log;
//
//import chat21.android.core.users.models.IChatUser;
//import chat21.android.utils.IOUtils;
//
//import static chat21.android.utils.DebugConstants.DEBUG_SESSION;
//
///**
// * Created by stefanodp91 on 21/12/17.
// */
//
//public class UserSessionManager {
//    // filename of the serialized user
//    private static final String SERIALIZED_CURRENT_USER = "SERIALIZED_CURRENT_USER_UNIQUE_RESOURCE";
//
//    private IChatUser currentUser;
//
//    // singleton
//    // source : https://android.jlelse.eu/how-to-make-the-perfect-singleton-de6b951dfdb0
//    private static volatile UserSessionManager instance;
//
//    // private constructor.
//    private UserSessionManager() {
//
//        // Prevent form the reflection api.
//        if (instance != null) {
//            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
//        }
//    }
//
//    public static UserSessionManager getInstance() {
//        if (instance == null) { //if there is no instance available... create new one
//            synchronized (UserSessionManager.class) {
//                if (instance == null) instance = new UserSessionManager();
//            }
//        }
//
//        return instance;
//    }
//
//    // Make singleton from serialize and deserialize operation.
//    protected UserSessionManager readResolve() {
//        return getInstance();
//    }
//    // end singleton
//
//
//    public void setCurrentUser(Context context, IChatUser currentUser) {
//        Log.d(DEBUG_SESSION, "UserSessionManager.setCurrentUser: currentUser == " + currentUser.toString());
//        IOUtils.saveObjectToFile(context, SERIALIZED_CURRENT_USER, currentUser); // serialize on disk
//        this.currentUser = currentUser;
//    }
//
//    public IChatUser getCurrentUser(Context context) {
//        Log.d(DEBUG_SESSION, "UserSessionManager.getCurrentUser");
//        // retrieve from disk
//        if (currentUser!=null) {
//            return  currentUser;
//        }else {
//            currentUser = (IChatUser) IOUtils.getObjectFromFile(context, SERIALIZED_CURRENT_USER);
//            return currentUser;
//        }
//    }
//
//    public boolean isUserLogged(Context context) {
//        Log.d(DEBUG_SESSION, "UserSessionManager.isUserLogged");
//        boolean isUserLogged = getCurrentUser(context) != null ? true : false;
//        Log.d(DEBUG_SESSION, "UserSessionManager.isUserLogged: isUserLogged == " + isUserLogged);
//        return isUserLogged;
//    }
//}
