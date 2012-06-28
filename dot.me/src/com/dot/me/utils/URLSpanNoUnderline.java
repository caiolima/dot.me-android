package com.dot.me.utils;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.URLSpan;

public class URLSpanNoUnderline extends URLSpan {
	
    public URLSpanNoUnderline(String url) {
        super(url);
    }
    @Override public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        // ds.setColor(Color.argb(0, 178, 223, 238));
       ds.setColor(Color.parseColor("#4682B4"));
    }
}