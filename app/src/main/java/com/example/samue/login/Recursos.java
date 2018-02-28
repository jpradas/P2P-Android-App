package com.example.samue.login;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;

import me.kevingleason.pnwebrtc.PnPeer;
import me.kevingleason.pnwebrtc.PnRTCClient;
import me.kevingleason.pnwebrtc.PnRTCListener;
import util.Constants;

public class Recursos extends AppCompatActivity {
    private String username;
    private PnRTCClient pnRTCClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recursos);
        Bundle extras = getIntent().getExtras();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.username = extras.getString("user_name", "");

        PeerConnectionFactory.initializeAndroidGlobals(
                this,  // Context
                false,  // Audio Enabled
                false,  // Video Enabled
                false,  // Hardware Acceleration Enabled
                null); // Render EGL Context

        PeerConnectionFactory pcFactory = new PeerConnectionFactory();
        this.pnRTCClient = new PnRTCClient(Constants.PUB_KEY, Constants.SUB_KEY, this.username);
       /* List<PeerConnection.IceServer> servers = getXirSysIceServers();
        if (!servers.isEmpty()){
            this.pnRTCClient.setSignalParams(new PnSignalingParams());
        }*/

        this.pnRTCClient.attachRTCListener(new myRTCListener());
        this.pnRTCClient.listenOn(this.username);

    }

    /*public List<PeerConnection.IceServer> getXirSysIceServers(){
        List<PeerConnection.IceServer> servers = new ArrayList<PeerConnection.IceServer>();
        try {
            servers = new XirSysRequest().execute().get();
        } catch (InterruptedException e){
            e.printStackTrace();
        }catch (ExecutionException e){
            e.printStackTrace();
        }
        return servers;
    }*/

    private class myRTCListener extends PnRTCListener{
        @Override
        public void onPeerConnectionClosed(PnPeer peer) {
            super.onPeerConnectionClosed(peer);
        }

        @Override
        public void onLocalStream(MediaStream localStream) {
            super.onLocalStream(localStream);
        }

        @Override
        public void onMessage(PnPeer peer, Object message) {
            super.onMessage(peer, message);
        }
    }
}
