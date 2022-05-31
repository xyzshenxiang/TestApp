package com.xs.testapp.ui.camera;

import android.media.Image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageSaveRunnable implements Runnable {
    private final Image mImage;

    private final File mFile;

    public ImageSaveRunnable(Image mImage, File mFile) {
        this.mImage = mImage;
        this.mFile = mFile;
    }

    @Override
    public void run() {
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mFile);
            fos.write(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mImage.close();
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
