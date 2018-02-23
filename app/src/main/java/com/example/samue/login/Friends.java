package com.example.samue.login;

public class Friends {
    String nombre;
    int img;

    public Friends(){}

    public Friends(String nombre, int img) {
        this.nombre = nombre;
        this.img = img;
    }

    public String getNombre(){return this.nombre;}

    public int getImg(){return this.img;}
}
