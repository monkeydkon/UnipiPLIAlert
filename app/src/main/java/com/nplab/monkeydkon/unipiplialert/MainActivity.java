package com.nplab.monkeydkon.unipiplialert;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    SensorManager sensorManager;
    Sensor lightSensor;
    Sensor accelerometerSensor;
    //timer
    Timer timer = new Timer();
    boolean shown = false;
    boolean fallShown = false;
    //SQLite DATABASE
    SQLiteDatabase db;

    LocationManager locationManager;

    LocationListener locationListener;

    // SMS TO BE SENT ONLY ONCE BECAUSE ON LOCATION CHANGED WOULD SEND INFINITE
    boolean alreadyExecuted = false;

    //for contacts
    SharedPreferences sharedPreferences;

    //TEXT TO SPEECH FOR SOS
    MyTts tts;

    public TextView textViewX,textViewY,textViewZ,textViewTotal;

    //COUNT DOWN TIMER DECLARATION FOR FALL
    CountDownTimer countDownTimer;

    ToneGenerator beep;

    Button abortButton,register;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //light
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
        //accelerometer
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometerSensor , SensorManager.SENSOR_DELAY_NORMAL);

        //FOR GPS
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        //SQLITE CREATE DATABASE
        db = openOrCreateDatabase("UNIPLI_ALERT", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS LIGHT(light_limit TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS SOS(sos_message TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS FALL(fell TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS FALL_CANCEL(canceled TEXT)");


        //SHARED PREFERENCES FOR SMS
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putString("number1","+306987729343");
        editor.putString("number2","+306987729343");
        editor.commit();

        // FOR TTS
        tts = new MyTts(getApplicationContext());

        //HIDE ABORT BUTTON
        abortButton = (Button) findViewById(R.id.button);
        register = (Button) findViewById(R.id.register);





        // T E X T V I E W S
        textViewX = (TextView) findViewById(R.id.textViewX);
        textViewY = (TextView) findViewById(R.id.textViewY);
        textViewZ = (TextView) findViewById(R.id.textViewZ);
        textViewTotal= (TextView) findViewById(R.id.textViewTotal);


        beep = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);





    }



    // A C T I V I T Y    1    B U T T O N S



    // LANGUAGE CHANGE
    public void language(View view){

        Intent intent = new Intent(this,Main5Activity.class);
        startActivity(intent);

    }


    // ABORT
    public void abort(View view){

        countDownTimer.cancel();
        Intent intent = new Intent(this,Main3Activity.class);
        startActivity(intent);

    }


    //REGISTER
    public void register(View view){
        Intent intent = new Intent(this,Main4Activity.class);
        startActivity(intent);
    }

    //SOS
    public void sos(View view){

        ssos();

    }



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        // INFORM THE USER THAT HE IS IN MORE LIGHT THAN 10.000
        // USE TIMER TO DO THIS ONLY AFTER 60.000 ms (one minute)
        // WRITE THIS EVENT INTO A DATABASE

        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_LIGHT) {


        if(sensorEvent.values[0] > 10000f){
            if(!shown) {
                db.execSQL("INSERT INTO LIGHT VALUES(CURRENT_TIMESTAMP)");
                Toast.makeText(this, "DANGER, you are in too much light!!", Toast.LENGTH_LONG).show();
                shown = true;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        shown = false;

                    }
                }, 60000);
            }
        }
        }

        if(mySensor.getType()==Sensor.TYPE_ACCELEROMETER){



                textViewX.setText("X: "+sensorEvent.values[0]);
                textViewY.setText("Y: "+sensorEvent.values[1]);
                textViewZ.setText("Z: "+sensorEvent.values[2]);

                double x = sensorEvent.values[0];
                double y = sensorEvent.values[1];
                double z = sensorEvent.values[2];
                double totalAccel = Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));

                textViewTotal.setText("Total: "+Double.toString(totalAccel));

                DecimalFormat precision = new DecimalFormat("0.00");
                double ldAccRound = Double.parseDouble(precision.format(totalAccel));

                if (ldAccRound > 0.3d && ldAccRound < 0.5d) {

                    if(!fallShown) {


                        // FIRST WRITE EVENT INTO DB
                        db.execSQL("INSERT INTO FALL VALUES(CURRENT_TIMESTAMP)");

                        //SHOW BUTTON
                        abortButton.setVisibility(View.VISIBLE);
                        register.setVisibility(View.INVISIBLE);



                        // COUNTDOWN

                        countDownTimer = new CountDownTimer(30000, 1000){

                            @Override
                            public void onTick(long l) {

                                beep.startTone(ToneGenerator.TONE_PROP_BEEP);

                            }

                            @Override
                            public void onFinish() {

                                ssos();

                            }
                        }.start();

                        fallShown = true;
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                fallShown = false;

                            }
                        }, 2500);

                    }

                }




        }

    }

    //

    // SEND SMS
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

    public void ssos(){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED)
        {
            //ask for permission

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.SEND_SMS}, 1);

        }else{
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Toast.makeText(this,"GPS is not enabled", LENGTH_SHORT).show();
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                alreadyExecuted = false;
                onLocationChanged(location);

                //SOS EVENT SAVE INTO TABLE
                db.execSQL("INSERT INTO SOS VALUES(CURRENT_TIMESTAMP)");

                // SOS TTS TO RUN 10 TIMES WITH 1 SECOND DELAY
                for(int i=1; i<10; i++) {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            tts.speak("I NEED HELP!");


                        }
                    }, 1000);


                }
            }
        }

    }


    // O V E R R I D E S

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    @Override
    public void onLocationChanged(Location location) {
        String x = String.valueOf(location.getLatitude());
        String y = String.valueOf(location.getLongitude());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        String number1 = (sharedPreferences.getString("number1", ""));
        String number2 = (sharedPreferences.getString("number2", ""));


        if(!alreadyExecuted) {
            sendSms(number1,"im at x: "+x+ " and y: "+y);
            sendSms(number2,"im at x: "+x+ " and y: "+y);

            alreadyExecuted = true;
        }


    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }






}


