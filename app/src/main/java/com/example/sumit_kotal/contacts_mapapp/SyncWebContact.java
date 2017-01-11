package com.example.sumit_kotal.contacts_mapapp;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.sumit_kotal.contacts_mapapp.R.id.latitude;
import static com.example.sumit_kotal.contacts_mapapp.R.id.longitude;

@SuppressWarnings("ALL")
public class SyncWebContact extends AppCompatActivity {

    // URL to get contacts JSON
    private static String url = "http://private-b08d8d-nikitest.apiary-mock.com/contacts";
    public ProgressDialog pDialog;
    public ListView lv;
    Button button, button2;

    String email, name, phone, ofc, lat, lon, address;
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
                saveData();
            }
        });


        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SyncWebContact.this, Main2Activity.class));
                finish();
            }
        });

        new GetCont().execute();

    }

    private void saveData() {

        for (int i = 0; i < conList.size(); i++) {

            HashMap<String, String> cont = conList.get(i);

            String names = cont.get("name");
            String mails = cont.get("email");
            String ph = cont.get("phone");
            String ofcph = cont.get("officePhone");
            double lat = Double.parseDouble(cont.get("latitude"));
            double lon = Double.parseDouble(cont.get("longitude"));
            String add = cont.get("address");

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();


            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            );

            //------------------------------------------------------ Names
            if (names != null) {
                ops.add(ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                names).build()
                );
            }

            //------------------------------------------------------ Mobile Number
            if (ph != null) {
                ops.add(ContentProviderOperation.
                        newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, ph)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build()
                );
            }

            //------------------------------------------------------ Work Numbers
            if (ofcph != null) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, ofcph)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                        .build());
            }

            //------------------------------------------------------ Email
            if (mails != null) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, mails)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                        .build());
            }

            //------------------------------------------------------ Address
            if (add != null) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.DATA, add)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME)
                        .build());
            }

            // Asking the Contact provider to create a new contact
            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                Toast.makeText(this, "" + names + " added to your Contacts", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                //  Toast.makeText(myContext, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public JSONObject getLocationInfo(double lat, double lng) {

        HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&sensor=false");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (IOException ignored) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
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

            String jsonStr = sh.makeServiceCall(url);


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

                        // get lat and lng value
                        JSONObject ret = getLocationInfo(Double.parseDouble(lat), Double.parseDouble(lon));
                        JSONObject location;
                        try {

                            location = ret.getJSONArray("results").getJSONObject(0);
                            // Get the value of the attribute whose name is "formatted_string"
                            address = location.getString("formatted_address");
                            address = address.replace("Unnamed Road, ", "");

                        } catch (JSONException e1) {
                            e1.printStackTrace();

                        }


                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value

                        contact.put("name", name);
                        contact.put("email", email);
                        contact.put("phone", phone);
                        contact.put("officePhone", ofc);
                        contact.put("latitude", lat);
                        contact.put("longitude", lon);
                        contact.put("address", address);
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
                    "phone", "officePhone", "latitude", "longitude", "address"}, new int[]{R.id.name,
                    R.id.email, R.id.mobile, R.id.officephone, latitude, longitude, R.id.address});

            lv.setAdapter(adapter);
        }

    }

}

