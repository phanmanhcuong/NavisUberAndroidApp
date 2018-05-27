package com.example.admin.navisuber;

import android.Manifest;
import android.content.DialogInterface;
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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Admin on 3/13/2018.
 */

public class ShowOrderPathActivity extends AppCompatActivity {
    private static String phoneNumber;
    private static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 1;
    private static final int ORIGIN_ON_CLEAR_BUTTON_CLICKED = 1;
    private static final int DESTINATION_ON_CLEAR_BUTTON_CLICKED = 2;
    private static GoogleMap googleMap;
    private static Polyline line;
    private Marker markerDestination;
    private Marker markerOrigin;
    private static String placeOrigin;
    private static String placeDestination;
    private static LatLng destinationLatlng;
    private static LatLng originLatlng;
    private static String carType;
    private static String pickupTime;
    private static RouteWrapper sRouteWrapper;
    private static LatLngBounds sLatLngBounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_order_path);

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();
        if(bundle != null){
            //phoneNumber = bundle.getString(getResources().getString(R.string.phone_number));
            placeOrigin = bundle.getString(getResources().getString(R.string.origin));
            originLatlng = bundle.getParcelable(getResources().getString(R.string.origin_latlng));
            placeDestination = bundle.getString(getResources().getString(R.string.destination));
            destinationLatlng = bundle.getParcelable(getResources().getString(R.string.destination_latlng));
            carType = bundle.getString(getResources().getString(R.string.car_type));
            pickupTime = bundle.getString(getResources().getString(R.string.pick_up_time));
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

                //set camera to Ha Noi
                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        LatLngBounds HNBound = new LatLngBounds(new LatLng(20.9950991, 105.7974815), new LatLng(21.0503801, 105.8764459));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(HNBound, 5));
                    }
                });


                //set camera to origin or destination or show path if exist
                SetCamera();
            }
        });

        final PlaceAutocompleteFragment autocompleteOrigin = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_origin);
        //dùng cho cả 2 fragment
        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder().setCountry("VN").build();
        autocompleteOrigin.setFilter(autocompleteFilter);
        autocompleteOrigin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                placeOrigin = place.getName().toString();
                originLatlng = place.getLatLng();
                
                //đặt camera đến điểm Origin nếu chưa có điểm Đón
                if(markerOrigin != null) markerOrigin.remove();

                //Marker cho map
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(getResources().getString(R.string.origin));
                markerOptions.position(originLatlng);
                markerOrigin = googleMap.addMarker(markerOptions);
                markerOrigin.showInfoWindow();

                if(placeDestination == null){
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(originLatlng)             // Sets the center of the map to location user
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
                Toast.makeText(ShowOrderPathActivity.this, "error" + status, Toast.LENGTH_LONG).show();
            }
        });

        autocompleteOrigin.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaceAutocompleteClearButtonListener placeAutocompleteClearButtonListener = new PlaceAutocompleteClearButtonListener(ORIGIN_ON_CLEAR_BUTTON_CLICKED);
                placeAutocompleteClearButtonListener.execute();

            }
        });


        final PlaceAutocompleteFragment autocompleteDestination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_destination);
        autocompleteDestination.setFilter(autocompleteFilter);
        autocompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if(markerDestination != null) markerDestination.remove();

                placeDestination = String.valueOf(place.getName());
                destinationLatlng = place.getLatLng();

                //Marker cho map
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(getResources().getString(R.string.destination));
                markerOptions.position(destinationLatlng);
                markerDestination = googleMap.addMarker(markerOptions);
                markerDestination.showInfoWindow();

                if(placeOrigin == null){
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(destinationLatlng)             // Sets the center of the map to location user
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

        autocompleteDestination.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlaceAutocompleteClearButtonListener placeAutocompleteClearButtonListener = new PlaceAutocompleteClearButtonListener(DESTINATION_ON_CLEAR_BUTTON_CLICKED);
                    placeAutocompleteClearButtonListener.execute();
                }
            });

//        //show path nếu đã điền đủ điểm đón và điểm đến ở màn hình detail order, nếu chỉ có 1 trong 2 điểm thì move camera
//        if(placeOrigin != null && placeDestination != null){
//            autocompleteOrigin.setText(placeOrigin);
//            autocompleteDestination.setText(placeDestination);
//
//            ShowDirectionTask showDirectionTask = new ShowDirectionTask();
//            showDirectionTask.execute();
//        } else if (placeOrigin != null){
//            autocompleteOrigin.setText(placeOrigin);
//
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.title(getResources().getString(R.string.origin));
//            markerOptions.position(originLatlng);
//            markerOrigin = googleMap.addMarker(markerOptions);
//            markerOrigin.showInfoWindow();
//
//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .target(originLatlng)             // Sets the center of the map to location user
//                    .zoom(15)                   // Sets the zoom
//                    .build();                   // Creates a CameraPosition from the builder
//            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        } else if (placeDestination != null){
//            autocompleteOrigin.setText(placeDestination);
//
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.title(getResources().getString(R.string.destination));
//            markerOptions.position(destinationLatlng);
//            markerDestination = googleMap.addMarker(markerOptions);
//            markerDestination.showInfoWindow();
//
//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .target(destinationLatlng)             // Sets the center of the map to location user
//                    .zoom(15)                   // Sets the zoom
//                    .build();                   // Creates a CameraPosition from the builder
//            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        } else{
//            autocompleteOrigin.setHint(getResources().getString(R.string.origin));
//            autocompleteDestination.setHint(getResources().getString(R.string.destination));
//        }
    }

    //set camera to origin or destination or show path if exist
    private void SetCamera() {
        //show path nếu đã điền đủ điểm đón và điểm đến ở màn hình detail order, nếu chỉ có 1 trong 2 điểm thì move camera
        PlaceAutocompleteFragment autocompleteOrigin = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_origin);
        PlaceAutocompleteFragment autocompleteDestination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_destination);

        if(placeOrigin != null && placeDestination != null){
            autocompleteOrigin.setText(placeOrigin);
            autocompleteDestination.setText(placeDestination);

            ShowDirectionTask showDirectionTask = new ShowDirectionTask();
            showDirectionTask.execute();
        } else if (placeOrigin != null){
            autocompleteOrigin.setText(placeOrigin);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title(getResources().getString(R.string.origin));
            markerOptions.position(originLatlng);
            markerOrigin = googleMap.addMarker(markerOptions);
            markerOrigin.showInfoWindow();

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(originLatlng)             // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else if (placeDestination != null){
            autocompleteDestination.setText(placeDestination);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.title(getResources().getString(R.string.destination));
            markerOptions.position(destinationLatlng);
            markerDestination = googleMap.addMarker(markerOptions);
            markerDestination.showInfoWindow();

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(destinationLatlng)             // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else{
            autocompleteOrigin.setHint(getResources().getString(R.string.origin));
            autocompleteDestination.setHint(getResources().getString(R.string.destination));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_show_path, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.btn_detail_order:
                DetailCarOrder(placeOrigin, originLatlng, placeDestination, destinationLatlng, carType, pickupTime);
                break;

            case R.id.btn_order_info:
                Intent orderStatusIntent = new Intent(ShowOrderPathActivity.this, OrderStatusActivity.class);
                if(placeOrigin != null){
                    orderStatusIntent.putExtra(getString(R.string.origin), placeOrigin);
                    orderStatusIntent.putExtra(getString(R.string.origin_latlng), originLatlng);
                }

                if(placeDestination != null){
                    orderStatusIntent.putExtra(getString(R.string.destination), placeDestination);
                    orderStatusIntent.putExtra(getString(R.string.destination_latlng), destinationLatlng);
                }

                if(carType != null){
                    orderStatusIntent.putExtra(getString(R.string.car_type), carType);
                }

                if(pickupTime != null){
                    orderStatusIntent.putExtra(getString(R.string.pick_up_time), pickupTime);
                }
                startActivity(orderStatusIntent);
                break;

            case R.id.btn_edit_user:
                Intent editUserIntent = new Intent(ShowOrderPathActivity.this, SignUpActivity.class);
                startActivity(editUserIntent);
        }
        return true;
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
            }
        }

        //this.showMyLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_ID_ACCESS_COURSE_FINE_LOCATION){
            //kiểm tra số phần tử của mảng kết quả, = 0 nếu denied
            if(grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Cho phép quyền truy cập !", Toast.LENGTH_SHORT).show();
                //this.showMyLocation();
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
            Toast.makeText(this, "Không tìm được vị trí hiện tại do bạn không bật định vị !", Toast.LENGTH_SHORT).show();
        }
    }

    //phải encode tên điểm đến và điểm đón
    private String makeGoogleMapDirectionRequest(String placeOrigin, String placeDestination) {
        StringBuilder stringRequest = new StringBuilder();
        stringRequest.append("https://maps.googleapis.com/maps/api/directions/json");
        stringRequest.append("?origin=");
        try {
            String originEncode = URLEncoder.encode(placeOrigin, "utf-8");
            String destinationEncode = URLEncoder.encode(placeDestination, "utf-8");
            stringRequest.append(originEncode);
            stringRequest.append("&destination=");
            stringRequest.append(destinationEncode);
            stringRequest.append("&key=");
            stringRequest.append(getResources().getString(R.string.google_api_key_map));
            return stringRequest.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    //lấy tọa độ các điểm để vẽ đường đi
    private RouteWrapper getDirectionLatLng(String googleMapDirectionRequest) {
        ArrayList<LatLng> listLatLngBound = new ArrayList<>();
        ArrayList<String> pointsList = new ArrayList<String>();

        try {
            URL url = new URL(googleMapDirectionRequest);
            InputStreamReader reader = new InputStreamReader(url.openStream(), "UTF-8");

            Direction results = new Gson().fromJson(reader, Direction.class);
            Direction.Route[] routes = results.getRoutes();

            //get bound
            if(routes.length == 0){
                return null;
            }
            Direction.Bound bounds = routes[0].getBound();
            Direction.Bound.Northeast northeast = bounds.getNortheast();
            Direction.Bound.Southwest southwest = bounds.getSouthwest();
            LatLng latLngNortheast = new LatLng(northeast.getLat(), northeast.getLng());
            LatLng latLngSouthwest = new LatLng(southwest.getLat(), southwest.getLng());
            listLatLngBound.add(latLngNortheast);
            listLatLngBound.add(latLngSouthwest);

            //get leg
            Direction.Leg[] legs = routes[0].getLegs();
            Direction.Leg.Distance distance = legs[0].getDistance();
            Direction.Leg.Duration duration = legs[0].getDuration();

            Direction.Leg.Step[] steps = legs[0].getSteps();
            for(Direction.Leg.Step step : steps){
                LatLng latLngOrigin = new LatLng(step.getstart_location().getLat(), step.getstart_location().getLng());
                LatLng latLngDestination = new LatLng(step.getend_location().getLat(), step.getend_location().getLng());
                listLatLngBound.add(latLngOrigin);
                listLatLngBound.add(latLngDestination);

                pointsList.add(step.getPolyline().getPoints());
            }

            ArrayList<LatLng> latLngList = new ArrayList<>();
            for (String point : pointsList){
                List<LatLng> latLngListPoint = PolyUtil.decode(point);
                latLngList.addAll(latLngListPoint);
            }

            return new RouteWrapper(listLatLngBound, latLngList, distance, duration);
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
            String googleMapDirectionRequest = makeGoogleMapDirectionRequest(placeOrigin, placeDestination);
            RouteWrapper routeWrapper = getDirectionLatLng(googleMapDirectionRequest);
            return routeWrapper;
        }

        @Override
        protected void onPostExecute(RouteWrapper routeWrapper ){
            super.onPostExecute(routeWrapper);

            //marker khi đc back từ màn hình detail order
            if(markerOrigin == null){
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(getResources().getString(R.string.origin));
                markerOptions.position(originLatlng);
                markerOrigin = googleMap.addMarker(markerOptions);
                markerOrigin.showInfoWindow();
            }

            if(markerDestination == null){
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(getResources().getString(R.string.destination));
                markerOptions.position(destinationLatlng);
                markerDestination = googleMap.addMarker(markerOptions);
                markerDestination.showInfoWindow();
            }

            if(routeWrapper == null){
                if(placeOrigin != null && placeDestination != null){
                    Toast.makeText(ShowOrderPathActivity.this, getResources().getString(R.string.route_not_found), Toast.LENGTH_LONG).show();
                    return;
                }else {
                    return;
                }
            }
            ArrayList<LatLng> listLatLngBound = routeWrapper.getLatLngBound();

            //bound
            LatLng latLngNortheast = listLatLngBound.get(0);
            LatLng latLngSouthwest = listLatLngBound.get(1);
            LatLngBounds latLngBounds = new LatLngBounds(latLngSouthwest, latLngNortheast);

            //decode ra list<LatLng> từ List<String> của point trong polyline
            ArrayList<LatLng> listLatLngPoint = routeWrapper.getArrayListPolyline();

            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(listLatLngPoint);
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
//
//            ImageButton imageButton = (ImageButton)findViewById(R.id.btn_detail_order);
//            imageButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    DetailCarOrder(placeOrigin, placeDestination, phoneNumber);
//                }
//            });

            //giữ data khi xoay màn hình
            sRouteWrapper = routeWrapper;

        }
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState){
//        super.onSaveInstanceState(outState);
//        outState.putParcelable(getResources().getString(R.string.latlng_bound), sLatLngBounds);
//        if(sRouteWrapper != null){
//            outState.putSerializable(getResources().getString(R.string.list_lat_lng_bound), sRouteWrapper.getLatLngBound());
//            outState.putSerializable(getResources().getString(R.string.latlng_point_arraylist), sRouteWrapper.getArrayListPolyline());
//            outState.putString(getResources().getString(R.string.duration), sRouteWrapper.getDuration().getText());
//            outState.putString(getResources().getString(R.string.distance), sRouteWrapper.getDistance().getText());
//        }
//}
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        LatLngBounds latLngBounds = savedInstanceState.getParcelable(getResources().getString(R.string.list_lat_lng_bound));
//        ArrayList<LatLng> latLngPointArrayList = (ArrayList<LatLng>) savedInstanceState.getSerializable(getResources().getString(R.string.latlng_point_arraylist));
//        String duration = savedInstanceState.getString(getResources().getString(R.string.duration));
//        String distance = savedInstanceState.getString(getResources().getString(R.string.distance));
//
//        PolylineOptions polylineOptions = new PolylineOptions();
//        // khi chưa nhập điểm đến hay điểm đón mà xoay màn hình thì latLngArrayList sẽ null
//        if(latLngPointArrayList != null){
//            polylineOptions.addAll(latLngPointArrayList);
//            if(line != null){
//                line.remove();
//            }
//            line = googleMap.addPolyline(new PolylineOptions()
//                    .addAll(latLngPointArrayList)
//                    .width(10)
//                    .color(Color.RED));
////        line = googleMap.addPolyline(polylineOptions);
////        line.setColor(Color.RED);
////        line.setWidth(10);
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(), 12));
//
//            TextView tvDistanceDuration = (TextView)findViewById(R.id.tv_distance_duration);
//            tvDistanceDuration.setText(duration + " (" + distance + ")");
//
//            ImageButton imageButton = (ImageButton)findViewById(R.id.btn_detail_order);
//            imageButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    DetailCarOrder(placeOrigin, placeDestination, destinationLatlng, phoneNumber);
//                }
//            });
//
//            LinearLayout lnBottomMenu = (LinearLayout)findViewById(R.id.ln_bottom_menu);
//            lnBottomMenu.setVisibility(View.VISIBLE);
//        }
//    }

    //chuyển data qua intent sang màn hình detail car order
    private void DetailCarOrder(final String placeOrigin, final LatLng originLatlng, final String placeDestination, final LatLng destinationLatlng, final String carType, final String pickupTime) {
        final Intent detailCarOrderIntent = new Intent(ShowOrderPathActivity.this, DetailCarOrderActivity.class);
        //detailCarOrderIntent.putExtra(getResources().getString(R.string.phone_number), phoneNumber);

        //nếu chưa nhập điểm đón hoặc điểm đón thì confirm xem có muốn chuyển màn hình ko
        if(placeOrigin == null || placeDestination == null){
            final AlertDialog.Builder builder = new AlertDialog.Builder(ShowOrderPathActivity.this);
            builder.setTitle(getResources().getString(R.string.alert_dialog_title));
            builder.setMessage(getResources().getString(R.string.alert_dialog_message));

            builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(placeOrigin != null){
                        detailCarOrderIntent.putExtra(getResources().getString(R.string.origin), placeOrigin);
                        detailCarOrderIntent.putExtra(getResources().getString(R.string.origin_latlng), originLatlng);
                    }

                    if(placeDestination != null){
                        detailCarOrderIntent.putExtra(getResources().getString(R.string.destination), placeDestination);
                        detailCarOrderIntent.putExtra(getResources().getString(R.string.destination_latlng), destinationLatlng);
                    }

                    detailCarOrderIntent.putExtra(getResources().getString(R.string.car_type), carType);
                    detailCarOrderIntent.putExtra(getResources().getString(R.string.pick_up_time), pickupTime);

                    startActivity(detailCarOrderIntent);
                }
            });
            builder.show();
        } else{
            detailCarOrderIntent.putExtra(getResources().getString(R.string.origin), placeOrigin);
            detailCarOrderIntent.putExtra(getResources().getString(R.string.origin_latlng), originLatlng);
            detailCarOrderIntent.putExtra(getResources().getString(R.string.destination), placeDestination);
            detailCarOrderIntent.putExtra(getResources().getString(R.string.destination_latlng), destinationLatlng);
            detailCarOrderIntent.putExtra(getResources().getString(R.string.car_type), carType);
            detailCarOrderIntent.putExtra(getResources().getString(R.string.pick_up_time), pickupTime);

            startActivity(detailCarOrderIntent);
        }
    }

    //xóa điểm đến hoặc điểm đón ngay khi ấn dấu x
    private class PlaceAutocompleteClearButtonListener extends AsyncTask<Void, Void, Void> {
            private int flag;

            public PlaceAutocompleteClearButtonListener(int flag) {
                this.flag = flag;
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if(flag == ORIGIN_ON_CLEAR_BUTTON_CLICKED){
                    placeOrigin = null;
                    originLatlng = null;
                } else{
                    placeDestination = null;
                    destinationLatlng = null;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void params){
                super.onPostExecute(params);

                if(flag == ORIGIN_ON_CLEAR_BUTTON_CLICKED){
                    PlaceAutocompleteFragment autocompleteOrigin = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_origin);
                    autocompleteOrigin.setText("");
                    autocompleteOrigin.setHint(getResources().getString(R.string.origin));
                    markerOrigin.remove();
                } else{
                    PlaceAutocompleteFragment autocompleteDestination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_destination);
                    autocompleteDestination.setText("");
                    autocompleteDestination.setHint(getResources().getString(R.string.destination));
                    markerDestination.remove();
                }

                if (line != null) {
                    line.remove();
                }

            }

    }
}
