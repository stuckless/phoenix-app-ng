package com.androideasyapps.phoenix.shared;

import android.util.Base64;

import com.androideasyapps.phoenix.util.Base64EncoderProvider;

import java.io.UnsupportedEncodingException;

/**
 * Created by seans on 21/12/14.
 */
public class AndroidBase64Encoder implements Base64EncoderProvider {
    @Override
    public String encode(String in) {
        try {
            return Base64.encodeToString(in.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
