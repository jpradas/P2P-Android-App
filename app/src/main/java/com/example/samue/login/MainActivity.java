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

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import util.Constants;

public class MainActivity extends AppCompatActivity {
LinearLayout l1, l2;
Button loginbutton;
Animation uptodown, downtoup;
private EditText user, password;
private TextView signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginbutton = (Button) findViewById(R.id.loginbutton);
        signup = (TextView) findViewById(R.id.signup);
        user = (EditText) findViewById(R.id.user);
        password = (EditText) findViewById(R.id.pass);
        l1 = (LinearLayout) findViewById(R.id.l1);
        l2 = (LinearLayout) findViewById(R.id.l2);
        uptodown = AnimationUtils.loadAnimation(this, R.anim.uptodown);
        l1.setAnimation(uptodown);
        downtoup = AnimationUtils.loadAnimation(this, R.anim.downtoup);
        l2.setAnimation(downtoup);


        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Signup.class);
                startActivity(intent);
                overridePendingTransition(R.anim.zoom_back_in, R.anim.zoom_back_out);
                finish();
            }
        });
    }

    public void login(){ //TODO hacer que solo exista un usuario por telefono, no nos interesa que haya más

        if(!validate()){
            onLoginFailed();
            return;
        }

        loginbutton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String userName = user.getText().toString();
        String pw = password.getText().toString();

        File af = new File("/data/data/com.example.samue.login/files/credenciales_"+ userName +".txt");
        try{
            if(af.isFile()){
                BufferedReader fin = new BufferedReader(new InputStreamReader(openFileInput("credenciales_" + userName + ".txt")));
                String usuario = fin.readLine();
                String ct = fin.readLine();

                if(userName.equals(usuario) && pw.equals(ct)){
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    // On complete call either onLoginSuccess or onLoginFailed
                                    onLoginSuccess();
                                    // onLoginFailed();
                                    progressDialog.dismiss();
                                }
                            }, 2000);
                }else{
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    // On complete call either onLoginSuccess or onLoginFailed
                                    onLoginFailed();
                                    // onLoginFailed();
                                    progressDialog.dismiss();
                                }
                            }, 2000);
                }
            }else{
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                // On complete call either onLoginSuccess or onLoginFailed
                                onLoginFailed();
                                user.setError("User doesn't exist");
                                // onLoginFailed();
                                progressDialog.dismiss();
                            }
                        }, 2000);
            }
        }catch(Exception ex){

        }
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        loginbutton.setEnabled(true);
    }

    public void onLoginSuccess() {
        loginbutton.setEnabled(true);
        Intent intent = new Intent(MainActivity.this, Profile.class);
        intent.putExtra("user", user.getText().toString());
        startActivity(intent);
    }

    private boolean validate(){
        boolean valid = true;

        String userName = user.getText().toString();
        String pw = password.getText().toString();

        if(userName.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(userName).matches()){
            user.setError("enter a valid username");
            valid = false;
        }else{
            user.setError(null);
        }

        if(pw.isEmpty()){
            password.setError("password cannot be empty");
            valid = false;
        }else{
            password.setError(null);
        }

        return valid;
    }
}
