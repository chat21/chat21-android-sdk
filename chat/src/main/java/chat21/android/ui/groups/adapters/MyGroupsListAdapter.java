package chat21.android.ui.groups.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import chat21.android.R;
import chat21.android.core.chat_groups.models.ChatGroup;
import chat21.android.ui.adapters.AbstractRecyclerAdapter;
import chat21.android.ui.groups.listeners.OnGroupClickListener;
import chat21.android.utils.image.CropCircleTransformation;

/**
 * Created by stefano on 29/06/2017.
 */
public class MyGroupsListAdapter extends AbstractRecyclerAdapter<ChatGroup, MyGroupsListAdapter.ViewHolder> {

    private OnGroupClickListener onGroupClickListener;

    public OnGroupClickListener getOnGroupClickListener() {
        return onGroupClickListener;
    }

    public void setOnGroupClickListener(OnGroupClickListener onGroupClickListener) {
        this.onGroupClickListener = onGroupClickListener;
    }

    public MyGroupsListAdapter(Context context, List<ChatGroup> mList) {
        super(context, mList);
        setList(mList);
    }

    private void sortItems(List<ChatGroup> mList) {
        // sort by descending timestamp (first the last created, than the oldest)
        Collections.sort(mList, new Comparator<ChatGroup>() {
            @Override
            public int compare(ChatGroup item1, ChatGroup item2) {
                return Long.compare(item2.getCreatedOnLong(), item1.getCreatedOnLong());
            }
        });
    }

    @Override
    public void setList(List<ChatGroup> mList) {
        sortItems(mList);
        super.setList(mList);
    }

    @Override
    public MyGroupsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyGroupsListAdapter.ViewHolder holder, final int position) {
        ChatGroup chatGroup = getItem(position);

        setName(holder, chatGroup.getName());

//        setCreatedOn(holder, chatGroup.getCreatedOnLong());

        setImage(holder, chatGroup.getIconURL());

        setMembers(holder, chatGroup);

        setOnGroupClickListener(holder, chatGroup, position, getOnGroupClickListener());
    }


    private void setName(MyGroupsListAdapter.ViewHolder holder, String name) {
        holder.name.setText(name);
    }

//    private void setCreatedOn(MyGroupsListAdapter.ViewHolder holder, long createdOn) {
//
//        // parse the timestamp in a nice formal
//        String timestampStr = TimeUtils.getFormattedTimestamp(createdOn);
//
//        // set it
//        holder.createdOn.setText(timestampStr);
//    }

    private void setImage(MyGroupsListAdapter.ViewHolder holder, String imageUrl) {
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_group_avatar)
                .bitmapTransform(new CropCircleTransformation(holder.itemView.getContext()))
                .into(holder.image);
    }

    private void setMembers(MyGroupsListAdapter.ViewHolder holder, ChatGroup chatGroup) {

        String members;
        if (chatGroup.getMembersList() != null && chatGroup.getMembersList().size() > 0) {
            members = chatGroup.printMembersListWithSeparator(", ");
        } else {
            // if there are no members show the logged user as "you"
            members = holder.itemView.getContext().getString(R.string.activity_message_list_group_info_you_label);
        }

        holder.members.setText(members);
    }

    private void setOnGroupClickListener(MyGroupsListAdapter.ViewHolder holder, final ChatGroup chatGroup,
                                         final int position,
                                         final OnGroupClickListener callback) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onGroupClicked(chatGroup, position);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        //        private final TextView createdOn;
        private final ImageView image;
        private final TextView members;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
//            createdOn = (TextView) itemView.findViewById(R.id.created_on);
            image = (ImageView) itemView.findViewById(R.id.image);
            members = (TextView) itemView.findViewById(R.id.members);
        }
    }
}