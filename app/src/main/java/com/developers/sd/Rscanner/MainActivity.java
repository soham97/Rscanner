package com.developers.sd.Rscanner;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.EventLog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


public class MainActivity extends Activity {

    private Button mCameraPhoto;
    private ImageView mImageView;
    private TextView mTouchcolor;
    private File mPhotoFile;
    private Mat imgMAT;
    private Mat mRgba;
    private Scalar mBlobColorHsv;
    private Scalar mBlobColorRgba;


    private static final int SELECT_PHOTO = 0;
    private static final int REQUEST_PHOTO = 1;
    final int PIC_CROP = 2;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 63;
    double x = -1;
    double y = -1;
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
        mImageView = (ImageView) findViewById(R.id.cropped_image);
        mTouchcolor = (TextView)  findViewById(R.id.touchcolor);

        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);

        mCameraPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri uri = FileProvider.getUriForFile(context, "com.developers.sd.Rscanner.provider", mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });


        mImageView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                Uri uri = FileProvider.getUriForFile(context, "com.developers.sd.Rscanner.provider", mPhotoFile);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                mRgba = new Mat();
                mRgba = bitmap_to_MAT(bitmap);
                int cols = mRgba.cols();
                int rows = mRgba.rows();

                double yLow = (double)mImageView.getHeight() * 0.2401961;
                double yHigh = (double)mImageView.getHeight() * 0.7696078;

                double xScale = (double)cols / (double)mImageView.getWidth();
                double yScale = (double)rows / (yHigh - yLow);

                x = event.getX();
                y = event.getY();

                y = y - yLow;

                x = x * xScale;
                y = y * yScale;

                Log.e("MSG","x--- "+x);
                Log.e("MSG1","y--- "+y);

                if((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;


                Rect touchedRect = new Rect();

                touchedRect.x = (int)x;
                touchedRect.y = (int)y;

                touchedRect.width = 8;
                touchedRect.height = 8;

                Mat touchedRegionRgba = new Mat();
                touchedRegionRgba = mRgba.submat(touchedRect);

                Mat touchedRegionHsv = new Mat();
                Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

                mBlobColorHsv = Core.sumElems(touchedRegionHsv);
                int pointCount = touchedRect.width * touchedRect.height;
                for (int i = 0; i < mBlobColorHsv.val.length; i++)
                    mBlobColorHsv.val[i] /= pointCount;

                mBlobColorRgba = convertScalarHsv2Rgba(mBlobColorHsv);

                mTouchcolor.setText("Color: #" + String.format("%02X", (int)mBlobColorRgba.val[0])
                        + String.format("%02X", (int)mBlobColorRgba.val[1])
                        + String.format("%02X", (int)mBlobColorRgba.val[2]));

                mTouchcolor.setTextColor(Color.rgb((int) mBlobColorRgba.val[0],
                        (int) mBlobColorRgba.val[1],
                        (int) mBlobColorRgba.val[2]));

                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_PHOTO) {
            performCrop(FileProvider.getUriForFile(this, "com.developers.sd.Rscanner.provider", mPhotoFile));
        }
        if(requestCode == PIC_CROP){
            mImageView.setImageURI(FileProvider.getUriForFile(this, "com.developers.sd.Rscanner.provider", mPhotoFile));
            mImageView.setVisibility(View.VISIBLE);
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
        imgMAT = new Mat();
        Utils.bitmapToMat(bmp, imgMAT);
        return imgMAT;
    }

    private Bitmap JPGtoRGB888(Bitmap img){
        Bitmap result = null;

        int numPixels = img.getWidth() * img.getHeight();
        int[] pixels = new int[numPixels];
        img.getPixels(pixels,0,img.getWidth(),0,0,img.getWidth(),img.getHeight());
        result = Bitmap.createBitmap(img.getWidth(),img.getHeight(), Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
        return result;
    }

    private Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
    }
