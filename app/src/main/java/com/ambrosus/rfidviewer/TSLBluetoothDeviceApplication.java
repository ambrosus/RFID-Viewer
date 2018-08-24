//----------------------------------------------------------------------------------------------
// Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package com.ambrosus.rfidviewer;

import android.app.Application;

import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import model.AmbrosusSDK;

public class TSLBluetoothDeviceApplication extends Application {

    private final String KEY_PATH = "key";
    private static AsciiCommander commander = null;
    private AmbrosusSDK ambSdk;


    public void setupAmbrosusSDK() {
        String line = null;

        BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.private_key)));
        try {
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (line != null) {
            ambSdk = new AmbrosusSDK(
                    new AmbrosusSDK.Config(
                            "https://gateway-test.ambrosus.com",
                            line
                    )
            );
            ambSdk.init();
        }
    }

    public AmbrosusSDK ambrosusSDK() {
        return ambSdk;
    }

    /// Returns the current AsciiCommander
    public AsciiCommander getCommander() {
        return commander;
    }

    /// Sets the current AsciiCommander
    public void setCommander(AsciiCommander _commander) {
        commander = _commander;
    }


}
