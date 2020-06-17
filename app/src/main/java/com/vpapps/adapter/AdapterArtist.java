package com.vpapps.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.vpapps.item.ItemArtist;
import com.vpapps.cocomusics.R;
import com.vpapps.utils.Methods;

import java.util.ArrayList;

public class AdapterArtist extends RecyclerView.Adapter {

    private ArrayList<ItemArtist> arrayList;
    private ArrayList<ItemArtist> filteredArrayList;
    private NameFilter filter;
    private int columnWidth = 0;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    public AdapterArtist(Context context, ArrayList<ItemArtist> arrayList) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        Methods methods = new Methods(context);
        columnWidth = methods.getColumnWidth(3, 20);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView iv;
        RoundedImageView imageView;
        CardView cardView;
        LinearLayout ll;
        View vieww;

        MyViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.tv_cat);
            imageView = view.findViewById(R.id.iv_cat);
            iv = view.findViewById(R.id.iv);
            cardView = view.findViewById(R.id.cv_cat);
            ll = view.findViewById(R.id.ll);
            vieww = view.findViewById(R.id.view_cat);
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
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_cat, parent, false);
            return new MyViewHolder(itemView);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_progressbar, parent, false);
            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            ((MyViewHolder) holder).iv.setVisibility(View.GONE);

            ((MyViewHolder) holder).vieww.setLayoutParams(new FrameLayout.LayoutParams(columnWidth, columnWidth));
            ((MyViewHolder) holder).imageView.setLayoutParams(new FrameLayout.LayoutParams(columnWidth, columnWidth));
            ((MyViewHolder) holder).cardView.setLayoutParams(new LinearLayout.LayoutParams(columnWidth, columnWidth));
            ((MyViewHolder) holder).cardView.setRadius(columnWidth / 2);
            ((MyViewHolder) holder).imageView.setCornerRadius(columnWidth / 2);
            ((MyViewHolder) holder).textView.setText(arrayList.get(position).getName());
            ((MyViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.get()
                    .load(arrayList.get(position).getImage())
                    .placeholder(R.drawable.placeholder_artist)
                    .into(((MyViewHolder) holder).imageView);
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    public boolean isHeader(int position) {
        return arrayList.get(position) == null;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (arrayList.get(position) != null) {
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }
    }

    public ItemArtist getItem(int pos) {
        return arrayList.get(pos);
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
                ArrayList<ItemArtist> filteredItems = new ArrayList<>();

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

            arrayList = (ArrayList<ItemArtist>) results.values;
            notifyDataSetChanged();
        }
    }
}