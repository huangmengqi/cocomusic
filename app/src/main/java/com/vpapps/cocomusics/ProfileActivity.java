package com.vpapps.cocomusics;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vpapps.asyncTask.LoadProfile;
import com.vpapps.interfaces.SuccessListener;
import com.vpapps.utils.Constant;
import com.vpapps.utils.Methods;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileActivity extends AppCompatActivity {

    Methods methods;
    Toolbar toolbar;
    TextView textView_name, textView_email, textView_mobile, textView_notlog;
    LinearLayout ll_mobile;
    View view_phone;
    ProgressDialog progressDialog;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        toolbar = findViewById(R.id.toolbar_pro);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        textView_name = findViewById(R.id.tv_prof_fname);
        textView_email = findViewById(R.id.tv_prof_email);
        textView_mobile = findViewById(R.id.tv_prof_mobile);
        textView_notlog = findViewById(R.id.textView_notlog);

        ll_mobile = findViewById(R.id.ll_prof_phone);

        view_phone = findViewById(R.id.view_prof_phone);

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);

        if (Constant.itemUser != null && !Constant.itemUser.getId().equals("")) {
            loadUserProfile();
        } else {
            setEmpty(true, getString(R.string.not_log));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);

        if (Constant.itemUser != null && !Constant.itemUser.getId().equals("")) {
            menu.findItem(R.id.item_profile_edit).setVisible(true);
        } else {
            menu.findItem(R.id.item_profile_edit).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.item_profile_edit:
                if (Constant.itemUser != null && !Constant.itemUser.getId().equals("")) {
                    Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.not_log), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUserProfile() {
        if (methods.isNetworkAvailable()) {
            LoadProfile loadProfile = new LoadProfile(new SuccessListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String registerSuccess, String message) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        if (registerSuccess.equals("1")) {
                            setVariables();
                        } else {
                            setEmpty(false, getString(R.string.invalid_user));
                            methods.logout(ProfileActivity.this);
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, getString(R.string.err_server), Toast.LENGTH_SHORT).show();
                    }
                }
            }, methods.getAPIRequest(Constant.METHOD_PROFILE,0,"","","","","","","","","","","","","", Constant.itemUser.getId(),"", null));
            loadProfile.execute();
        } else {
            Toast.makeText(ProfileActivity.this, getString(R.string.err_internet_not_conn), Toast.LENGTH_SHORT).show();
        }
    }

    public void setVariables() {
        textView_name.setText(Constant.itemUser.getName());
        textView_mobile.setText(Constant.itemUser.getMobile());
        textView_email.setText(Constant.itemUser.getEmail());

        if (!Constant.itemUser.getMobile().trim().isEmpty()) {
            ll_mobile.setVisibility(View.VISIBLE);
            view_phone.setVisibility(View.VISIBLE);
        }

        textView_notlog.setVisibility(View.GONE);
    }

    public void setEmpty(Boolean flag, String message) {
        if (flag) {
            textView_notlog.setText(message);
            textView_notlog.setVisibility(View.VISIBLE);
        } else {
            textView_notlog.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        if (Constant.isUpdate) {
            Constant.isUpdate = false;
            setVariables();
        }
        super.onResume();
    }
}