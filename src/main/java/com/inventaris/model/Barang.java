package com.inventaris.model;

public class Barang {
    private int idBarang;
    private int idKategori;
    private String kodeBarang;
    private String namaBarang;
    private String satuan;
    private int stokMinimum;
    private String namaKategori;
    private int totalStok;

    public Barang(int idBarang, int idKategori, String kodeBarang, String namaBarang, String satuan, int stokMinimum, String namaKategori, int totalStok) {
        this.idBarang = idBarang;
        this.idKategori = idKategori;
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.satuan = satuan;
        this.stokMinimum = stokMinimum;
        this.namaKategori = namaKategori;
        this.totalStok = totalStok;
    }

    public int getIdBarang() { return idBarang; }
    public int getIdKategori() { return idKategori; }
    public String getKodeBarang() { return kodeBarang; }
    public String getNamaBarang() { return namaBarang; }
    public String getSatuan() { return satuan; }
    public int getStokMinimum() { return stokMinimum; }
    public String getNamaKategori() { return namaKategori; }
    public int getTotalStok() { return totalStok; }
    public boolean isStokMenipis() { return totalStok <= stokMinimum; }

    public void setNamaBarang(String n) { this.namaBarang = n; }
    public void setKodeBarang(String k) { this.kodeBarang = k; }
    public void setSatuan(String s) { this.satuan = s; }
    public void setStokMinimum(int sm) { this.stokMinimum = sm; }
    public void setIdKategori(int ik) { this.idKategori = ik; }
}
