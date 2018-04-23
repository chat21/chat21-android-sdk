package org.chat21.android.ui.chat_groups.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import org.chat21.android.R;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.adapters.AbstractRecyclerAdapter;
import org.chat21.android.ui.chat_groups.listeners.OnGroupMemberClickListener;
import org.chat21.android.utils.StringUtils;
import org.chat21.android.utils.image.CropCircleTransformation;

/**
 * Created by stefano on 29/06/2017.
 */
public class GroupMembersListAdapter extends AbstractRecyclerAdapter<IChatUser,
        GroupMembersListAdapter.ViewHolder> {

    private List<IChatUser> admins;

    private OnGroupMemberClickListener onGroupMemberClickListener;

    public GroupMembersListAdapter(Context context, List<IChatUser> mList) {
        super(context, mList);

        admins = new ArrayList<>();
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

        holder.contact.setText(StringUtils.isValid(contact.getFullName()) ? contact.getFullName() : contact.getId());

        loadProfileImage(holder, contact);

        showAdminLabel(holder, contact);

        setOnMemberClickListener(holder, position, contact);
    }

    // if the contact is an admin it shows the admin label near the name
    private void showAdminLabel(ViewHolder holder, IChatUser contact) {
        if (admins.contains(contact)) {
            holder.mGroupAdminLabel.setVisibility(View.VISIBLE);
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

    /**
     * Add a group admin if isn't already added
     *
     * @param admin
     */
    public void addAdmin(IChatUser admin) {
        if (!admins.contains(admin)) {
            admins.add(admin);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView contact;
        private final ImageView profilePicture;
        private final TextView mGroupAdminLabel;

        public ViewHolder(View itemView) {
            super(itemView);
            contact = (TextView) itemView.findViewById(R.id.recipient_display_name);
            profilePicture = (ImageView) itemView.findViewById(R.id.recipient_picture);
            mGroupAdminLabel = (TextView) itemView.findViewById(R.id.label_admin);
        }
    }
}