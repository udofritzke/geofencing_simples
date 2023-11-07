package com.example.geofencing_simples;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

/*
https://github.com/udofritzke/geofencing_simples
 */

/*
Google-CA
37.4219983333333, -122.084
37°25'19.2"N 122°05'02.4"W
Mountain View, CA 94043, EUA
 */

public class MainActivity extends AppCompatActivity {
    private GeofencingClient geofencingClient;
    private String GEOFENCE_ID = "ID DA FRONTEIRA";
    private static final String TAG = "MainActivity";
    private ArrayList<Geofence> geofenceList = null;
    private PendingIntent geofencePendingIntent;
    //TextView texto;
    private FusedLocationProviderClient clienteFusedLocation;
    static TextView texto = null;
    TextView texto_coords = null;
    Button busca, seta_fronteira;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        texto = findViewById(R.id.texto);
        texto_coords = findViewById(R.id.texto_coord);

        busca = (Button) findViewById(R.id.botao_busca);
        busca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                texto.setText("Botao busca coordenadas");
                buscaCoordenadas();
            }
        });
        seta_fronteira = (Button) findViewById(R.id.botao_seta_fronteira);
        seta_fronteira.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                texto.setText("Botao seta fronteira");
                setaFronteira();
            }
        });
        //seta_fronteira.setVisibility(View.INVISIBLE);
    }

    //definir uma PendingIntent que gerencia um BroadcastReceiver
    private PendingIntent getGeofencePendingIntent() {
        // Reusar a PendingIntent se já existe uma
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // Foi alterada a  FLAG_UPDATE_CURRENT por FLAG_IMMUTABLE pois deu erro no Samsung S10
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        ); //FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }
    void setaFronteira(){
        // obtem um objeto cliente de fronteira geográfica virtual
        geofencingClient = LocationServices.getGeofencingClient(this);
        Geofence geofence = null;
        GeofencingRequest geofencingRequest = null;

        // define uma lista de fronteiras virtuais
        geofenceList = new ArrayList<Geofence>();

        String txt = Double.toString(latitude)+"\n"+Double.toString(longitude);
        texto_coords.setText(txt);
        geofence = new Geofence.Builder()
                // Define o  of da fronteira. Isto é uma string que identifica a fronteira
                .setRequestId("CHAVE_MINHA_CASA")
                //-21.80092805741686, -46.58606422149775
                .setCircularRegion(
                        latitude,
                        longitude,
                        //-21.80092805741686, -46.58606422149775,
                        200 //50  // 50m de raio
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT|
                        Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(5000)
                .build();

        if (geofence == null) {
            Log.d(TAG, "Geofence não foi construída");
            texto.setText("Geofence não foi construída");
        } else {
            geofenceList.add(geofence);
            Log.d(TAG, "Geofence foi construída");
            texto.setText("Geofence construída");
            // Especificar fronteiras geográficas virtuais montando uma
            // requisição de fronteira virtual
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofences(geofenceList);

            if (builder != null) {
                geofencingRequest = builder.build();
                Log.d(TAG, "geofencingRequest foi construído");
            }
            else{
                texto.setText("Requisições de fronteira não construída");
                Log.d(TAG, "Builder não foi construído");
            }
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "Permissão ACCESS_FINE_LOCATION não concedida");
            texto.setText("Permissão ACCESS_FINE_LOCATION não concedida");
            return;
        }
        if (geofencingRequest !=null) {
            // adiciona a requisição de fronteira ao cliente de fronteiras virtuais
            geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Geofences added
                            Log.d(TAG, "Fronteiras adicionadas");
                            texto.setText("Fronteiras adicionadas");
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to add geofences
                            Log.d(TAG, "Fronteiras NÃO definidas");
                            texto.setText("Fronteiras NÃO definidas");
                        }
                    });
        }else  {
            texto.setText("Listener não definido");
        }
    }

    void buscaCoordenadas() {
        // Obtem provedor de localização combinada
        clienteFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        Log.d(TAG, "Iniciando localização");
        TextView texto = findViewById(R.id.texto);
        texto.setText("Iniciando localização");

        GoogleApiAvailability disponibilidadeAPI = GoogleApiAvailability.getInstance();
        int codErro = disponibilidadeAPI.isGooglePlayServicesAvailable(this);
        if (codErro != ConnectionResult.SUCCESS) {
            Log.d(TAG, "Serviço não disponível");
        } else Log.d(TAG, "Serviço disponível");

        try {
            clienteFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    TextView texto = findViewById(R.id.texto);
                    texto.setText("sucesso");
                    // Recuperou ultima localização conhecida
                    if (location != null) {
                        seta_fronteira.setVisibility(View.VISIBLE);
                        // Manipulação o objeto com a localização
                        Log.d(TAG, "Pegou localização");
                        //seta_fronteira.setVisibility(1);
                        String txt = "Longitude: " + location.getLongitude() + "\n" + "Latitude: " + location.getLatitude();
                        texto.setText(txt);
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    } else Log.d(TAG, "sucesso mas não pegou localização");
                }
            });
        } catch (SecurityException e) {
            Log.d(TAG, e.getMessage());
        }
    }
}