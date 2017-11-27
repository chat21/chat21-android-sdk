package  chat21.android.dao.message;

import android.content.Context;

/**
 * Created by stefanodp91 on 08/09/17.
 */
abstract class MessageDAOAbstract implements MessageDAO {
    private Context mContext;

    public MessageDAOAbstract(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }
}