package com.example.android.books;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.nio.file.Files.*;

public class UploadImageActivity extends AppCompatActivity {


    TextView txtView;
    Button btn;
    Button process;
    ImageView myImageView;
    Bitmap myBitmap;
    String imagePath;
    String ans = " ";

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_IMAGE = 100;

    @BindView(R.id.imgview1)
    ImageView bookImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        btn = (Button) findViewById(R.id.button1);
        process = (Button) findViewById(R.id.process1);
        txtView = (TextView) findViewById(R.id.txtContent1);
        myImageView  = (ImageView) findViewById(R.id.imgview1);

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
                if(txtView.getText().toString().equals("NOT FOUND")){
                    Toast.makeText(UploadImageActivity.this,"Please click again",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(UploadImageActivity.this,"Book"+txtView.getText().toString() ,Toast.LENGTH_LONG).show();
                    Intent results = new Intent(UploadImageActivity.this, QueryResultsActivity.class);
                    results.putExtra("topic", txtView.getText().toString());
                    results.putExtra("author", "inauthor=");
                    results.putExtra("title", "intitle=");
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
                            launchCameraIntent();
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

    private void launchCameraIntent() {
        Intent intent = new Intent(UploadImageActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);


        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);


        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);


        startActivityForResult(intent, REQUEST_IMAGE);

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
        UploadTask tc = new UploadTask();
        tc.execute();
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadImageActivity.this);
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

    public class UploadTask extends AsyncTask<String, String, String> {


        /*private HttpURLConnection httpConn;
        private DataOutputStream request;
        private final String boundary =  "*****";
        private final String crlf = "\r\n";
        private final String twoHyphens = "--";
        */

        @Override
        protected String doInBackground(String... strings) {



            ///storage/emulated/0/Android/data/com.example.android.books/files/DCIM/
            File imageDir = getExternalFilesDir(Environment.DIRECTORY_DCIM);

            //imagePath - file:/data/user/0/com.example.android.books/cache/1575503198173.jpg

            // WHEN CLICKED FROM CAMERA
            String fileName = imagePath.substring(imagePath.lastIndexOf("/")+1);
            imagePath = "/storage/emulated/0/Android/data/com.example.android.books/cache/camera/" + fileName;
            Log.i("CheckPoint", imagePath);
            Log.i("CheckPoint", fileName);
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024;
            try {
                FileInputStream input = new FileInputStream(new File(imagePath));

                try {
                    URL serverUrl =
                            new URL("http://sunilsamal.pythonanywhere.com:80/extract");
                    HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();

                    String boundaryString = "----SomeRandomText";
                    //String fileUrl = "/Users/sunilsamal/Documents/Fall2019/MobileComputing/MC_final_project/hp2.jpg";
                    String fileUrl = imagePath;
                    File logFileToUpload = new File(fileUrl);

                    // Indicate that we want to write to the HTTP request body
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

                    Log.i("CheckPoint", "1");
                    OutputStream outputStreamToRequestBody = urlConnection.getOutputStream();
                    BufferedWriter httpRequestBodyWriter =
                            new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));


                    Log.i("CheckPoint", "2");
                    // Include the section to describe the file
                    httpRequestBodyWriter.write("\n--" + boundaryString + "\n");
                    httpRequestBodyWriter.write("Content-Disposition: form-data;"
                            + "name=\"file\";"
                            + "filename=\""+ logFileToUpload.getName() +"\""
                            + "\nContent-Type: text/plain\n\n");
                    httpRequestBodyWriter.flush();

                    // Write the actual file contents
                    FileInputStream inputStreamToLogFile = new FileInputStream(logFileToUpload);

                    byte[] dataBuffer = new byte[1024];
                    while((bytesRead = inputStreamToLogFile.read(dataBuffer)) != -1) {
                        outputStreamToRequestBody.write(dataBuffer, 0, bytesRead);
                    }

                    Log.i("CheckPoint", "3");
                    outputStreamToRequestBody.flush();

                    // Mark the end of the multipart http request
                    httpRequestBodyWriter.write("\n--" + boundaryString + "--\n");
                    httpRequestBodyWriter.flush();

                    // Close the streams
                    outputStreamToRequestBody.close();
                    httpRequestBodyWriter.close();

                    Log.i("CheckPoint", "4");

                    Log.i("Checkpoint ---",String.valueOf(urlConnection.getResponseCode()));
                    Log.i("Checkpoint ---",String.valueOf(urlConnection.getResponseMessage()));

                    BufferedReader httpResponseReader =
                            new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));

                    String lineRead;
                    while((lineRead = httpResponseReader.readLine()) != null) {
                        ans = ans + lineRead;
                        Log.i("Checkpoint ---","lineRead == "+lineRead+" ==");
                    }

                    //ans = ans + " ";
                    Log.i("Checkpoint ---","Ans == "+ ans +" ==");

                    Log.i("CheckPoint", "5");
                    Log.i("CHECKPOINT : ", "Filename : " + fileName);
                    Log.i("CHECKPOINT : ", "Image path : " + imagePath);

                    Log.i("CheckPoint", "6");
                    // Close the streams
                    outputStreamToRequestBody.close();
                    httpRequestBodyWriter.close();

                    Log.i("CheckPoint", "7");

                    /*URL url = new URL("http://sunilsamal.pythonanywhere.com/extract");
                    Map<String,Object> params = new LinkedHashMap<>();
                    params.put("msg", fileName);

                    StringBuilder postData = new StringBuilder();
                    for (Map.Entry<String,Object> param : params.entrySet()) {
                        if (postData.length() != 0) postData.append('&');
                        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                        postData.append('=');
                        postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                    }
                    byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(postDataBytes);

                    Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                    ans = new StringBuilder();
                    for (int c; (c = in.read()) >= 0;)
                        ans.append((char)c);

                    Log.i("CHECKPOINT : ", "Filename : " + fileName);
                    Log.i("CHECKPOINT : ", "Image path : " + imagePath);
                    Log.i("CHECKPOINT : ", "Response : " + ans.toString());

                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();
                    Log.i("CHECKPOINT : ", "HTTP Response is : " + serverResponseMessage + " : " + serverResponseCode);



                    String delimiter = "\r\n";
                    String hyphens = "--";
                    String split = "*****";
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    URL url = new URL("http://sunilsamal.pythonanywhere.com/extract");
                    //urlConnection.setDoInput(true); // Allow Inputs
                    //urlConnection.setDoOutput(true); // Allow Outputs
                    //urlConnection.setUseCaches(false); // Don't use a Cached Copy
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Connection", "Keep-Alive");
                    urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    urlConnection.setRequestProperty("Content-Type", "multipart/form-data;split=" + split);
                    //urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("msg", fileName);

                    Log.i("CHECKPOINT : ", "Filename : " + fileName);
                    Log.i("CHECKPOINT : ", "Image path : " + imagePath);
                    urlConnection.connect();

                    DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
                    //dos.writeBytes(fileName);


                    dos.writeBytes(hyphens + split + delimiter);
                    dos.writeBytes("Content-Disposition: form-data; name=\"msg\"" + delimiter);
                    dos.writeBytes(delimiter);
                    dos.writeBytes(fileName);
                    dos.writeBytes(delimiter);
                    dos.writeBytes(hyphens + split + delimiter);


                    ADDING IMAGE IN HEADER
                    dos.writeBytes(hyphens + split + delimiter);
                    dos.writeBytes("Content-Disposition: form-data; name=\"msg\";filename=" + fileName +  delimiter);
                    dos.writeBytes(delimiter);

                    /*bytesAvailable = input.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    bytesRead = input.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = input.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = input.read(buffer, 0, bufferSize);
                    }

                    int serverResponseCode = urlConnection.getResponseCode();


                    Log.d("serverResponse", String.valueOf(serverResponseCode));
                    //Toast.makeText(getApplicationContext(), serverResponseCode, Toast.LENGTH_LONG).show();
                    String serverResponseMessage = urlConnection.getResponseMessage();

                    Log.i("CHECKPOINT : ", "HTTP Response is : " + serverResponseMessage + " : " + serverResponseCode);


                    if (serverResponseCode == 200) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("CHECKPOINT : ","File upload complete");
                            }
                        });
                    }
                    else
                        Log.i("CHECKPOINT : ","File upload failure : "+serverResponseCode);

                    input.close();
                    dos.flush();
                    dos.close();
                    */

                }
                catch (Exception exception)
                {
                    Log.d("Exception 1: ", String.valueOf(exception));
                    publishProgress(String.valueOf(exception));
                }
            }
            catch (Exception exception)
            {
                Log.d("Exception 2: ", String.valueOf(exception));
                publishProgress(String.valueOf(exception));
            }
            return "null";
        }

        @Override
        protected void onProgressUpdate(String... text){
            //Toast.makeText(getApplicationContext(), "In Background Task" + text[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String text){
            Toast.makeText(getApplicationContext(), "Done --- 1", Toast.LENGTH_SHORT).show();
            txtView.setText(ans);
            Toast.makeText(getApplicationContext(), "Done --- 2", Toast.LENGTH_SHORT).show();
        }
    }
}
