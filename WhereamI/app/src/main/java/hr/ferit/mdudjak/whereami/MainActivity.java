package hr.ferit.mdudjak.whereami;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_LOCATION_PERMISSION = 10;
    TextView tvLocationDisplay;
    TextView tvLocation;
    LocationListener mLocationListener;
    LocationManager mLocationManager;
    GoogleMap mGoogleMap;
    MapFragment mMapFragment;
    HashMap<Integer, Integer> mSoundMap = new HashMap<>();
    SoundPool mSoundPool;
    boolean mLoaded = false;
    private GoogleMap.OnMapClickListener mCustomOnMapClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        this.mLocationListener = new SimpleLocationListener();
        this.setUpUi();
        this.loadSounds();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasLocationPermission() == false) {
            requestPermission();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.hasLocationPermission()) {
            startTracking();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTracking();
    }

    private void startTracking() {
        Log.d("Tracking", "Tracking started.");
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String locationProvider = this.mLocationManager.getBestProvider(criteria, true);
        long minTime = 1000;
        float minDistance = 10;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            this.mLocationManager.requestLocationUpdates(locationProvider, minTime, minDistance, this.mLocationListener);
            return;
        }
        this.mLocationManager.requestLocationUpdates(locationProvider, minTime, minDistance, this.mLocationListener);
    }
    private void stopTracking() {
        Log.d("Tracking", "Tracking stopped.");
        this.mLocationManager.removeUpdates(this.mLocationListener);
    }
    private void setUpUi() {
        this.tvLocationDisplay = (TextView) this.findViewById(R.id.tvGeoLocation);
        this.mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fGoogleMap);
        this.mMapFragment.getMapAsync(this);
        this.tvLocation= (TextView) this.findViewById(R.id.tvLocation);
        this.mCustomOnMapClickListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions newMarkerOptions = new MarkerOptions();
                newMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
                newMarkerOptions.title("My place");
                newMarkerOptions.snippet("I declare this my teritory!");
                newMarkerOptions.position(latLng);
                mGoogleMap.addMarker(newMarkerOptions);
                if(mLoaded == true) playSound(R.raw.sound);
            }
        };
    }
    private void updateLocationDisplay(Location location){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Lat: ").append(location.getLatitude()).append("\n");
        stringBuilder.append("Lon: ").append(location.getLongitude());
        tvLocationDisplay.setText(stringBuilder.toString());
        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<android.location.Address> nearByAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (nearByAddresses.size() > 0) {
                    StringBuilder stringBuilder2 = new StringBuilder();
                    android.location.Address nearestAddress = nearByAddresses.get(0);
                    stringBuilder2.append(nearestAddress.getAddressLine(0)).append("\n")
                            .append(nearestAddress.getLocality()).append("\n")
                            .append(nearestAddress.getCountryName());
                    tvLocation.setText(stringBuilder2.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private boolean hasLocationPermission(){
        String LocationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        int status = ContextCompat.checkSelfPermission(this,LocationPermission);
        if(status == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }
    private void requestPermission(){
        String[] permissions = new String[]{ Manifest.permission.ACCESS_FINE_LOCATION };
        ActivityCompat.requestPermissions(MainActivity.this,
                permissions, REQUEST_LOCATION_PERMISSION);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION:
                if(grantResults.length >0){
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        Log.d("Permission","Permission granted. User pressed allow.");
                    }
                    else{
                        Log.d("Permission","Permission not granted. User pressed deny.");
                        askForPermission();
                    }
                }
        }
    }
    private void askForPermission(){
        boolean shouldExplain = ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(shouldExplain){
            Log.d("Permission","Permission should be explained, - don't show again not clicked.");
            this.displayDialog();
        }
        else{
            Log.d("Permission","Permission not granted. User pressed deny and don't show again.");
            tvLocationDisplay.setText("Sorry, we really need that permission");
        }
    }
    private void displayDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Location permission")
                .setMessage("We display your location and need your permission")
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("Permission", "User declined and won't be asked again.");
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("Permission","Permission requested because of the explanation.");
                        requestPermission();
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        UiSettings uiSettings = this.mGoogleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        this.mGoogleMap.setOnMapClickListener(this.mCustomOnMapClickListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.mGoogleMap.setMyLocationEnabled(true);
    }

    private void loadSounds() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mSoundPool = new SoundPool.Builder().setMaxStreams(10).build();
        } else {
            this.mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }
        this.mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.d("Test",String.valueOf(sampleId));
                mLoaded = true;
            }
        });
        this.mSoundMap.put(R.raw.sound, this.mSoundPool.load(this, R.raw.sound,1));
    }

    void playSound(int selectedSound){
        int soundID = this.mSoundMap.get(selectedSound);
        this.mSoundPool.play(soundID, 1,1,1,0,1f);
    }

    private class SimpleLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            updateLocationDisplay(location);
            MarkerOptions newMarkerOptions = new MarkerOptions();
            newMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.yourlocationmarker));
            newMarkerOptions.title("You are here!");
            newMarkerOptions.snippet("This is your location.");
            LatLng latlng = new LatLng(location.getLatitude(),location.getLongitude());
            newMarkerOptions.position(latlng);
            mGoogleMap.addMarker(newMarkerOptions);
        }
        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override public void onProviderEnabled(String provider) { }
        @Override public void onProviderDisabled(String provider) {}
    }


    }