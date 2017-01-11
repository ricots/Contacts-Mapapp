package com.example.sumit_kotal.contacts_mapapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;


public class ContactMap extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    View view;
    ArrayList<String> Name = new ArrayList<String>();
    ArrayList<String> Phone = new ArrayList<String>();
    ArrayList<String> Email = new ArrayList<String>();
    ArrayList<LatLng> LatLng = new ArrayList<LatLng>();


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    // Google Map
    private SupportMapFragment googleMap;

    public ContactMap() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactMap.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactMap newInstance(String param1, String param2) {
        ContactMap fragment = new ContactMap();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_contact_map, container, false);

        readContacts();

        try {
            // Loading map
            FragmentManager fragmentManager = getChildFragmentManager();

            googleMap = ((SupportMapFragment) fragmentManager.findFragmentById(R.id.map));

            googleMap.getMapAsync(this);

            // check if map is created successfully or not
            if (googleMap == null) {
                googleMap = SupportMapFragment.newInstance();
                fragmentManager.beginTransaction().replace(R.id.map, googleMap).commit();
                Toast.makeText(getActivity().getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;

    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        if (strAddress != null) {
            try {
                address = coder.getFromLocationName(strAddress, 5);
                if (address == null) {
                    return null;
                }
                Address location = address.get(0);
                location.getLatitude();
                location.getLongitude();

                p1 = new LatLng(location.getLatitude(), location.getLongitude());

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return p1;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


        for (int i = 0; i < LatLng.size(); i++) {
            LatLng lat = LatLng.get(i);
            MarkerOptions mark = new MarkerOptions().position(lat)
                    .title("Contact Details").snippet("Name : " + Name.get(i) + "\nPhone : " + Phone.get(i) + "\nEmail : " + Email.get(i));

            googleMap.addMarker(mark);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(lat));
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    LinearLayout info = new LinearLayout(getActivity());
                    info.setOrientation(LinearLayout.VERTICAL);

                    TextView title = new TextView(getActivity());
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(getActivity());
                    snippet.setTextColor(Color.GRAY);
                    snippet.setGravity(Gravity.CENTER);
                    snippet.setText(marker.getSnippet());

                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });
        }
    }

    public void readContacts() {

        String name = null, phone = null, email = null, address = null;


        ContentResolver cr = getActivity().getContentResolver();
        Cursor cur = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ECLAIR) {
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);
        }

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    if (name != null)
                        Name.add(name);
                    else
                        Name.add(" ");

                    // get the phone number
                    Cursor pCur = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ECLAIR) {
                        pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                    }
                    while (pCur.moveToNext()) {
                        phone = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        if (phone != null)
                            Phone.add(phone);
                        else
                            Phone.add(" ");
                    }
                    pCur.close();


                    // get email

                    Cursor emailCur = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ECLAIR) {
                        emailCur = cr.query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                    }
                    while (emailCur.moveToNext()) {

                        email = emailCur.getString(
                                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

                        if (email != null)
                            Email.add(email);
                        else
                            Email.add(" ");
                    }
                    emailCur.close();

                    //Get Postal Address....

                    String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                    String[] addrWhereParams = new String[]{id,
                            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
                    Cursor addrCur = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                        addrCur = cr.query(ContactsContract.Data.CONTENT_URI,
                                null, addrWhere, addrWhereParams, null);
                    }
                    while (addrCur.moveToNext()) {
                        String poBox = "";
                        String street = "";
                        String city = "";
                        String state = "";
                        String postalCode = "";
                        String country = "";

                        if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX)) != null)
                            poBox = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));

                        if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)) != null)
                            street = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));

                        if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)) != null)
                            city = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));

                        if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)) != null)
                            state = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));

                        if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)) != null)
                            postalCode = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));

                        if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)) != null)
                            country = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));

                        address = poBox + street + city + state + postalCode + country;


                        LatLng lt = getLocationFromAddress(getActivity(), address);

                        LatLng.add(lt);

                    }
                    addrCur.close();

                }
            }
        }
    }
}
