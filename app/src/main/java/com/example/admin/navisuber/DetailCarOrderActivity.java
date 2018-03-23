package com.example.admin.navisuber;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Admin on 3/18/2018.
 */

public class DetailCarOrderActivity extends AppCompatActivity {
    private static String originPlace;
    private static String destinationPlace;
    private static LatLng destinationLatlng;
    private static String pickupTime;
    private String phoneNumber;
    private String carType;
    private final String preferencFileName = "preference_file";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_car_order);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            originPlace = bundle.getString(getResources().getString(R.string.origin));
            destinationPlace = bundle.getString(getResources().getString(R.string.destination));
            destinationLatlng = bundle.getParcelable(getResources().getString(R.string.destination_latlng));
            phoneNumber = bundle.getString(getResources().getString(R.string.phone_number));
            carType = bundle.getString(getResources().getString(R.string.car_type));
            pickupTime = bundle.getString(getResources().getString(R.string.pick_up_time));

            AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder().setCountry("VN").build();

            PlaceAutocompleteFragment autocompleteOrigin = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_origin);
            if (originPlace != null) {
                EditText et = ((EditText)autocompleteOrigin.getView().findViewById(R.id.place_autocomplete_search_input));
                et.setHint(originPlace);
                et.setText(originPlace);
                //autocompleteOrigin.setText(originPlace);
            } else {
                autocompleteOrigin.setHint(getResources().getString(R.string.origin));
            }
            autocompleteOrigin.setFilter(autocompleteFilter);
            autocompleteOrigin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    originPlace = place.getName().toString();
                }

                @Override
                public void onError(Status status) {

                }
            });

            PlaceAutocompleteFragment autocompleteDestination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_destination);
            if (destinationPlace != null) {
                autocompleteDestination.setHint(destinationPlace);
                autocompleteDestination.setText(destinationPlace);
            } else {
                autocompleteDestination.setHint(getResources().getString(R.string.destination));
            }
            autocompleteDestination.setFilter(autocompleteFilter);
            autocompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    destinationPlace = place.getName().toString();
                    destinationLatlng = place.getLatLng();
                }

                @Override
                public void onError(Status status) {

                }
            });

            EditText etPhoneNumber = (EditText) findViewById(R.id.et_phone_contact);
            etPhoneNumber.setText(phoneNumber);

            if(carType != null){
                EditText etCarType = (EditText)findViewById(R.id.et_car_type);
                etCarType.setText(carType);
            }

            if(pickupTime != null){
                EditText etPickupTime = (EditText)findViewById(R.id.et_pick_up_time);
                etPickupTime.setText(pickupTime);
            }

            Button btnConfirmCarOrder = (Button) findViewById(R.id.btn_car_order_submit);
            btnConfirmCarOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    EditText etCarType = (EditText) findViewById(R.id.et_car_type);
                    carType = etCarType.getText().toString();

                    EditText etPickupTime = (EditText) findViewById(R.id.et_pick_up_time);
                    pickupTime = etPickupTime.getText().toString();
//                    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
//                    try {
//                        pickupTime = format.parse(etPickupTime.getText().toString());
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }

                    EditText etPhoneNumber = (EditText) findViewById(R.id.et_phone_contact);
                    phoneNumber = etPhoneNumber.getText().toString();

                    if (originPlace == null || destinationPlace == null || phoneNumber == null || carType == null
                            || pickupTime == null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DetailCarOrderActivity.this);
                        builder.setTitle(getResources().getString(R.string.warning_dialog));
                        builder.setMessage(getResources().getString(R.string.waring_message));
                        builder.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(DetailCarOrderActivity.this, ShowOrderPathActivity.class);

                if (originPlace != null) {
                    intent.putExtra(getResources().getString(R.string.origin), originPlace);
                }

                if (destinationPlace != null) {
                    intent.putExtra(getResources().getString(R.string.destination), destinationPlace);
                    intent.putExtra(getResources().getString(R.string.destination_latlng), destinationLatlng);
                }

                EditText etCarType = (EditText) findViewById(R.id.et_car_type);
                carType = etCarType.getText().toString();
                if (carType != null) {
                    intent.putExtra(getResources().getString(R.string.car_type), carType);
                }

                EditText etPickUpTime = (EditText) findViewById(R.id.et_pick_up_time);
                pickupTime = etPickUpTime.getText().toString();
                if (pickupTime != null) {
                    intent.putExtra(getResources().getString(R.string.pick_up_time), pickupTime);
                }

                EditText etPhoneNumber = (EditText) findViewById(R.id.et_phone_contact);
                phoneNumber = etPhoneNumber.getText().toString();
                if (phoneNumber != null) {
                    intent.putExtra(getResources().getString(R.string.phone_number), phoneNumber);
                }

                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //lưu dữ liệu khi ấn nút back
//    @Override
//    protected void onPause() {
//        super.onPause();
//        SharedPreferences sharedPreferences = getSharedPreferences(preferencFileName, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//
//        if (originPlace != null) {
//            editor.putString(getResources().getString(R.string.origin), originPlace);
//        }
//
//        if (destinationPlace != null) {
//            editor.putString(getResources().getString(R.string.destination), destinationPlace);
//        }
//
//        EditText etCarType = (EditText) findViewById(R.id.et_car_type);
//        carType = etCarType.getText().toString();
//        if (carType != null) {
//            editor.putString(getResources().getString(R.string.car_type), carType);
//        }
//
//        EditText etPickUpTime = (EditText) findViewById(R.id.et_pick_up_time);
//        String pickupTimeString = etPickUpTime.getText().toString();
//        if (pickupTimeString != null) {
//            editor.putString(getResources().getString(R.string.pick_up_time), pickupTimeString);
//        }
//
//        EditText etPhoneNumber = (EditText) findViewById(R.id.et_phone_contact);
//        phoneNumber = etPhoneNumber.getText().toString();
//        if (phoneNumber != null) {
//            editor.putString(getResources().getString(R.string.phone_number), phoneNumber);
//        }
//
//        editor.commit();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        SharedPreferences sharedPreferences = getSharedPreferences(preferencFileName, Context.MODE_PRIVATE);
//
//        originPlace = sharedPreferences.getString(getResources().getString(R.string.origin), originPlace);
//        if (originPlace != null) {
//            PlaceAutocompleteFragment autocompleteOrigin = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_origin);
//            autocompleteOrigin.setText(originPlace);
//        }
//
//        destinationPlace = sharedPreferences.getString(getResources().getString(R.string.destination), destinationPlace);
//        if (destinationPlace != null) {
//            PlaceAutocompleteFragment autocompleteDestination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_destination);
//            autocompleteDestination.setText(destinationPlace);
//        }
//
//        carType = sharedPreferences.getString(getResources().getString(R.string.car_type), null);
//        if (carType != null) {
//            EditText etPickupTime = (EditText) findViewById(R.id.et_car_type);
//            etPickupTime.setText(carType);
//        }
//
//        String pickupTimeString = sharedPreferences.getString(getResources().getString(R.string.pick_up_time), null);
//        if (pickupTimeString != null) {
//            EditText etPickupTime = (EditText) findViewById(R.id.et_pick_up_time);
//            etPickupTime.setText(pickupTimeString);
//        }
//
//        phoneNumber = sharedPreferences.getString(getResources().getString(R.string.phone_number), phoneNumber);
//        EditText etPhoneNumber = (EditText) findViewById(R.id.et_phone_contact);
//        etPhoneNumber.setText(phoneNumber);
//    }
}
