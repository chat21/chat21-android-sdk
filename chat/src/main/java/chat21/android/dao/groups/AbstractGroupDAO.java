package  chat21.android.dao.groups;

import android.content.Context;

import  chat21.android.core.groups.models.Group;
import  chat21.android.utils.StringUtils;

/**
 * Created by stefanodp91 on 26/09/17.
 */

abstract class AbstractGroupDAO implements GroupsDAO {
    private Context mContext;

    public AbstractGroupDAO(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {

        mContext = context;
    }

    @Override
    public boolean isValidGroup(Group group) {
        if (group != null) {

            boolean isValidName = StringUtils.isValid(group.getName());
            boolean isValidMembers = group.getMembers() != null && group.getMembers().size() > 0;
            boolean isValidOwner = StringUtils.isValid(group.getOwner());
            boolean isValidTimestamp = group.getCreatedOnLong() != 0;

            if (isValidName && isValidMembers && isValidOwner && isValidTimestamp)
                return true;
            else
                return false;
        } else {
            return false;
        }
    }
}