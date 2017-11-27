package  chat21.android.dao.node;

import android.content.Context;

/**
 * Created by stefanodp91 on 08/09/17.
 */

public abstract class NodeDAOAbstract implements NodeDAO {
    private Context mContext;

    public NodeDAOAbstract(Context context) {
        mContext = context;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void setContext(Context context) {
        mContext = context;
    }
}