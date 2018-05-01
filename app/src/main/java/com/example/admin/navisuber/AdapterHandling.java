package com.example.admin.navisuber;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.Calendar;
//import java.util.Date;
//import static android.content.Context.MODE_PRIVATE;

public class AdapterHandling extends BaseAdapter {
    //private static final String PREFERENCES_NAME = "tokenIdPref" ;
    private Context context;
    private ArrayList<Order> orders;

    public AdapterHandling(Context context, ArrayList<Order> orders) {
        this.context = context;
        this.orders = orders;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.order_in_listview_layout, null);

        //orders = getOrdersFromServer();

        Order currentOrder = orders.get(position);

        TextView tv_path = view.findViewById(R.id.tv_path);
        tv_path.setText("- " + currentOrder.getOriginPlace() + " - " + currentOrder.getDestinationPlace() + ".");

        TextView tv_seatNumber = view.findViewById(R.id.tv_seatNumber);
        tv_seatNumber.setText("- Số ghế: " + currentOrder.getSeatNumber());

        TextView tv_pickupTime = view.findViewById(R.id.tv_pickup_time);
        tv_pickupTime.setText("- " + currentOrder.getPickupTime());

        TextView tv_orderedTime = view.findViewById(R.id.tv_ordered_time);
        tv_orderedTime.setText("- " + currentOrder.getOrderedTime());

        return view;
    }

//    private ArrayList<Order> getOrdersFromServer() {
//        ArrayList<Order> orders = null;
//
//        SharedPreferences sharedPreferences = this.context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
//        String tokenID = sharedPreferences.getString(this.context.getResources().getString(R.string.refreshed_token), null);
//
//        Date currentTime = Calendar.getInstance().getTime();
//
//        try {
//            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
//
//            //Connection DbConn = DriverManager.getConnection("jdbc:jtds:sqlserver://ADMIN\\SQLEXPRESS;test_device_db;Integrated Security=True");
//            String servername = context.getResources().getString(R.string.server_name);
//            String dbname = context.getResources().getString(R.string.database_name);
//            String username = context.getResources().getString(R.string.username);
//            String password = context.getResources().getString(R.string.password);
//
//            Connection DbConn = DriverManager.getConnection("jdbc:jtds:sqlserver://" + servername + ";databaseName=" + dbname + ";user=" + username
//                    + ";password=" + password);
//
//            Statement stmt = DbConn.createStatement();
//            ResultSet resultSet = stmt.executeQuery("SELECT id_dat_xe, diem_bat_dau, diem_ket_thuc, thoi_diem_dat_xe," +
//                    " thoi_diem_khoi_hanh, so_ghe FROM dbo.Lst_DatXe WHERE registrationID = '" + tokenID + "' AND status = 0 AND thoi_diem_khoi_hanh >= " + currentTime);
//            while (resultSet.next()) {
//                int orderID = resultSet.getInt("id_da_xe");
//                String originPlace = resultSet.getString("diem_bat_dau");
//                String destinationPlace = resultSet.getString("diem_ket_thuc");
//                Date orderedtime = resultSet.getDate("thoi_diem_dat_xe");
//                Date pickupTime = resultSet.getDate("thoi_diem_khoi_hanh");
//                int seatNumber = resultSet.getInt("so_ghe");
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
}
