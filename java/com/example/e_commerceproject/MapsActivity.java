package com.example.e_commerceproject;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.e_commerceproject.Map.myLocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    EditText addressText;
    LocationManager locManager;
    myLocationListener locListener;
    Button getLocation;
    String provider;
    SharedPreferences.Editor perEditor;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addressText = findViewById(R.id.editText);
        getLocation = findViewById(R.id.btn1);
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locListener = new myLocationListener(getApplicationContext());
        provider = locManager.getBestProvider(new Criteria(), false);
        perEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
        }

        try{
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,6000,0,locListener);
        }
        catch (SecurityException ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.04441960,31.235711600),8));
        getLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mMap.clear();
                Geocoder coder = new Geocoder(MapsActivity.this);
                List<android.location.Address> addressList;
                Location loc = null;
                try{
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                    }
                    for(int i=0;i < locManager.getAllProviders().size();i++){
                        loc = locManager.getLastKnownLocation(locManager.getAllProviders().get(i));
                        if(loc != null)
                            break;
                    }
                }
                catch (SecurityException ex){
                    Toast.makeText(MapsActivity.this, "You did not allow to access the current location.", Toast.LENGTH_SHORT).show();
                }
                if(loc != null){
                    LatLng myPosition = new LatLng(loc.getLatitude(),loc.getLongitude());
                    try{
                        addressList = coder.getFromLocation(myPosition.latitude,myPosition.longitude,1);
                        if(!addressList.isEmpty()){
                            String address = "";
                            for(int i=0; i<addressList.get(0).getMaxAddressLineIndex();i++){
                                address += addressList.get(0).getAddressLine(i) + ", ";
                            }
                            mMap.addMarker(new MarkerOptions().position(myPosition).title("My Location").snippet(address)).setDraggable(true);
                            addressText.setText(address.toString());
                            perEditor.putString("KeyAddress",address);
                            perEditor.apply();
                            onBackPressed();
                        }
                    }
                    catch (IOException e){
                        mMap.addMarker(new MarkerOptions().position(myPosition).title("My Location"));
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition,15));
                }
                else{
                    Toast.makeText(MapsActivity.this, "Please wait until your position is determined. ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


//    public boolean checkLocationPermission() {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)) {
//
//                // Show an explanation to the user asynchronously -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//                new AlertDialog.Builder(this)
//                        .setTitle("Location Access")
//                        .setMessage("Allow Location Permission.")
//                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                //Prompt the user once explanation has been shown
//                                ActivityCompat.requestPermissions(MapsActivity.this,
//                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                        MY_PERMISSIONS_REQUEST_LOCATION);
//                            }
//                        })
//                        .create()
//                        .show();
//
//
//            } else {
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        MY_PERMISSIONS_REQUEST_LOCATION);
//            }
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // location-related task you need to do.
//                    if (ContextCompat.checkSelfPermission(this,
//                            Manifest.permission.ACCESS_FINE_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED) {
//
//                        //Request location updates:
//                        locManager.requestLocationUpdates(provider, 400, 1, locListener);
//                    }
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//
//                }
//                return;
//            }
//
//        }
//    }
}