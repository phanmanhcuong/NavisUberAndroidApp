package com.example.admin.navisuber;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private static final String PREFERENCES_PHONENUMBER = "PhoneNumber" ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_PHONENUMBER, MODE_PRIVATE);
        String phoneNumber = sharedPreferences.getString(getResources().getString(R.string.phone_number), null);
        if(phoneNumber == null){
            Intent signupIntent = new Intent(this, SignUpActivity.class);
            startActivity(signupIntent);
        } else{
            Intent orderPathIntent = new Intent(this, ShowOrderPathActivity.class);
            startActivity(orderPathIntent);
        }
    }

//    public void moveToPathOrder (View clickedBtn){
//        EditText et_phoneNumber = (EditText) findViewById(R.id.et_phoneNumber);
//        String phoneNumber = et_phoneNumber.getText().toString();
//        if(! phoneNumber.isEmpty()){
//            Intent orderPathIntent = new Intent(MainActivity.this, ShowOrderPathActivity.class);
//            orderPathIntent.putExtra(getResources().getString(R.string.phone_number), phoneNumber);
//            startActivity(orderPathIntent);
//        }
//    }
}
