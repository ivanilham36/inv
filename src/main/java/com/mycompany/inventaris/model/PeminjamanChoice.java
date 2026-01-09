/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.model;

/**
 *
 * @author pnady
 */
public class PeminjamanChoice {
    private int id_peminjaman;
    private int id_barang;
    private String kode_barang;
    private String nama_barang;
    private int jumlah;
    
    public PeminjamanChoice(int id_peminjaman, int id_barang, String kode_barang, String nama_barang, int jumlah) {
        this.id_peminjaman = id_peminjaman;
        this.id_barang = id_barang;
        this.kode_barang = kode_barang;
        this.nama_barang = nama_barang;
        this.jumlah = jumlah;
    }

    public int getIdPeminjaman() { return id_peminjaman; }
    public int getIdBarang() { return id_barang; }
    public String getKodeBarang() { return kode_barang; }
    public String getNamaBarang() { return nama_barang; }
    public int getJumlah() { return jumlah; }

    @Override
    public String toString() {
        return kode_barang + " - " + nama_barang + " (Qty: " + jumlah + ")";
    }
}
