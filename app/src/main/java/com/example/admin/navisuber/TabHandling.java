package com.example.admin.navisuber;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class TabHandling extends Fragment {
    private static final String PREFERENCES_NAME = "tokenIdPref" ;
    private ArrayList<Order> orderList;
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_handling, container, false);
        ListView listView = view.findViewById(R.id.listview_handling);

        orderList = getOrdersFromServer(view);

        AdapterHandling adapterHandling = new AdapterHandling(this.getActivity(), orderList);
        listView.setAdapter(adapterHandling);
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return view;
    }

    private ArrayList<Order> getOrdersFromServer(View view) {
        ArrayList<Order> orders = null;

        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        String tokenID = sharedPreferences.getString(view.getContext().getResources().getString(R.string.refreshed_token), null);

        Date currentTime = Calendar.getInstance().getTime();

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();

            //Connection DbConn = DriverManager.getConnection("jdbc:jtds:sqlserver://ADMIN\\SQLEXPRESS;test_device_db;Integrated Security=True");
            String servername = view.getContext().getResources().getString(R.string.server_name);
            String dbname = view.getContext().getResources().getString(R.string.database_name);
            String username = view.getContext().getResources().getString(R.string.username);
            String password = view.getContext().getResources().getString(R.string.password);

            Connection DbConn = DriverManager.getConnection("jdbc:jtds:sqlserver://" + servername + ";databaseName=" + dbname + ";user=" + username
                    + ";password=" + password);

            Statement stmt = DbConn.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT id_dat_xe, diem_bat_dau, diem_ket_thuc, thoi_diem_dat_xe," +
                    " thoi_diem_khoi_hanh, so_ghe FROM dbo.Lst_DatXe WHERE registrationID = '" + tokenID + "' AND status = 0 AND thoi_diem_khoi_hanh <= " + currentTime);
            while (resultSet.next()) {
                int orderID = resultSet.getInt("id_da_xe");
                String originPlace = resultSet.getString("diem_bat_dau");
                String destinationPlace = resultSet.getString("diem_ket_thuc");
                Date orderedtime = resultSet.getDate("thoi_diem_dat_xe");
                Date pickupTime = resultSet.getDate("thoi_diem_khoi_hanh");
                int seatNumber = resultSet.getInt("so_ghe");

                Order order = new Order(orderID, originPlace, destinationPlace, orderedtime, pickupTime, seatNumber);
                orders.add(order);
            }

            DbConn.close();

        } catch (Exception e) {
            Log.e("error", e.toString());
        }
        return orders;
    }
}
