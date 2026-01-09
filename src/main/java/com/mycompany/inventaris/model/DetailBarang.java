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
    private String lokasi;
    private String idBarang;
    private String namaBarang;
    private String qty;

    public DetailBarang(String lokasi, String idBarang, String namaBarang, String qty) {
        this.lokasi = lokasi;
        this.idBarang = idBarang;
        this.namaBarang = namaBarang;
        this.qty = qty;
    }

    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }
    public String getIdBarang() { return idBarang; }
    public void setIdBarang(String idBarang) { this.idBarang = idBarang; }
    public String getNamaBarang() { return namaBarang; }
    public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }
    public String getQty() { return qty; }
    public void setQty(String qty) { this.qty = qty; } // supaya bisa update

}
