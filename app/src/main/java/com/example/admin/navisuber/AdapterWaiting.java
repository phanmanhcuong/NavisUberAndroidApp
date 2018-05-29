package com.example.admin.navisuber;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class AdapterWaiting extends BaseAdapter {
    private Context context;
    private ArrayList<Order> orders;

    public AdapterWaiting(Context context) {
        this.context = context;
        orders = new ArrayList<>();
    }


    public void addList(ArrayList<Order> orderList){
        for(Order order : orderList){
            orders.add(order);
        }
    }

    public void removeList(){
        orders.clear();
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Object getItem(int i) {
        return orders.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.order_in_listview_waiting_layout, null);

        Order currentOrder = orders.get(position);

        TextView tv_path = view.findViewById(R.id.tv_path);
        tv_path.setText("- Tuyến đường: " + currentOrder.getOriginPlace() + " - " + currentOrder.getDestinationPlace() + ".");

        TextView tv_pickupTime = view.findViewById(R.id.tv_pickup_time);
        tv_pickupTime.setText("- Thời điểm khởi hành: " + currentOrder.getPickupTime());

        TextView tv_orderedTime = view.findViewById(R.id.tv_ordered_time);
        tv_orderedTime.setText("- Thời điểm đặt: " + currentOrder.getOrderedTime());

        TextView tv_driverName = view.findViewById(R.id.tv_driver_name);
        tv_driverName.setText("- Lái xe: " + currentOrder.getDriverName());

        TextView tv_driverPhoneNumber = view.findViewById(R.id.tv_driver_phone_number);
        tv_driverPhoneNumber.setText("- Số điện thoại lái xe: " + currentOrder.getDriverPhoneNumber());

        TextView tv_carPlate = view.findViewById(R.id.tv_car_plate);
        tv_carPlate.setText("- Loại xe: " + currentOrder.getCarType() + " - Biển số xe: " + currentOrder.getCarPlate());
        return view;
    }
}
