package com.example.evan.scout;

import android.app.Application;

import com.instabug.library.IBGInvocationEvent;
import com.instabug.library.Instabug;

public class ScoutApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        new Instabug.Builder(this, "c178ad40771c0f46fc047a11b88fbc38")
                .setInvocationEvent(IBGInvocationEvent.IBGInvocationEventShake)
                .build();
    }
}
