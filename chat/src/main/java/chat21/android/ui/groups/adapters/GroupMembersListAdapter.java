package chat21.android.ui.groups.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import chat21.android.R;
import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.groups.listeners.OnGroupMemberClickListener;
import chat21.android.core.groups.models.Group;
import chat21.android.ui.adapters.AbstractRecyclerAdapter;
import chat21.android.utils.StringUtils;
import chat21.android.utils.image.CropCircleTransformation;

/**
 * Created by stefano on 29/06/2017.
 */
public class GroupMembersListAdapter extends AbstractRecyclerAdapter<IChatUser,
        GroupMembersListAdapter.ViewHolder> {
    private static final String TAG = GroupMembersListAdapter.class.getName();
    private Group group;

    private OnGroupMemberClickListener onGroupMemberClickListener;

    public GroupMembersListAdapter(Context context, List<IChatUser> mList) {
        super(context, mList);
    }

    public OnGroupMemberClickListener getOnGroupMemberClickListener() {
        return onGroupMemberClickListener;
    }

    public void setOnGroupMemberClickListener(OnGroupMemberClickListener onGroupMemberClickListener) {
        this.onGroupMemberClickListener = onGroupMemberClickListener;
    }

    @Override
    public GroupMembersListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_group_members, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupMembersListAdapter.ViewHolder holder, final int position) {
//        Log.d(TAG, "onBindViewHolder");
        IChatUser contact = getItem(position);

        String denormalizedContactId = contact.getId().replace("_", ".");

        holder.contact.setText(StringUtils.isValid(contact.getFullName()) ?
                contact.getFullName() : denormalizedContactId);

        loadProfileImage(holder, contact);

        setGroupAdmin(holder, contact);

        setOnMemberClickListener(holder, position, contact);
    }

    // if the current contact is an admin it shows the admin label near the name
    private void setGroupAdmin(ViewHolder holder, IChatUser contact) {
        if (group != null) {
            String admin = group.getOwner(); // it's an id
            if (contact.getId().equals(admin)) {
                holder.mGroupAdminLabel.setVisibility(View.VISIBLE);
            } else {
                holder.mGroupAdminLabel.setVisibility(View.GONE);
            }
        } else {
            holder.mGroupAdminLabel.setVisibility(View.GONE);
        }
    }

    // load the current contact profile image
    private void loadProfileImage(ViewHolder holder, IChatUser contact) {
//        Log.d(TAG, "loadProfileImage");

        String url = contact.getProfilePictureUrl();

        Glide.with(holder.itemView.getContext())
                .load(url)
                .placeholder(R.drawable.ic_person_avatar)
                .bitmapTransform(new CropCircleTransformation(holder.itemView.getContext()))
                .into(holder.profilePicture);
    }

    // handle the click on a member
    private void setOnMemberClickListener(ViewHolder holder,
                                          final int position, final IChatUser contact) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnGroupMemberClickListener().onGroupMemberClicked(contact, position);
            }
        });
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView contact;
        private final ImageView profilePicture;
        private final TextView mGroupAdminLabel;

        public ViewHolder(View itemView) {
            super(itemView);
            contact = (TextView) itemView.findViewById(R.id.fullname);
            profilePicture = (ImageView) itemView.findViewById(R.id.contact_profile_picture);
            mGroupAdminLabel = (TextView) itemView.findViewById(R.id.label_admin);
        }
    }
}