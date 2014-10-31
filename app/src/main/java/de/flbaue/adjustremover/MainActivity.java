package de.flbaue.adjustremover;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity {

    private String macAddress;
    private String androidId;
    private String appToken;
    private String response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        macAddress = getMacAddress();
        androidId = getAndroidId();
        ((EditText) findViewById(R.id.androidId)).setText(androidId);
        ((EditText) findViewById(R.id.macAddress)).setText(macAddress);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getMacAddress() {
        WifiManager wifiMan = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        return wifiInf.getMacAddress();
    }

    public String getAndroidId() {
        return android.provider.Settings.Secure.getString(getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
    }

    public String getAppToken() {
        return ((EditText) findViewById(R.id.adjustAppToken)).getText().toString();
    }

    public void removeDevice(View view) throws InterruptedException {
        appToken = getAppToken();
        if (appToken == null || appToken.isEmpty()) {
            ((EditText) findViewById(R.id.adjustAppToken)).setText("Insert App Token first!");
            return;
        }

        new httpConnectionTask().execute();
        while (response == null || response.isEmpty()) {
            Thread.sleep(500);
        }
        ((TextView) findViewById(R.id.response)).setText(response);
    }

    class httpConnectionTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://app.adjust.io/forget_device?app_token=" + appToken + "&mac=" + macAddress + "&android_id=" + androidId);
                urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                String result = "";
                while((line = reader.readLine()) != null){
                    result += line + "\n";
                }
                response = result;
            } catch (IOException e) {
            } finally {
                urlConnection.disconnect();
            }
            return null;
        }
    }
}
