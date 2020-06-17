package com.vpapps.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.vpapps.interfaces.ClickListenerPlayList;
import com.vpapps.item.ItemMyPlayList;
import com.vpapps.item.ItemSong;
import com.vpapps.cocomusics.R;
import com.vpapps.utils.Constant;
import com.vpapps.utils.GlobalBus;
import com.vpapps.utils.Methods;

import java.util.ArrayList;

public class AdapterRecent extends RecyclerView.Adapter<AdapterRecent.MyViewHolder> {

    private Methods methods;
    private Context context;
    private ArrayList<ItemSong> arrayList;
    private ClickListenerPlayList clickListenerPlayList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView iv_song;
        ImageView iv_more;
        TextView tv_title, tv_cat;

        MyViewHolder(View view) {
            super(view);
            iv_song = view.findViewById(R.id.iv_recent);
            iv_more = view.findViewById(R.id.iv_recent_more);
            tv_title = view.findViewById(R.id.tv_recent_song);
            tv_cat = view.findViewById(R.id.tv_recent_cat);
        }
    }

    public AdapterRecent(Context context, ArrayList<ItemSong> arrayList, ClickListenerPlayList clickListenerPlayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.clickListenerPlayList = clickListenerPlayList;
        methods = new Methods(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_recent, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        holder.tv_title.setText(arrayList.get(position).getTitle());
        holder.tv_cat.setText(arrayList.get(position).getCatName());
        Picasso.get()
                .load(arrayList.get(position).getImageBig())
                .placeholder(R.drawable.placeholder_song)
                .into(holder.iv_song);

        holder.iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOptionPopUp(holder.iv_more, holder.getAdapterPosition());
            }
        });

        holder.iv_song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    private void openOptionPopUp(ImageView imageView, final int pos) {
        PopupMenu popup = new PopupMenu(context, imageView);
        popup.getMenuInflater().inflate(R.menu.popup_song, popup.getMenu());

        if (!Constant.isOnline) {
            popup.getMenu().findItem(R.id.popup_add_queue).setVisible(false);
        }
        if(!Constant.isSongDownload) {
            popup.getMenu().findItem(R.id.popup_download).setVisible(false);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.popup_add_song:
                        methods.openPlaylists(arrayList.get(pos), true);
                        break;
                    case R.id.popup_add_queue:
                        Constant.arrayList_play.add(arrayList.get(pos));
                        GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));
                        Toast.makeText(context, context.getString(R.string.add_to_queue), Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.popup_youtube:
                        try {
                            Intent intent = new Intent(Intent.ACTION_SEARCH);
                            intent.setPackage("com.google.android.youtube");
                            intent.putExtra("query", arrayList.get(pos).getTitle());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(context, context.getString(R.string.youtube_not_installed), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.popup_share:
                        methods.shareSong(arrayList.get(pos), true);
                        break;
                    case R.id.popup_download:
                        methods.download(arrayList.get(pos));
                        break;
                }
                return true;
            }
        });
        popup.show();
    }
}