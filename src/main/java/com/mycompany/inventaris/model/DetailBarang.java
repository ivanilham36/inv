/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.model;

/**
 *
 * @author pnady
 */
public class DetailBarang {
    private final int idBarang;
    private final String kodeBarang;
    private final String namaBarang;
    private final String lokasi;
    private final int stok;

    public DetailBarang(int idBarang, String kodeBarang, String namaBarang, String lokasi, int stok) {
        this.idBarang = idBarang;
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.lokasi = lokasi;
        this.stok = stok;
    }

    public int getIdBarang() { return idBarang; }
    public String getKodeBarang() { return kodeBarang; }
    public String getNamaBarang() { return namaBarang; }
    public String getLokasi() { return lokasi; }
    public int getStok() { return stok; }
}
