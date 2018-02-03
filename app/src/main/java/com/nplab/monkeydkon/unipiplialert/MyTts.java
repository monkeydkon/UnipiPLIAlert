package com.nplab.monkeydkon.unipiplialert;

/**
 * Created by monkeydkon on 27/12/2017.
 */

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class MyTts {

    public TextToSpeech tts;
    public TextToSpeech.OnInitListener initListener=new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status==TextToSpeech.SUCCESS)
                tts.setLanguage(Locale.US);
        }
    };
    public MyTts(Context context){

        tts=new TextToSpeech(context,initListener);

    }



    public void speak(String string){

        tts.speak(string,TextToSpeech.QUEUE_ADD,null);

    }
}
