package com.example.samue.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.OutputStreamWriter;


public class CreateName extends AppCompatActivity {

    private Button confirmName;
    private EditText name;
    private String userName;
    private TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_name);


        confirmName = (Button) findViewById(R.id.button_confirm_name);


        name = (EditText) findViewById(R.id.name);



        confirmName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(createUser()){
                    nameSuccess();
                }else{
                    nameFailed();
                }
            }
        });

    }

    public void nameSuccess(){
        confirmName.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Welcome " + userName);
        progressDialog.show();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        confirmName.setEnabled(true);
                        Intent intent = new Intent(CreateName.this, Profile.class);
                        intent.putExtra("user", name.getText().toString());
                        startActivity(intent);
                        finish();
                        progressDialog.dismiss();
                    }
                }, 2000);
    }

    public void nameFailed(){
        Toast.makeText(getBaseContext(), "Empty Name. Try again", Toast.LENGTH_LONG).show();
        confirmName.setEnabled(true);
    }

    private boolean createUser(){
        userName = name.getText().toString();

        boolean valid = true;

        if(!validate()){
            return false;
        }

        try{
            OutputStreamWriter fileOut = new OutputStreamWriter(openFileOutput("nombre.txt", Context.MODE_PRIVATE));
            fileOut.write(userName);
            fileOut.close();

        }catch(Exception e){
            valid = false;
        }
        return valid;
    }

    private boolean validate(){
        boolean valid = true;

        String userName = name.getText().toString();


        if(userName.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(userName).matches()){
            name.setError("Enter a valid username");
            valid = false;
        }else{
            name.setError(null);
        }

        File af = new File("/data/data/com.example.samue.login/files/nombre.txt");
        if(af.isFile()){
            valid = false;
            name.setError("User already exists");
        }else{
            new File("/data/data/com.example.samue.login/files/shared_stuff.txt");
            new File("/data/data/com.example.samue.login/files/archives_path.txt");
        }

        return valid;
    }
}
