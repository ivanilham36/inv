package com.mycompany.inventaris.model;

import java.util.Date;

public class Replacement {
    private int idReplacement;
    private int idUser;
    private int idBarang;
    private int idPeminjaman;
    private int jumlah;
    private String alasan;
    private String kondisiBarang;   
    private String status;          
    private Date tanggal;
    private String namaBarang;
    private String kodeBarang;
    private String lokasi;

    public Replacement(int idUser, int idBarang, int idPeminjaman,
                       int jumlah, String alasan, String kondisiBarang) {
        this.idUser = idUser;
        this.idBarang = idBarang;
        this.idPeminjaman = idPeminjaman;
        this.jumlah = jumlah;
        this.alasan = alasan;
        this.kondisiBarang = kondisiBarang;
    }

    public Replacement() {}

    public int getIdReplacement() {
        return idReplacement;
    }

    public void setIdReplacement(int idReplacement) {
        this.idReplacement = idReplacement;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdBarang() {
        return idBarang;
    }

    public void setIdBarang(int idBarang) {
        this.idBarang = idBarang;
    }

    public int getIdPeminjaman() {
        return idPeminjaman;
    }

    public void setIdPeminjaman(int idPeminjaman) {
        this.idPeminjaman = idPeminjaman;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public String getAlasan() {
        return alasan;
    }

    public void setAlasan(String alasan) {
        this.alasan = alasan;
    }

    public String getKondisiBarang() {
        return kondisiBarang;
    }

    public void setKondisiBarang(String kondisiBarang) {
        this.kondisiBarang = kondisiBarang;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }

    // ===== OPTIONAL (UNTUK POPUP / TABEL) =====
    public String getNamaBarang() {
        return namaBarang;
    }

    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }

    public String getKodeBarang() {
        return kodeBarang;
    }

    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }

    public String getLokasi() {
        return lokasi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }
}
