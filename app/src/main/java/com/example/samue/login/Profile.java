package com.example.samue.login;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import util.Constants;

public class Profile extends AppCompatActivity {
Dialog mdialog;
FloatingActionButton fab;
EditText name;
Button bf;
ListView friends_list;
FriendsAdapter adapter;
ArrayList<Friends> al_friends;
DatabaseHelper mDatabaseHelper;

    private Pubnub mPubNub;
    public String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mDatabaseHelper = new DatabaseHelper(this);
        friends_list = (ListView) findViewById(R.id.friends_list);

        populateListView();

        friends_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = al_friends.get(position).getNombre();
                Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show(); //TODO cuando hacemos click en uno comienza el mensaje al canal mediante PubNub
            }
        });

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getIntent().getExtras().getString("user"));
        this.username = getIntent().getExtras().getString("user");

        fab = (FloatingActionButton) findViewById(R.id.fab);

        /*fab.setOnClickListener(new View.OnClickListener() { //TODO debe subir al fichero interno el path del archivo que elije
            @Override
            public void onClick(View v) {

            }
        });*/

      /*  txt = (TextView) findViewById(R.id.josue);

        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String callNumStdBy = txt.getText().toString() + Constants.STDBY_SUFFIX;
                JSONObject jsonCall = new JSONObject();
                try {
                    jsonCall.put(Constants.JSON_CALL_USER, username);
                    mPubNub.publish(callNumStdBy, jsonCall, new Callback() {
                        @Override
                        public void successCallback(String channel, Object message) {
                            Log.d("MA-dCall", "SUCCESS: " + message.toString());
                            //Toast.makeText(getBaseContext(), "me han contestado", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });*/
        initPubNub();
    }

    public void initPubNub(){
        String stdbyChannel = this.username + Constants.STDBY_SUFFIX;
        this.mPubNub = new Pubnub(Constants.PUB_KEY, Constants.SUB_KEY);
        this.mPubNub.setUUID(this.username);
        try {
            this.mPubNub.subscribe(stdbyChannel, new Callback(){ //creamos nuestro canal y nos quedamos en stand-by esperando alguna conexión
                @Override
                public void successCallback(String channel, Object message) { //despierta cuando alguien se conecta a nuestro canal y responde con ACK
                    Log.d("MA-success", "MESSAGE: " + message.toString());
                    if (!(message instanceof JSONObject)) return; // Ignore if not JSONObject
                    JSONObject jsonMsg = (JSONObject) message;
                    try { //TODO manejar las distintas peticiones de otros dependiendo del tipo de mensaje recibido
                        if (!jsonMsg.has(Constants.JSON_CALL_USER)) return;
                        String user = jsonMsg.getString(Constants.JSON_CALL_USER);
                        // Consider Accept/Reject call here
                        //Toast.makeText(getBaseContext(), "user_call: " + user, Toast.LENGTH_LONG).show();
                        //intent.putExtra(Constants.JSON_CALL_USER, user);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_toolbar, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Toast.makeText(getBaseContext(), "Settings clicked", Toast.LENGTH_LONG).show();
                return true;

            case R.id.action_add_friend:
                mdialog = new Dialog(Profile.this);
                mdialog.setContentView(R.layout.dialog_newfriend);
                mdialog.show();
                name = (EditText) mdialog.findViewById(R.id.name);
                bf = (Button) mdialog.findViewById(R.id.button_friend);

                bf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addData(name.getText().toString()); //TODO metodo para comprobar si existe el usuario
                        populateListView();
                        mdialog.dismiss();
                    }
                });

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void addData(String newEntry){
        boolean insertData = mDatabaseHelper.addData(newEntry);

        if(insertData){
            Toast.makeText(this, "Friend successfully added", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateListView(){
        Cursor data = mDatabaseHelper.getData();
        al_friends = new ArrayList<>();
        while(data.moveToNext()){
            al_friends.add(new Friends(data.getString(1), R.drawable.ic_launcher_foreground));
        }
        adapter = new FriendsAdapter(this, al_friends);
        friends_list.setAdapter(adapter);
    }
}
