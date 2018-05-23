package com.example.samue.login;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;


public class Recursos extends AppCompatActivity {
    private Dialog mdialog;
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

        boolean listener = extras.getBoolean("listener");
        final String sendTo = extras.getString("sendTo");

        if(listener){
            shared.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Toast.makeText(Recursos.this, "has clickado " + listaNombresArchivos.get(position), Toast.LENGTH_SHORT).show();
                    final String name = listaNombresArchivos.get(position).toString();

                    mdialog = new Dialog(Recursos.this);
                    mdialog.setContentView(R.layout.dialog_confirmsharedarchive);
                    mdialog.show();

                    TextView tv = (TextView) mdialog.findViewById(R.id.confirm_archive_tv);
                    tv.setText("Do you want to download " + name + "?");

                    Button yes = (Button) mdialog.findViewById(R.id.confirm_archive_yes);
                    Button no = (Button) mdialog.findViewById(R.id.confirm_archive_no);

                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mdialog.dismiss();
                        }
                    });

                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mdialog.dismiss();
                            Uri dato = Uri.parse("content://name/" + name);
                            Intent resultado = new Intent(null, dato);
                            resultado.putExtra("name", name);
                            resultado.putExtra("sendTo", sendTo);
                            setResult(RESULT_OK, resultado);
                            finish();
                        }
                    });
                }
            });
        }else{
            shared.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final String name = listaNombresArchivos.get(position).toString();

                    mdialog = new Dialog(Recursos.this);
                    mdialog.setContentView(R.layout.dialog_confirmsharedarchive);
                    mdialog.show();

                    TextView tv = (TextView) mdialog.findViewById(R.id.confirm_archive_tv);
                    tv.setText("Do you want to erase " + name + "?");

                    Button yes = (Button) mdialog.findViewById(R.id.confirm_archive_yes);
                    Button no = (Button) mdialog.findViewById(R.id.confirm_archive_no);

                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mdialog.dismiss();
                        }
                    });

                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mdialog.dismiss();
                            Uri dato = Uri.parse("content://name/" + name);
                            Intent resultado = new Intent(null, dato);
                            resultado.putExtra("name", name);
                            setResult(RESULT_OK, resultado);
                            finish();
                        }
                    });
                }
            });
        }

        adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaNombresArchivos);
        shared.setAdapter(adaptador);
    }



}
