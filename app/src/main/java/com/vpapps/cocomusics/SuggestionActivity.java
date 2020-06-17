package com.vpapps.cocomusics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vpapps.asyncTask.LoadSuggestion;
import com.vpapps.interfaces.SuccessListener;
import com.vpapps.utils.Constant;
import com.vpapps.utils.Methods;

import java.io.File;
import java.io.IOException;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SuggestionActivity extends AppCompatActivity {

    Toolbar toolbar;
    Methods methods;
    LinearLayout ll_sugg;
    TextView editText_title, editText_desc;
    ImageView imageView;
    Button button_submit;
    String imagePath = "";
    Bitmap bitmap_upload;
    ProgressDialog progressDialog;
    private int PICK_IMAGE_REQUEST = 1;
    final static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        progressDialog = new ProgressDialog(SuggestionActivity.this);
        progressDialog.setMessage(getString(R.string.loading));

        toolbar = this.findViewById(R.id.toolbar_sugg);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ll_sugg = findViewById(R.id.ll_sugg);
        button_submit = findViewById(R.id.button_sugg_submit);
        imageView = findViewById(R.id.iv_sugg_song);
        editText_desc = findViewById(R.id.et_sugg_desc);
        editText_title = findViewById(R.id.et_sugg_title);

        button_submit.setBackground(methods.getRoundDrawable(getResources().getColor(R.color.colorPrimary)));

        ll_sugg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPer()) {
                    pickImage();
                }
            }
        });

        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText_title.getText().toString().equals("")) {
                    Toast.makeText(SuggestionActivity.this, getString(R.string.enter_song_title), Toast.LENGTH_SHORT).show();
                } else if(editText_desc.getText().toString().equals("")) {
                    Toast.makeText(SuggestionActivity.this, getString(R.string.enter_song_desc), Toast.LENGTH_SHORT).show();
                } else if(imagePath!= null && imagePath.equals("")) {
                    Toast.makeText(SuggestionActivity.this, getString(R.string.select_song_image), Toast.LENGTH_SHORT).show();
                } else {
                    if(Constant.isLogged) {
                        loadSuggestion();
//                        Toast.makeText(SuggestionActivity.this, "Suggestion is disabled in demo app", Toast.LENGTH_SHORT).show();
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

    public void loadSuggestion() {
        if (methods.isNetworkAvailable()) {
            LoadSuggestion loadSuggestion = new LoadSuggestion(new SuccessListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String registerSuccess, String message) {
                    progressDialog.dismiss();
                    switch (registerSuccess) {
                        case "1":
                            imagePath = "";
                            bitmap_upload = null;
                            editText_title.setText("");
                            editText_desc.setText("");
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_song));
                            uploadDialog(message);
                            break;
                        case "-1":
                            methods.getVerifyDialog(getString(R.string.error_unauth_access), message);
                            break;
                        default:
                            Toast.makeText(SuggestionActivity.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, methods.getAPIRequest(Constant.METHOD_SUGGESTION, 0, "", "", "", "", "", "", "", "", "", "", "", editText_title.getText().toString(),"", Constant.itemUser.getId(),editText_desc.getText().toString(), new File(imagePath)));
            loadSuggestion.execute();
        } else {
            Toast.makeText(this, getString(R.string.err_internet_not_conn), Toast.LENGTH_SHORT).show();
        }
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image)), PICK_IMAGE_REQUEST);
    }

    private void uploadDialog(String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SuggestionActivity.this);
        alertDialog.setTitle(getString(R.string.upload_success));
        alertDialog.setMessage(message);
        alertDialog.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            imagePath = methods.getPathImage(uri);

            try {
                bitmap_upload = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(bitmap_upload);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean checkPer() {
        if ((ContextCompat.checkSelfPermission(SuggestionActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return false;
            }
            return true;
        } else {
            return true;
        }
    }
}