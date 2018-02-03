package com.nplab.monkeydkon.unipiplialert;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.Locale;


public class Main5Activity extends AppCompatActivity {



    int langpos = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);
    }

    public void changelang(View view){

        switch (langpos){
            case 0:
                updateconfig("en");
                langpos = 1;
                break;
            case 1:
                updateconfig("de");
                langpos = 2;
                break;
            case 2:
                updateconfig("el");
                langpos = 0;
                break;

        }



    }

    public void updateconfig(String s){
        Locale locale=new Locale(s);
        Locale.setDefault(locale);
        Configuration configuration=new Configuration();
        configuration.locale=locale;
        getBaseContext().getResources().updateConfiguration(configuration,
                getBaseContext().getResources().getDisplayMetrics());
        Bundle bundle=new Bundle();
        onCreate(bundle);
        setTitle(R.string.app_name);
    }
}
