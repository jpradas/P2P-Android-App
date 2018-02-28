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
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoRendererGui;

import java.util.ArrayList;
import java.util.List;

import me.kevingleason.pnwebrtc.PnPeer;
import me.kevingleason.pnwebrtc.PnRTCClient;
import me.kevingleason.pnwebrtc.PnRTCListener;
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

    public static final String LOCAL_MEDIA_STREAM_ID = "localStreamPN";
    private PnRTCClient pnRTCClient;
    private Pubnub mPubNub;
    public String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        this.username = getIntent().getExtras().getString("user");
        mDatabaseHelper = new DatabaseHelper(this);
        friends_list = (ListView) findViewById(R.id.friends_list);

        populateListView();

        friends_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String connectTo = al_friends.get(position).getNombre();
                String userCall = connectTo + Constants.STDBY_SUFFIX;
                //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show(); //TODO cuando hacemos click en uno comienza el mensaje al canal mediante PubNub
                JSONObject jsonCall = new JSONObject();
                try {
                    jsonCall.put(Constants.JSON_CALL_USER, username);
                    mPubNub.publish(userCall, jsonCall, new Callback() {
                        @Override
                        public void successCallback(String channel, Object message) { //TODO conectamos nosotros al otro
                            Log.d("MA-dCall", "SUCCESS: " + message.toString());
                            //Toast.makeText(getBaseContext(), "me han contestado", Toast.LENGTH_LONG).show();
                            /*Intent intent = new Intent(Profile.this, Recursos.class);
                            intent.putExtra("user_name", username);
                            intent.putExtra("call_user", connectTo);
                            startActivity(intent);*/

                            connectPeer(connectTo, true);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getIntent().getExtras().getString("user"));


        fab = (FloatingActionButton) findViewById(R.id.fab);

        /*fab.setOnClickListener(new View.OnClickListener() { //TODO debe subir al fichero interno el path del archivo que elije
            @Override
            public void onClick(View v) {

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
                    Log.v("MA-success", "MESSAGE: " + message.toString());
                    if (!(message instanceof JSONObject)) return; // Ignore if not JSONObject
                    JSONObject jsonMsg = (JSONObject) message;
                    try { //TODO conectar con el peer que envió el SDP
                        if (!jsonMsg.has(Constants.JSON_CALL_USER)) return;
                        String user = jsonMsg.getString(Constants.JSON_CALL_USER);
                        // Consider Accept/Reject call here
                        //intent.putExtra(Constants.JSON_CALL_USER, user);
                        /*Intent intent = new Intent(Profile.this, Recursos.class);
                        intent.putExtra("user_name", username);
                        intent.putExtra("call_user", user); //TODO podriamos pasar una variable que indique que pasa lo que necesite y vuelve a la actividad
                        startActivity(intent);*/
                        connectPeer("", false);

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

    private void connectPeer(String connectTo, boolean call){
        PeerConnectionFactory.initializeAndroidGlobals(
                getApplicationContext(),  // Context
                true,  // Audio Enabled
                true,  // Video Enabled
                true,  // Hardware Acceleration Enabled
                VideoRendererGui.getEGLContext()); // Render EGL Context

        PeerConnectionFactory pcFactory = new PeerConnectionFactory();
        this.pnRTCClient = new PnRTCClient(Constants.PUB_KEY, Constants.SUB_KEY, this.username);

        MediaStream mediaStream = pcFactory.createLocalMediaStream(LOCAL_MEDIA_STREAM_ID);

        this.pnRTCClient.attachRTCListener(new myRTCListener());
        this.pnRTCClient.attachLocalMediaStream(mediaStream);

        this.pnRTCClient.listenOn(this.username);

        if(call){
            this.pnRTCClient.connect(connectTo);

           try{
               Thread.sleep(2000);
           } catch(Exception e){
               e.printStackTrace();
           }
            JSONObject msg = new JSONObject();
            try{
                msg.put("name", "Josue Pradas");
                msg.put("msg", "HOLA MUNDO");

                this.pnRTCClient.transmit(connectTo, msg);
            }catch(JSONException e){
                e.printStackTrace();
            }

            //this.pnRTCClient.closeConnection(connectTo);

        }
    }

    private class myRTCListener extends PnRTCListener{
        @Override
        public void onPeerConnectionClosed(PnPeer peer) {
            super.onPeerConnectionClosed(peer);
        }

        @Override
        public void onLocalStream(MediaStream localStream) {
            super.onLocalStream(localStream);
        }

        public void onConnected(String userId){
            Log.d("Md-a", "connectado a: " + userId);
        }

        @Override
        public void onMessage(PnPeer peer, Object message) {
            if (!(message instanceof JSONObject)) return; //Ignore if not JSONObject
            JSONObject jsonMsg = (JSONObject) message;
            try {
                final String user = jsonMsg.getString("name");
                final String text = jsonMsg.getString("msg");

                Profile.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Profile.this,user + " dice " + text,Toast.LENGTH_SHORT).show();
                    }
                });
                //Log.d("Md-a", user + " : " + text);
            } catch (JSONException e){
                e.printStackTrace();
            }

        }
    }
}
