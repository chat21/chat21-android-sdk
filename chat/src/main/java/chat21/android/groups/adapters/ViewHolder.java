package chat21.android.groups.adapters;

/**
 * Created by stefanodp91 on 26/09/17.
 */

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import chat21.android.R;
import chat21.android.adapters.AbstractRecyclerAdapter;
import chat21.android.groups.models.Group;
import chat21.android.groups.utils.GroupUtils;
import chat21.android.utils.StringUtils;
import chat21.android.utils.glide.CropCircleTransformation;

class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView mName;
    //    private final TextView mCreatedOn;
    private final ImageView mImage;
    private final TextView mMembers;

    public ViewHolder(View itemView) {
        super(itemView);
        mName = (TextView) itemView.findViewById(R.id.name);
//        mCreatedOn = (TextView) itemView.findViewById(R.id.created_on);
        mImage = (ImageView) itemView.findViewById(R.id.image);
        mMembers = (TextView) itemView.findViewById(R.id.members);
    }

    public void bind(Group group, int position, AbstractRecyclerAdapter.OnRecyclerItemClickListener<Group> callback) {

        setName(group.getName());

//        setCreatedOn(group.getCreatedOnLong());

        setImage(group.getIconURL());

        setOnGroupClickListener(group, position, callback);

        setMembers(group);
    }

    private void setName(String name) {
        mName.setText(name);
    }

//    private void setCreatedOn(long createdOn) {
//
//        // parse the timestamp in a nice formal
//        String timestampStr = TimeUtils.getFormattedTimestamp(createdOn);
//
//        // set it
//        mCreatedOn.setText(timestampStr);
//    }

    private void setImage(String imageUrl) {

        if (StringUtils.isValid(imageUrl) && !imageUrl.equals("NOIMAGE")) {
            // url is valid
            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_person_avatar)
                    .bitmapTransform(new CropCircleTransformation(itemView.getContext()))
                    .into(mImage);
        } else {
            // url is not valid (contains NOIMAGE)
            Glide.with(itemView.getContext())
                    .load("")
                    .placeholder(R.drawable.ic_person_avatar)
                    .bitmapTransform(new CropCircleTransformation(itemView.getContext()))
                    .into(mImage);
        }
    }

    private void setMembers(Group group) {

        String members;
        if (group != null && group.getMembers() != null) {
            members = GroupUtils.getGroupMembersAsList(itemView.getContext(), group.getMembers());
        } else {
            // if there are no members show the logged user as "you"
            members = itemView.getContext().getString(R.string.activity_message_list_group_info_you_label);
        }

        mMembers.setText(members);

    }

    private void setOnGroupClickListener(final Group group,
                                         final int position,
                                         final AbstractRecyclerAdapter.OnRecyclerItemClickListener<Group> callback) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onRecyclerItemClicked(group, position);
            }
        });
    }
}