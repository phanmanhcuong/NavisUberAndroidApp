package com.example.admin.navisuber;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Admin on 3/18/2018.
 */

public class DetailCarOrderActivity extends AppCompatActivity {
    private static String originPlace;
    private static String destinationPlace;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_car_order);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            originPlace = bundle.getString(getResources().getString(R.string.origin));
            destinationPlace = bundle.getString(getResources().getString(R.string.destination));
            String phoneNumber = bundle.getString(getResources().getString(R.string.phone_number));

            AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder().setCountry("VN").build();

            PlaceAutocompleteFragment autocompleteOrigin = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_origin);
            autocompleteOrigin.setText(originPlace);
            autocompleteOrigin.setFilter(autocompleteFilter);
            autocompleteOrigin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {

                }

                @Override
                public void onError(Status status) {

                }
            });

            PlaceAutocompleteFragment autocompleteDestination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_destination);
            autocompleteDestination.setHint(getResources().getString(R.string.destination));
            autocompleteDestination.setFilter(autocompleteFilter);
            autocompleteDestination.setText(destinationPlace);
            autocompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    destinationPlace = place.getName().toString();
                }

                @Override
                public void onError(Status status) {

                }
            });
            EditText etPhoneNumber = (EditText) findViewById(R.id.et_phone_contact);

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

    @Override
    public void onBackPressed(){

    }
}
