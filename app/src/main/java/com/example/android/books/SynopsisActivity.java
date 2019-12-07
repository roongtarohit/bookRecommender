package com.example.android.books;

import android.app.LoaderManager;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class SynopsisActivity extends AppCompatActivity {


    private static final String details = "KITAAB_TITLE";
    String synopsisJSON;
    private String GOOGLE_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    private static final String API_KEY = "  "; //USE YOUR  KEY

    TextView kitaabTitle;
    TextView kitaabInfoLink;
    TextView kitaabDescription;
    TextView kitaabASULink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synopsis);

        kitaabTitle = (TextView) findViewById(R.id.kitaabTitle);
        kitaabInfoLink = (TextView) findViewById(R.id.kitaabInfoLink);
        kitaabDescription = (TextView) findViewById(R.id.kitaabDescription);
        kitaabASULink = (TextView) findViewById(R.id.kitaabASUlink);

        Intent intent = getIntent();
        String title = intent.getStringExtra(details);

        if (title.length() == 0 || title == null){
            kitaabTitle.setText("NOT FOUND");
        }
        else{
            kitaabTitle.setText(title);
            kitaabASULink.setText("https://lib.asu.edu/");
            //https://www.googleapis.com/books/v1/volumes?q=harry+meghan&key=AIzaSyBRaCyj7M50tKb_T6TLrNDCIdORubwjr44
            GOOGLE_URL = GOOGLE_URL + title.replaceAll(" ","") + "&key=" + API_KEY;

            Log.d("KITAAB TITLE", title.replaceAll(" ",""));

            UploadTask tc = new UploadTask();
            tc.execute();
        }

    }


    public class UploadTask extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... strings) {

            try {

                try {

                    URL url = new URL(GOOGLE_URL);
                    Map<String,Object> params = new LinkedHashMap<>();
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setRequestMethod("GET");

                    Log.d("DATA LINES", GOOGLE_URL);
                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();
                    Log.i("CHECKPOINT : ", "HTTP Response is : " + serverResponseMessage + " : " + serverResponseCode);


                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder json = new StringBuilder();

                    String line;
                    while ((line = in.readLine()) != null) {
                        json.append(line);
                        //Log.d("DATA LINES", line);
                    }


                    synopsisJSON = json.toString();

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
            String description = " ", infoLink= " ";
            try {
                JSONObject obj = new JSONObject(synopsisJSON);
                JSONArray arr = obj.getJSONArray("items");
                description = arr.getJSONObject(0).getJSONObject("volumeInfo").getString("description");
                infoLink = arr.getJSONObject(0).getJSONObject("volumeInfo").getString("infoLink");
                Log.d("JSON",infoLink);
                Log.d("JSON",description);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            kitaabInfoLink.setText(infoLink);
            kitaabDescription.setText(description);
        }
    }
}