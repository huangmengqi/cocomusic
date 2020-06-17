package com.vpapps.cocomusics;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.vpapps.interfaces.AdConsentListener;
import com.vpapps.utils.AdConsent;
import com.vpapps.utils.Constant;
import com.vpapps.utils.Methods;
import com.vpapps.utils.SharedPref;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SettingActivity extends AppCompatActivity {

    Toolbar toolbar;
    SharedPref sharedPref;
    Methods methods;
    AdConsent adConsent;
    LinearLayout ll_consent, ll_adView;
    SwitchCompat switch_consent, switch_noti;
    Boolean isNoti = true;
    TextView tv_privacy, tv_about, tv_moreapp, tv_rateapp, tv_shareapp;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        isNoti = sharedPref.getIsNotification();

        toolbar = this.findViewById(R.id.toolbar_setting);
        toolbar.setTitle(getString(R.string.settings));
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adConsent = new AdConsent(this, new AdConsentListener() {
            @Override
            public void onConsentUpdate() {
                setConsentSwitch();
            }
        });

        ll_consent = findViewById(R.id.ll_consent);
        switch_noti = findViewById(R.id.switch_noti);
        switch_consent = findViewById(R.id.switch_consent);
        tv_rateapp = findViewById(R.id.tv_rateapp);
        tv_shareapp = findViewById(R.id.tv_shareapp);
        tv_moreapp = findViewById(R.id.tv_moreapp);
        tv_about = findViewById(R.id.tv_about);
        tv_privacy = findViewById(R.id.tv_privacy);
        ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);

        if (adConsent.isUserFromEEA()) {
            setConsentSwitch();
        } else {
            ll_consent.setVisibility(View.GONE);
        }
        if (isNoti) {
            switch_noti.setChecked(true);
        } else {
            switch_noti.setChecked(false);
        }

        switch_noti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                OneSignal.setSubscription(isChecked);
                sharedPref.setIsNotification(isChecked);
            }
        });

        switch_consent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ConsentInformation.getInstance(SettingActivity.this).setConsentStatus(ConsentStatus.PERSONALIZED);
                } else {
                    ConsentInformation.getInstance(SettingActivity.this).setConsentStatus(ConsentStatus.NON_PERSONALIZED);
                }
            }
        });

        tv_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        tv_rateapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appName = getPackageName();//your application package name i.e play store application url
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id="
                                    + appName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id="
                                    + appName)));
                }
            }
        });

        tv_shareapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ishare = new Intent(Intent.ACTION_SEND);
                ishare.setType("text/plain");
                ishare.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_name) + " - http://play.google.com/store/apps/details?id=" + getPackageName());
                startActivity(ishare);
            }
        });

        tv_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPrivacyDialog();
            }
        });

        tv_moreapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));
            }
        });

        ll_consent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adConsent.requestConsent();
            }
        });

        changeThemeColor();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void setConsentSwitch() {
        if (ConsentInformation.getInstance(this).getConsentStatus() == ConsentStatus.PERSONALIZED) {
            switch_consent.setChecked(true);
        } else {
            switch_consent.setChecked(false);
        }
    }

    public void openPrivacyDialog() {
        Dialog dialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog = new Dialog(SettingActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            dialog = new Dialog(SettingActivity.this);
        }

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_privacy);

        WebView webview = dialog.findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        String mimeType = "text/html;charset=UTF-8";
        String encoding = "utf-8";

        if (Constant.itemAbout != null) {
            String text = "<html><head>"
                    + "<style> body{color: #000 !important;text-align:left}"
                    + "</style></head>"
                    + "<body>"
                    + Constant.itemAbout.getPrivacy()
                    + "</body></html>";
            webview.loadDataWithBaseURL("blarg://ignored", text, mimeType, encoding, "");
        }

        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void changeThemeColor() {

        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked},
        };

        int[] thumbColors = new int[]{
                ContextCompat.getColor(SettingActivity.this, R.color.switch_thumb_disable),
                ContextCompat.getColor(SettingActivity.this, R.color.colorPrimary),
        };

        int[] trackColors = new int[]{
                ContextCompat.getColor(SettingActivity.this, R.color.black40),
                ContextCompat.getColor(SettingActivity.this, R.color.black40),
        };
        DrawableCompat.setTintList(DrawableCompat.wrap(switch_noti.getThumbDrawable()), new ColorStateList(states, thumbColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(switch_noti.getTrackDrawable()), new ColorStateList(states, trackColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(switch_consent.getThumbDrawable()), new ColorStateList(states, thumbColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(switch_consent.getTrackDrawable()), new ColorStateList(states, trackColors));
    }
}