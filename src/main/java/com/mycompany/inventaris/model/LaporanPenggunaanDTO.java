
package com.mycompany.inventaris.model;

import java.sql.Date;

public class LaporanPenggunaanDTO {

    private String idPenggunaan;
    private String namaBarang;
    private String kategori;
    private String namaPengguna;
    private String role;
    private Date tanggalPeminjaman;
    private Date tanggalKembali;
    private int durasiHari;
    private String kondisi;
    private String deskripsi;

    public LaporanPenggunaanDTO(
            String idPenggunaan,
            String namaBarang,
            String kategori,
            String namaPengguna,
            String role,
            Date tanggalPeminjaman,
            Date tanggalKembali,
            int durasiHari,
            String kondisi,
            String deskripsi
    ) {
        this.idPenggunaan = idPenggunaan;
        this.namaBarang = namaBarang;
        this.kategori = kategori;
        this.namaPengguna = namaPengguna;
        this.role = role;
        this.tanggalPeminjaman = tanggalPeminjaman;
        this.tanggalKembali = tanggalKembali;
        this.durasiHari = durasiHari;
        this.kondisi = kondisi;
        this.deskripsi = deskripsi;
    }

    public String getIdPenggunaan() {
        return idPenggunaan;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public String getKategori() {
        return kategori;
    }

    public String getNamaPengguna() {
        return namaPengguna;
    }

    public String getRole() {
        return role;
    }

    public Date getTanggalPeminjaman() {
        return tanggalPeminjaman;
    }

    public Date getTanggalKembali() {
        return tanggalKembali;
    }

    public int getDurasiHari() {
        return durasiHari;
    }

    public String getKondisi() {
        return kondisi;
    }

    public String getDeskripsi() {
        return deskripsi;
    }
}