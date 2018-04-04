package com.example.samue.login;

import android.*;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
                publish(connectTo, "VAR");
            }
        });

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getIntent().getExtras().getString("user"));


        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() { //TODO debe subir al fichero interno el path del archivo que elije
            @Override
            public void onClick(View v) {
                comprobarPermisos();
            }
        });
        initPubNub();
    }

    private void publish(final String connectTo, final String connectionType){
        String userCall = connectTo + Constants.STDBY_SUFFIX;
        JSONObject jsonCall = new JSONObject();
        try {
            jsonCall.put(Constants.JSON_CALL_USER, username);
            mPubNub.publish(userCall, jsonCall, new Callback() {
                @Override
                public void successCallback(String channel, Object message) { //conectamos nosotros al otro
                    Log.d("MA-dCall", "SUCCESS: " + message.toString());
                    connectPeer(connectTo, true); //conectamos con el peer

                    if(connectionType.equals("VAR")){ //buscamos que tipo de mensaje debemos enviar
                        VAR(connectTo);
                    }else if(connectionType.equals("FR")){
                        FR(connectTo);
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void comprobarPermisos(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else{
            Intent intent = new Intent(Profile.this, ArchiveExplorer.class);
            startActivity(intent);
        }
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
                    try {
                        if (!jsonMsg.has(Constants.JSON_CALL_USER)) return;
                        connectPeer("", false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Profile.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(Profile.this, ArchiveExplorer.class);
                            startActivity(intent);
                        }
                    });
                }else{
                    Profile.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Profile.this, "no se puede acceder a los archivos", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return;
            }
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
                    public void onClick(View v) {//TODO metodo para comprobar si existe el usuario en la lista de amigos
                        String fr = name.getText().toString();
                        if(!listContains(fr)) {
                            mdialog.dismiss();
                            publish(fr, "FR");
                            Toast.makeText(getApplicationContext(), "Friend request sent", Toast.LENGTH_SHORT).show();
                        }else{
                            //mdialog.dismiss();
                            Toast.makeText(getApplicationContext(), "you're already friend of " + fr, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private boolean listContains(String nombre){
        boolean contains = false;
        for(Friends f : al_friends){
            if(f.getNombre().equals(nombre)){
                contains = true;
            }
        }
        return contains;
    }

    public void addData(String newEntry){ //llamar cuando aceptemos la peticion de amistad y cuando nos la acepten
        boolean insertData = mDatabaseHelper.addData(newEntry);

        if(insertData){
            populateListView();
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
        }
    }

    private void FR(String sendTo){ //Friend Request
        try{
            JSONObject msg = new JSONObject();
            msg.put("type", "FR");
            msg.put("sendTo", this.username);

            this.pnRTCClient.transmit(sendTo, msg);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void handleFR(JSONObject jsonMsg){
        try{
            final String userFR = jsonMsg.getString("sendTo");
            mdialog = new Dialog(Profile.this);
            mdialog.setContentView(R.layout.dialog_acceptfriend);
            mdialog.show();
            TextView f_name = (TextView) mdialog.findViewById(R.id.accept_friend_tv);
            f_name.setText("Do you want to accept " + userFR + " as a friend?");

            Button yes = (Button) mdialog.findViewById(R.id.accept_friend_yes);
            Button no = (Button) mdialog.findViewById(R.id.accept_friend_no);

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mdialog.dismiss();
                }
            });

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Profile.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addData(userFR);
                        }
                    });
                    FA(userFR);
                    mdialog.dismiss();
                }
            });

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void FA(String sendTo){
        try{
            JSONObject msg = new JSONObject();
            msg.put("type", "FA");
            msg.put("addme", this.username);

            this.pnRTCClient.transmit(sendTo, msg);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void handleFA(JSONObject jsonMsg){
        try{
            String addme = jsonMsg.getString("addme");
            addData(addme);

            //this.pnRTCClient.closeConnection(addme);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void VAR(String sendTo){ //envia peticion para ver archivos
       try{
           JSONObject msg = new JSONObject();
           msg.put("type", "VAR"); //tipo de mensaje
           msg.put("sendTo", this.username); //usuario para devolver mensaje con datos

           this.pnRTCClient.transmit(sendTo, msg);
       }catch(Exception e){
           e.printStackTrace();
       }
    }

    private void handleVAL(JSONObject jsonMsg){
        try{
            ArrayList<String> al = new ArrayList();

            String item1 = jsonMsg.getString("item1");
            al.add(item1);
            String item2 = jsonMsg.getString("item2");
            al.add(item2);
            String item3 = jsonMsg.getString("item3");
            al.add(item3);

            Intent intent = new Intent(Profile.this, Recursos.class);
            intent.putExtra("lista", al);
            startActivity(intent); //para volver a esta activity, llamar finish() desde la otra.

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void VAL(JSONObject jsonMsg){
       try{
           BufferedReader fin = new BufferedReader(new InputStreamReader(openFileInput("shared_stuff.txt")));
           int size = Integer.parseInt(fin.readLine());
           String item = "item";
           JSONObject msg = new JSONObject();
           msg.put("type", "VAL");

           for(int i=0; i < size; i++){
               msg.put(item + (i+1), fin.readLine());
           }

           this.pnRTCClient.transmit(jsonMsg.getString("sendTo"), msg);
       }catch(Exception e){
           e.printStackTrace();
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
            final JSONObject jsonMsg = (JSONObject) message;
            try {
               final String type = jsonMsg.getString("type"); //TODO el manejo de los mensajes estaría bien hacerlos fuera de perfil, ya que no es su objetivo principal
               if(type.equals("VAR")){
                   VAL(jsonMsg);
               }else if(type.equals("VAL")){ //se debe manejar en la hebra principal ya que inicia una nueva actividad
                   Profile.this.runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           handleVAL(jsonMsg);
                       }
                   });
               }else if(type.equals("FR")){
                   Profile.this.runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           handleFR(jsonMsg);
                       }
                   });
               }else if(type.equals("FA")){
                   Profile.this.runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           handleFA(jsonMsg);
                       }
                   });
               }

            } catch (JSONException e){
                e.printStackTrace();
            }

        }
    }
}
