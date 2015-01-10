package com.androideasyapps.phoenix.services.sagetv;

import com.androideasyapps.phoenix.util.Base64EncoderProvider;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import retrofit.RequestInterceptor;

/**
 * Created by seans on 20/12/14.
 */
public class SageTVRequestInterceptor implements RequestInterceptor {
    private final Credentials creds;
    private final Base64EncoderProvider base64;

    public static class Credentials {
        private final String user;
        private final String pass;

        public Credentials(String user, String pass) {
            this.user=user;
            this.pass=pass;
        }

        public String getUsername() {
            return user;
        }
        public String getPassword() {
            return pass;
        }
    }

    public SageTVRequestInterceptor(Credentials creds, Base64EncoderProvider base64EncoderProvider) {
        this.creds=creds;
        this.base64=base64EncoderProvider;
    }

    @Override
    public void intercept(RequestFacade request) {
        String authValue = base64.encode(creds.getUsername() + ":" + creds.getPassword());
        request.addHeader("Authorization", "Basic " + authValue);
    }

    public void addAuthHeader(Map<String,String> headers) {
        String authValue = base64.encode(creds.getUsername() + ":" + creds.getPassword());
        headers.put("Authorization", "Basic " + authValue);
    }
}
