package com.example.admin.navisuber;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Admin on 3/18/2018.
 */

public class DetailCarOrderActivity extends AppCompatActivity {
    private static String originPlace;
    private static String destinationPlace;
    private static Date pickupTime;
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
            phoneNumber = bundle.getString(getResources().getString(R.string.phone_number));

            AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder().setCountry("VN").build();

            PlaceAutocompleteFragment autocompleteOrigin = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_origin);
            //((EditText)autocompleteOrigin.getActivity().findViewById(R.id.place_autocomplete_search_input)).setText(originPlace);
            if (originPlace != null) {
                autocompleteOrigin.setText(originPlace);
            } else {
                autocompleteOrigin.setHint(getResources().getString(R.string.origin));
            }
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
            if (destinationPlace != null) {
                autocompleteDestination.setHint(destinationPlace);
            } else {
                autocompleteDestination.setHint(getResources().getString(R.string.destination));
            }
            autocompleteDestination.setFilter(autocompleteFilter);
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

            Button btnConfirmCarOrder = (Button) findViewById(R.id.btn_car_order_submit);
            btnConfirmCarOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    EditText etCarType = (EditText) findViewById(R.id.et_car_type);
                    carType = etCarType.getText().toString();

                    EditText etPickupTime = (EditText) findViewById(R.id.et_pick_up_time);
                    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                    try {
                        pickupTime = format.parse(etPickupTime.getText().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

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
                }

                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //lưu dữ liệu khi ấn nút back
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getSharedPreferences(preferencFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (originPlace != null) {
            editor.putString(getResources().getString(R.string.origin), originPlace);

        }

        if (destinationPlace != null) {
            editor.putString(getResources().getString(R.string.destination), destinationPlace);
        }

        EditText etCarType = (EditText) findViewById(R.id.et_car_type);
        carType = etCarType.getText().toString();
        if (carType != null) {
            editor.putString(getResources().getString(R.string.car_type), carType);
        }

        EditText etPickUpTime = (EditText) findViewById(R.id.et_pick_up_time);
        String pickupTimeString = etPickUpTime.getText().toString();
        if (pickupTimeString != null) {
            editor.putString(getResources().getString(R.string.pick_up_time), pickupTimeString);
        }

        EditText etPhoneNumber = (EditText) findViewById(R.id.et_phone_contact);
        phoneNumber = etPhoneNumber.getText().toString();
        if (phoneNumber != null) {
            editor.putString(getResources().getString(R.string.phone_number), phoneNumber);
        }

        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(preferencFileName, Context.MODE_PRIVATE);

        originPlace = sharedPreferences.getString(getResources().getString(R.string.origin), "");
        if (originPlace != null) {
            PlaceAutocompleteFragment autocompleteOrigin = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_origin);
            autocompleteOrigin.setText(originPlace);
        }

        destinationPlace = sharedPreferences.getString(getResources().getString(R.string.destination), "");
        if (destinationPlace != null) {
            PlaceAutocompleteFragment autocompleteDestination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_destination);
            autocompleteDestination.setText(destinationPlace);
        }

        carType = sharedPreferences.getString(getResources().getString(R.string.car_type), "");
        if (carType != null) {
            EditText etPickupTime = (EditText) findViewById(R.id.et_car_type);
            etPickupTime.setText(carType);
        }

        String pickupTimeString = sharedPreferences.getString(getResources().getString(R.string.pick_up_time), "");
        if (pickupTimeString != null) {
            EditText etPickupTime = (EditText) findViewById(R.id.et_pick_up_time);
            etPickupTime.setText(pickupTimeString);
        }

        phoneNumber = sharedPreferences.getString(getResources().getString(R.string.phone_number), phoneNumber);
        EditText etPhoneNumber = (EditText) findViewById(R.id.et_phone_contact);
        etPhoneNumber.setText(phoneNumber);
    }
}
