package com.example.samue.login;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import java.util.ArrayList;


public class Recursos extends AppCompatActivity {
    private ArrayList listaNombresArchivos;
    private ArrayAdapter<String> adaptador;
    private ListView shared;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recursos);
        Bundle extras = getIntent().getExtras();
        shared = (ListView) findViewById(R.id.shared);

        listaNombresArchivos = extras.getParcelableArrayList("lista"); //TODO poner los listeners para la lista

        adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaNombresArchivos);
        shared.setAdapter(adaptador);
    }



}
