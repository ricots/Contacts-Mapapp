package com.example.sumit_kotal.contacts_mapapp;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.sumit_kotal.contacts_mapapp.R.id.latitude;
import static com.example.sumit_kotal.contacts_mapapp.R.id.longitude;

public class SyncWebContact extends AppCompatActivity {

    // URL to get contacts JSON
    private static String url = "http://private-b08d8d-nikitest.apiary-mock.com/contacts";
    public ProgressDialog pDialog;
    public ListView lv;
    Button button;

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
            String add = null;

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
                //Toast.makeText(this, ""+names+" added", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                //  Toast.makeText(myContext, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
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

            //Log.e(TAG, "Response from url: " + jsonStr);

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

/*
                        HttpHandler sh2 = new HttpHandler();

                        String jsonStr2 = sh2.makeServiceCall("http://maps.googleapis.com/maps/api/geocode/json?latlng="+lat+","+lon+"&sensor=true");

                        if (jsonStr2 != null) {
                            try {

                                JSONObject m=new JSONObject(jsonStr2);

                                JSONArray a=m.getJSONArray("results");

                                JSONObject n=a.getJSONObject(0);

                                address=n.getString("formatted_address");

                            } catch (final JSONException e) {
                                Log.e(TAG, "Json parsing error: " + e.getMessage());
                            }
                        }
*/
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
                    "phone", "officePhone", "latitude", "longitude"}, new int[]{R.id.name,
                    R.id.email, R.id.mobile, R.id.officephone, latitude, longitude});

            lv.setAdapter(adapter);
        }

    }

}

