package org.chat21.android.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom abstrac adapter.
 * It requires just to create the viewholder and the methods "onBindViewHolder()" and "onCreateViewHolder".
 * <p/>
 * It works fine from RecyclerView v7:23.0.2 to v7:27.0.2
 * <p/>
 * Created by stefanodp91 on 26/08/2015.
 *
 * @param <T> Object class
 * @param <U> ViewHolder class
 */
public abstract class AbstractRecyclerAdapter<T extends Object, U extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<U> {
    private Context context;
    private Activity activity;
    private List<T> items;

    public AbstractRecyclerAdapter(Context context, List<T> items) {
        this.context = context;
        this.items = items;
    }

    public AbstractRecyclerAdapter(Activity activity, List<T> items) {
        this.activity = activity;
        this.items = items;
    }

    public void setList(List<T> mList) {
        this.items = mList;
    }


    @Override
    public abstract U onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(U holder, final int position);

    /**
     * Return the item in the selected position
     *
     * @param position the item's position
     * @return
     */
    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        if (items == null)
            return 0;

        return items.size();
    }

    public Context getContext() {
        return context;
    }

    public Activity getActivity() {
        return activity;
    }

    /**
     * @return the list of items
     */
    public List<T> getItems() {
        List<T> mList = new ArrayList<>();

        if (items != null && items.size() != 0) {
            mList = items;
        }

        return mList;
    }

    /**
     * Remove the item into a specific position
     *
     * @param position the position
     */
    public void remove(int position) {
        if (items != null && items.size() > 0) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * add an item into at the top of the list
     *
     * @param item the item to add
     */
    public void insertTop(T item) {
        int position = 0;
        if (item != null) {
            items.add(position, item);
            notifyItemInserted(position);
        }
    }

    /**
     * add an item into at the bottom of the list
     *
     * @param item the item to add
     */
    public void insertBottom(T item) {
        if (items != null) {
            int position = items.size();
            if (item != null) {
                items.add(position, item);
                notifyItemInserted(position);
            }
        }
    }

    /**
     * add an item into at a specific position
     *
     * @param item the item to add
     */
    public void insert(T item, int position) {
        if (items != null) {
            if (item != null) {
                items.add(position, item);
                notifyItemInserted(position);
            }
        }
    }

    /**
     * Clear the list
     */
    public void clear() {
        if (items != null) {
            if (items.size() > 0) {
                items.clear();
            }
            notifyDataSetChanged();
        }
    }

    /**
     * Update an item with a new value
     *
     * @param item the item
     */
    public void update(T item) {
        if (item != null) {
            List<T> list = getItems();

            int itemPosition = list.indexOf(item);

            if (itemPosition >= 0 && itemPosition < list.size()) {
                list.set(itemPosition, item);
            } else {
                list.add(item);
            }
            notifyDataSetChanged();
        }
    }
}