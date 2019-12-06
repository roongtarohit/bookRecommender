package com.example.android.books;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BarcodeActivity extends AppCompatActivity {

    TextView txtView;
    Button btn;
    Button process;
    ImageView myImageView;
    Bitmap myBitmap;
    String imagePath;

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_IMAGE = 100;

    @BindView(R.id.imgview)
    ImageView bookImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        btn = (Button) findViewById(R.id.button);
        process = (Button) findViewById(R.id.process);
        txtView = (TextView) findViewById(R.id.txtContent);
        myImageView  = (ImageView) findViewById(R.id.imgview);

        ButterKnife.bind(this);

        //STATIC IMAGE
        /*myBitmap = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                R.drawable.isbn);
        myImageView.setImageBitmap(myBitmap);*/


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBarcodeScanRequest();
            }
        });

        process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(BarcodeActivity.this,"Process the selected Image", Toast.LENGTH_LONG).show();

                BarcodeDetector detector =
                        new BarcodeDetector.Builder(getApplicationContext())
                                .build();
                if(!detector.isOperational()){
                    txtView.setText("Could not set up the detector!");
                    return;
                }

                myBitmap = ((BitmapDrawable)myImageView.getDrawable()).getBitmap();

                //Toast.makeText(BarcodeActivity.this, myBitmap.toString(), Toast.LENGTH_LONG).show();

                Frame frame =  new Frame.Builder().setBitmap(myBitmap).build();
                SparseArray<Barcode> barcodes = detector.detect(frame);

                //To check whether it is a valid book
                //barcodes will be empty - thats my assumption

                if( barcodes.size() == 0) {
                    //Toast.makeText(BarcodeActivity.this, "Invalid Barcode", Toast.LENGTH_LONG).show();
                }
                else{
                    Barcode thisCode = barcodes.valueAt(0);
                    txtView.setText(thisCode.rawValue);

                    Intent results = new Intent(BarcodeActivity.this, QueryResultsActivity.class);
                    results.putExtra("topic", thisCode.rawValue);
                    results.putExtra("isbn", "isbn=");
                    //Toast.makeText(BarcodeActivity.this, thisCode.rawValue, Toast.LENGTH_LONG).show();
                    startActivity(results);
                }
            }
        });

    }

    //@OnClick({R.id.button})
    void onBarcodeScanRequest() {
        //Toast.makeText(BarcodeActivity.this, "FINALLY BC!", Toast.LENGTH_LONG).show();
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            showImagePickerOptions();
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showImagePickerOptions() {
        ImagePickerActivity.showImagePickerOptions(this, new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent();
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }
        });
    }

    private void launchCameraIntent() {
        //Toast.makeText(BarcodeActivity.this, "Camera is selected", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(BarcodeActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        //Toast.makeText(BarcodeActivity.this, "Camera is selected 2", Toast.LENGTH_LONG).show();
        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        //Toast.makeText(BarcodeActivity.this, "Camera is selected 3", Toast.LENGTH_LONG).show();
        startActivityForResult(intent, REQUEST_IMAGE);
        //Toast.makeText(BarcodeActivity.this, "Camera is selected 4", Toast.LENGTH_LONG).show();
    }

    private void launchGalleryIntent() {
        //Toast.makeText(BarcodeActivity.this, "Gallery is selected 1", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(BarcodeActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        //Toast.makeText(BarcodeActivity.this, "Gallery is selected 2", Toast.LENGTH_LONG).show();
        startActivityForResult(intent, REQUEST_IMAGE);
        //Toast.makeText(BarcodeActivity.this, "Gallery is selected 3", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getParcelableExtra("path");
                try {
                    // You can update this bitmap to your server
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                    // loading profile image from local cache
                    loadImage(uri.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadImage(String url) {
        Log.d(TAG, "Image cache path: " + url);

        GlideApp.with(this).load(url)
                .into(bookImage);
        bookImage.setColorFilter(ContextCompat.getColor(this, android.R.color.transparent));

        imagePath = url;
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeActivity.this);
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();

    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }
}
