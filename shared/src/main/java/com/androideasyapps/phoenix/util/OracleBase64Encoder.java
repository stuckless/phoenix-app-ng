package com.androideasyapps.phoenix.util;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * Created by seans on 21/12/14.
 */
public class OracleBase64Encoder implements Base64EncoderProvider {
    @Override
    public String encode(String in) {
        try {
            return new String(Base64.getEncoder().encode(in.getBytes("UTF-8")), "UTF-8)");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
