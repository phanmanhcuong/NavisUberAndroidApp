package com.example.admin.navisuber;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Admin on 3/18/2018.
 */

public class DetailCarOrderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_car_order);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            String originPlace = bundle.getString(getResources().getString(R.string.origin));
            String destinationPlace = bundle.getString(getResources().getString(R.string.destination));
            String phoneNumber = bundle.getString(getResources().getString(R.string.phone_number));

            EditText etOriginPlace = (EditText) findViewById(R.id.et_origin);
            EditText etDestinationPlace = (EditText) findViewById(R.id.et_destination);
            EditText etPhoneNumber = (EditText) findViewById(R.id.et_phone_contact);

            etOriginPlace.setText(originPlace);
            etDestinationPlace.setText(destinationPlace);
            etPhoneNumber.setText(phoneNumber);

            EditText etPickupTime = (EditText) findViewById(R.id.et_pick_up_time);
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            try {
                Date date = format.parse(etPickupTime.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Button btnConfirmCarOrder = (Button)findViewById(R.id.btn_car_order_submit);
            btnConfirmCarOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }
    }
}
