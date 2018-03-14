package com.example.admin.navisuber;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Admin on 3/13/2018.
 */

public class ShowOrderPathActivity extends AppCompatActivity {
    private static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 1;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_order_path);

        SupportMapFragment ggMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_ggmap);
        ggMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap ggMap) {
                askPermissionsAndShowMyLocation();
                googleMap = ggMap;
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                if(ContextCompat.checkSelfPermission(ShowOrderPathActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(ShowOrderPathActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                googleMap.setMyLocationEnabled(true);
            }
        });
    }

    private void askPermissionsAndShowMyLocation() {
        if(Build.VERSION.SDK_INT >= 23){
            //vị trí tương đối
            int accessCoarsePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
            //vị trí chính xác
            int accessFinePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

            if(accessCoarsePermission != PackageManager.PERMISSION_GRANTED || accessFinePermission != PackageManager.PERMISSION_GRANTED){
                String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
                //Dialog
                ActivityCompat.requestPermissions(this, permissions, REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);
                return;
            }
        }

        this.showMyLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_ID_ACCESS_COURSE_FINE_LOCATION){
            //kiểm tra số phần tử của mảng kết quả, = 0 nếu denied
            if(grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Cho phép quyền truy cập !", Toast.LENGTH_SHORT).show();
                this.showMyLocation();
            } else{
                Toast.makeText(this, "Từ chối quyền truy cập !", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showMyLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //String locationProvider = this.getEnabledLocationProvider();
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        if(locationProvider == null) return;
        // Millisecond
        //final long MIN_TIME_BW_UPDATES = 1000;
        // Met
        //final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
        Location myLocation = null;

        try{
            //locationManager.requestLocationUpdates(locationProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener)this);
            myLocation = locationManager.getLastKnownLocation(locationProvider);
        } catch (SecurityException e) {
            Toast.makeText(this, "Show My Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        if(myLocation != null){
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)             // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 40 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            //Marker cho map
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title("My location");
            markerOptions.position(latLng);
            Marker currentMarker = googleMap.addMarker(markerOptions);
            currentMarker.showInfoWindow();
        } else{
            Toast.makeText(this, "Không tìm được vị trí hiện tại", Toast.LENGTH_SHORT).show();
        }
    }

    //tim nhà cung cấp vị trí
    private String getEnabledLocationProvider() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Tiêu chí để tìm một nhà cung cấp vị trí.
        Criteria criteria = new Criteria();

        String bestProvider = locationManager.getBestProvider(criteria, true);
        boolean enabled = locationManager.isProviderEnabled(bestProvider);
        if(!enabled){
            Toast.makeText(this, "Không tìm thấy nhà cung cấp nào!", Toast.LENGTH_LONG).show();
            return null;
        }
        return bestProvider;
    }
}
