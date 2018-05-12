package com.example.admin.navisuber;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class TabHandling extends Fragment {
    private static final String PREFERENCES_NAME = "tokenIdPref" ;
    private static final String PREFERENCES_PHONENUMBER = "PhoneNumber";
    private static ListView listView;
    private static AdapterHandling adapterHandling;
    private static ArrayList<Order> orderList;
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_handling, container, false);

        listView = view.findViewById(R.id.listview_handling);

        adapterHandling = new AdapterHandling(this.getActivity());

        listView.setAdapter(adapterHandling);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            }

        });


        //get data from server to add to adapter
        ConnectToDb connectToDb = new ConnectToDb(this.getActivity());
        connectToDb.execute();

        return view;
    }

//    private ArrayList<Order> getOrdersFromServer(View view) {
//        ArrayList<Order> orders = null;
//
//        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
//        String tokenID = sharedPreferences.getString(view.getContext().getResources().getString(R.string.refreshed_token), null);
//
//        Date currentTime = Calendar.getInstance().getTime();
//
//        try {
//            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
//
//            //Connection DbConn = DriverManager.getConnection("jdbc:jtds:sqlserver://ADMIN\\SQLEXPRESS;test_device_db;Integrated Security=True");
//            String servername = view.getContext().getResources().getString(R.string.server_name);
//            String dbname = view.getContext().getResources().getString(R.string.database_name);
//            String username = view.getContext().getResources().getString(R.string.username);
//            String password = view.getContext().getResources().getString(R.string.password);
//
//            Connection DbConn = DriverManager.getConnection("jdbc:jtds:sqlserver://" + servername + ";databaseName=" + dbname + ";user=" + username
//                    + ";password=" + password);
//
//            Statement stmt = DbConn.createStatement();
//            ResultSet resultSet = stmt.executeQuery("SELECT id_dat_xe, diem_bat_dau, diem_ket_thuc, thoi_diem_dat_xe," +
//                    " thoi_diem_khoi_hanh, so_ghe FROM dbo.Lst_DatXe WHERE registrationID = '" + tokenID + "' AND status = 0 AND thoi_diem_khoi_hanh <= " + currentTime);
//            while (resultSet.next()) {
//                int orderID = resultSet.getInt("id_da_xe");
//                String originPlace = resultSet.getString("diem_bat_dau");
//                String destinationPlace = resultSet.getString("diem_ket_thuc");
//                Date orderedtime = resultSet.getDate("thoi_diem_dat_xe");
//                Date pickupTime = resultSet.getDate("thoi_diem_khoi_hanh");
//                int seatNumber = resultSet.getInt("so_ghe");
//
//                Order order = new Order(orderID, originPlace, destinationPlace, orderedtime, pickupTime, seatNumber);
//                orders.add(order);
//            }
//
//            DbConn.close();
//
//        } catch (Exception e) {
//            Log.e("error", e.toString());
//        }
//        return orders;
//    }

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

            String webserviceUrl = this.activity.getResources().getString(R.string.webservice_get_waiting_orders) + phoneNumber + "&status=0";
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
            adapterHandling.addList(orderList);
            adapterHandling.notifyDataSetChanged();
        }
    }
}
