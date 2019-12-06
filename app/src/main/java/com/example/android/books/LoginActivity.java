package com.example.android.books;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    CardView login;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (CardView) findViewById(R.id.cardView);
        sharedPreferences = getSharedPreferences("loginRef",MODE_PRIVATE);

    }

    public void loginCheck(View view){
        if(username.getText().toString() == null || username.getText().toString().length() == 0){
            Toast.makeText(LoginActivity.this,"Enter username", Toast.LENGTH_LONG).show();
        }
        else if(password.getText().toString() == null || password.getText().toString().length() == 0){
            Toast.makeText(LoginActivity.this,"Enter password", Toast.LENGTH_LONG).show();
        }
        else{
            if(username.getText().toString().equals("shaitaan") && password.getText().toString().equals("123")){
                Toast.makeText(LoginActivity.this,"Login successful", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
            else if(sharedPreferences.getString(username.getText().toString(),null) == null){
                Toast.makeText(LoginActivity.this,"Invalid credentials", Toast.LENGTH_LONG).show();
            }
            else if(sharedPreferences.getString(username.getText().toString(),null).equals(password.getText().toString())){
                Toast.makeText(LoginActivity.this,"Login successful", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(LoginActivity.this,"Invalid credentials", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void register(View view){
        Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(intent);
    }
}
