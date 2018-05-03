package com.example.samue.login;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArchiveExplorer extends AppCompatActivity {
    private Dialog mdialog;
    private List listaNombresArchivos;
    private List listaRutasArchivos;
    private ArrayAdapter adaptador;
    private String directorioRaiz;
    private TextView carpetaActual;
    private ListView listaItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_explorer);

        carpetaActual = (TextView) findViewById(R.id.rutaActual);
        listaItems = (ListView) findViewById(R.id.lista_items);

        directorioRaiz = Environment.getExternalStorageDirectory().getPath();

        verArchivosDirectorio(directorioRaiz);

        listaItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File archivo = new File((String)listaRutasArchivos.get(position));

                // Si es un archivo se muestra un Toast con su nombre y si es un directorio
                // se cargan los archivos que contiene en el listView
                if (archivo.isFile()) {
                    //Toast.makeText(ArchiveExplorer.this, "Has seleccionado el archivo: " + archivo.getName(),Toast.LENGTH_LONG).show();
                    final String name = archivo.getName();
                    final String path = archivo.getPath();

                    /*if(name.contains(".jpeg") || name.contains(".jpg") || name.contains(".png")){

                    }*/

                    mdialog = new Dialog(ArchiveExplorer.this);
                    mdialog.setContentView(R.layout.dialog_confirmsharedarchive);
                    mdialog.show();

                    TextView tv = (TextView) mdialog.findViewById(R.id.confirm_archive_tv);
                    tv.setText("Do you want to share " + archivo.getName() + " with your friends?");

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
                            final ProgressDialog progressDialog = new ProgressDialog(ArchiveExplorer.this);
                            progressDialog.setIndeterminate(true);
                            progressDialog.setMessage("uploading " + name + "...");
                            progressDialog.show();

                            new android.os.Handler().postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            // On complete call either onLoginSuccess or onLoginFailed
                                            Uri dato = Uri.parse("content://name/" + name);

                                            Intent resultado = new Intent(null, dato);
                                            resultado.putExtra("name", name);
                                            resultado.putExtra("path", path);
                                            setResult(RESULT_OK, resultado);
                                            finish();
                                            progressDialog.dismiss();
                                        }
                                    }, 2000);
                        }
                    });

                } else {
                    // Si no es un directorio mostramos todos los archivos que contiene
                    verArchivosDirectorio((String)listaRutasArchivos.get(position));
                }
            }
        });
    }

    private void verArchivosDirectorio(String rutaDirectorio) {
        carpetaActual.setText("Estas en: " + rutaDirectorio);
        listaNombresArchivos = new ArrayList();
        listaRutasArchivos = new ArrayList();
        File directorioActual = new File(rutaDirectorio);
        if(!directorioActual.exists()){
            return;
        }
        File[] listaArchivos = directorioActual.listFiles();

        int x = 0;

        if (listaArchivos == null) {
            Toast.makeText(ArchiveExplorer.this, "No se puede acceder",Toast.LENGTH_LONG).show(); return;
        }

        // Si no es nuestro directorio raiz creamos un elemento que nos
        // permita volver al directorio padre del directorio actual
        if (!rutaDirectorio.equals(directorioRaiz)) {
            listaNombresArchivos.add("../");
            listaRutasArchivos.add(directorioActual.getParent());
            x = 1;
        }

        // Almacenamos las rutas de todos los archivos y carpetas del directorio
        for (File archivo : listaArchivos) {
            listaRutasArchivos.add(archivo.getPath());
        }

        // Ordenamos la lista de archivos para que se muestren en orden alfabetico
        Collections.sort(listaRutasArchivos, String.CASE_INSENSITIVE_ORDER);


        // Recorredos la lista de archivos ordenada para crear la lista de los nombres
        // de los archivos que mostraremos en el listView
        for (int i = x; i < listaRutasArchivos.size(); i++){
            File archivo = new File((String)listaRutasArchivos.get(i));
            if (archivo.isFile()) {
                listaNombresArchivos.add(archivo.getName());
            } else {
                listaNombresArchivos.add("/" + archivo.getName());
            }
        }

        // Si no hay ningun archivo en el directorio lo indicamos
        if (listaArchivos.length < 1) {
            listaNombresArchivos.add("No hay ningun archivo");
            listaRutasArchivos.add(rutaDirectorio);
        }


        // Creamos el adaptador y le asignamos la lista de los nombres de los
        // archivos y el layout para los elementos de la lista
        adaptador = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listaNombresArchivos);
        listaItems.setAdapter(adaptador);
    }
}
