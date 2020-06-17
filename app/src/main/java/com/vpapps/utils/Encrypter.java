package com.vpapps.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;
import com.vpapps.item.ItemSong;
import com.vpapps.cocomusics.BuildConfig;
import com.vpapps.cocomusics.R;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import okio.BufferedSource;

public class Encrypter {
    private DBHelper dbHelper;
    private Context context;
    public Crypto _crypto;
    private Entity _entity;
    private boolean isInited = false;
    private static Encrypter instance = null;

    private Encrypter() {
        isInited = false;
    }

    public static Encrypter GetInstance() {
        if (instance == null) {
            instance = new Encrypter();
        }
        return instance;
    }

    public void Init(Context context, String saltString) {
        this.context = context;
        _crypto = new Crypto(new SharedPrefsBackedKeyChain(context),
                new SystemNativeCryptoLibrary());
        _entity = new Entity(saltString);
        isInited = true;
        dbHelper = new DBHelper(context);
    }

    public String GetEditedFileName(File file, String token) {
        String path = file.getAbsolutePath();
        String extension;
        String fname;
        int i = path.lastIndexOf('.');
        if (i > 0) {
            extension = path.substring(i);
            fname = path.substring(0, i) + token;
        } else {
            fname = path + token;
        }
        return fname;
    }


    public InputStream GetDecryptedBlockData(String filePath) throws IOException, KeyChainException, CryptoInitializationException {
        long a = System.currentTimeMillis();
        FileInputStream fileInputStream = new FileInputStream(filePath);
        InputStream inputStream = _crypto.getCipherInputStream(
                fileInputStream,
                _entity);

        return inputStream;
    }

    public void encrypt(String fileName, BufferedSource bufferedSource, final ItemSong itemSong) {
        try {
            final long a = System.currentTimeMillis();

            File file_encypt = new File(GetEditedFileName(new File(fileName.concat(".mp3")), ""));
            final String fileSavedName = file_encypt.getName().replace(".mp3", "");
            itemSong.setTempName(fileSavedName);

            if (!_crypto.isAvailable()) {
                return;
            }

            OutputStream fileStream = new BufferedOutputStream(new FileOutputStream(file_encypt));
            OutputStream outputStream = _crypto.getCipherOutputStream(
                    fileStream, new Entity(BuildConfig.DOWNLOAD_ENC_KEY));


            InputStream fis = bufferedSource.inputStream();
            int len;
            byte[] buffer = new byte[2048];
            while ((len = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            fis.close();
            outputStream.close();
            bufferedSource.close();

            new AsyncTask<String, String, String>() {
                String imageName;

                @Override
                protected String doInBackground(String... strings) {
                    imageName = getBitmapFromURL(itemSong.getImageBig(), fileSavedName);
                    if (!imageName.equals("0")) {
                        return "1";
                    } else {
                        return "0";
                    }
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    if (s.equals("1")) {
//                        itemSong.setImageBitmap(bitmap);
                        itemSong.setImageBig(imageName);
                        itemSong.setImageSmall(imageName);
                        itemSong.setTempName(fileSavedName);
                    } else {
                        imageName = "null";
                        itemSong.setImageBig(imageName);
                        itemSong.setImageSmall(imageName);
                        itemSong.setTempName(fileSavedName);
                    }
                    dbHelper.addToDownloads(itemSong);
                }
            }.execute();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        file.delete();
    }

    public String getBitmapFromURL(String src, String name) {
        try {
            if (src != null && src.equals("")) {
                src = "null";
            }
            URL url = new URL(src);
            InputStream input;
            if(BuildConfig.SERVER_URL.contains("https://")) {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            } else {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            }
            Bitmap myBitmap = BitmapFactory.decodeStream(input);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            myBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);

            File f_root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + context.getString(R.string.app_name) + File.separator + "/tempim/");
            if (!f_root.exists()) {
                f_root.mkdirs();
            }

            File f = new File(f_root, name + ".jpg");

            f.createNewFile();
//write the bytes in file
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());

// remember close de FileOutput
            fo.close();

            return f.getAbsolutePath();
        } catch (IOException e) {
            // Log exception
            return "0";
        }
    }
}
