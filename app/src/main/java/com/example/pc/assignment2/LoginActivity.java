package com.example.pc.assignment2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private final String WEB_SERVICE = "https://baf7ef0f.ngrok.io";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        // Prompt user for permissions if Marshmallow+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LoginActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_FINE_LOCATION);
            }
        }


        // Login
        final Button login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText userText = (EditText) findViewById(R.id.username);
                EditText passText = (EditText) findViewById(R.id.password);

                String username = userText.getText().toString();
                String password = passText.getText().toString();

                if (!username.isEmpty() && !password.isEmpty()) {
                    new Login(username, password).execute();
                }
            }
        });

        // Register
        final Button register = findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText userText = (EditText) findViewById(R.id.username);
                EditText passText = (EditText) findViewById(R.id.password);

                String username = userText.getText().toString();
                String password = passText.getText().toString();

                if (!username.isEmpty() && !password.isEmpty()) {
                    new Register(username, password).execute();
                }
            }
        });

    }

    /**
     *  sources:
     *  https://stackoverflow.com/questions/25647881/android-asynctask-example-and-explanation/25647882#25647882
     *  https://stackoverflow.com/questions/23216038/calling-web-service-using-async-task-in-android
     *  https://www.journaldev.com/13629/okhttp-android-example-tutorial
     */
    private class Login extends AsyncTask<Void, Void, Boolean>
    {
        String url = WEB_SERVICE + "/usermgmt/login/";
        String username;
        String password;

        Login(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here

            String query = "username=" + username + "&password=" + password;
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, query);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.body() == null) return false;
                String strResponse = response.body().string();
                final int code = response.code();

                Log.d("webtag", strResponse);

                if (code != 200) {
                    return false;
                }

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result == true) {
                Toast.makeText(getApplicationContext(), "Logged in.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("password", password);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Invalid username/password.", Toast.LENGTH_SHORT).show();
            }

        }
    }


    private class Register extends AsyncTask<Void, Void, Boolean>
    {
        String url = WEB_SERVICE + "/usermgmt/user/";
        String username;
        String password;

        Register(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here

            String query = "username=" + username + "&password=" + password;
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, query);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.body() == null) return false;
                String strResponse = response.body().string();
                final int code = response.code();

                Log.d("webtag", strResponse);

                if (code != 200) {
                    return false;
                }

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result == true) {
                Toast.makeText(getApplicationContext(), "Registered.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("password", password);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Username already taken.", Toast.LENGTH_SHORT).show();
            }

        }
    }


}
