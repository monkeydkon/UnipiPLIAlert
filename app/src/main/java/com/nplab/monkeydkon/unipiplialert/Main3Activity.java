package com.nplab.monkeydkon.unipiplialert;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Main3Activity extends AppCompatActivity {

     EditText editText,editText3;
     String username,password,number1,number2;
     SharedPreferences sharedPreferences;
     int counter = 0;
     SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        editText = (EditText)findViewById(R.id.editText);
        editText3 = (EditText)findViewById(R.id.editText3);

        username = sharedPreferences.getString("username",null);
        password = sharedPreferences.getString("password",null);
        number1 = sharedPreferences.getString("number1",null);
        number2 = sharedPreferences.getString("number2",null);

        db = openOrCreateDatabase("UNIPLI_ALERT", Context.MODE_PRIVATE,null);



        Toast.makeText(this,"You have 3 tries to cancel the sending of sms", Toast.LENGTH_SHORT).show();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            //ask for permission

            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS}, 1);
        }
    }

    public void confirm(View view){

        if(counter <= 3) {
            if (editText.getText().toString().equals(username) && editText3.getText().toString().equals(password)) {
                sendSms(number1,"Alarm canceled. Everything is OK");
                sendSms(number1,"Alarm canceled. Everything is OK");
                Toast.makeText(this,"Message Sent",Toast.LENGTH_SHORT).show();
                db.execSQL("INSERT INTO FALL_CANCEL VALUES(CURRENT_TIMESTAMP)");

                counter = 4;
            } else {
                counter++;
            }
        }else{
            Toast.makeText(this,"You were not authorized",Toast.LENGTH_SHORT).show();
        }
    }

    public void sendSms(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

}
