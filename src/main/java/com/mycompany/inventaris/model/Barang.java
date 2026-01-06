/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.model;

/**
 *
 * @author pnady
 */
public class Barang {
    
    private int id_barang;
    private String kode_barang;
    private String nama_barang;
    private String kategori;
    private int stok;
    private String kondisi;
    private String lokasi;
    private String status;    
    public Barang(int id_barang, String kode_barang, String nama_barang, String kategori,
                  int stok, String kondisi, String lokasi, String status){
        
        this.id_barang = id_barang;
        this.kode_barang = kode_barang;
        this.nama_barang = nama_barang;
        this.kategori = kategori;
        this.stok = stok;
        this.kondisi = kondisi;
        this.lokasi = lokasi;
        this.status = status;
    }
    
    public int getIdBarang(){
        return id_barang;
    }
    
    public void setIdBarang(int id_barang){
        this.id_barang = id_barang;
    }
    
    public String getKode(){
        return kode_barang;
    }
    
    public void setKode(String kode_barang){
        this.kode_barang = kode_barang;
    }
    
    public String getNama(){
        return nama_barang;
    }
    
    public void setNama(String nama_barang){
        this.nama_barang = nama_barang;
    }
    
    public String getKategori(){
        return kategori;
    }
    
    public void setKategori(String kategori){
        this.kategori = kategori;
    }
    
    public int getStok(){
        return stok;
    }
    
    public void setStok(int stok){
        this.stok = stok;
    }
    
    public String getKondisi(){
        return kondisi;
    }
    
    public void setKondisi(String kondisi){
        this.kondisi = kondisi;
    }
    
    public String getLokasi(){
        return lokasi;
    }
    
    public void setLokasi(String lokasi){
        this.lokasi = lokasi;
    }
    
    public String getStatus(){
        return status;
    }
    
    public void setStatus(String status){
        this.status = status;
    }
}

