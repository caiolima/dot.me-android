package com.twittemarkup.app;

import android.app.Activity;
import android.os.Bundle;
import com.twittemarkup.app.R;

public class TwitteMarkupActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}