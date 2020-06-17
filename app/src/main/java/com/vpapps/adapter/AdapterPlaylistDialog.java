package com.vpapps.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import com.vpapps.interfaces.ClickListenerPlayList;
import com.vpapps.item.ItemMyPlayList;
import com.vpapps.cocomusics.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class AdapterPlaylistDialog extends RecyclerView.Adapter<AdapterPlaylistDialog.MyViewHolder> {

    private ArrayList<ItemMyPlayList> arrayList;
    private ArrayList<ItemMyPlayList> filteredArrayList;
    private NameFilter filter;
    private ClickListenerPlayList clickListenerPlayList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        MyViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textView_playlist_dialog);
        }
    }

    public AdapterPlaylistDialog(ArrayList<ItemMyPlayList> arrayList, ClickListenerPlayList clickListenerPlayList) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        this.clickListenerPlayList = clickListenerPlayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_playlist_dialog, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        holder.textView.setText(arrayList.get(position).getName());
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListenerPlayList.onClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public String getID(int pos) {
        return arrayList.get(pos).getId();
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new NameFilter();
        }
        return filter;
    }

    private class NameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<ItemMyPlayList> filteredItems = new ArrayList<>();

                for (int i = 0, l = filteredArrayList.size(); i < l; i++) {
                    String nameList = filteredArrayList.get(i).getName();
                    if (nameList.toLowerCase().contains(constraint))
                        filteredItems.add(filteredArrayList.get(i));
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.values = filteredArrayList;
                    result.count = filteredArrayList.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            arrayList = (ArrayList<ItemMyPlayList>) results.values;
            notifyDataSetChanged();
        }
    }
}