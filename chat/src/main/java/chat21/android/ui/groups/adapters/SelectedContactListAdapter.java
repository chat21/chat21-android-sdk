package chat21.android.ui.groups.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import chat21.android.R;
import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.groups.listeners.OnRemoveClickListener;
import chat21.android.ui.adapters.AbstractRecyclerAdapter;

/**
 * Created by stefanodp91 on 07/12/17.
 */

public class SelectedContactListAdapter extends AbstractRecyclerAdapter<IChatUser,
        SelectedContactViewHolder> {

    private OnRemoveClickListener onRemoveClickListener;

    public SelectedContactListAdapter(Context context, List<IChatUser> mList) {
        super(context, mList);
    }

    public OnRemoveClickListener getOnRemoveClickListener() {
        return onRemoveClickListener;
    }

    // set a listener called when the "remove" button is pressed
    public void setOnRemoveClickListener(OnRemoveClickListener onRemoveClickListener) {
        this.onRemoveClickListener = onRemoveClickListener;
    }

    @Override
    public SelectedContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_selected_contact_list, parent, false);
        return new SelectedContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SelectedContactViewHolder holder, final int position) {

        IChatUser contact = getItem(position);
        holder.bind(contact, position, getOnRemoveClickListener());
    }
}