package hr.ferit.mdudjak.whereami;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,View.OnClickListener {
    private static final int REQUEST_LOCATION_PERMISSION = 10;
    TextView tvLocationDisplay;
    TextView tvLocation;
    Button bCaptureImage;
    LocationListener mLocationListener;
    LocationManager mLocationManager;
    String mCurrentPhotoPath;
    GoogleMap mGoogleMap;
    StringBuilder stringBuilder;
    public static final String MSG_KEY = "message";
    MapFragment mMapFragment;
    HashMap<Integer, Integer> mSoundMap = new HashMap<>();
    SoundPool mSoundPool;
    Uri photoURI;
    static final int REQUEST_TAKE_PHOTO = 1;
    boolean mLoaded = false;
    static final int REQUEST_IMAGE_CAPTURE = 1;
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

    private void sendNotification() {
        String msgText = String.valueOf(stringBuilder);
// We need a pending intent to kick off when the notification is pressed:
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, photoURI);
        notificationIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
// Compat builder should be used to create the notification when working
// with api level 15 and lower
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setAutoCancel(true)
                .setContentTitle("Where am I?")
                .setContentText(msgText)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentIntent(notificationPendingIntent)
                .setLights(Color.BLUE, 2000, 1000)
                .setVibrate(new long[]{1000,1000,1000,1000,1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        Notification notification = notificationBuilder.build();
// When you have a notification, call notify on the notification manager object
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);
// When the notification is sent, this activity is no longer neccessary
        this.finish();
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
        this.bCaptureImage= (Button) this.findViewById(R.id.bCaptureImage);
        this.bCaptureImage.setOnClickListener(this);
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
        stringBuilder = new StringBuilder();
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

    @Override
    public void onClick(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }

        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        sendNotification();
    }
        private File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + stringBuilder + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
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