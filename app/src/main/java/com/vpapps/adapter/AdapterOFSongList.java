package com.vpapps.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;
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

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import es.claucookie.miniequalizerlibrary.EqualizerView;


public class AdapterOFSongList extends RecyclerView.Adapter<AdapterOFSongList.MyViewHolder> {

    private Context context;
    private ArrayList<ItemSong> arrayList;
    private ArrayList<ItemSong> filteredArrayList;
    private ClickListenerPlayList recyclerClickListener;
    private NameFilter filter;
    private String type;
    private Methods methods;
    private DBHelper dbHelper;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView_song, textView_duration, textView_artist;
        EqualizerView equalizer;
        ImageView imageView, imageView_option;
        RelativeLayout rl;

        MyViewHolder(View view) {
            super(view);
            rl = view.findViewById(R.id.ll_off_songlist);
            textView_song = view.findViewById(R.id.tv_off_songlist_name);
            textView_duration = view.findViewById(R.id.tv_off_songlist_duration);
            equalizer = view.findViewById(R.id.equalizer_view_off);
            textView_artist = view.findViewById(R.id.tv_off_songlist_art);
            imageView = view.findViewById(R.id.iv_off_songlist);
            imageView_option = view.findViewById(R.id.iv_off_songlist_option);
        }
    }

    public AdapterOFSongList(Context context, ArrayList<ItemSong> arrayList, ClickListenerPlayList recyclerClickListener, String type) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        this.context = context;
        this.type = type;
        this.recyclerClickListener = recyclerClickListener;
        methods = new Methods(context);
        dbHelper = new DBHelper(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_offline_songs, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        holder.textView_song.setText(arrayList.get(position).getTitle());
        holder.textView_duration.setText(arrayList.get(position).getDuration());

        Uri uri;
        if (type.equals("downloads")) {
            uri = Uri.fromFile(new File(arrayList.get(holder.getAdapterPosition()).getImageSmall()));
        } else {
            uri = methods.getAlbumArtUri(Integer.parseInt(arrayList.get(holder.getAdapterPosition()).getImageBig()));
        }
        Picasso.get()
                .load(uri)
                .placeholder(R.drawable.placeholder_song)
                .into(holder.imageView);

        if (PlayerService.getIsPlayling() && Constant.arrayList_play.get(Constant.playPos).getId().equals(arrayList.get(position).getId())) {
            holder.imageView.setVisibility(View.GONE);
            holder.equalizer.setVisibility(View.VISIBLE);
            holder.equalizer.animateBars();
        } else {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.equalizer.setVisibility(View.GONE);
            holder.equalizer.stopBars();
        }

        holder.textView_artist.setText(arrayList.get(position).getArtist());

        holder.rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constant.isDownloaded = type.equals("downloads");
                recyclerClickListener.onClick(getPosition(arrayList.get(holder.getAdapterPosition()).getId()));
            }
        });

        holder.imageView_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOptionPopUp(holder.imageView_option, holder.getAdapterPosition());
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

    public ItemSong getItem(int pos) {
        return arrayList.get(pos);
    }

    private int getPosition(String id) {
        int count = 0;
        for (int i = 0; i < filteredArrayList.size(); i++) {
            if (id.equals(filteredArrayList.get(i).getId())) {
                count = i;
                break;
            }
        }
        return count;
    }

    private void openOptionPopUp(ImageView imageView, final int pos) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(context, imageView);
        popup.getMenuInflater().inflate(R.menu.popup_song_off, popup.getMenu());
        if (type.equals("downloads")) {
            popup.getMenu().findItem(R.id.popup_off_setas).setVisible(false);
            popup.getMenu().findItem(R.id.popup_off_add_song).setVisible(false);
            popup.getMenu().findItem(R.id.popup_off_add_queue).setVisible(false);
        }
        if (type.equals(context.getString(R.string.playlist))) {
            popup.getMenu().findItem(R.id.popup_off_add_song).setTitle(context.getString(R.string.remove));
        }
        if (Constant.isOnline || Constant.isDownloaded) {
            popup.getMenu().findItem(R.id.popup_off_add_queue).setVisible(false);
        }
        if (!methods.isYoutubeAppInstalled()) {
            popup.getMenu().findItem(R.id.popup_off_youtube).setVisible(false);
        }
        popup.setOnMenuItemClickListener(new android.widget.PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.popup_off_add_song:
                        if (type.equals(context.getString(R.string.playlist))) {
                            dbHelper.removeFromPlayList(arrayList.get(pos).getId(), false);
                            arrayList.remove(pos);
                            notifyItemRemoved(pos);
                            Toast.makeText(context, context.getString(R.string.remove_from_playlist), Toast.LENGTH_SHORT).show();
                            if (arrayList.size() == 0) {
                                recyclerClickListener.onItemZero();
                            }
                        } else {
                            methods.openPlaylists(arrayList.get(pos), false);
                        }
                        break;
                    case R.id.popup_off_add_queue:
                        Constant.arrayList_play.add(arrayList.get(pos));
                        GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));
                        break;
                    case R.id.popup_off_delete:
                        openDeleteDialog(pos);
                        break;
                    case R.id.popup_off_setas:
                        showBottomSheetDialog(pos);
                        break;
                    case R.id.popup_off_youtube:
                        Intent intent = new Intent(Intent.ACTION_SEARCH);
                        intent.setPackage("com.google.android.youtube");
                        intent.putExtra("query", arrayList.get(pos).getTitle());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        break;
                    case R.id.popup_off_share:
                        methods.shareSong(arrayList.get(pos), false);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    private void openDeleteDialog(final int pos) {
        final File file = new File(arrayList.get(pos).getUrl());
        final File file_image = new File(arrayList.get(pos).getImageBig());
        AlertDialog.Builder dialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog = new AlertDialog.Builder(context, R.style.ThemeDialog);
        } else {
            dialog = new AlertDialog.Builder(context);
        }
        dialog.setTitle(context.getString(R.string.delete));
        dialog.setMessage(context.getString(R.string.sure_delete));
        dialog.setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (file.exists()) {
                    if (type.equals("downloads")) {
                        dbHelper.removeFromDownload(arrayList.get(pos).getId());
                    }
                    file.delete();
                    file_image.delete();
                    arrayList.remove(pos);
                    notifyItemRemoved(pos);
                    Toast.makeText(context, context.getString(R.string.file_deleted), Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
    }


    private void showBottomSheetDialog(final int pos) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view = inflater.inflate(R.layout.layout_audio_setas, null);

        BottomSheetDialog dialog_setas = new BottomSheetDialog(context);
        dialog_setas.setContentView(view);
        dialog_setas.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        dialog_setas.show();

        LinearLayout ll_set_ring = dialog_setas.findViewById(R.id.ll_set_ring);
        LinearLayout ll_set_noti = dialog_setas.findViewById(R.id.ll_set_noti);
        LinearLayout ll_set_alarm = dialog_setas.findViewById(R.id.ll_set_alarm);

        ll_set_ring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRingtone(pos, "ring");
            }
        });

        ll_set_noti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRingtone(pos, "noti");
            }
        });

        ll_set_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRingtone(pos, "alarm");
            }
        });
    }

    private void setRingtone(final int pos, final String type) {
        boolean settingsCanWrite = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            settingsCanWrite = Settings.System.canWrite(context);

            if (!settingsCanWrite) {
                // If do not have write settings permission then open the Can modify system settings panel.
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                context.startActivity(intent);
            } else {
                loadRingTone(pos, type);
            }
        } else {
            loadRingTone(pos, type);
        }
    }

    private void loadRingTone(final int pos, final String type) {
        switch (type) {
            case "ring":
                setAsRingtone(arrayList.get(pos));
                break;
            case "noti":
                setNoti(arrayList.get(pos));
                break;
            case "alarm":
                setAlarm(arrayList.get(pos));
                break;
        }
    }

    private void setAsRingtone(ItemSong itemSong) {
        String filePath = itemSong.getUrl();

        File ringtoneFile = new File(filePath);

        MediaMetadataRetriever md = new MediaMetadataRetriever();
        md.setDataSource(ringtoneFile.getAbsolutePath());
        String title = ringtoneFile.getName();
        String duration = md.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String artist = md.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

        ContentValues content = new ContentValues();
        content.put(MediaStore.MediaColumns.DATA, filePath);
        content.put(MediaStore.MediaColumns.TITLE, title);
        content.put(MediaStore.MediaColumns.SIZE, ringtoneFile.length());
        content.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        content.put(MediaStore.Audio.Media.ARTIST, artist);
        content.put(MediaStore.Audio.Media.DURATION, duration);
        content.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        content.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        content.put(MediaStore.Audio.Media.IS_ALARM, true);
        content.put(MediaStore.Audio.Media.IS_MUSIC, true);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(ringtoneFile.getAbsolutePath());

        context.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + ringtoneFile.getAbsolutePath() + "\"", null);
        Uri newUri = context.getContentResolver().insert(uri, content);

        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);

        Toast.makeText(context, context.getString(R.string.ringtone_set), Toast.LENGTH_SHORT).show();
    }

    private void setAlarm(ItemSong itemSong) {
        String filePath = itemSong.getUrl();

        File ringtoneFile = new File(filePath);

        MediaMetadataRetriever md = new MediaMetadataRetriever();
        md.setDataSource(ringtoneFile.getAbsolutePath());
        String title = ringtoneFile.getName();
        String duration = md.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String artist = md.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

        ContentValues content = new ContentValues();
        content.put(MediaStore.MediaColumns.DATA, filePath);
        content.put(MediaStore.MediaColumns.TITLE, title);
        content.put(MediaStore.MediaColumns.SIZE, ringtoneFile.length());
        content.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        content.put(MediaStore.Audio.Media.ARTIST, artist);
        content.put(MediaStore.Audio.Media.DURATION, Integer.parseInt(duration));
        content.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        content.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        content.put(MediaStore.Audio.Media.IS_ALARM, true);
        content.put(MediaStore.Audio.Media.IS_MUSIC, true);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(ringtoneFile.getAbsolutePath());

        context.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + ringtoneFile.getAbsolutePath() + "\"", null);
        Uri newUri = context.getContentResolver().insert(uri, content);

        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, newUri);

        Toast.makeText(context, context.getString(R.string.alarm_set), Toast.LENGTH_SHORT).show();
    }

    private void setNoti(ItemSong itemSong) {

        String filePath = itemSong.getUrl();

        File ringtoneFile = new File(filePath);

        MediaMetadataRetriever md = new MediaMetadataRetriever();
        md.setDataSource(ringtoneFile.getAbsolutePath());
        String title = ringtoneFile.getName();
        String duration = md.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String artist = md.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

        ContentValues content = new ContentValues();
        content.put(MediaStore.MediaColumns.DATA, filePath);
        content.put(MediaStore.MediaColumns.TITLE, title);
        content.put(MediaStore.MediaColumns.SIZE, ringtoneFile.length());
        content.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        content.put(MediaStore.Audio.Media.ARTIST, artist);
        content.put(MediaStore.Audio.Media.DURATION, duration);
        content.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        content.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        content.put(MediaStore.Audio.Media.IS_ALARM, true);
        content.put(MediaStore.Audio.Media.IS_MUSIC, true);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(ringtoneFile.getAbsolutePath());

        context.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + ringtoneFile.getAbsolutePath() + "\"", null);
        Uri newUri = context.getContentResolver().insert(uri, content);

        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, newUri);

        Toast.makeText(context, context.getString(R.string.noti_set), Toast.LENGTH_SHORT).show();
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