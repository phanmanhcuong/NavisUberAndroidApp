package com.example.admin.navisuber;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;

public class TabWaiting extends Fragment {
    private static final String PREFERENCES_NAME = "tokenIdPref";
    private static final String PREFERENCES_PHONENUMBER = "PhoneNumber";
    private static ListView listView;
    private static AdapterHandling adapterWaiting;
    private static GoogleMap googleMap;
    private static Marker carMarker;
    private static ArrayList<Order> orderList;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View tabView = inflater.inflate(R.layout.tab_waiting, container, false);

        listView = tabView.findViewById(R.id.listview_waiting);

        adapterWaiting = new AdapterHandling(this.getActivity());

        listView.setAdapter(adapterWaiting);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int position, long id) {
                //get selected item
                Order selectedOrder = orderList.get(position);
                int currentCarId = selectedOrder.getCarID();

                //dialog fragment for google map
                android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentShowMap fragmentShowMap = FragmentShowMap.newInstance(currentCarId, selectedOrder.getPickupTime());
                fragmentShowMap.show(fm, "map fragment");
            }
        });

        //get data from server to add to adapter
        ConnectToDb connectToDb = new ConnectToDb(this.getActivity());
        connectToDb.execute();

        return tabView;
    }

    private class ConnectToDb extends AsyncTask<Void, Void, ArrayList<Order>> {
        private Context activity;

        public ConnectToDb(Context context) {
            this.activity = context;
        }

        @Override
        protected ArrayList<Order> doInBackground(Void... voids) {

            orderList = new ArrayList<>();

            SharedPreferences sharedPreferencesPhoneNumber = getContext().getSharedPreferences(PREFERENCES_PHONENUMBER, MODE_PRIVATE);
            String phoneNumber = sharedPreferencesPhoneNumber.getString(getResources().getString(R.string.phone_number), null);

            StringBuilder builder = new StringBuilder();
            HttpURLConnection connection;

            String webserviceUrl = this.activity.getResources().getString(R.string.webservice_get_waiting_orders) + phoneNumber + "&status=1";
            URL url;
            try {
                url = new URL(webserviceUrl);
                connection = (HttpURLConnection) url.openConnection();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        builder.append(line);
                    }

                    String responseWaitingOrders = builder.toString();

                    JSONObject jsonResponse = new JSONObject(responseWaitingOrders);
                    JSONArray jsonArray = jsonResponse.getJSONArray("GetOrdersResult");

                    for (int i = 0; i <jsonArray.length(); i++){
                        JSONObject order = jsonArray.getJSONObject(i);

                        int orderID = order.getInt("id_dat_xe");
                        int carID = order.getInt("id_xe");
                        String originPlace = order.getString("diem_bat_dau");
                        String destinationPlace = order.getString("diem_ket_thuc");
                        String orderedString = order.getString("thoi_diem_dat_xe");
                        String pickupString = order.getString("thoi_diem_khoi_hanh");

                        Order waitingOrder = new Order(orderID, carID, originPlace.trim(), destinationPlace.trim(), pickupString, orderedString);
                        orderList.add(waitingOrder);
                    }

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return orderList;
        }

        @Override
        protected void onPostExecute(ArrayList<Order> orderList) {
            adapterWaiting.addList(orderList);
            adapterWaiting.notifyDataSetChanged();
        }
    }
}
