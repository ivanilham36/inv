package com.mycompany.inventaris.model;

import java.util.Date;

public class LaporanPeminjamanDTO {
    private String idPeminjaman;
    private String namaPeminjam;
    private String role;
    private String barang;
    private int jumlah;
    private Date tglPinjam;
    private Date tglKembali;
    private String statusVerifikasi;
    private String statusBarang;

    public LaporanPeminjamanDTO(
            String idPeminjaman, String namaPeminjam, String role,
            String barang, int jumlah, Date tglPinjam,
            Date tglKembali, String statusVerifikasi, String statusBarang) {

        this.idPeminjaman = idPeminjaman;
        this.namaPeminjam = namaPeminjam;
        this.role = role;
        this.barang = barang;
        this.jumlah = jumlah;
        this.tglPinjam = tglPinjam;
        this.tglKembali = tglKembali;
        this.statusVerifikasi = statusVerifikasi;
        this.statusBarang = statusBarang;
    }

    // GETTER
    public String getIdPeminjaman() { return idPeminjaman; }
    public String getNamaPeminjam() { return namaPeminjam; }
    public String getRole() { return role; }
    public String getBarang() { return barang; }
    public int getJumlah() { return jumlah; }
    public Date getTglPinjam() { return tglPinjam; }
    public Date getTglKembali() { return tglKembali; }
    public String getStatusVerifikasi() { return statusVerifikasi; }
    public String getStatusBarang() { return statusBarang; }
}