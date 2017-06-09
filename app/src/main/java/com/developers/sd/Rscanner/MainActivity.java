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
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.util.EventLog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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


public class MainActivity extends AppCompatActivity {

    private Button mCameraPhoto;
    private ImageView mImageView;
    private TextView mTouchcolor;
    private File mPhotoFile;
    private Mat imgMAT;
    private Mat mRgba;
    private Scalar mBlobColorHsv;
    private Scalar mBlobColorRgba;
    private MenuItem mSave;
    private MenuItem mReset;
    private String mtempColor;

    private List<String> rings_colors = new ArrayList<>();
    private int[] color_values = new int[4];
    List<String> mList = new ArrayList<>();


    private static final int SELECT_PHOTO = 0;
    private static final int REQUEST_PHOTO = 1;
    final int PIC_CROP = 2;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 63;
    double x = -1;
    double y = -1;
    private static final String TAG = "MainActivity";
    private static final String imageview = "imageview";



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

        final Toolbar tToolbar = (Toolbar) findViewById(R.id.tToolbar);

        mCameraPhoto = (Button) findViewById(R.id.camera_button);
        mImageView = (ImageView) findViewById(R.id.cropped_image);
        mTouchcolor = (TextView)  findViewById(R.id.touchcolor);

        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);

        mCameraPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoFile = getPhotoFile();
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

                //originally the value was 8, but changed to 5 to prevent crashing of app but reduced screen
                //coverage
                touchedRect.width = 1;
                touchedRect.height = 1;

                Mat touchedRegionRgba = new Mat();
                touchedRegionRgba = mRgba.submat(touchedRect);

                Mat touchedRegionHsv = new Mat();
                Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

                mBlobColorHsv = Core.sumElems(touchedRegionHsv);
                int pointCount = touchedRect.width * touchedRect.height;
                for (int i = 0; i < mBlobColorHsv.val.length; i++)
                    mBlobColorHsv.val[i] /= pointCount;

                mBlobColorRgba = convertScalarHsv2Rgba(mBlobColorHsv);

                mTouchcolor.setTextColor(Color.rgb((int) mBlobColorRgba.val[0],
                        (int) mBlobColorRgba.val[1],
                        (int) mBlobColorRgba.val[2]));

                ColorUtils colorUtils = new ColorUtils();
                ColorUtils1 colorUtils1 = new ColorUtils1();

//                String s = colorUtils.getColorNameFromRgb((int) mBlobColorRgba.val[0],(int) mBlobColorRgba.val[1], (int) mBlobColorRgba.val[2]);
//                mtempColor = colorUtils.getColorNameFromRgb((int) mBlobColorRgba.val[0],(int) mBlobColorRgba.val[1], (int) mBlobColorRgba.val[2]);
                mtempColor = colorUtils1.getColorNameFromHSV((int) mBlobColorHsv.val[0],(int) mBlobColorHsv.val[1], (int) mBlobColorHsv.val[2]);


//                Log.e("COLORUTILS", s);
//                rings_colors.add(s);
//
//                if (rings_colors.size() == 4) {
//                    decodeColor(rings_colors);
//                }

                mTouchcolor.setText("COLOR_HEX: #" + String.format("%02X", (int)mBlobColorRgba.val[0])
                        + String.format("%02X", (int)mBlobColorRgba.val[1])
                        + String.format("%02X", (int)mBlobColorRgba.val[2]) + "\n" + "COLOR = " + mtempColor);

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
            mImageView.invalidate();
            mSave.setVisible(true);
            mReset.setVisible(true);
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

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu)
    {
        mSave = menu.findItem(R.id.Save);
        mReset = menu.findItem(R.id.Reset);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.Save:
                mList.add(mtempColor);
                if (mList.size() == 4) {
                    decodeColor(mList);
                }
                if (mList.size() == 5) {
                    Toast.makeText(this, "4 colors already recorded, hence reseting", Toast.LENGTH_SHORT).show();
                    mList.clear();
                    mTouchcolor.setText("");
                }
                return true;
            case R.id.Reset:
                mList.clear();
                mTouchcolor.setText("");
                return true;
            default:
                return true;
        }
    }

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

    private void decodeColor (List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            switch (list.get(i)) {
                case "Black":
                    color_values[i] = 0;
                    break;
                case "Brown":
                    color_values[i] = 1;
                    break;
                case "Red":
                    color_values[i] = 2;
                    break;
                case "Orange":
                    color_values[i] = 3;
                    break;
                case "Yellow":
                    color_values[i] = 4;
                    break;
                case "Green":
                    color_values[i] = 5;
                    break;
                case "Blue":
                    color_values[i] = 6;
                    break;
                case "Violet":
                    color_values[i] = 7;
                    break;
                case "Grey":
                    color_values[i] = 8;
                    break;
                case "White":
                    color_values[i] = 9;
                    break;
                default:
                    break;
            }
//                            rings_colors = new ArrayList<>();
        }

        String resistorValue = color_values[0] +" " +color_values[1] + " x 10^(" + color_values[2] + ")" +" " +"\u03a9";
        Log.e("RESISTOR_VALUE", resistorValue);
        mTouchcolor.setText(resistorValue);
        Toast.makeText(this, resistorValue, Toast.LENGTH_LONG).show();
    }

}