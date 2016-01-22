package com.test.tabs.tabs.com.tabs.activity;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

/**
 * Created by schan on 1/14/16.
 */
public class ParseApplication extends Application{


    @Override
    public void onCreate(){
        super.onCreate();
        Parse.enableLocalDatastore(this);
        //Parse.initialize(this);
        Parse.initialize(this, "HEIu8jAXO0b0Bf0558cVlWrEyIOIZUxPnPG2LM8o", "NtfN1EEPraxrei8RgxWy4HWz69jCVqIZCddQkHCN");

        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
        System.out.println("On create in application");

        //Initialize push notifications for parse
        ParseInstallation.getCurrentInstallation().saveInBackground();

        //save parse

    }

}
