package com.example.admin.navisuber;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 3/26/2018.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "Car_Database";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Car";
    private static final String COLUMN_NAME = "Car_Type";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    //create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String script = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_NAME + " NVARCHAR(50) PRIMARY KEY )";
        // Chạy lệnh tạo bảng.
        db.execSQL(script);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //drop table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        //recreate table
        onCreate(db);
    }

    public void insertDataFromSSMS (){
        int count = this.getCarTypeCount();
        if(count == 0) {
            StringBuilder builder = new StringBuilder();
            HttpURLConnection connection;
            try {
                URL url = new URL(context.getString(R.string.webservice_get_car_type_url));
                connection = (HttpURLConnection)url.openConnection();

                String responseCode = connection.getResponseMessage();
                int code = connection.getResponseCode();
                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null){
                        builder.append(line);
                    }

                    String responseCarType = builder.toString();

                    JSONObject jsonResponse = new JSONObject(responseCarType);
                    JSONArray jsonArray = jsonResponse.getJSONArray("GetCarTypeResult");
                    for (int i = 0; i <jsonArray.length(); i++){
                        Car car = new Car(jsonArray.get(i).toString());
                        this.addCar(car);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            try{
//                Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
//
//                //Connection DbConn = DriverManager.getConnection("jdbc:jtds:sqlserver://ADMIN\\SQLEXPRESS;test_device_db;Integrated Security=True");
//                String servername = context.getResources().getString(R.string.server_name);
//                String dbname = context.getResources().getString(R.string.database_name);
//                String username = context.getResources().getString(R.string.username);
//                String password = context.getResources().getString(R.string.password);
//
//                Connection DbConn = DriverManager.getConnection("jdbc:jtds:sqlserver://" + servername + ";databaseName=" + dbname + ";user=" + username
//                + ";password=" + password);
//
//                Statement stmt = DbConn.createStatement();
//                ResultSet resultSet = stmt.executeQuery("SELECT ten_loai_xe FROM dbo.Lst_LoaiXe");
//                while(resultSet.next()){
//                    String cartype = resultSet.getString("ten_loai_xe");
//                    Car car = new Car(cartype);
//                    this.addCar(car);
//                }
//
//                DbConn.close();
//
//            } catch (Exception e) {
//                Log.e("error", e.toString());
//            }
        }
    }

    public void addCar(Car car) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, car.getCarType());

        //insert 1 row to table
        db.insert(TABLE_NAME, null, values);

        db.close();
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db == null) {
            return;
        }
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    public int getCarTypeCount() {

        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();

        // return count
        return count;
    }

    public List<String> getListCarType(){
        List<String> cartypeList = new ArrayList<>();

        //query
        String query = "SELECT " + COLUMN_NAME + " FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            do {
                cartypeList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        return cartypeList;
    }

    public static class CarJson {
        public static String carType[];

        public void setCarType(String carType[]) {
            this.carType = carType;
        }

        public String[] getCarType() {

            return carType;
        }

        public CarJson(String[] carType) {

            this.carType = carType;
        }
    }
}
