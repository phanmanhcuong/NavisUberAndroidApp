package com.example.admin.navisuber;

import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String PREFERENCES_NAME = "tokenIdPref" ;
    private SharedPreferences sharedPreferences;
    @Override
    public void onTokenRefresh(){
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getResources().getString(R.string.refreshed_token), refreshedToken);
        editor.commit();
    }
}
