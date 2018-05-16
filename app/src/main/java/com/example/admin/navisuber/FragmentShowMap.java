package com.example.admin.navisuber;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentShowMap extends DialogFragment {
    int carId;
    int orderId;
    String pickupTime;
    private static GoogleMap googleMap;

    public static FragmentShowMap newInstance(int carId, int orderId, String pickupTime){
        FragmentShowMap frag = new FragmentShowMap();
        Bundle bundle = new Bundle();
        bundle.putInt("carId", carId);
        bundle.putInt("orderId", orderId);
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
            orderId = bundle.getInt("orderId");
            pickupTime = bundle.getString("pickupTime");
        }

        Button btnCancel = view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new CancelOrderListener());
        //set up for google map
        SupportMapFragment ggMapFragment = (SupportMapFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_ggmap);

        ggMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap ggMap) {
                googleMap = ggMap;
                googleMap.clear();

                //askPermissionsAndShowMyLocation();
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                if(ContextCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    googleMap.setMyLocationEnabled(true);

            }
        });

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

    //cancel button on click
    private class CancelOrderListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.cancel_order));
            builder.setMessage("Bạn muốn hủy chuyến xe này không ?");
            builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SendCancelOrder sendCancelOrder = new SendCancelOrder(orderId);
                    sendCancelOrder.execute();
                }
            });
            builder.show();


        }
    }

    private class SendCancelOrder extends AsyncTask<Void, Void, String>{
        private int orderId;

        public SendCancelOrder(int orderId) {
            this.orderId = orderId;
        }

        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder builder = new StringBuilder();
            HttpURLConnection connection;

            String webserviceUrl = getString(R.string.webservice_cancel_order);
            URL url;
            try {
                url = new URL(webserviceUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //OutputStream
                OutputStream os = connection.getOutputStream();
                BufferedWriter streamWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                streamWriter.write(ConvertMapObjectToEncodeUrl(orderId));

                streamWriter.flush();
                streamWriter.close();
                os.close();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    if ((line = bufferedReader.readLine()) != null) {
                        builder.append(line);
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //response
            if (builder == null) {
                return null;
            } else {
                return builder.toString();
            }
        }

        @Override
        protected void onPostExecute(String response) {
            AlertDialog.Builder responseBuilder = new AlertDialog.Builder(getActivity());
            responseBuilder.setTitle(getResources().getString(R.string.cancel_response));
            responseBuilder.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            //close this map fragment

            DialogFragment showmapFragment  = (DialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag("map fragment");
            showmapFragment.dismiss();
            //show response dialog
            if (response.contains(getResources().getString(R.string.success_response))) {
                responseBuilder.setMessage(getResources().getString(R.string.cancel_success));

            } else {
                responseBuilder.setMessage(getResources().getString(R.string.cancel_fail));
            }
            responseBuilder.show();
        }

        private String ConvertMapObjectToEncodeUrl(int orderId) {
            StringBuilder result = new StringBuilder();
            try {
                result.append(URLEncoder.encode("orderId", "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(String.valueOf(orderId), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return result.toString();
        }
    }
}
