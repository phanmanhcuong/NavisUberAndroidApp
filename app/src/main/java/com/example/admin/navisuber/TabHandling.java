package com.example.admin.navisuber;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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
import java.util.ArrayList;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class TabHandling extends Fragment {
    private static final String PREFERENCES_PHONENUMBER = "PhoneNumber";
    private static ListView listView;
    private static AdapterHandling adapterHandling;
    private static ArrayList<Order> orderList;
    private static String phoneNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View tabView = inflater.inflate(R.layout.tab_handling, container, false);

        SharedPreferences sharedPreferencesPhoneNumber = getContext().getSharedPreferences(PREFERENCES_PHONENUMBER, MODE_PRIVATE);
        phoneNumber = sharedPreferencesPhoneNumber.getString(getResources().getString(R.string.phone_number), null);

        listView = tabView.findViewById(R.id.listview_handling);

        adapterHandling = new AdapterHandling(this.getActivity());

        listView.setAdapter(adapterHandling);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Order selectedOrder = orderList.get(position);
                final int orderId = selectedOrder.getOrderID();
                String originPlace = selectedOrder.getOriginPlace();
                String destinationPlace = selectedOrder.getDestinationPlace();
                String pickupTime = selectedOrder.getPickupTime();
                String orderTime = selectedOrder.getOrderedTime();

                String message = "- Tuyến đường: " + originPlace + " - " + destinationPlace + ".\n" +
                        "- Thời điểm khởi hành: " + pickupTime + "\n" +
                        "- Thời điểm đặt: " + orderTime;


                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.cancel_order));
                builder.setMessage(message);
                builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CancelOrder cancelOrder = new CancelOrder(orderId);
                        cancelOrder.execute();
                    }
                });
                builder.show();
            }

        });


        //get data from server to add to adapter
        ConnectToDb connectToDb = new ConnectToDb(this.getActivity());
        connectToDb.execute();

        return tabView;
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

                    for (int i = 0; i < jsonArray.length(); i++) {
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

    private class CancelOrder extends AsyncTask<Void, Void, String> {
        private int orderId;

        public CancelOrder(int orderId) {
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

                }
            });

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
