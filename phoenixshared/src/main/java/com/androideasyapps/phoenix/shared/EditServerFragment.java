package com.androideasyapps.phoenix.shared;


import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androideasyapps.phoenix.dao.Server;

import butterknife.ButterKnife;
import rx.android.observables.AndroidObservable;
import rx.functions.Action0;
import rx.functions.Action1;

import com.androideasyapps.phoenix.services.sagetv.SageTVService;
import com.androideasyapps.phoenix.services.sagetv.model.Result;
import com.androideasyapps.phoenix.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class EditServerFragment extends DialogFragment {
    private static final Logger log  = LoggerFactory.getLogger(EditServerFragment.class);

    public interface OnConfigured {
        public void onConfigured(Server server);
    }

    Server server;

    EditText displayName;
    EditText host;
    EditText port;
    EditText username;
    EditText password;

    Button btnConnect;
    private boolean connected;

    public EditServerFragment() {
        super();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        server=new Server();

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_server, container, false);

        displayName = ButterKnife.findById(v, R.id.displayName);
        host = ButterKnife.findById(v, R.id.host);
        port = ButterKnife.findById(v, R.id.port);
        username = ButterKnife.findById(v, R.id.username);
        password = ButterKnife.findById(v, R.id.password);
        btnConnect = ButterKnife.findById(v, R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });

        getDialog().setTitle("SageTV Server");
        return v;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void connect() {
        connected=false;
        server.setTitle(displayName.getText().toString());
        server.setHost(host.getText().toString());
        server.setPort(Util.parseInt(port.getText().toString()));
        server.setUsername(username.getText().toString());
        server.setPassword(password.getText().toString());

        server.setServerId("default");
        server.setIsDefault(true);

        log.info("Connecting to Server: " + server.getHost());

        SageTVService service = AppInstance.getInstance(this.getActivity()).getSageTVService(server);
        btnConnect.animate().alpha(0).setDuration(500).start();

        AndroidObservable.bindFragment(this, service.getDatabaseLastModified("TV")).subscribe(new Action1<Result<Number>>() {
            @Override
            public void call(Result<Number> numberResult) {
                if (numberResult == null) {
                    onConnectFailed(new Throwable("connection failed"));
                } else {
                    onConnectOK();
                }

            }

        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                onConnectFailed(throwable);
            }
        }, new Action0() {
            @Override
            public void call() {
                if (!connected) {
                    onConnectFailed(new Throwable("connection did not work"));
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void onConnectOK() {
        log.info("Connect OK");
        try {
            AppInstance.getInstance(this.getActivity()).futureDAOManager.get().getServerDAO().save(server);
            connected=true;
            AppInstance.getInstance(this.getActivity()).setServer(server);
            Toast.makeText(getActivity(), R.string.msg_connection_ok, Toast.LENGTH_LONG).show();
            dismiss();
            ((OnConfigured)getActivity()).onConfigured(server);
        } catch (Exception e) {
            log.error("Save Server Failed", e);
        }
    }

    private void onConnectFailed(Throwable t) {
        btnConnect.setAlpha(1.0f);
        log.error("We Did Not Connect", t);
        Toast.makeText(getActivity(), R.string.msg_connection_failed, Toast.LENGTH_LONG).show();
    }


}
