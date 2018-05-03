package com.example.pc.assignment2;

import android.content.Intent;
import android.os.AsyncTask;
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

public class RegisterActivity extends AppCompatActivity {
    private static String WEB_SERVICE = "http://c8f3bc35.ngrok.io";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Register
        final Button register = findViewById(R.id.new_register);
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText userText = (EditText) findViewById(R.id.new_username);
                EditText passText = (EditText) findViewById(R.id.new_password);

                String username = userText.getText().toString();
                String password = passText.getText().toString();

                if (!username.isEmpty() && !password.isEmpty()) {
                    new Register(username, password).execute();
                }
            }
        });
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

                if (code == 200) {
                    return true;
                }

                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
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
