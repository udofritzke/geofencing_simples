package com.example.geofencing_simples;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
/*
Ver codigo exemplo em
https://developer.android.com/training/location/geofencing?hl=pt-br#java
 */

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Geofence triggered...", Toast.LENGTH_LONG).show();
        MainActivity.texto.setText("FRONTEIRA ULTRAPASSADA");
    }
}