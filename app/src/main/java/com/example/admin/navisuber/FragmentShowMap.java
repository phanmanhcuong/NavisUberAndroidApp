package com.example.admin.navisuber;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentShowMap extends DialogFragment {
    int carId;
    String pickupTime;
    private static GoogleMap googleMap;

    public static FragmentShowMap newInstance(int carId, String pickupTime){
        FragmentShowMap frag = new FragmentShowMap();
        Bundle bundle = new Bundle();
        bundle.putInt("carId", carId);
        bundle.putString("pickupTime", pickupTime);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_show_vehicle_on_map_layout, container, false);

        Bundle bundle = this.getArguments();
        if(bundle != null){
            carId = bundle.getInt("carId");
            pickupTime = bundle.getString("pickupTime");
        }

        //set up for google map
        SupportMapFragment ggMapFragment = (SupportMapFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_ggmap);

        ggMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap ggMap) {
                googleMap = ggMap;
                //askPermissionsAndShowMyLocation();
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                if(ContextCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    googleMap.setMyLocationEnabled(true);

            }
        });

        //GetCarLocation getCarLocation = new GetCarLocation(view.getContext(), carId);

        //timer to request car's location and update every 5 seconds
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        GetCarLocation getCarLocation = new GetCarLocation(view.getContext(), carId);
                        getCarLocation.execute();
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 5000);

        return view;
    }

    //fix map dialog size
    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = 1500;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    //remove dilog when click outside
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SupportMapFragment ggMapFragment = (SupportMapFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_ggmap);

        if (ggMapFragment != null)
            getFragmentManager().beginTransaction().remove(ggMapFragment).commit();
    }

    private static class GetCarLocation extends AsyncTask<Void, Void, HashMap<String, Double>> {
        private Context context;
        private int carId;

        private GetCarLocation(Context context, int carId) {
            this.context = context;
            this.carId = carId;
        }

        //get car location to pass to onPostExecute()
        @Override
        protected HashMap<String, Double> doInBackground(Void... voids) {
            HashMap<String, Double> carLocation = new HashMap<>();

            StringBuilder builder = new StringBuilder();
            HttpURLConnection connection;

            try {
                String webserviceUrl = context.getResources().getString(R.string.webservice_get_car_location) + this.carId;
                URL url = new URL(webserviceUrl);
                connection = (HttpURLConnection) url.openConnection();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        builder.append(line);
                    }

                    String responseCarLocation = builder.toString();

                    JSONObject jsonResponse = new JSONObject(responseCarLocation);
                    JSONArray jsonArray = jsonResponse.getJSONArray("GetCarLocationResult");

                    JSONObject carlatlng = jsonArray.getJSONObject(0);
                    double latitude = carlatlng.getDouble("latitude");
                    double longitude = carlatlng.getDouble("longitude");
                    carLocation.put("carLatitude", latitude);
                    carLocation.put("carLongitude", longitude);

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return carLocation;
        }

            //update map on UI
        @Override
        protected void onPostExecute(HashMap<String, Double> carLocation) {
            double latitude = carLocation.get("carLatitude");
            double longitude = carLocation.get("carLongitude");
            LatLng carLatLng = new LatLng(latitude, longitude);
            //show car
            Marker carMarker;
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car_24px));
            markerOptions.position(carLatLng);
            carMarker = googleMap.addMarker(markerOptions);
            carMarker.showInfoWindow();

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(carLatLng)             // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }
}
