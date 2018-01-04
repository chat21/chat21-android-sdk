package chat21.android.ui.groups.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import chat21.android.R;
import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.groups.listeners.OnRemoveClickListener;
import chat21.android.utils.image.CropCircleTransformation;

/**
 * Created by stefanodp91 on 07/12/17.
 */

class SelectedContactViewHolder extends RecyclerView.ViewHolder {
    private final TextView contact;
    private final ImageView profilePicture;
    private final ImageView remove;

    SelectedContactViewHolder(View itemView) {
        super(itemView);
        contact = (TextView) itemView.findViewById(R.id.username);
        profilePicture = (ImageView) itemView.findViewById(R.id.profile_picture);
        remove = (ImageView) itemView.findViewById(R.id.remove);
    }

    public void bind(IChatUser contact, int position, OnRemoveClickListener callback) {
        setDisplayName(contact.getFullName());
        loadProfileImage(contact);
        onRemoveClickListener(position, callback);
    }

    private void setDisplayName(String displayName) {
        contact.setText(displayName);
    }

    private void loadProfileImage(IChatUser contact) {

        String url = contact.getProfilePictureUrl();

        Glide.with(itemView.getContext())
                .load(url)
                .placeholder(R.drawable.ic_person_avatar)
                .bitmapTransform(new CropCircleTransformation(itemView.getContext()))
                .into(profilePicture);
    }

    private void onRemoveClickListener(final int position, final OnRemoveClickListener callback) {

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int returnedPosition = 0;
                if (position > 0) {
                    returnedPosition = position;
                }

                callback.onRemoveClickListener(returnedPosition);
            }
        });
    }
}