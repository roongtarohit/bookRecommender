package com.example.android.books;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BarcodeActivity extends AppCompatActivity {

    TextView txtView;
    Button btn;
    Button process;
    ImageView myImageView;
    Bitmap myBitmap;
    String imagePath;

    private static final String KEY = MainActivity.class.getSimpleName();
    public static final int imageRequestCode = 100;

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

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBarcodeScanRequest();
            }
        });

        process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BarcodeDetector detector =
                        new BarcodeDetector.Builder(getApplicationContext())
                                .build();
                if(!detector.isOperational()){
                    txtView.setText("Could not set up the detector!");
                    return;
                }

                myBitmap = ((BitmapDrawable)myImageView.getDrawable()).getBitmap();

                Frame frame =  new Frame.Builder().setBitmap(myBitmap).build();
                SparseArray<Barcode> barcodes = detector.detect(frame);

                if( barcodes.size() == 0) {
                    Toast.makeText(BarcodeActivity.this, "Invalid Barcode", Toast.LENGTH_LONG).show();
                }
                else{
                    Barcode thisCode = barcodes.valueAt(0);
                    txtView.setText(thisCode.rawValue);

                    Intent results = new Intent(BarcodeActivity.this, ResultsActivity.class);
                    results.putExtra("topic", thisCode.rawValue);
                    results.putExtra("isbn", "isbn=");
                    startActivity(results);
                }
            }
        });

    }

    //@OnClick({R.id.button})
    void onBarcodeScanRequest() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            displayOptions();
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

    private void displayOptions() {
        ImagePickerActivity.showImagePickerOptions(this, new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                camera();
            }

            @Override
            public void onChooseGallerySelected() {
                gallery();
            }
        });
    }

    private void camera() {
        Intent intent = new Intent(BarcodeActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.imagePickerOption, ImagePickerActivity.cameraOption);
        intent.putExtra(ImagePickerActivity.lock, true);
        intent.putExtra(ImagePickerActivity.widthX, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.heightY, 1);
        intent.putExtra(ImagePickerActivity.maxDimensionsBitmap, true);
        intent.putExtra(ImagePickerActivity.maxWidthBitmap, 1000);
        intent.putExtra(ImagePickerActivity.maxHeightBitmap, 1000);
        startActivityForResult(intent, imageRequestCode);
    }

    private void gallery() {

        Intent intent = new Intent(BarcodeActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.imagePickerOption, ImagePickerActivity.galleryOption);
        intent.putExtra(ImagePickerActivity.lock, true);
        intent.putExtra(ImagePickerActivity.widthX, 1);
        intent.putExtra(ImagePickerActivity.heightY, 1);
        startActivityForResult(intent, imageRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == imageRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getParcelableExtra("path");
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    loadImage(uri.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadImage(String url) {
        Log.d(KEY, "Image cache path: " + url);

        GlideApp.with(this).load(url)
                .into(bookImage);
        bookImage.setColorFilter(ContextCompat.getColor(this, android.R.color.transparent));

        imagePath = url;

    }

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

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }
}
