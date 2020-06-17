package com.vpapps.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.vpapps.item.ItemServerPlayList;
import com.vpapps.cocomusics.R;
import com.vpapps.utils.Methods;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterServerPlaylist extends RecyclerView.Adapter {

    private ArrayList<ItemServerPlayList> arrayList;
    private ArrayList<ItemServerPlayList> filteredArrayList;
    private NameFilter filter;
    private int columnWidth = 0;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    public AdapterServerPlaylist(Context context, ArrayList<ItemServerPlayList> arrayList) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        Methods methods = new Methods(context);
        columnWidth = methods.getColumnWidth(2,5);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        RoundedImageView imageView;

        MyViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.tv_album_name);
            imageView = view.findViewById(R.id.iv_albums);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private static ProgressBar progressBar;

        private ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_albums, parent, false);
            return new MyViewHolder(itemView);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_progressbar, parent, false);
            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            ((MyViewHolder) holder).imageView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth));
            ((MyViewHolder) holder).textView.setText(arrayList.get(position).getName());
            ((MyViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.get()
                    .load(arrayList.get(position).getImage())
                    .placeholder(R.drawable.placeholder_song)
                    .into(((MyViewHolder) holder).imageView);

            ((MyViewHolder) holder).textView.setTypeface(((MyViewHolder) holder).textView.getTypeface(), Typeface.BOLD);
        } else {
            if (getItemCount() == 1) {
                ProgressViewHolder.progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    public ItemServerPlayList getItem(int pos) {
        return arrayList.get(pos);
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    public boolean isHeader(int position) {
        return position == arrayList.size();
    }

    @Override
    public int getItemCount() {
        return arrayList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? VIEW_PROG : VIEW_ITEM;
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
                ArrayList<ItemServerPlayList> filteredItems = new ArrayList<>();

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

            arrayList = (ArrayList<ItemServerPlayList>) results.values;
            notifyDataSetChanged();
        }
    }
}