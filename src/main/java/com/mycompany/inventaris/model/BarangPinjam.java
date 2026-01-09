/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.model;

/**
 *
 * @author pnady
 */
public class BarangPinjam {
    private int idPeminjaman;
    private String kodeBarang;
    private String namaBarang;

    public BarangPinjam(int idPeminjaman, String kodeBarang, String namaBarang) {
        this.idPeminjaman = idPeminjaman;
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
    }

    public int getIdPeminjaman() { return idPeminjaman; }
    public String getKodeBarang() { return kodeBarang; }
    public String getNamaBarang() { return namaBarang; }

    @Override
    public String toString() {
        return kodeBarang + " - " + namaBarang;
    }
}
