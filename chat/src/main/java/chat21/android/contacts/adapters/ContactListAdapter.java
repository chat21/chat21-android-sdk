package chat21.android.contacts.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import chat21.android.R;
import chat21.android.adapters.AbstractRecyclerAdapter;
import chat21.android.user.models.IChatUser;

/**
 * Created by stefano on 17/09/2015.
 */
public class ContactListAdapter extends AbstractRecyclerAdapter<IChatUser,
        ViewHolder> {

    public ContactListAdapter(Context context, List<IChatUser> contacts) {
        super(context, contacts);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_contact_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        IChatUser contact = getItem(position);

        holder.bind(contact, position, getOnRecyclerItemClickListener());
    }
}