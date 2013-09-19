package org.geekosphere.sharewithwoot;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ShareActivity extends Activity {

    private final static String HAL_HOST = "apoc.cc";
    private final static int HAL_PORT = 7272;
    private final static String HAL_KEY = "ROFGNIKOOLERAUOYSDIORDEHTTON";
    private final static String HAL_CHANNEL = "#woot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (savedInstanceState == null && intent != null) {
            if (intent.getAction().equals(Intent.ACTION_SEND)) {
                final String url = intent.getStringExtra(Intent.EXTRA_TEXT);

                // might be empty
                final String title = intent.getStringExtra(Intent.EXTRA_SUBJECT);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        shareWithWoot(url, title);
                    }
                });
                thread.start();
            }
        }

        finish();
    }

    private void shareWithWoot(String url, String title) {
        StringBuilder packet = new StringBuilder();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString("KEY_NAME", "changeme");

        packet.append(HAL_KEY);
        packet.append("|");
        packet.append(HAL_CHANNEL);
        packet.append("|");
        packet.append(String.format("\002%s just shared:\017 %s", name, url));
        if (title != null && !title.equals(""))
            packet.append(String.format(" (%s)", title));

        final String message; 
        if (sendUdpString(packet.toString(), HAL_HOST, HAL_PORT)) {
            message = "Successfully shared with " + HAL_CHANNEL + "!";
        }
        else {
            message = "An error occured, try again later!";
        }
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }});
    }

    private boolean sendUdpString(String message, String host, int port) {
        byte[] rawBytes = message.getBytes();
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress local = InetAddress.getByName(host);
            DatagramPacket packet = new DatagramPacket(rawBytes, rawBytes.length, local, port);
            socket.send(packet);
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
        catch (SocketException e) {
            e.printStackTrace();
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            if (socket != null) {
                socket.close();
            }
        }
        return true;
    }

}
