package chat21.android.ui.groups.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import chat21.android.R;
import chat21.android.core.groups.models.ChatGroup;
import chat21.android.ui.groups.listeners.OnGroupClickListener;
import chat21.android.ui.adapters.AbstractRecyclerAdapter;

/**
 * Created by stefano on 29/06/2017.
 */
public class MyGroupsListAdapter extends AbstractRecyclerAdapter<ChatGroup,
        ViewHolder> {

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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_group, parent, false);
//                .inflate(R.layout.row_group_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        ChatGroup chatGroup = getItem(position);

        holder.bind(chatGroup, position, getOnGroupClickListener());
    }
}