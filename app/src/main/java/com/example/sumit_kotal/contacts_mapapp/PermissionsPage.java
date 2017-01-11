package com.example.sumit_kotal.contacts_mapapp;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

public class PermissionsPage extends RuntimePermissionsActivity {


    private static final int REQUEST_PERMISSIONS = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_page);

        PermissionsPage.super.requestAppPermissions(new
                        String[]{android.Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, R.string
                        .runtime_permissions_txt
                , REQUEST_PERMISSIONS);

    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        startActivity(new Intent(PermissionsPage.this, SyncWebContact.class));
        finish();
    }
}
