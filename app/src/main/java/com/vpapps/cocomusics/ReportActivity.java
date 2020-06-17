package com.vpapps.cocomusics;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vpapps.asyncTask.LoadReport;
import com.vpapps.interfaces.SuccessListener;
import com.vpapps.item.ItemSong;
import com.vpapps.utils.Constant;
import com.vpapps.utils.Methods;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ReportActivity extends AppCompatActivity {

    Toolbar toolbar;
    Methods methods;
    ItemSong itemSong;
    TextView textView_song, textView_duration, textView_catname, tv_avg_rate, tv_views, tv_download;
    ImageView imageView;
    RatingBar ratingBar;
    EditText editText_report;
    Button button_submit;
    ProgressDialog progressDialog;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        itemSong = Constant.arrayList_play.get(Constant.playPos);
        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        progressDialog = new ProgressDialog(ReportActivity.this);
        progressDialog.setMessage(getString(R.string.loading));

        toolbar = this.findViewById(R.id.toolbar_about);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        button_submit = findViewById(R.id.button_report_submit);
        editText_report = findViewById(R.id.et_report);
        tv_views = findViewById(R.id.tv_report_song_views);
        tv_download = findViewById(R.id.tv_report_song_downloads);
        textView_song = findViewById(R.id.tv_report_song_name);
        textView_duration = findViewById(R.id.tv_report_song_duration);
        tv_avg_rate = findViewById(R.id.tv_report_song_avg_rate);
        textView_catname = findViewById(R.id.tv_report_song_cat);
        imageView = findViewById(R.id.iv_report_song);
        ratingBar = findViewById(R.id.rb_report_song);

        button_submit.setBackground(methods.getRoundDrawable(getResources().getColor(R.color.colorPrimary)));

        tv_views.setText(methods.format(Double.parseDouble(itemSong.getViews())));
        tv_download.setText(methods.format(Double.parseDouble(itemSong.getDownloads())));

        textView_song.setText(itemSong.getTitle());
        textView_duration.setText(itemSong.getDuration());
        Picasso.get()
                .load(itemSong.getImageSmall())
                .placeholder(R.drawable.placeholder_song)
                .into(imageView);

        tv_avg_rate.setTypeface(tv_avg_rate.getTypeface(), Typeface.BOLD);
        tv_avg_rate.setText(itemSong.getAverageRating());
        ratingBar.setRating(Float.parseFloat(itemSong.getAverageRating()));

        if (itemSong.getCatName() != null) {
            textView_catname.setText(itemSong.getCatName());
        } else {
            textView_catname.setText(itemSong.getArtist());
        }

        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText_report.getText().toString().trim().isEmpty()) {
                    Toast.makeText(ReportActivity.this, getString(R.string.enter_report), Toast.LENGTH_SHORT).show();
                } else {
                    if(Constant.isLogged) {
                        loadReportSubmit();
//                        Toast.makeText(ReportActivity.this, "Report is disabled in demo app", Toast.LENGTH_SHORT).show();
                    } else {
                        methods.clickLogin();
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadReportSubmit() {
        if (methods.isNetworkAvailable()) {
            LoadReport loadReport = new LoadReport(new SuccessListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String registerSuccess, String message) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        if (registerSuccess.equals("1")) {
                            finish();
                            Toast.makeText(ReportActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ReportActivity.this, getString(R.string.err_server), Toast.LENGTH_SHORT).show();
                    }
                }
            }, methods.getAPIRequest(Constant.METHOD_REPORT, 0, "", itemSong.getId(), "", "", "", "", "", "", "", "", "", "", "",Constant.itemUser.getId(),editText_report.getText().toString(), null));
            loadReport.execute();
        } else {
            Toast.makeText(this, getString(R.string.err_internet_not_conn), Toast.LENGTH_SHORT).show();
        }
    }
}