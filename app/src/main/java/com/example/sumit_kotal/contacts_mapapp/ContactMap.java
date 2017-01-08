package com.example.sumit_kotal.contacts_mapapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;
import java.util.HashMap;


public class ContactMap extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // Google Map
    private SupportMapFragment googleMap;
    View view;

    private String TAG = Main2Activity.class.getSimpleName();

    private ProgressDialog pDialog;


    String email,name,phone,ofc,lat,lon;

    // URL to get contacts JSON
    private static String url = "http://private-b08d8d-nikitest.apiary-mock.com/contacts";

    ArrayList<HashMap<String, String>> contactList;

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
        view= inflater.inflate(R.layout.fragment_contact_map, container, false);

        try {
            // Loading map
                FragmentManager fragmentManager = getChildFragmentManager();

                googleMap = ((SupportMapFragment) fragmentManager.findFragmentById(R.id.map));

                // check if map is created successfully or not
                if (googleMap == null) {
                    googleMap = SupportMapFragment.newInstance();
                    fragmentManager.beginTransaction().replace(R.id.map,googleMap).commit();
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                            .show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }



        return view;

    }



}
