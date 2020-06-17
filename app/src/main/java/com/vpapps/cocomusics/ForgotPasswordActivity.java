package com.vpapps.cocomusics;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vpapps.asyncTask.LoadForgotPass;
import com.vpapps.interfaces.SuccessListener;
import com.vpapps.utils.Constant;
import com.vpapps.utils.Methods;
import com.vpapps.utils.SharedPref;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ForgotPasswordActivity extends AppCompatActivity {

    Toolbar toolbar;
    Methods methods;
    Button button_send;
    EditText editText_email;
    SharedPref sharedPref;
    ProgressDialog progressDialog;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpass);

        toolbar = findViewById(R.id.toolbar_forgostpass);

        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(ForgotPasswordActivity.this);
        progressDialog.setMessage(getString(R.string.loading));

        ImageView iv = findViewById(R.id.iv);
        iv.setColorFilter(Color.RED);

        TextView tv = findViewById(R.id.tv);
        button_send = findViewById(R.id.button_forgot_send);
        editText_email = findViewById(R.id.et_forgot_email);

        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);

        sharedPref = new SharedPref(this);
        button_send.setBackground(methods.getRoundDrawable(getResources().getColor(R.color.colorPrimary)));
        button_send.setTypeface(button_send.getTypeface(), Typeface.BOLD);

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (methods.isNetworkAvailable()) {
                    if (!editText_email.getText().toString().trim().isEmpty()) {
                        loadForgotPass();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, getString(R.string.err_internet_not_conn), Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    private void loadForgotPass() {
        LoadForgotPass loadForgotPass = new LoadForgotPass(new SuccessListener() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, String registerSuccess, String message) {
                progressDialog.dismiss();
                if (success.equals("1")) {
                    Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, getString(R.string.err_server), Toast.LENGTH_SHORT).show();
                }
            }
        }, methods.getAPIRequest(Constant.METHOD_FORGOT_PASSWORD, 0, "", "", "", "", "", "", "", "", "", editText_email.getText().toString(), "","", "","","", null));
        loadForgotPass.execute();
    }
}