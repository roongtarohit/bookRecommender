package com.example.android.books;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {


    EditText username;
    EditText password;
    EditText confirmPassword;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = (EditText) findViewById(R.id.username1);
        password = (EditText) findViewById(R.id.password1);
        confirmPassword = (EditText) findViewById(R.id.confirmPassword);

        sharedPreferences = getSharedPreferences("loginRef",MODE_PRIVATE);
        editor = sharedPreferences.edit();

    }

    public void doneRegister(View view){

        if(username.length() == 0 || password.length() == 0 || confirmPassword.length() == 0){
            Toast.makeText(this,"Enter all the fields", Toast.LENGTH_SHORT).show();
        }
        else if(!password.getText().toString().equals(confirmPassword.getText().toString())){
            Toast.makeText(this,"password doesn't match", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this,sharedPreferences.getString(username.getText().toString(), "Not found"),Toast.LENGTH_SHORT);
            if(sharedPreferences.getString(username.getText().toString(),null) == null) {
                editor.putString(username.getText().toString(),password.getText().toString());
                Toast.makeText(this,sharedPreferences.getString(username.getText().toString(), "Not found"),Toast.LENGTH_SHORT);
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(this,"Username already exists", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
