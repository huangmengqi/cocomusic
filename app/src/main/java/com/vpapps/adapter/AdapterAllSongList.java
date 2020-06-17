package com.vpapps.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vpapps.interfaces.ClickListenerPlayList;
import com.vpapps.item.ItemMyPlayList;
import com.vpapps.item.ItemSong;
import com.vpapps.cocomusics.PlayerService;
import com.vpapps.cocomusics.R;
import com.vpapps.utils.Constant;
import com.vpapps.utils.DBHelper;
import com.vpapps.utils.GlobalBus;
import com.vpapps.utils.Methods;

import java.util.ArrayList;

import es.claucookie.miniequalizerlibrary.EqualizerView;


public class AdapterAllSongList extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<ItemSong> arrayList;
    private ArrayList<ItemSong> filteredArrayList;
    private ClickListenerPlayList recyclerClickListener;
    private NameFilter filter;
    private String type;
    private Methods methods;
    private DBHelper dbHelper;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    public AdapterAllSongList(Context context, ArrayList<ItemSong> arrayList, ClickListenerPlayList recyclerClickListener, String type) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        this.context = context;
        this.type = type;
        this.recyclerClickListener = recyclerClickListener;
        methods = new Methods(context);
        dbHelper = new DBHelper(context);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView_song, textView_duration, textView_catname, tv_avg_rate, tv_views, tv_download;
        EqualizerView equalizer;
        ImageView imageView, imageView_option, iv_downlaod_icon;
        RelativeLayout rl;
        RatingBar ratingBar;

        MyViewHolder(View view) {
            super(view);
            rl = view.findViewById(R.id.ll_songlist);
            tv_views = view.findViewById(R.id.tv_songlist_views);
            tv_download = view.findViewById(R.id.tv_songlist_downloads);
            textView_song = view.findViewById(R.id.tv_songlist_name);
            textView_duration = view.findViewById(R.id.tv_songlist_duration);
            tv_avg_rate = view.findViewById(R.id.tv_songlist_avg_rate);
            equalizer = view.findViewById(R.id.equalizer_view);
            textView_catname = view.findViewById(R.id.tv_songlist_cat);
            imageView = view.findViewById(R.id.iv_songlist);
            imageView_option = view.findViewById(R.id.iv_songlist_option);
            ratingBar = view.findViewById(R.id.rb_songlist);
            iv_downlaod_icon = view.findViewById(R.id.iv_downlaod_icon);

            if(!Constant.isSongDownload) {
                tv_download.setVisibility(View.GONE);
                iv_downlaod_icon.setVisibility(View.GONE);
            }
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
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recent_songs, parent, false);
            return new MyViewHolder(itemView);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_progressbar, parent, false);
            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {

            ((MyViewHolder) holder).tv_views.setText(methods.format(Double.parseDouble(arrayList.get(position).getViews())));
            ((MyViewHolder) holder).tv_download.setText(methods.format(Double.parseDouble(arrayList.get(position).getDownloads())));

            ((MyViewHolder) holder).textView_song.setText(arrayList.get(position).getTitle());
            ((MyViewHolder) holder).textView_duration.setText(arrayList.get(position).getDuration());
            Picasso.get()
                    .load(arrayList.get(position).getImageSmall())
                    .placeholder(R.drawable.placeholder_song)
                    .into(((MyViewHolder) holder).imageView);

            ((MyViewHolder) holder).tv_avg_rate.setTypeface(((MyViewHolder) holder).tv_avg_rate.getTypeface(), Typeface.BOLD);
            ((MyViewHolder) holder).tv_avg_rate.setText(arrayList.get(position).getAverageRating());
            ((MyViewHolder) holder).ratingBar.setRating(Float.parseFloat(arrayList.get(position).getAverageRating()));

            if (PlayerService.getIsPlayling() && Constant.arrayList_play.get(Constant.playPos).getId().equals(arrayList.get(position).getId())) {
                ((MyViewHolder) holder).imageView.setVisibility(View.GONE);
                ((MyViewHolder) holder).equalizer.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).equalizer.animateBars();
            } else {
                ((MyViewHolder) holder).imageView.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).equalizer.setVisibility(View.GONE);
                ((MyViewHolder) holder).equalizer.stopBars();
            }

            if (arrayList.get(position).getCatName() != null) {
                ((MyViewHolder) holder).textView_catname.setText(arrayList.get(position).getCatName());
            } else {
                ((MyViewHolder) holder).textView_catname.setText(arrayList.get(position).getArtist());
            }

            ((MyViewHolder) holder).rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(arrayList.size() > holder.getAdapterPosition()) {
                        recyclerClickListener.onClick(getPosition(arrayList.get(holder.getAdapterPosition()).getId()));
                    }
                }
            });

            ((MyViewHolder) holder).imageView_option.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openOptionPopUp(((MyViewHolder) holder).imageView_option, holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
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

    private int getPosition(String id) {
        int count=0;
        for(int i=0;i<filteredArrayList.size();i++) {
            if(id.equals(filteredArrayList.get(i).getId())) {
                count = i;
                break;
            }
        }
        return count;
    }

    private void openOptionPopUp(ImageView imageView, final int pos) {
        PopupMenu popup = new PopupMenu(context, imageView);
        popup.getMenuInflater().inflate(R.menu.popup_song, popup.getMenu());
        if (type.equals("playlist")) {
            popup.getMenu().findItem(R.id.popup_add_song).setTitle(context.getString(R.string.remove));
        }
        if (!Constant.isOnline) {
            popup.getMenu().findItem(R.id.popup_add_queue).setVisible(false);
        }
        if(!methods.isYoutubeAppInstalled()) {
            popup.getMenu().findItem(R.id.popup_youtube).setVisible(false);
        }
        if(!Constant.isSongDownload) {
            popup.getMenu().findItem(R.id.popup_download).setVisible(false);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.popup_add_song:
                        switch (type) {
                            case "playlist":
                                dbHelper.removeFromPlayList(arrayList.get(pos).getId(), true);
                                arrayList.remove(pos);
                                notifyItemRemoved(pos);
                                Toast.makeText(context, context.getString(R.string.remove_from_playlist), Toast.LENGTH_SHORT).show();
                                if (arrayList.size() == 0) {
                                    recyclerClickListener.onItemZero();
                                }
                                break;
                            default:
                                methods.openPlaylists(arrayList.get(pos), true);
                                break;
                        }
                        break;
                    case R.id.popup_add_queue:
                        Constant.arrayList_play.add(arrayList.get(pos));
                        GlobalBus.getBus().postSticky(new ItemMyPlayList("","",null));
                        Toast.makeText(context, context.getString(R.string.add_to_queue), Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.popup_youtube:
                        Intent intent = new Intent(Intent.ACTION_SEARCH);
                        intent.setPackage("com.google.android.youtube");
                        intent.putExtra("query", arrayList.get(pos).getTitle());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
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
                ArrayList<ItemSong> filteredItems = new ArrayList<>();

                for (int i = 0, l = filteredArrayList.size(); i < l; i++) {
                    String nameList = filteredArrayList.get(i).getTitle();
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

            arrayList = (ArrayList<ItemSong>) results.values;
            notifyDataSetChanged();
        }
    }
}