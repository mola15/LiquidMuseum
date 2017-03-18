package it.polimi.molinaroli.liquidmuseum;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import it.polimi.molinaroli.liquidmuseum.Logic.Client;
import it.polimi.molinaroli.liquidmuseum.Logic.IntentConverter;
import it.polimi.molinaroli.liquidmuseum.Logic.LiquidAndroidService;
import it.polimi.molinaroli.liquidmuseum.Logic.NsdHelper;
import xdroid.toaster.Toaster;

public class MainActivity extends AppCompatActivity {

    int myServerPort;
    Client client;
    Context c;
    Button discover;
    Button display;
    Button start;
    Button forward;
    NsdHelper helper;
    ListView serviceList;
    LiquidAndroidService mService;
    boolean mBound = false;
    Intent arrivalIntent;

    Button url;
    Button video;
    Button image;
    Button play;
    Button pause;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LiquidAndroidService.LocalBinder binder = (LiquidAndroidService.LocalBinder) service;
            mService = binder.getService();
            Log.d("Activity","service connected");
            mBound = true;
            myServerPort = mService.getPort();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("Activity","service disconnected");
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        arrivalIntent = getIntent();
        if(getIntent().getAction().equals("OPEN")){
            //qui è stato avviato dalla notifica e quindi sicuramente il service sta andando
            final Intent intent = new Intent(this, LiquidAndroidService.class);
            bindService(intent, mConnection, 0);
            Log.d("bound", "" + mBound);
            Log.d("helperinit",""+mService.getHelper().getInit());
        } else if ((arrivalIntent.getAction().equals("android.media.action.IMAGE_CAPTURE")) || (arrivalIntent.getAction().equals(Intent.ACTION_VIEW)) || (arrivalIntent.getAction().equals(Intent.ACTION_SEND)) || (arrivalIntent.getAction().equals(Intent.ACTION_SENDTO))){
            //voglio che sia partito il servizio e quindi dico che mbound è true;
            final Intent intent = new Intent(this, LiquidAndroidService.class);
            bindService(intent, mConnection, 0);
            Log.d("bound", "" + mBound);
        }

        setContentView(R.layout.activity_main);
        Log.e("azione intent",getIntent().getAction());

        try {
            Log.e("intento arrivato", IntentConverter.intentToJSON(getIntent()).toString());
        }catch(Exception e){
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {


        final Intent intent = new Intent(this, LiquidAndroidService.class);


        c = this;
        start = (Button) findViewById(R.id.startservice) ;
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("bindstate",""+ mBound);
                if(!mBound) {
                    Intent myIntent = new Intent(getApplicationContext(), LiquidAndroidService.class);
                    startService(myIntent);
                    bindService(intent, mConnection, 0);
                    Log.d("bound", "" + mBound);
                } else{
                    Toaster.toast("Service already Started");
                }
            }
        });

        setupUrl();
        setupVideo();
        setupImage();
        setupplay();
        setuppause();

        super.onStart();

    }

    public void setupUrl(){
        url = (Button) findViewById(R.id.openurl);
        url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    //eseguo solo se ho il bind attivo
                    helper = mService.getHelper();

                    Intent uintent = new Intent(Intent.ACTION_VIEW);
                    uintent.setData(Uri.parse("http://www.louvre.fr/en"));

                    helper.forwardIntent(c,uintent,myServerPort);
                }catch (Exception e){
                Toaster.toast("Service not Started");
            }
            }
        });

    }
    public void setupVideo(){
        video = (Button) findViewById(R.id.openviedo);
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    //eseguo solo se ho il bind attivo
                    helper = mService.getHelper();
                    //uso un altro metodo perchè non serve il dialog voglio mandare direttamente gli intent
                    String videoUrl = "https://firebasestorage.googleapis.com/v0/b/liquid-museum.appspot.com/o/video.mp4?alt=media&token=a7a50687-533a-48b8-a7c4-3d8de28df271";
                    //String videoUrl ="http://clips.vorwaerts-gmbh.de/VfE_html5.mp4";
                    Intent vintent = new Intent(Intent.ACTION_VIEW);
                    vintent.setDataAndType(Uri.parse(videoUrl),"video/mp4");
                    Log.e("button",vintent.getDataString());
                    Log.e("button",vintent.getType());
                    helper.forwardIntent(c,vintent,myServerPort);
                }catch (Exception e){
                    Toaster.toast("Service not Started");
                }

            }
        });

    }
    public void setupImage(){
        image = (Button) findViewById(R.id.openimpage);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //eseguo solo se ho il bind attivo
                    helper = mService.getHelper();
                    //uso un altro metodo perchè non serve il dialog voglio mandare direttamente gli intent
                    String imageUrl = "https://firebasestorage.googleapis.com/v0/b/liquid-museum.appspot.com/o/photo.jpg?alt=media&token=dbf5eaa5-686b-47b3-8282-89e9fc3c55d8";
                    //String videoUrl ="http://clips.vorwaerts-gmbh.de/VfE_html5.mp4";
                    Intent pintent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
                    //pintent.setType("image/jpg");
                    //pintent.setData(Uri.parse(imageUrl));
                    pintent.setDataAndType(Uri.parse(imageUrl), "image/jpg");
                    Log.e("button", pintent.getDataString());

                    helper.forwardIntent(c, pintent, myServerPort);
                }catch (Exception e){
                    Toaster.toast("Service not Started");
                }
            }
        });
    }

    public void setuppause(){
        pause = (Button) findViewById(R.id.stopvideo);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //eseguo solo se ho il bind attivo
                    helper = mService.getHelper();
                    //intento per stoppare
                    Intent iy = new Intent(Intent.ACTION_MEDIA_BUTTON);
                    iy.putExtra("andorid.intent.extra.LIQUIDMETHOD","BROADCAST");
                    iy.putExtra("android.intent.extra.KEY_EVENT_CODE", KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                    Log.e("mediabuttonintent",IntentConverter.intentToJSON(iy).toString());
                    helper.forwardIntent(c, iy, myServerPort);
                }catch (Exception e){
                    Toaster.toast("Service not Started");
                }
            }
        });
    }

    public void setupplay(){
        play = (Button) findViewById(R.id.playvideo);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //eseguo solo se ho il bind attivo
                    helper = mService.getHelper();
                    //intento per stoppare
                    Intent iy = new Intent(Intent.ACTION_MEDIA_BUTTON);
                    iy.putExtra("andorid.intent.extra.LIQUIDMETHOD","BROADCAST");
                    iy.putExtra("android.intent.extra.KEY_EVENT_CODE", KeyEvent.KEYCODE_SPACE);
                    Log.e("mediabuttonintent",IntentConverter.intentToJSON(iy).toString());
                    helper.forwardIntent(c, iy, myServerPort);
                }catch (Exception e){
                    Toaster.toast("Service not Started");
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        /*
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        */
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onStop();
    }

}
