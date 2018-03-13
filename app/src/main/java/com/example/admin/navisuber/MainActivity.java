package com.example.admin.navisuber;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void moveToPathOrder (View clickedBtn){
        EditText et_phoneNumber = (EditText) findViewById(R.id.et_phoneNumber);
        String phoneNumber = et_phoneNumber.getText().toString();
        if(! phoneNumber.isEmpty()){
            Intent orderPathIntent = new Intent(MainActivity.this, ShowOrderPathActivity.class);
            orderPathIntent.putExtra(getResources().getString(R.string.phone_number), phoneNumber);
            startActivity(orderPathIntent);
        }
    }
}
