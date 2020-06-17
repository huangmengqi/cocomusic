package com.vpapps.utils;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.vpapps.cocomusics.BuildConfig;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import androidx.annotation.Nullable;

public class StreamDataSource implements DataSource {

    private String baseFileName;

    private long bytesRemaining;
    private long nextReadPos;
    private InputStream inputStream;

    public StreamDataSource(Context context,String fileName) {
        //this.path = path;
        baseFileName = fileName;

        nextReadPos = 0;
        Encrypter enc = Encrypter.GetInstance();
        if(enc._crypto == null) {
            enc.Init(context, BuildConfig.DOWNLOAD_ENC_KEY);
        }

        try {
            inputStream = enc.GetDecryptedBlockData(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        try {
            long totalFileSize;
            RandomAccessFile file2 = new RandomAccessFile(baseFileName, "r");
            totalFileSize = file2.length();
            nextReadPos = dataSpec.position;
            bytesRemaining = dataSpec.length == C.LENGTH_UNSET ? totalFileSize - dataSpec.position
                    : dataSpec.length;
            if (bytesRemaining < 0) {
                throw new EOFException();
            }
        } catch (IOException e) {
            throw new FileDataSource.FileDataSourceException(e);
        }

        return bytesRemaining;
    }

    @Override
    public void close() {
        //stream.close();
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        if (bytesRemaining == 0) {
            return -1;
        } else {
            int bytesRead = 0;
            try {
                bytesRead = inputStream.read(buffer, offset, (int) Math.min(bytesRemaining, readLength));
                nextReadPos += bytesRead;
            } catch (IOException e) {
                throw new FileDataSource.FileDataSourceException(e);
            }
            return bytesRead;
        }
    }

    @Nullable
    @Override
    public Uri getUri() {
        return null;
    }
}