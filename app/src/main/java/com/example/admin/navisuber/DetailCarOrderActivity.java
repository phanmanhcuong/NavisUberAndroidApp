package com.example.admin.navisuber;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Admin on 3/18/2018.
 */

public class DetailCarOrderActivity extends AppCompatActivity {
    private static final int ORIGIN_ON_CLEAR_BUTTON_CLICKED = 1;
    private static final int DESTINATION_ON_CLEAR_BUTTON_CLICKED = 2;
    private static final String PREFERENCES_PHONENUMBER = "PhoneNumber";
    private static String originPlace;
    private static String destinationPlace;
    private static LatLng destinationLatlng;
    private static LatLng originLatlng;
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
            originLatlng = bundle.getParcelable(getResources().getString(R.string.origin_latlng));
            destinationPlace = bundle.getString(getResources().getString(R.string.destination));
            destinationLatlng = bundle.getParcelable(getResources().getString(R.string.destination_latlng));
            carType = bundle.getString(getResources().getString(R.string.car_type));
            pickupTime = bundle.getString(getResources().getString(R.string.pick_up_time));
        }

        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder().setCountry("VN").build();

        PlaceAutocompleteFragment autocompleteOrigin = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_origin);
        if (originPlace != null) {
            EditText et = autocompleteOrigin.getView().findViewById(R.id.place_autocomplete_search_input);
            et.setHint(originPlace);
            et.setText(originPlace);
        } else {
            autocompleteOrigin.setHint(getResources().getString(R.string.origin));
        }
        autocompleteOrigin.setFilter(autocompleteFilter);
        autocompleteOrigin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                originPlace = place.getName().toString();
                originLatlng = place.getLatLng();
            }

            @Override
            public void onError(Status status) {

            }
        });

        autocompleteOrigin.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaceAutocompleteClearButtonListener placeAutocompleteClearButtonListener = new PlaceAutocompleteClearButtonListener(ORIGIN_ON_CLEAR_BUTTON_CLICKED);
                placeAutocompleteClearButtonListener.execute();

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

        autocompleteDestination.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaceAutocompleteClearButtonListener placeAutocompleteClearButtonListener = new PlaceAutocompleteClearButtonListener(DESTINATION_ON_CLEAR_BUTTON_CLICKED);
                placeAutocompleteClearButtonListener.execute();

            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_PHONENUMBER, MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString(getResources().getString(R.string.phone_number), null);
        EditText etPhoneNumber = findViewById(R.id.et_phone_contact);
        etPhoneNumber.setText(phoneNumber);

        //cartype dropdown list
        DatabaseHelper db = new DatabaseHelper(this);

        cartypeList = db.getListCarType();
        if (cartypeList.size() == 0) {
            InsertDataFromSSMS insertDataFromSSMS = new InsertDataFromSSMS();
            insertDataFromSSMS.execute();
        } else {
            Spinner spinnerCarType = findViewById(R.id.spinner_car_type);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, cartypeList);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCarType.setAdapter(dataAdapter);
            if (carType != null) {
                int selectedPosition = dataAdapter.getPosition(carType);
                spinnerCarType.setSelection(selectedPosition);
            }
        }

        //set date to tv_date and time to tv_time
        if(pickupTime != null){
            String pickupTimeString[] = pickupTime.split(" ");
            String date = pickupTimeString[0];
            String time = pickupTimeString[1];

            TextView tv_date = findViewById(R.id.tv_date);
            tv_date.setText(date);

            TextView tv_time = findViewById(R.id.tv_time);
            tv_time.setText(time);
        }
        //pick up time dialog
        Button btn_date = findViewById(R.id.btn_date);
        btn_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getSupportFragmentManager(), "Date Picker");
            }
        });

        Button btn_time = findViewById(R.id.btn_time);
        btn_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.show(getSupportFragmentManager(), "Time Picker");
            }
        });

        Button btnConfirmCarOrder = findViewById(R.id.btn_car_order_submit);
        btnConfirmCarOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Spinner spinnerCartype = findViewById(R.id.spinner_car_type);
                int selectedItemPosition = spinnerCartype.getSelectedItemPosition();
                carType = cartypeList.get(selectedItemPosition);

                TextView tv_date = findViewById(R.id.tv_date);
                String date = tv_date.getText().toString();

                TextView tv_time = findViewById(R.id.tv_time);
                String time = tv_time.getText().toString();
                if(date != "" && time != ""){
                    pickupTime = date + " " + time;
                }

                if (originPlace == null || destinationPlace == null || carType == null || pickupTime == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailCarOrderActivity.this);
                    builder.setTitle(getResources().getString(R.string.warning_dialog));
                    builder.setMessage(getResources().getString(R.string.waring_message));
                    builder.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                } else {
                    AlertDialog.Builder builderRequest = new AlertDialog.Builder(DetailCarOrderActivity.this);
                    builderRequest.setTitle(getResources().getString(R.string.send_order));
                    builderRequest.setMessage("Điểm đón: " + originPlace + "\nĐiểm đến: " + destinationPlace + "\nLoại xe: "
                            + carType + "\nThời gian đón: " + pickupTime + "\nSố điện thoại: " + phoneNumber);
                    builderRequest.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    builderRequest.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //HashMap
                            Date currentTime = Calendar.getInstance().getTime();
                            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            String currentTimeString = format.format(currentTime);

                            HashMap<String, String> carOrder = new HashMap<>();
                            carOrder.put(getResources().getString(R.string.json_origin), originPlace);
                            carOrder.put(getResources().getString(R.string.origin_lat), String.valueOf(originLatlng.latitude));
                            carOrder.put(getResources().getString(R.string.origin_long), String.valueOf(originLatlng.longitude));
                            carOrder.put(getResources().getString(R.string.json_destination), destinationPlace);
                            carOrder.put(getResources().getString(R.string.destination_lat), String.valueOf(destinationLatlng.latitude));
                            carOrder.put(getResources().getString(R.string.destination_long), String.valueOf(destinationLatlng.longitude));
                            carOrder.put(getResources().getString(R.string.json_cartype), carType);
                            carOrder.put(getResources().getString(R.string.json_order_time), currentTimeString);
                            carOrder.put(getResources().getString(R.string.json_pickup_time), pickupTime);
                            carOrder.put(getResources().getString(R.string.json_phone_number), phoneNumber);

                            PostDataToWebService postDataToWebService = new PostDataToWebService(carOrder);
                            postDataToWebService.execute();
                        }
                    });

                    builderRequest.show();
                }
            }
        });
    }

    //xóa điểm đến hoặc điểm đón ngay khi ấn dấu x
    private class PlaceAutocompleteClearButtonListener extends AsyncTask<Void, Void, Void> {
        private int flag;

        public PlaceAutocompleteClearButtonListener(int flag) {
            this.flag = flag;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (flag == ORIGIN_ON_CLEAR_BUTTON_CLICKED) {
                originPlace = null;
            } else {
                destinationPlace = null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            super.onPostExecute(params);

            if (flag == ORIGIN_ON_CLEAR_BUTTON_CLICKED) {
                PlaceAutocompleteFragment autocompleteOrigin = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_origin);
                autocompleteOrigin.setText("");
                autocompleteOrigin.setHint(getResources().getString(R.string.origin));
            } else {
                PlaceAutocompleteFragment autocompleteDestination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fragment_auto_complete_destination);
                autocompleteDestination.setText("");
                autocompleteDestination.setHint(getResources().getString(R.string.destination));
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_show_path, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent showPathIntent = new Intent(DetailCarOrderActivity.this, ShowOrderPathActivity.class);

                //put extra data to intent ShowPath
                PutDataToIntent(showPathIntent);
                startActivity(showPathIntent);
                break;

            case R.id.btn_order_info:
                Intent orderStatusIntent = new Intent(DetailCarOrderActivity.this, OrderStatusActivity.class);

                //put extra data to intent OrderStatus
                PutDataToIntent(orderStatusIntent);
                startActivity(orderStatusIntent);
                break;

            case R.id.btn_edit_user:
                Intent editUserIntent = new Intent(DetailCarOrderActivity.this, SignUpActivity.class);
                startActivity(editUserIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void PutDataToIntent(Intent intent) {
        if (originPlace != null) {
            intent.putExtra(getResources().getString(R.string.origin), originPlace);
            intent.putExtra(getResources().getString(R.string.origin_latlng), originLatlng);
        }

        if (destinationPlace != null) {
            intent.putExtra(getResources().getString(R.string.destination), destinationPlace);
            intent.putExtra(getResources().getString(R.string.destination_latlng), destinationLatlng);
        }

        Spinner spinnerCartype = findViewById(R.id.spinner_car_type);
        int selectedItemPosition = -1;
        selectedItemPosition = spinnerCartype.getSelectedItemPosition();
        if (selectedItemPosition != -1) {
            carType = cartypeList.get(selectedItemPosition);
            if (carType != null) {
                intent.putExtra(getResources().getString(R.string.car_type), carType);
            }
        }

        TextView tv_date = findViewById(R.id.tv_date);
        String date = tv_date.getText().toString();

        TextView tv_time = findViewById(R.id.tv_time);
        String time = tv_time.getText().toString();
        if(date != "" && time != ""){
            pickupTime = date + " " + time;
        }

        if (pickupTime != null) {
            intent.putExtra(getResources().getString(R.string.pick_up_time), pickupTime);
        }
    }

    //get car types
    private class InsertDataFromSSMS extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            DatabaseHelper db = new DatabaseHelper(DetailCarOrderActivity.this);

            //db.deleteAll();
            db.insertDataFromSSMS();
            return db.getListCarType();
        }

        @Override
        protected void onPostExecute(List<String> carTypeList) {
            Spinner spinnerCarType = findViewById(R.id.spinner_car_type);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(DetailCarOrderActivity.this,
                    android.R.layout.simple_spinner_item, cartypeList);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCarType.setAdapter(dataAdapter);
            if (carType != null) {
                int selectedPosition = dataAdapter.getPosition(carType);
                spinnerCarType.setSelection(selectedPosition);
            }
        }


    }

    private class PostDataToWebService extends AsyncTask<Void, Void, String> {
        private HashMap<String, String> carOrder;

        public PostDataToWebService(HashMap<String, String> carOrder) {
            this.carOrder = carOrder;
        }

        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder builder = new StringBuilder();
            HttpURLConnection connection;
            try {
                URL url = new URL(getResources().getString(R.string.webservice_url));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //OutputStream
                OutputStream os = connection.getOutputStream();
                BufferedWriter streamWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                streamWriter.write(ConvertMapObjectToEncodeUrl(carOrder));

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
            AlertDialog.Builder responseBuilder = new AlertDialog.Builder(DetailCarOrderActivity.this);
            responseBuilder.setTitle(getResources().getString(R.string.webservice_response));
            responseBuilder.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            if (response.contains(getResources().getString(R.string.success_response))) {
                responseBuilder.setMessage(getResources().getString(R.string.webservice_response_success));
            } else {
                responseBuilder.setMessage(getResources().getString(R.string.webservice_response_fail));
            }
            responseBuilder.show();
        }
    }

    //convert hashmap
    private String ConvertMapObjectToEncodeUrl(HashMap<String, String> carOrder) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : carOrder.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
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
