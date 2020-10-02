package com.example.user06.aidlclient;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.anjan.server.IDisplayOrientation;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btnShow;
    private TextView pitch, roll;
    private boolean mIsServiceBound = false;
    private String TAG = "MainActivity Client";
    protected IDisplayOrientation mDisplayOrientationService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnShow = findViewById(R.id.buttonshow);
        pitch = (TextView)findViewById(R.id.pitch);
        roll = (TextView)findViewById(R.id.roll);
        initView();
    }

    private void initView() {
        new CountDownTimer(8000, 1000) {

            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "seconds remaining: "+ millisUntilFinished/1000);
            }

            public void onFinish() {
                try {
                    pitch.setText(String.valueOf(mDisplayOrientationService.getOrientationPitch()));
                    roll.setText(String.valueOf(mDisplayOrientationService.getOrientationRoll()));
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        }.start();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void initConnection() {
        if (mDisplayOrientationService == null) {
            Intent intent = new Intent(IDisplayOrientation.class.getName());

            /*this is service name*/
            intent.setAction("service.multiply");

            /*From 5.0 annonymous intent calls are suspended so replacing with server app's package name*/
            intent.setPackage("com.anjan.server");

            // binding to remote service
            bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        initConnection();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        unbindService(serviceConnection);
    }

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        //Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        //Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        //Get component info and create ComponentName
        ResolveInfo serviceInfo = (ResolveInfo) resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        //Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        //Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(getApplicationContext(),"Service Connected", Toast.LENGTH_LONG).show();
            mDisplayOrientationService =  IDisplayOrientation.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getApplicationContext(),"Service DisConnected", Toast.LENGTH_LONG).show();
            mDisplayOrientationService = null;
        }
    };
}
