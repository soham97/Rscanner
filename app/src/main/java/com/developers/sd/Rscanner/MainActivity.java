package com.developers.sd.Rscanner;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;


public class MainActivity extends AppCompatActivity {

    private Button mCameraPhoto;
    private Button mGalleryPhoto;
    private Bitmap mBitmap;
    private File mPhotoFile;
    private Mat imgMAT;

    private static final int SELECT_PHOTO = 0;
    private static final int REQUEST_PHOTO = 1;
    final int PIC_CROP = 2;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 63;
    private static final String TAG = "MainActivity";


    @Override
    public void onResume() {
        super.onResume();
        checkAndRequestPermissions();
    }

    static {
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPhotoFile = getPhotoFile();
        final Context context = this;

        mCameraPhoto = (Button) findViewById(R.id.camera_button);
//        mGalleryPhoto = (Button) findViewById(R.id.gallery_button);

        mCameraPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri uri = FileProvider.getUriForFile(context, "com.developers.sd.Rscanner.provider", mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

//        mGalleryPhoto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PHOTO);
//            }
//        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_PHOTO) {
            performCrop(FileProvider.getUriForFile(this, "com.developers.sd.Rscanner.provider", mPhotoFile));
        }
//        if (requestCode == SELECT_PHOTO) {
//            Uri uri = data.getData();
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
//                Bitmap scaledBitmap = BitmapUtility.scaleDown(bitmap, 1000, true);
////                mBitmap = RotateBitmap(BitmapUtility.getImage(BitmapUtility.getBytes(scaledBitmap)),90);
//                mBitmap = BitmapUtility.getImage(BitmapUtility.getBytes(scaledBitmap));
//
//                FileOutputStream gallery_fos = new FileOutputStream(mPhotoFile);
//                mBitmap.compress(Bitmap.CompressFormat.JPEG,100,gallery_fos);
//                gallery_fos.flush();
//                gallery_fos.close();
//                performCrop(uri);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

//    public File getPhotoFile() {
//        File externalFilesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        if (externalFilesDir == null) {
//            return null;
//        }
//        return new File(externalFilesDir, getPhotoFilename());
//    }

    public File getPhotoFile() {
        File externalFilesDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (externalFilesDir == null) {
            return null;
        }
        return new File(externalFilesDir, getPhotoFilename());
    }


    public String getPhotoFilename() {
        return "IMG_" + UUID.randomUUID().toString() + ".jpg";
    }

    private boolean checkAndRequestPermissions() {
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int storage1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);


        List<String> listPermissionsNeeded = new ArrayList<>();

        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void performCrop(Uri uri){
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            cropIntent.setDataAndType(uri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, PIC_CROP);
        }
        catch(ActivityNotFoundException anfe){
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private Mat bitmap_to_MAT(Bitmap bitmap){
        Bitmap bmp = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        Utils.bitmapToMat(bmp, imgMAT);
        return imgMAT;
    }

}
