/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.model;

import java.util.Date;

/**
 *
 * @author pnady
 */
public class Peminjaman {
    private int id_peminjaman;
    private int id_user;
    private int id_barang;
    private String nama_barang;
    private String kode_barang;
    private int jumlah;
    private Date tanggal_peminjaman;
    private Date tanggal_kembali;
    private String status;

    // ✅ tambahan lokasi
    private String lokasi;

    public Peminjaman(int id_peminjaman, int id_user, int id_barang, String nama_barang,
            String kode_barang, int jumlah, Date tanggal_peminjaman, Date tanggal_kembali, String status) {
        this.id_peminjaman = id_peminjaman;
        this.id_user = id_user;
        this.id_barang = id_barang;
        this.nama_barang = nama_barang;
        this.kode_barang = kode_barang;
        this.jumlah = jumlah;
        this.tanggal_peminjaman = tanggal_peminjaman;
        this.tanggal_kembali = tanggal_kembali;
        this.status = status;
    }

    // ✅ overload constructor kalau kamu mau sekalian isi lokasi
    public Peminjaman(int id_peminjaman, int id_user, int id_barang, String nama_barang,
            String kode_barang, int jumlah, Date tanggal_peminjaman, Date tanggal_kembali, String status, String lokasi) {
        this(id_peminjaman, id_user, id_barang, nama_barang, kode_barang, jumlah, tanggal_peminjaman, tanggal_kembali, status);
        this.lokasi = lokasi;
    }

    public Peminjaman() {

    }

    public int getIdPeminjaman() {
        return id_peminjaman;
    }

    public void setIdPeminjaman(int id_peminjaman) {
        this.id_peminjaman = id_peminjaman;
    }

    public int getIdUser() {
        return id_user;
    }

    public void setIdUser(int id_user) {
        this.id_user = id_user;
    }

    public int getIdBarang() {
        return id_barang;
    }

    public void setIdBarang(int id_barang) {
        this.id_barang = id_barang;
    }

    public String getNamaBarang() {
        return nama_barang;
    }

    public void setNamaBarang(String nama_barang) {
        this.nama_barang = nama_barang;
    }

    public String getKodeBarang() {
        return kode_barang;
    }

    public void setKodeBarang(String kode_barang) {
        this.kode_barang = kode_barang;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public Date getTanggalPeminjaman() {
        return tanggal_peminjaman;
    }

    public void setTanggalPeminjaman(Date tanggal_peminjaman) {
        this.tanggal_peminjaman = tanggal_peminjaman;
    }

    public Date getTanggalKembali() {
        return tanggal_kembali;
    }

    public void setTanggalKembali(Date tanggal_kembali) {
        this.tanggal_kembali = tanggal_kembali;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLokasi() {
        return lokasi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }
}
