package chat21.android.ui.contacts.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import chat21.android.R;
import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.contacts.listeners.OnContactClickListener;
import chat21.android.utils.image.CropCircleTransformation;

/**
 * Created by stefanodp91 on 19/10/17.
 */

class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView mContactFullName;
    private final TextView mContactUsername;
    private final ImageView mProfilePicture;

     ViewHolder(View itemView) {
        super(itemView);
        mContactFullName = (TextView) itemView.findViewById(R.id.fullname);
        mContactUsername = (TextView) itemView.findViewById(R.id.username);
        mProfilePicture = (ImageView) itemView.findViewById(R.id.profile_picture);
    }

    public void bind(IChatUser contact, int position, OnContactClickListener callback) {
        mContactFullName.setText(contact.getFullName());
        mContactUsername.setText(contact.getId());

        loadContactProfileImage(contact);

        onViewClickListener(contact, position, callback);
    }

    // load image
    private void loadContactProfileImage(IChatUser contact) {

        Glide.with(itemView.getContext())
                .load(contact.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person_avatar)
                .bitmapTransform(new CropCircleTransformation(itemView.getContext()))
                .into(mProfilePicture);
    }

    // handle the click on the item view
    private void onViewClickListener(
            final IChatUser contact,
            final int position,
            final OnContactClickListener callback) {

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onContactClicked(contact, position);
            }
        });
    }
}