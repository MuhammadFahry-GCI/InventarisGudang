package com.inventaris.model;

public class Supplier {
    private int idSupplier;
    private String namaSupplier;
    private String kontak;
    private String alamat;

    public Supplier(int idSupplier, String namaSupplier, String kontak, String alamat) {
        this.idSupplier = idSupplier;
        this.namaSupplier = namaSupplier;
        this.kontak = kontak;
        this.alamat = alamat;
    }

    public int getIdSupplier() {
        return idSupplier;
    }

    public String getNamaSupplier() {
        return namaSupplier;
    }

    public String getKontak() {
        return kontak != null ? kontak : "-";
    }

    public String getAlamat() {
        return alamat != null ? alamat : "-";
    }
}