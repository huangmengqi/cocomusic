package com.vpapps.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tiagosantos.enchantedviewpager.EnchantedViewPager;
import com.tiagosantos.enchantedviewpager.EnchantedViewPagerAdapter;
import com.vpapps.interfaces.InterAdListener;
import com.vpapps.item.ItemHomeBanner;
import com.vpapps.cocomusics.R;
import com.vpapps.cocomusics.SongByCatActivity;
import com.vpapps.utils.Methods;

import java.util.ArrayList;

public class HomePagerAdapter extends EnchantedViewPagerAdapter {

    private Context mContext;
    private LayoutInflater inflater;
    private ArrayList<ItemHomeBanner> arrayList;
    private Methods methods;

    public HomePagerAdapter(Context context, ArrayList<ItemHomeBanner> arrayList) {
        super(arrayList);
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.arrayList = arrayList;
        methods = new Methods(context, interAdListener);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View mCurrentView = inflater.inflate(R.layout.layout_home_banner, container, false);

        TextView tv_title, tv_desc, tv_total;
        ImageView iv_banner, iv_forward;
        View view_gradient;

        tv_title = mCurrentView.findViewById(R.id.tv_home_banner);
        tv_desc = mCurrentView.findViewById(R.id.tv_home_banner_desc);
        tv_total = mCurrentView.findViewById(R.id.tv_home_banner_total);
        iv_banner = mCurrentView.findViewById(R.id.iv_home_banner);
        iv_forward = mCurrentView.findViewById(R.id.iv3);
        view_gradient = mCurrentView.findViewById(R.id.view_home_banner);

        iv_forward.setColorFilter(Color.WHITE);
        tv_title.setText(arrayList.get(position).getTitle());
        tv_desc.setText(arrayList.get(position).getDesc());
        tv_total.setText(arrayList.get(position).getTotalSong() + " " + mContext.getString(R.string.songs));
        Picasso.get()
                .load(arrayList.get(position).getImage())
                .placeholder(R.drawable.placeholder_song)
                .into(iv_banner);

        new LoadColor(view_gradient, tv_title).execute(arrayList.get(position).getImage());

        mCurrentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                methods.showInterAd(position, "");
            }
        });

        mCurrentView.setTag(EnchantedViewPager.ENCHANTED_VIEWPAGER_POSITION + position);
        container.addView(mCurrentView);

        return mCurrentView;
    }

    public class LoadColor extends AsyncTask<String, String, String> {

        Bitmap bitmap;
        TextView textView;
        View view;

        LoadColor(View view, TextView textView) {
            this.view = view;
            this.textView = textView;
        }

        @Override
        protected String doInBackground(String... strings) {
            bitmap = methods.getBitmapFromURL(strings[0]);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@NonNull Palette palette) {
                        int defaultValue = 0x000000;
                        int vibrant = palette.getVibrantColor(defaultValue);

                        view.setBackground(methods.getGradientDrawable(vibrant, Color.parseColor("#00000000")));
                    }
                });
                super.onPostExecute(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    InterAdListener interAdListener = new InterAdListener() {
        @Override
        public void onClick(int position, String type) {
            Intent intent = new Intent(mContext, SongByCatActivity.class);
            intent.putExtra("type", mContext.getString(R.string.banner));
            intent.putExtra("id", arrayList.get(position).getId());
            intent.putExtra("name", arrayList.get(position).getTitle());
            intent.putExtra("songs", arrayList.get(position).getArrayListSongs());
            mContext.startActivity(intent);
        }
    };
}