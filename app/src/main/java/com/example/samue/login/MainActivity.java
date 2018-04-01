package com.example.samue.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.iid.InstanceID;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import util.Constants;

public class MainActivity extends AppCompatActivity {

    private String usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String iid = InstanceID.getInstance(this).getId();

        File af = new File("/data/data/com.example.samue.login/files/nombre.txt");
        try {
            if (af.isFile()) {
                BufferedReader aNombre = new BufferedReader(new InputStreamReader(openFileInput("nombre.txt")));
                usuario = aNombre.readLine();
                aNombre.close();

                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Welcome Back " + usuario);
                progressDialog.show();


                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                // On complete call either onLoginSuccess or onLoginFailed

                                Intent intent = new Intent(MainActivity.this, Profile.class);
                                intent.putExtra("user", usuario);
                                startActivity(intent);
                                finish();
                                progressDialog.dismiss();
                            }
                        }, 2000);
            } else {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                // On complete call either onLoginSuccess or onLoginFailed
                                Intent intent = new Intent(MainActivity.this, CreateName.class);
                                startActivity(intent);
                                finish();

                            }
                        }, 2000);

            }

        } catch (Exception e) {
            Log.e("Error Nombre", e.getMessage());
        }

    }

}
