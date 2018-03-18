package com.example.admin.navisuber;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


/**
 * Created by Admin on 3/13/2018.
 */

public class ShowOrderPathActivity extends AppCompatActivity {
    private static String phoneNumber;
    private static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 1;
    private static GoogleMap googleMap;
    private static Polyline line;
    private Marker markerOrigin, markerDestination;
    private static String originLocation;
    private static String destinationLocation;
    private static RouteWrapper sRouteWrapper;
    private static LatLngBounds sLatLngBounds;
    private String placeOrigin;
    private String placeDestination;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_order_path);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            phoneNumber = bundle.getString(getResources().getString(R.string.phone_number));
        }

        SupportMapFragment ggMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_ggmap);
        ggMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap ggMap) {
                googleMap = ggMap;
                askPermissionsAndShowMyLocation();
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                if(ContextCompat.checkSelfPermission(ShowOrderPathActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(ShowOrderPathActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                googleMap.setMyLocationEnabled(true);
            }
        });

        PlaceAutocompleteFragment autocompleteOrigin = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_origin);
        autocompleteOrigin.setHint(getResources().getString(R.string.origin));
        //dùng cho cả 2 fragment
        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder().setCountry("VN").build();
        autocompleteOrigin.setFilter(autocompleteFilter);
        autocompleteOrigin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                placeDestination = place.getName().toString();

                if(markerOrigin != null) markerOrigin.remove();

                LatLng latLngOrigin = place.getLatLng();
                originLocation = String.valueOf(place.getName());

                //Marker cho map
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLngOrigin);
                markerOrigin = googleMap.addMarker(markerOptions);
                markerOrigin.showInfoWindow();

                //đặt camera đến điểm Origin nếu chưa có điểm Đón
                if(markerDestination == null){
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLngOrigin)             // Sets the center of the map to location user
                            .zoom(15)                   // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
                //Nếu đã có điểm đón thì vẽ đường đi
                else{
                    ShowDirectionTask showDirectionTask = new ShowDirectionTask();
                    showDirectionTask.execute();
                }
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(ShowOrderPathActivity.this, "error" + status, Toast.LENGTH_LONG).show();
            }
        });

        PlaceAutocompleteFragment autocompleteDestination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_destination);
        autocompleteDestination.setHint(getResources().getString(R.string.destination));
        autocompleteDestination.setFilter(autocompleteFilter);
        autocompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                placeOrigin = place.getName().toString();

                if(markerDestination != null) markerDestination.remove();

                LatLng latLngDestination = place.getLatLng();
                destinationLocation = String.valueOf(place.getName());

                //Marker cho map
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title("Điểm đến");
                markerOptions.position(latLngDestination);
                markerDestination = googleMap.addMarker(markerOptions);
                markerDestination.showInfoWindow();

                if(markerOrigin == null){
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLngDestination)             // Sets the center of the map to location user
                            .zoom(15)                   // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
                else{
                    ShowDirectionTask showDirectionTask = new ShowDirectionTask();
                    showDirectionTask.execute();
                }
            }

            @Override
            public void onError(Status status) {

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
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        Location myLocation;

        try{
            myLocation = locationManager.getLastKnownLocation(locationProvider);
        } catch (SecurityException e) {
            Toast.makeText(this, "Show My Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        if(myLocation != null){
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
            if(sLatLngBounds == null){
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)             // Sets the center of the map to location user
                        .zoom(15)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
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

    //phải encode tên điểm đến và điểm đón
    private String makeGoogleMapDirectionRequest(String originLocation, String destinationLocation) {
        StringBuilder stringRequest = new StringBuilder();
        stringRequest.append("https://maps.googleapis.com/maps/api/directions/json");
        stringRequest.append("?origin=");
        try {
            String originEncode = URLEncoder.encode(originLocation, "utf-8");
            String destinationEncode = URLEncoder.encode(destinationLocation, "utf-8");
            stringRequest.append(originEncode);
            stringRequest.append("&destination=");
            stringRequest.append(destinationEncode);
            stringRequest.append("&key=");
            stringRequest.append(getResources().getString(R.string.google_api_key));
            return stringRequest.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private RouteWrapper getDirectionLatLng(String googleMapDirectionRequest) {
        ArrayList<LatLng> listLatLng = new ArrayList<>();
        try {
            URL url = new URL(googleMapDirectionRequest);
            InputStreamReader reader = new InputStreamReader(url.openStream(), "UTF-8");

            Direction results = new Gson().fromJson(reader, Direction.class);
            Direction.Route[] routes = results.getRoutes();

            //get bound
            Direction.Bound bounds = routes[0].getBound();
            Direction.Bound.Northeast northeast = bounds.getNortheast();
            Direction.Bound.Southwest southwest = bounds.getSouthwest();
            LatLng latLngNortheast = new LatLng(northeast.getLat(), northeast.getLng());
            LatLng latLngSouthwest = new LatLng(southwest.getLat(), southwest.getLng());
            listLatLng.add(latLngNortheast);
            listLatLng.add(latLngSouthwest);

            //get leg
            Direction.Leg[] legs = routes[0].getLegs();
            Direction.Leg.Distance distance = legs[0].getDistance();
            Direction.Leg.Duration duration = legs[0].getDuration();

            Direction.Leg.Step[] steps = legs[0].getSteps();
            for(Direction.Leg.Step step : steps){
                LatLng latLngOrigin = new LatLng(step.getstart_location().getLat(), step.getstart_location().getLng());
                LatLng latLngDestination = new LatLng(step.getend_location().getLat(), step.getend_location().getLng());
                listLatLng.add(latLngOrigin);
                listLatLng.add(latLngDestination);
            }

            return new RouteWrapper(listLatLng, distance, duration);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class ShowDirectionTask extends AsyncTask<Void, Void, RouteWrapper> {
        @Override
        protected RouteWrapper doInBackground(Void... params) {
            String googleMapDirectionRequest = makeGoogleMapDirectionRequest(originLocation, destinationLocation);
            RouteWrapper routeWrapper = getDirectionLatLng(googleMapDirectionRequest);
            return routeWrapper;
        }

        @Override
        protected void onPostExecute(RouteWrapper routeWrapper ){
            super.onPostExecute(routeWrapper);

            ArrayList<LatLng> listLatLng = routeWrapper.getArrayListLatLng();
            LatLng latLngNortheast = listLatLng.get(0);
            LatLng latLngSouthwest = listLatLng.get(1);
            listLatLng.remove(0);
            listLatLng.remove(0);
            LatLngBounds latLngBounds = new LatLngBounds(latLngSouthwest, latLngNortheast);

            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(listLatLng);
            if(line != null){
                line.remove();
            }
            line = googleMap.addPolyline(polylineOptions);
            line.setColor(Color.RED);
            line.setWidth(10);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(), 12));

            //hiển thị quãng đường và thời gian di chuyển
            String distance = routeWrapper.getDistance().getText();
            String duration = routeWrapper.getDuration().getText();
            TextView tvDistanceDuration = (TextView)findViewById(R.id.tv_distance_duration);
            tvDistanceDuration.setText(duration + " (" + distance + ")");

            ImageButton imageButton = (ImageButton)findViewById(R.id.btn_detail_order);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DetailCarOrder(placeOrigin, placeDestination, phoneNumber);
                }
            });
            
            LinearLayout lnBottomMenu = (LinearLayout)findViewById(R.id.ln_bottom_menu);
            lnBottomMenu.setVisibility(View.VISIBLE);

            //giữ data khi xoay màn hình
            sRouteWrapper = routeWrapper;
            sLatLngBounds = latLngBounds;

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable(getResources().getString(R.string.latlng_bound), sLatLngBounds);
        if(sRouteWrapper != null){
            outState.putSerializable(getResources().getString(R.string.latlng_arraylist), sRouteWrapper.getArrayListLatLng());
            outState.putString(getResources().getString(R.string.duration), sRouteWrapper.getDuration().getText());
            outState.putString(getResources().getString(R.string.distance), sRouteWrapper.getDistance().getText());
        }
}

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        LatLngBounds latLngBounds = savedInstanceState.getParcelable(getResources().getString(R.string.latlng_bound));
        ArrayList<LatLng> latLngArrayList = (ArrayList<LatLng>) savedInstanceState.getSerializable(getResources().getString(R.string.latlng_arraylist));
        String duration = savedInstanceState.getString(getResources().getString(R.string.duration));
        String distance = savedInstanceState.getString(getResources().getString(R.string.distance));

        PolylineOptions polylineOptions = new PolylineOptions();
        // khi chưa nhập điểm đến hay điểm đón mà xoay màn hình thì latLngArrayList sẽ null
        if(latLngArrayList != null){
            polylineOptions.addAll(latLngArrayList);
            if(line != null){
                line.remove();
            }
            line = googleMap.addPolyline(new PolylineOptions()
                    .addAll(latLngArrayList)
                    .width(10)
                    .color(Color.RED));
//        line = googleMap.addPolyline(polylineOptions);
//        line.setColor(Color.RED);
//        line.setWidth(10);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(), 12));

            TextView tvDistanceDuration = (TextView)findViewById(R.id.tv_distance_duration);
            tvDistanceDuration.setText(duration + " (" + distance + ")");

            ImageButton imageButton = (ImageButton)findViewById(R.id.btn_detail_order);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DetailCarOrder(placeOrigin, placeDestination, phoneNumber);
                }
            });

            LinearLayout lnBottomMenu = (LinearLayout)findViewById(R.id.ln_bottom_menu);
            lnBottomMenu.setVisibility(View.VISIBLE);
        }
    }

    //chuyển data sang qua intent sang màn hình detail car order
    private void DetailCarOrder(String placeOrigin, String placeDestination, String phoneNumber) {
        Intent detailCarOrderIntent = new Intent(ShowOrderPathActivity.this, DetailCarOrderActivity.class);
        detailCarOrderIntent.putExtra(getResources().getString(R.string.origin), placeOrigin);
        detailCarOrderIntent.putExtra(getResources().getString(R.string.destination), placeDestination);
        detailCarOrderIntent.putExtra(getResources().getString(R.string.phone_number), phoneNumber);
        startActivity(detailCarOrderIntent);
    }

}
