package com.vpapps.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vpapps.interfaces.ClickListenerPlayList;
import com.vpapps.item.ItemMyPlayList;
import com.vpapps.cocomusics.R;
import com.vpapps.utils.DBHelper;
import com.vpapps.utils.Methods;

import java.util.ArrayList;


public class AdapterMyPlaylist extends RecyclerView.Adapter<AdapterMyPlaylist.MyViewHolder> {

    private DBHelper dbHelper;
    private Context context;
    private ArrayList<ItemMyPlayList> arrayList;
    private ArrayList<ItemMyPlayList> filteredArrayList;
    private NameFilter filter;
    private ClickListenerPlayList clickListenerPlayList;
    private int columnWidth = 0;
    private Boolean isOnline;
    private Methods methods;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView_more, imageView1, imageView2, imageView3, imageView4;
        RelativeLayout rl;

        MyViewHolder(View view) {
            super(view);
            rl = view.findViewById(R.id.rl_myplaylist);
            textView = view.findViewById(R.id.tv_myplaylist);
            imageView_more = view.findViewById(R.id.iv_more_myplaylist);
            imageView1 = view.findViewById(R.id.iv_myplaylist1);
            imageView2 = view.findViewById(R.id.iv_myplaylist2);
            imageView3 = view.findViewById(R.id.iv_myplaylist3);
            imageView4 = view.findViewById(R.id.iv_myplaylist4);
        }
    }

    public AdapterMyPlaylist(Context context, ArrayList<ItemMyPlayList> arrayList, ClickListenerPlayList clickListenerPlayList, Boolean isOnline) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        this.context = context;
        this.isOnline = isOnline;
        this.clickListenerPlayList = clickListenerPlayList;
        dbHelper = new DBHelper(context);
        methods = new Methods(context);
        columnWidth = methods.getColumnWidth(2, 5);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_my_playlist, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        holder.textView.setText(arrayList.get(position).getName());

        if(isOnline) {
            Picasso.get()
                    .load(arrayList.get(position).getArrayListUrl().get(3))
                    .placeholder(R.drawable.placeholder_song)
                    .into(holder.imageView1);
            Picasso.get()
                    .load(arrayList.get(position).getArrayListUrl().get(2))
                    .placeholder(R.drawable.placeholder_song)
                    .into(holder.imageView2);
            Picasso.get()
                    .load(arrayList.get(position).getArrayListUrl().get(1))
                    .placeholder(R.drawable.placeholder_song)
                    .into(holder.imageView3);
            Picasso.get()
                    .load(arrayList.get(position).getArrayListUrl().get(0))
                    .placeholder(R.drawable.placeholder_song)
                    .into(holder.imageView4);
        } else {
            Picasso.get()
                    .load(methods.getAlbumArtUri(Integer.parseInt(arrayList.get(position).getArrayListUrl().get(3))))
                    .placeholder(R.drawable.placeholder_song)
                    .into(holder.imageView1);
            Picasso.get()
                    .load(methods.getAlbumArtUri(Integer.parseInt(arrayList.get(position).getArrayListUrl().get(2))))
                    .placeholder(R.drawable.placeholder_song)
                    .into(holder.imageView2);
            Picasso.get()
                    .load(methods.getAlbumArtUri(Integer.parseInt(arrayList.get(position).getArrayListUrl().get(1))))
                    .placeholder(R.drawable.placeholder_song)
                    .into(holder.imageView3);
            Picasso.get()
                    .load(methods.getAlbumArtUri(Integer.parseInt(arrayList.get(position).getArrayListUrl().get(0))))
                    .placeholder(R.drawable.placeholder_song)
                    .into(holder.imageView4);
        }

        holder.rl.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, columnWidth));

        holder.rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListenerPlayList.onClick(holder.getAdapterPosition());
            }
        });

        holder.imageView_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOptionPopUp(holder.imageView_more, holder.getAdapterPosition());
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

    public ItemMyPlayList getItem(int pos) {
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

    private void openOptionPopUp(ImageView imageView, final int pos) {
        PopupMenu popup = new PopupMenu(context, imageView);
        popup.getMenuInflater().inflate(R.menu.popup_playlist, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.popup_option_playlist:
                        dbHelper.removePlayList(arrayList.get(pos).getId(), isOnline);
                        arrayList.remove(pos);
                        notifyItemRemoved(pos);
                        Toast.makeText(context, context.getString(R.string.remove_playlist), Toast.LENGTH_SHORT).show();
                        if (arrayList.size() == 0) {
                            clickListenerPlayList.onItemZero();
                        }
                        break;
                }
                return true;
            }
        });
        popup.show();
    }
}