package com.example.admin.navisuber;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


/**
 * Created by Admin on 3/18/2018.
 */

public class DetailCarOrderActivity extends AppCompatActivity {
    private static final int WAITING_DIALOG_RETURN = 1;
    private static String originPlace;
    private static String destinationPlace;
    private static LatLng destinationLatlng;
    private static String pickupTime;
    private String phoneNumber;
    private String carType;
    private static List<String> cartypeList;

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

            //cartype dropdown list
            DatabaseHelper db = new DatabaseHelper(this);
            InsertDataFromSSMS insertDataFromSSMS = new InsertDataFromSSMS();
            insertDataFromSSMS.execute();

            cartypeList = db.getListCarType();

            Spinner spinnerCarType = (Spinner)findViewById(R.id.spinner_car_type);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, cartypeList);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCarType.setAdapter(dataAdapter);
            if(carType != null){
                int selectedPosition = dataAdapter.getPosition(carType);
                spinnerCarType.setSelection(selectedPosition);
            }

            if(pickupTime != null){
                EditText etPickupTime = (EditText)findViewById(R.id.et_pick_up_time);
                etPickupTime.setText(pickupTime);
            }

            Button btnConfirmCarOrder = (Button) findViewById(R.id.btn_car_order_submit);
            btnConfirmCarOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Spinner spinnerCartype = (Spinner)findViewById(R.id.spinner_car_type);
                    int selectedItemPosition = spinnerCartype.getSelectedItemPosition();
                    carType = cartypeList.get(selectedItemPosition);

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

                    if (originPlace == null || destinationPlace == null || phoneNumber.isEmpty() || carType == null
                            || pickupTime.isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DetailCarOrderActivity.this);
                        builder.setTitle(getResources().getString(R.string.warning_dialog));
                        builder.setMessage(getResources().getString(R.string.waring_message));
                        builder.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    } else{
                        AlertDialog.Builder builderRequset = new AlertDialog.Builder(DetailCarOrderActivity.this);
                        builderRequset.setTitle(getResources().getString(R.string.send_order));
                        builderRequset.setMessage("Điểm đón: " + originPlace + "\nĐiểm đến: " + destinationPlace + "\nLoại xe: "
                        + carType + "\nThời gian đón: " + pickupTime + "\nSố điện thoại: " + phoneNumber);
                        builderRequset.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        builderRequset.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                JSONObject orderValue = new JSONObject();
                                JSONObject order = new JSONObject();
                                try {
                                    orderValue.put(getResources().getString(R.string.origin), originPlace);
                                    orderValue.put(getResources().getString(R.string.destination), destinationPlace);
                                    orderValue.put(getResources().getString(R.string.car_type), carType);
                                    orderValue.put(getResources().getString(R.string.pick_up_time), pickupTime);
                                    orderValue.put(getResources().getString(R.string.phone_number), phoneNumber);

                                    order.put(getResources().getString(R.string.order), orderValue.toString());

                                    String response = postDataToWebService("url", order);

                                    if(showWaitingDialog(order) == WAITING_DIALOG_RETURN) {

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        builderRequset.show();
                    }
                }
            });
        }
    }

    private int showWaitingDialog(final JSONObject order) {
        Dialog waitingDialog = new Dialog(DetailCarOrderActivity.this);
        waitingDialog.setContentView(R.layout.waiting_dialog_layout);
        waitingDialog.setCancelable(false);

        final Button btn_resend_order = (Button)waitingDialog.findViewById(R.id.btn_resend_order);
        btn_resend_order.setTextColor(ContextCompat.getColor(DetailCarOrderActivity.this, R.color.colorGrey));
        btn_resend_order.setEnabled(false);

        final TextView tv_dialog_msg = (TextView)waitingDialog.findViewById(R.id.tv_dialog_message);

        new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv_dialog_msg.setText(getResources().getString(R.string.waiting_dialog_message) + " " + millisUntilFinished/1000 + " giây.");
            }

            @Override
            public void onFinish() {
                btn_resend_order.setEnabled(true);
                btn_resend_order.setTextColor(ContextCompat.getColor(DetailCarOrderActivity.this, R.color.colorAccent));
            }
        }.start();

        Window dialogWindow = waitingDialog.getWindow();
        dialogWindow.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        waitingDialog.show();

        btn_resend_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postDataToWebService("url", order);
            }
        });
        return WAITING_DIALOG_RETURN;
    }


    private String postDataToWebService(String path, JSONObject jsonObject) {
        StringBuilder builder = null;
        HttpURLConnection connection;
        try {
            URL url = new URL(path);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            OutputStreamWriter streamWriter = new OutputStreamWriter(connection.getOutputStream());
            streamWriter.write(jsonObject.toString());
            streamWriter.flush();

            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    builder.append(line);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(builder == null){
            return null;
        } else{
            return builder.toString();
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

                Spinner spinnerCartype = (Spinner)findViewById(R.id.spinner_car_type);
                int selectedItemPosition = spinnerCartype.getSelectedItemPosition();
                carType = cartypeList.get(selectedItemPosition);
                if (carType != null) {
                    intent.putExtra(getResources().getString(R.string.car_type), carType);
                }

                EditText etPickUpTime = (EditText) findViewById(R.id.et_pick_up_time);
                pickupTime = etPickUpTime.getText().toString();
                if (!pickupTime.isEmpty()) {
                    intent.putExtra(getResources().getString(R.string.pick_up_time), pickupTime);
                }

                EditText etPhoneNumber = (EditText) findViewById(R.id.et_phone_contact);
                phoneNumber = etPhoneNumber.getText().toString();
                if (!phoneNumber.isEmpty()) {
                    intent.putExtra(getResources().getString(R.string.phone_number), phoneNumber);
                }

                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class InsertDataFromSSMS extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            DatabaseHelper db = new DatabaseHelper(DetailCarOrderActivity.this);
            db.insertDataFromSSMS();

            return null;
        }
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
