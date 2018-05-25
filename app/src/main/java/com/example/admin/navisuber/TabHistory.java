package com.example.admin.navisuber;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class TabHistory extends Fragment {
    private static final String PREFERENCES_PHONENUMBER = "PhoneNumber";
    private static String phoneNumber;
    private static ListView listView;
    private static AdapterWaiting adapterWaiting;
    private static ArrayList<Order> orderList;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View tabView = inflater.inflate(R.layout.tab_waiting, container, false);

        listView = tabView.findViewById(R.id.listview_waiting);

        adapterWaiting = new AdapterWaiting(this.getActivity());

        listView.setAdapter(adapterWaiting);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int position, long id) {
                //get selected item
                Order selectedOrder = orderList.get(position);
                int currentCarId = selectedOrder.getCarID();
                int orderId = selectedOrder.getOrderID();

                //dialog fragment for google map
                android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentShowMap fragmentShowMap = FragmentShowMap.newInstance(currentCarId, orderId, selectedOrder.getPickupTime());
                fragmentShowMap.show(fm, "map fragment");
            }
        });

        //get data from server to add to adapter
        ConnectToDb connectToDb = new ConnectToDb(this.getActivity());
        connectToDb.execute();

        SharedPreferences sharedPreferencesPhoneNumber = getContext().getSharedPreferences(PREFERENCES_PHONENUMBER, MODE_PRIVATE);
        phoneNumber = sharedPreferencesPhoneNumber.getString(getResources().getString(R.string.phone_number), null);

        return tabView;
    }

    public static class ConnectToDb extends AsyncTask<Void, Void, ArrayList<Order>> {
        private Context activity;

        public ConnectToDb(Context context) {
            this.activity = context;
        }

        @Override
        protected ArrayList<Order> doInBackground(Void... voids) {
            orderList = new ArrayList<>();

            StringBuilder builder = new StringBuilder();
            HttpURLConnection connection;

            String webserviceUrl = this.activity.getResources().getString(R.string.webservice_get_waiting_orders) + phoneNumber + "&status=2";
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
                        String driverName = order.getString("ten_lai_xe");
                        String driverPhoneNumber = order.getString("so_dien_thoai");
                        String carPlate = order.getString("bien_kiem_soat");
                        String carType = order.getString("loai_xe");

                        Order waitingOrder = new Order(orderID, carID, originPlace.trim(), destinationPlace.trim(), pickupString, orderedString, driverName, driverPhoneNumber, carPlate, carType);
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
