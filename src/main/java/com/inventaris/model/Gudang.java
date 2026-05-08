package com.inventaris.model;

public class Gudang {
    private int idGudang;
    private String namaGudang;
    private String lokasi;
    private String penanggungJawab;

    public Gudang(int idGudang, String namaGudang, String lokasi, String penanggungJawab) {
        this.idGudang = idGudang;
        this.namaGudang = namaGudang;
        this.lokasi = lokasi;
        this.penanggungJawab = penanggungJawab;
    }

    public int getIdGudang() {
        return idGudang;
    }

    public String getNamaGudang() {
        return namaGudang;
    }

    public String getLokasi() {
        return lokasi != null ? lokasi : "-";
    }

    public String getPenanggungJawab() {
        return penanggungJawab != null ? penanggungJawab : "-";
    }
}