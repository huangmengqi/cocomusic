package com.vpapps.cocomusics;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vpapps.asyncTask.LoadProfileEdit;
import com.vpapps.interfaces.SuccessListener;
import com.vpapps.utils.Constant;
import com.vpapps.utils.Methods;
import com.vpapps.utils.SharedPref;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileEditActivity extends AppCompatActivity {

    private EditText editText_name, editText_email, editText_phone, editText_pass, editText_cpass;
    Toolbar toolbar;
    Methods methods;
    SharedPref sharedPref;
    ProgressDialog progressDialog;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        toolbar = findViewById(R.id.toolbar_proedit);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AppCompatButton button_update = findViewById(R.id.button_prof_update);
        editText_name = findViewById(R.id.editText_profedit_name);
        editText_email = findViewById(R.id.editText_profedit_email);
        editText_phone = findViewById(R.id.editText_profedit_phone);
        editText_pass = findViewById(R.id.editText_profedit_password);
        editText_cpass = findViewById(R.id.editText_profedit_cpassword);

        ViewCompat.setBackgroundTintList(editText_name, ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        ViewCompat.setBackgroundTintList(editText_email, ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        ViewCompat.setBackgroundTintList(editText_phone, ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        ViewCompat.setBackgroundTintList(editText_pass, ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        ViewCompat.setBackgroundTintList(editText_cpass, ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);

        setProfileVar();

        button_update.setBackground(methods.getRoundDrawable(getResources().getColor(R.color.colorPrimary)));
        button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    loadUpdateProfile();
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

    private Boolean validate() {
        editText_name.setError(null);
        editText_email.setError(null);
        editText_cpass.setError(null);
        if (editText_name.getText().toString().trim().isEmpty()) {
            editText_name.setError(getString(R.string.cannot_empty));
            editText_name.requestFocus();
            return false;
        } else if (editText_email.getText().toString().trim().isEmpty()) {
            editText_email.setError(getString(R.string.email_empty));
            editText_email.requestFocus();
            return false;
        } else if (editText_pass.getText().toString().endsWith(" ")) {
            editText_pass.setError(getString(R.string.pass_end_space));
            editText_pass.requestFocus();
            return false;
        } else if (!editText_pass.getText().toString().trim().equals(editText_cpass.getText().toString().trim())) {
            editText_cpass.setError(getString(R.string.pass_nomatch));
            editText_cpass.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private void updateArray() {
        Constant.itemUser.setName(editText_name.getText().toString());
        Constant.itemUser.setEmail(editText_email.getText().toString());
        Constant.itemUser.setMobile(editText_phone.getText().toString());

        if (!editText_pass.getText().toString().equals("")) {
            sharedPref.setRemeber(false);
        }
    }

    private void loadUpdateProfile() {
        if (methods.isNetworkAvailable()) {
            LoadProfileEdit loadProfileEdit = new LoadProfileEdit(new SuccessListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String registerSuccess, String message) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        switch (registerSuccess) {
                            case "1":
                                updateArray();
                                Constant.isUpdate = true;
                                finish();
                                Toast.makeText(ProfileEditActivity.this, message, Toast.LENGTH_SHORT).show();
                                break;
                            case "-1":
                                methods.getVerifyDialog(getString(R.string.error_unauth_access), message);
                                break;
                            default:
                                if (message.contains("Email address already used")) {
                                    editText_email.setError(message);
                                    editText_email.requestFocus();
                                } else {
                                    Toast.makeText(ProfileEditActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    } else {
                        Toast.makeText(ProfileEditActivity.this, getString(R.string.err_server), Toast.LENGTH_SHORT).show();
                    }
                }
            }, methods.getAPIRequest(Constant.METHOD_PROFILE_EDIT,0,"","","","","","","","","",editText_email.getText().toString(),editText_pass.getText().toString(),editText_name.getText().toString(),editText_phone.getText().toString(), Constant.itemUser.getId(),"", null));
            loadProfileEdit.execute();
        } else {
            Toast.makeText(ProfileEditActivity.this, getString(R.string.err_internet_not_conn), Toast.LENGTH_SHORT).show();
        }
    }

    public void setProfileVar() {
        editText_name.setText(Constant.itemUser.getName());
        editText_phone.setText(Constant.itemUser.getMobile());
        editText_email.setText(Constant.itemUser.getEmail());
    }
}