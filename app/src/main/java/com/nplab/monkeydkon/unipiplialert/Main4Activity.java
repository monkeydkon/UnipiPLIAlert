package com.nplab.monkeydkon.unipiplialert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Main4Activity extends AppCompatActivity {

    EditText editText4, editText5;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        editText4 = (EditText)findViewById(R.id.editText4);
        editText5 = (EditText)findViewById(R.id.editText5);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public void register(View view){
        if (editText4.getText().toString().matches("") || editText5.getText().toString().matches("")){
            Toast.makeText(this,"You left a field empty. Please try again.",Toast.LENGTH_SHORT).show();
        }else{

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username",editText4.getText().toString());
            editor.putString("password",editText5.getText().toString());
            editor.commit();
            Toast.makeText(this,"Your data are saved",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this,Main3Activity.class);
            intent.putExtra("username",editText4.getText().toString());
            intent.putExtra("password",editText5.getText().toString());

            Intent intent2 = new Intent(this,MainActivity.class);
            intent2.putExtra("registered","yes");
            startActivity(intent2);


        }
    }
}
