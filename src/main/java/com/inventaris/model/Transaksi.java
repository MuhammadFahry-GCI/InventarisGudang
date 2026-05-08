package com.inventaris.model;

import java.time.LocalDate;

public class Transaksi {
    private int idTransaksi;
    private String namaBarang;
    private String kodeBarang;
    private String namaGudang;
    private String namaSupplier;
    private String namaUser;
    private String jenisTransaksi;
    private int jumlah;
    private LocalDate tanggal;
    private String keterangan;

    public Transaksi(int idTransaksi, String namaBarang, String kodeBarang, String namaGudang,
            String namaSupplier, String namaUser, String jenisTransaksi, int jumlah,
            LocalDate tanggal, String keterangan) {
        this.idTransaksi = idTransaksi;
        this.namaBarang = namaBarang;
        this.kodeBarang = kodeBarang;
        this.namaGudang = namaGudang;
        this.namaSupplier = namaSupplier;
        this.namaUser = namaUser;
        this.jenisTransaksi = jenisTransaksi;
        this.jumlah = jumlah;
        this.tanggal = tanggal;
        this.keterangan = keterangan;
    }

    public int getIdTransaksi() {
        return idTransaksi;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public String getKodeBarang() {
        return kodeBarang;
    }

    public String getNamaGudang() {
        return namaGudang;
    }

    public String getNamaSupplier() {
        return namaSupplier != null ? namaSupplier : "-";
    }

    public String getNamaUser() {
        return namaUser;
    }

    public String getJenisTransaksi() {
        return jenisTransaksi;
    }

    public int getJumlah() {
        return jumlah;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public String getKeterangan() {
        return keterangan != null ? keterangan : "";
    }
}
