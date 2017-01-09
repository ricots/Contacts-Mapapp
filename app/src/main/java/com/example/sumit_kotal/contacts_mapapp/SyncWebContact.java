package com.example.sumit_kotal.contacts_mapapp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SyncWebContact extends AppCompatActivity {

    // URL to get contacts JSON
    private static String url = "http://private-b08d8d-nikitest.apiary-mock.com/contacts";
    public ProgressDialog pDialog;
    public ListView lv;
    Button button;

    String email, name, phone, ofc, lat, lon;
    ArrayList<HashMap<String, String>> conList;
    private String TAG = SyncWebContact.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_web_contact);

        conList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SyncWebContact.this, "Button Pressed", Toast.LENGTH_SHORT).show();
            }
        });
        new GetCont().execute();
    }


    /**
     * Async task class to get json by making HTTP call
     */
    private class GetCont extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(SyncWebContact.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {

                    JSONArray jsonArray = new JSONArray(jsonStr);
                    JSONObject jsonObj = jsonArray.getJSONObject(0);

                    // Getting JSON Array node
                    JSONArray contacts = jsonObj.getJSONArray("contacts");

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        if (c.has("name")) name = c.getString("name");
                        if (c.has("email")) email = c.getString("email");
                        else email = "";
                        if (c.has("phone")) phone = c.getString("phone");
                        else phone = "";
                        if (c.has("officePhone")) ofc = c.getString("officePhone");
                        else ofc = "";
                        if (c.has("latitude")) lat = c.getString("latitude");
                        if (c.has("longitude")) lon = c.getString("longitude");


                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value

                        contact.put("name", name);
                        contact.put("email", email);
                        contact.put("phone", phone);
                        contact.put("officePhone", ofc);
                        contact.put("latitude", lat);
                        contact.put("longitude", lon);

                        // adding contact to contact list
                        conList.add(contact);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    SyncWebContact.this, conList, R.layout.list_item, new String[]{"name", "email",
                    "phone", "officePhone", "latitude", "longitude"}, new int[]{R.id.name,
                    R.id.email, R.id.mobile, R.id.officephone, R.id.latitude, R.id.longitude});

            lv.setAdapter(adapter);
        }

    }

}

