/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.model;

/**
 *
 * @author pnady
 */
public class Verifikasi {
    private int idPeminjaman;
    private String namaPengguna;
    private String tanggalPeminjaman;
    private String namaKodeBarang;
    private String jumlahBarang;
    private String ruang;
    
    public Verifikasi(
            int idPeminjaman, String namaPengguna, String tanggalPeminjaman, String namaKodeBarang, String jumlahBarang, String ruang) {
        this.idPeminjaman = idPeminjaman;
        this.namaPengguna = namaPengguna;
        this.tanggalPeminjaman = tanggalPeminjaman;
        this.namaKodeBarang = namaKodeBarang;
        this.jumlahBarang = jumlahBarang;
        this.ruang = ruang;
    }

    public int getIdPeminjaman() { return idPeminjaman; }
    public String getNamaPengguna() { return namaPengguna; }
    public String getTanggalPeminjaman() { return tanggalPeminjaman; }
    public String getNamaKodeBarang() { return namaKodeBarang; }
    public String getJumlahBarang() { return jumlahBarang; }
    public String getRuang() { return ruang; }
}
