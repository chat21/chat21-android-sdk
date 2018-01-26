package chat21.android.ui.groups;

import chat21.android.core.groups.models.ChatGroup;

/**
 * Created by stefanodp91 on 26/01/18.
 */

public class NewGroupWizard {
//    private static final String PRIVATE_NEW_GROUP_WIZARD_SERIALIZABLE_CHAT_GROUP =
//            "PRIVATE_NEW_GROUP_WIZARD_SERIALIZABLE_CHAT_GROUP";

    private ChatGroup tempChatGroup = new ChatGroup();

    // singleton
    // source : https://android.jlelse.eu/how-to-make-the-perfect-singleton-de6b951dfdb0
    private static volatile NewGroupWizard instance;

    //private constructor.
    private NewGroupWizard() {

        // Prevent form the reflection api.
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static NewGroupWizard getInstance() {
        if (instance == null) { //if there is no instance available... create new one
            synchronized (NewGroupWizard.class) {
                if (instance == null) instance = new NewGroupWizard();
            }
        }

        return instance;
    }

//    // Make singleton from serialize and deserialize operation.
//    protected NewGroupWizard readResolve() {
//        return getInstance();
//    }
    // end singleton

    public ChatGroup getTempChatGroup() {
        return tempChatGroup;
    }
}
