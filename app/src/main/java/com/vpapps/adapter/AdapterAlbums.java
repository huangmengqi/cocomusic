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
import com.vpapps.item.ItemAlbums;
import com.vpapps.cocomusics.R;
import com.vpapps.utils.Methods;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterAlbums extends RecyclerView.Adapter {

    private ArrayList<ItemAlbums> arrayList;
    private ArrayList<ItemAlbums> filteredArrayList;
    private NameFilter filter;
    private int columnWidth = 0;
    private Boolean isOnline;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    public AdapterAlbums(Context context, ArrayList<ItemAlbums> arrayList, Boolean isOnline) {
        this.arrayList = arrayList;
        this.isOnline = isOnline;
        this.filteredArrayList = arrayList;
        Methods methods = new Methods(context);
        columnWidth = methods.getColumnWidth(2, 5);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView_album, textView_artist;
        RoundedImageView imageView;

        MyViewHolder(View view) {
            super(view);
            textView_artist = view.findViewById(R.id.tv_album_artist);
            textView_album = view.findViewById(R.id.tv_album_name);
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
            ((MyViewHolder) holder).textView_album.setText(arrayList.get(position).getName());
            ((MyViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.get()
                    .load(arrayList.get(position).getImage())
                    .placeholder(R.drawable.placeholder_song)
                    .into(((MyViewHolder) holder).imageView);

            ((MyViewHolder) holder).textView_album.setTypeface(((MyViewHolder) holder).textView_album.getTypeface(), Typeface.BOLD);
            if (!isOnline) {
                ((MyViewHolder) holder).textView_artist.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).textView_artist.setText(arrayList.get(holder.getAdapterPosition()).getArtist());
            }
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    public ItemAlbums getItem(int pos) {
        return arrayList.get(pos);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    public boolean isHeader(int position) {
        return arrayList.get(position) == null;
    }

    @Override
    public int getItemViewType(int position) {
        if (arrayList.get(position) != null) {
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }
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
                ArrayList<ItemAlbums> filteredItems = new ArrayList<>();

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
        protected void publishResults(CharSequence constraint, FilterResults results) {

            arrayList = (ArrayList<ItemAlbums>) results.values;
            notifyDataSetChanged();
        }
    }
}