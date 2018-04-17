package com.example.samue.login;

import android.*;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
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
import android.widget.ProgressBar;
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
ArchivesDatabase mArchivesDatabase;
private String userRecursos;

    public static final String LOCAL_MEDIA_STREAM_ID = "localStreamPN";
    private PnRTCClient pnRTCClient;
    private Pubnub mPubNub;
    public String username;
    private String archivoCompartido;
    private int step, total;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        this.username = getIntent().getExtras().getString("user");
        this.archivoCompartido = "";
        this.step = 0; this.total = 0;
        mDatabaseHelper = new DatabaseHelper(this);
        mArchivesDatabase = new ArchivesDatabase(this);
        friends_list = (ListView) findViewById(R.id.friends_list);

        populateListView();

        friends_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String connectTo = al_friends.get(position).getNombre();
                userRecursos = connectTo;
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
            //intent.putExtra("archivesDatabase", mArchivesDatabase);
            startActivityForResult(intent, 1);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    String name = data.getStringExtra("name");
                    String path = data.getStringExtra("path");

                    if(!this.mArchivesDatabase.exists(name)){
                        this.mArchivesDatabase.addData(name, path);
                        notificate("Archive is now shared");
                    }else{
                        notificate("Archive is already shared");
                    }
                }
                break;
            case 2:
                if(resultCode == Activity.RESULT_OK){
                    final String name = data.getStringExtra("name");
                    String sendTo = data.getStringExtra("sendTo");
                    RA(name, sendTo);

                    Profile.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pd = new ProgressDialog(Profile.this);
                            pd.setMax(100);
                            pd.setTitle("Downloading " + name + "...");
                            pd.setMessage("wait for the download to complete");
                            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            pd.setCancelable(false);
                            pd.setProgress(0);
                            pd.show();
                        }
                    });
                }
            default:
                cerrarConexion(userRecursos);
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
                            //intent.putExtra("profileContext", this);
                            startActivityForResult(intent, 1);
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
            case R.id.see_shared_archives:
                // User chose the "Settings" item, show the app settings UI...
                //Toast.makeText(getBaseContext(), "Settings clicked", Toast.LENGTH_LONG).show();
                ArrayList<String> al = getArchivesList();
                Intent intent = new Intent(Profile.this, Recursos.class);
                intent.putExtra("lista", al);
                intent.putExtra("listener", false);
                startActivity(intent);
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

    private ArrayList<String> getArchivesList(){
        ArrayList<String> al = new ArrayList<String>();
        Cursor data = mArchivesDatabase.getData();

        while(data.moveToNext()){
            al.add(data.getString(1));
        }
        return al;
    }

    private void connectPeer(String connectTo, boolean call){
        if(this.pnRTCClient == null) {
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
        }

        if(call){
            this.pnRTCClient.connect(connectTo);
        }
    }

    private void notificate(String notification){
        final String notice = notification;
        Profile.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), notice, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cerrarConexion(String userTo){
        this.pnRTCClient.closeConnection(userTo);
    }

    private void RA(String name, String sendTo){ //Request Archive
        try{
            JSONObject msg = new JSONObject();
            msg.put("type", "RA");
            msg.put("sendTo", this.username);
            msg.put("name", name);

            this.pnRTCClient.transmit(sendTo, msg);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void handleRA(JSONObject jsonMsg){
        try{
            String archive = jsonMsg.getString("name");
            String sendTo = jsonMsg.getString("sendTo");
            Cursor c  = this.mArchivesDatabase.getData(archive);

            c.moveToNext();
            String path = c.getString(1);

            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            //BufferedInputStream bis = new BufferedInputStream(fis);

            JSONObject msg = new JSONObject();
            msg.put("type", "SA");
            msg.put("name", archive);

            String s="";
            int aLength = (int) file.length();

            byte[] bFile = new byte[aLength];
            fis.read(bFile);
            s = Base64.encodeToString(bFile, Base64.URL_SAFE);

            aLength = s.length();

            if(aLength > 5000){
                int inicio = 0, fin = 5000;
                msg.put("split", true);
                msg.put("lastPiece", false);
                msg.put("size", aLength);
                msg.put("inicio", inicio);
                String sub;
                while(fin < aLength){
                    sub = s.substring(inicio, fin);
                    msg.put("archive", sub);
                    this.pnRTCClient.transmit(sendTo, msg);
                    msg.remove("archive");
                    fin += 5000;
                    inicio += 5000;
                    msg.remove("inicio");
                    msg.put("inicio", inicio);
                }

                if(inicio < aLength){
                    sub = s.substring(inicio);
                    msg.remove("archive");
                    msg.put("archive", sub);
                }

                msg.remove("lastPiece");
                msg.put("lastPiece", true);
                this.pnRTCClient.transmit(sendTo, msg);

            }else{
                msg.put("split", false);
                msg.put("lastPiece", true);
                msg.put("archive", s);
                this.pnRTCClient.transmit(sendTo, msg);
            }

            fis.close();
            //bis.close();

            this.pnRTCClient.closeConnection(sendTo);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void handleSA(JSONObject jsonMsg){
        try{
            String aux = "";
            int size, inicio;
            size = jsonMsg.getInt("size");
            this.step = size / 100;
            inicio = jsonMsg.getInt("inicio");
            String name = jsonMsg.getString("name");
            boolean split = jsonMsg.getBoolean("split");
            boolean lastPiece = jsonMsg.getBoolean("lastPiece");
            String archive = jsonMsg.getString("archive");

            if(split){
                aux = this.archivoCompartido;
                this.archivoCompartido = aux + archive;
                if(!lastPiece){
                    if(inicio != 0){
                        if(inicio > this.total){
                            Profile.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pd.incrementProgressBy(1);
                                }
                            });
                            this.total += this.step;
                        }
                    }else{
                        this.total = this.step;
                    }
                }else{
                    byte[] bFile = Base64.decode(this.archivoCompartido, Base64.URL_SAFE);
                    Profile.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pd.dismiss();
                        }
                    });
                    guardarArchivo(bFile, name);
                }
            }else{
                byte[] bFile = Base64.decode(archive, Base64.URL_SAFE);
                Profile.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                    }
                });
                guardarArchivo(bFile, name);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void guardarArchivo(byte[] bFile, String name){
        String path = Environment.getExternalStorageDirectory().getPath() + "/Download";
        File file = new File(path, "P2PArchiveSharing");

        if(!file.isDirectory()){
            file.mkdirs();
        }

        path += "/P2PArchiveSharing/" + name;

        try{
            FileOutputStream fos = new FileOutputStream(path);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            bos.write(bFile);

            bos.close();
            fos.close();

            this.archivoCompartido = "";
            notificate("Archive " + name + " saved in Downloads");

        }catch(Exception e){
            e.printStackTrace();
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
                    cerrarConexion(userFR);
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

            cerrarConexion(addme);
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

            int size = jsonMsg.getInt("size");

            for(int i = 0; i < size; i++){
                al.add(jsonMsg.getString("item"+i));
            }

            Intent intent = new Intent(Profile.this, Recursos.class);
            intent.putExtra("lista", al);
            intent.putExtra("listener", true);
            intent.putExtra("sendTo", jsonMsg.getString("sendTo"));
            startActivityForResult(intent, 2); //para volver a esta activity, llamar finish() desde la otra.

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void VAL(JSONObject jsonMsg){
       try{
           JSONObject msg = new JSONObject();
           msg.put("type", "VAL");
           msg.put("sendTo", this.username);
           ArrayList<String> al = getArchivesList();
           msg.put("size", al.size());
           int i = 0;

           for(String item : al){
               msg.put("item"+i, item);
               i++;
           }

           this.pnRTCClient.transmit(jsonMsg.getString("sendTo"), msg);
           cerrarConexion(jsonMsg.getString("sendTo"));
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
               }else if(type.equals("RA")){
                   handleRA(jsonMsg);
               }else if(type.equals("SA")){
                   handleSA(jsonMsg);
               }

            } catch (JSONException e){
                e.printStackTrace();
            }

        }
    }
}
