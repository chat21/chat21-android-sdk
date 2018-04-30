package org.chat21.android.ui.contacts.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.chat21.android.R;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.contacts.listeners.OnContactClickListener;
import org.chat21.android.utils.StringUtils;
import org.chat21.android.utils.image.CropCircleTransformation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//import static org.chat21.android.ui.contacts.activites.ContactListActivity.TAG_CONTACTS_SEARCH;

/**
 * Created by stefanodp91 on 05/01/17.
 */

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder>
        implements Filterable {

    private List<IChatUser> contactList;

    private List<IChatUser> contactListFiltered;

    private OnContactClickListener onContactClickListener;

    public ContactListAdapter(List<IChatUser> contactList) {
        this.contactList = contactList;
        this.contactListFiltered = contactList;
    }

    public void setList(List<IChatUser> list) {
        this.contactList = list;
    }

    public void setOnContactClickListener(OnContactClickListener onContactClickListener) {
        this.onContactClickListener = onContactClickListener;
    }

    public OnContactClickListener getOnContactClickListener() {
        return onContactClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_contact_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactListAdapter.ViewHolder holder, final int position) {
        final IChatUser contact = contactListFiltered.get(position);
        holder.mContactFullName.setText(StringUtils.isValid(contact.getFullName()) ?
                contact.getFullName() : contact.getId());
        holder.mContactUsername.setText(contact.getId());

        Glide.with(holder.itemView.getContext())
                .load(contact.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person_avatar)
                .bitmapTransform(new CropCircleTransformation(holder.itemView.getContext()))
                .into(holder.mProfilePicture);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnContactClickListener().onContactClicked(contact, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
//                Log.d(TAG_CONTACTS_SEARCH, "ContactListAdapter.getFilter.performFiltering: " +
//                        "charString == " + charString);
                if (charString.isEmpty()) {
                    contactListFiltered = contactList;
                } else {
                    List<IChatUser> filteredList = new CopyOnWriteArrayList<>();
                    for (IChatUser row : contactList) {
                        // search on the user fullname
                        if (row.getFullName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    contactListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = contactListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contactListFiltered = (CopyOnWriteArrayList<IChatUser>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mContactFullName;
        private final TextView mContactUsername;
        private final ImageView mProfilePicture;

        ViewHolder(View itemView) {
            super(itemView);
            mContactFullName = (TextView) itemView.findViewById(R.id.fullname);
            mContactUsername = (TextView) itemView.findViewById(R.id.username);
            mProfilePicture = (ImageView) itemView.findViewById(R.id.profile_picture);
        }
    }
}