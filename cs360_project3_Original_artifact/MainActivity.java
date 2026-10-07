package com.example.cs360_projectthree;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    //establishing datatypes for UI elements
    EditText usernameText, passwordText;
    Button buttonLogin, buttonCreateAccount;
    TextView textStatus;
    LoginDBHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Linking the UI elements from the activity_main.xml
        usernameText = findViewById(R.id.usernameText);
        passwordText = findViewById(R.id.passwordText);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        textStatus = findViewById(R.id.textStatus);

        //Creating an instance of the LoginDBHelper
        DB = new LoginDBHelper(this);

        //Click listener for create account button
        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = usernameText.getText().toString();
                String pass = passwordText.getText().toString();

                if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)) {
                    textStatus.setText("Please enter a username and password.");
                } else {
                    boolean checkUser = DB.checkUsername(user);

                    if (!checkUser) {
                        boolean insert = DB.insertData(user, pass);
                        if (insert) {
                            textStatus.setText("Account created successfully.");
                            Toast.makeText(MainActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            textStatus.setText("Account creation failed.");
                        }
                    } else {
                        textStatus.setText("User already exists. Please log in.");
                    }
                }
            }
        });

        //Click listener for login button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = usernameText.getText().toString();
                String pass = passwordText.getText().toString();

                if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)) {
                    textStatus.setText("Please enter a username and password.");
                } else {
                    boolean checkUserPass = DB.checkUsernamePassword(user, pass);

                    if (checkUserPass) {
                        textStatus.setText("Login successful.");
                        Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                        //opens EventDataActivity
                        Intent intent = new Intent(MainActivity.this, EventDataActivity.class);
                        startActivity(intent);
                    } else {
                        textStatus.setText("Invalid username or password.");
                    }
                }
            }
        });
    }
}