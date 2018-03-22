package com.example.samue.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.OutputStreamWriter;

public class Signup extends AppCompatActivity {
    LinearLayout l1, l2;
    Animation uptodown, downtoup;
    Button cabutton;
    EditText user, pass, confirmpass;
    TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        l1 = (LinearLayout) findViewById(R.id.l1);
        l2 = (LinearLayout) findViewById(R.id.l2);
        cabutton = (Button) findViewById(R.id.cabutton);
        login = (TextView) findViewById(R.id.login);
        uptodown = AnimationUtils.loadAnimation(this, R.anim.uptodown);
        l1.setAnimation(uptodown);
        downtoup = AnimationUtils.loadAnimation(this, R.anim.downtoup);
        l2.setAnimation(downtoup);
        user = (EditText) findViewById(R.id.user);
        pass = (EditText) findViewById(R.id.pass);
        confirmpass = (EditText) findViewById(R.id.confirmpass);

        cabutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(createUser()){
                    onSignUpSuccess();
                }else{
                    onSignUpFailed();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.zoom_back_in, R.anim.zoom_back_out);
                finish();
            }
        });

    }

    public void onSignUpSuccess(){
        cabutton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        cabutton.setEnabled(true);
                        Intent intent = new Intent(Signup.this, Profile.class);
                        intent.putExtra("user", user.getText().toString());
                        startActivity(intent);
                        finish();
                        progressDialog.dismiss();
                    }
                }, 2000);
    }

    public void onSignUpFailed(){
        Toast.makeText(getBaseContext(), "Sign up failed. Try again", Toast.LENGTH_LONG).show();
        cabutton.setEnabled(true);
    }

    private boolean createUser(){
        String username = user.getText().toString();
        String pw = pass.getText().toString();
        boolean valid = true;

        if(!validate()){
            return false;
        }

        try{
            OutputStreamWriter fout = new OutputStreamWriter(openFileOutput("credenciales_" + username + ".txt", Context.MODE_PRIVATE));
            fout.write(username);
            fout.write('\n');
            fout.write(pw);
            fout.write('\n');
            fout.close();

           /* fout = new OutputStreamWriter(openFileOutput("shared_stuff.txt", Context.MODE_PRIVATE)); //TODO esto habra que quitarlo cuando se haga el explorador de archivos
            fout.write("3"); //size
            fout.write('\n');
            fout.write("item1");
            fout.write('\n');
            fout.write("item2");
            fout.write('\n');
            fout.write("foto.jpg");
            fout.write('\n');
            fout.close();*/

        }catch(Exception e){
            valid = false;
        }
        return valid;
    }

    private boolean validate(){
        boolean valid = true;

        String userName = user.getText().toString();
        String pw = pass.getText().toString();
        String pw2 = confirmpass.getText().toString();

        if(userName.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(userName).matches()){
            user.setError("enter a valid username");
            valid = false;
        }else{
            user.setError(null);
        }

        if(pw.isEmpty()){
            pass.setError("password cannot be empty");
            valid = false;
        }else{
            pass.setError(null);
        }

        if(pw2.isEmpty() || !pw2.equals(pw)){
            confirmpass.setError("password doesn't match");
            valid = false;
        }

        File af = new File("/data/data/com.example.samue.login/files/credenciales_"+ userName +".txt");
        if(af.isFile()){
          valid = false;
          user.setError("User already exists");
        }else{
            new File("/data/data/com.example.samue.login/files/shared_stuff.txt");
            new File("/data/data/com.example.samue.login/files/archives_path.txt");
        }

        return valid;
    }
}
