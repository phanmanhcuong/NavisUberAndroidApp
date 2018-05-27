package com.example.admin.navisuber;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity{
    private static final String PREFERENCES_PHONENUMBER = "PhoneNumber";
    private static final String PREFERENCES_NAME = "tokenIdPref";
    private static String phoneNumber;
    private static String name;
    private static String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_layout);

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //reload user's infor to edittext
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        String name = sharedPreferences.getString(getString(R.string.Name), null);
        String phonenumber = sharedPreferences.getString(getString(R.string.PhoneNumber), null);
        String email = sharedPreferences.getString(getString(R.string.Email), null);
        if(name != null && phonenumber != null && email != null){
            TextView tv_name = findViewById(R.id.tv_name);
            tv_name.setText(name);

            TextView tv_phone_number = findViewById(R.id.tv_phone_number);
            tv_phone_number.setText(phonenumber);

            TextView tv_email = findViewById(R.id.tv_email);
            tv_email.setText(email);
        }
    }

    public void SendSignUp(View v){
        TextView tv_name = findViewById(R.id.tv_name);
        name = tv_name.getText().toString();

        TextView tv_phone_number = findViewById(R.id.tv_phone_number);
        phoneNumber = tv_phone_number.getText().toString();

        TextView tv_email = findViewById(R.id.tv_email);
        email = tv_email.getText().toString();

        final SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        final String tokenID = sharedPreferences.getString(getResources().getString(R.string.refreshed_token), null);

        if(name != "" && phoneNumber != "" && email!= ""){
            AlertDialog.Builder builderRequest = new AlertDialog.Builder(SignUpActivity.this);
            builderRequest.setTitle(getResources().getString(R.string.signup_confirm));
            builderRequest.setMessage("Họ tên: " + name + "\nSố điện thoại: " + phoneNumber + "\nEmail: " + email );
            builderRequest.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builderRequest.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //save to reload when edit user information
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString(getString(R.string.Name), name);
//                    editor.putString(getString(R.string.PhoneNumber), phoneNumber);
//                    editor.putString(getString(R.string.Email), email);
//                    editor.commit();

                    HashMap<String, String> signupInfo = new HashMap<>();
                    signupInfo.put(getResources().getString(R.string.signup_name), name);
                    signupInfo.put(getResources().getString(R.string.signup_phonenumber), phoneNumber);
                    signupInfo.put(getResources().getString(R.string.signup_email), email);
                    signupInfo.put(getResources().getString(R.string.signup_tokenid), tokenID);
                    SignUpToWebService signUpToWebService = new SignUpToWebService(signupInfo);
                    signUpToWebService.execute();
                }
            });

            builderRequest.show();
        } else{
            AlertDialog.Builder warningDialog = new AlertDialog.Builder(SignUpActivity.this);
            warningDialog.setTitle(getResources().getString(R.string.warning_dialog));
            warningDialog.setMessage(getString(R.string.waring_message));
            warningDialog.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
        }
    }

    private class SignUpToWebService extends AsyncTask<Void, Void, String>{
        private HashMap<String, String> signupInfo;

        public SignUpToWebService(HashMap<String, String> signupInfo) {
            this.signupInfo = signupInfo;
        }

        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder builder = new StringBuilder();
            HttpURLConnection connection;
            try {
                URL url = new URL(getResources().getString(R.string.webservice_signup));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //OutputStream
                OutputStream os = connection.getOutputStream();
                BufferedWriter streamWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                streamWriter.write(ConvertMapObjectToEncodeUrl(signupInfo));

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
            AlertDialog.Builder responseBuilder = new AlertDialog.Builder(SignUpActivity.this);
            responseBuilder.setTitle(getResources().getString(R.string.signup_response));
            responseBuilder.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent showOderPathItent = new Intent(SignUpActivity.this, ShowOrderPathActivity.class);
                    startActivity(showOderPathItent);
                }
            });

            if (response.contains(getResources().getString(R.string.success_response))) {
                responseBuilder.setMessage(getResources().getString(R.string.signup_success));

                SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_PHONENUMBER, MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.Name), name);
                editor.putString(getString(R.string.PhoneNumber), phoneNumber);
                editor.putString(getString(R.string.Email), email);
                editor.commit();
            } else if (response.contains(getString(R.string.phone_number_already_exist))){
                responseBuilder.setMessage("Số điện thoại đã được đăng kí bởi tài khoản khác");
            } else{
                responseBuilder.setMessage(getString(R.string.signup_fail));
            }
            responseBuilder.show();
        }

        private String ConvertMapObjectToEncodeUrl(HashMap<String, String> signupInfo) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : signupInfo.entrySet()) {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_show_path, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.btn_detail_order:
                Intent detailOrderIntent = new Intent(SignUpActivity.this, DetailCarOrderActivity.class);
                startActivity(detailOrderIntent);
                break;

            case R.id.btn_order_info:
                Intent orderStatusIntent = new Intent(SignUpActivity.this, OrderStatusActivity.class);
                startActivity(orderStatusIntent);
                break;
        }
        return true;
    }
}
