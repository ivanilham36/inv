package com.mycompany.inventaris.model;

public class VerifikasiDTO {
    private int idPeminjaman;
    private String namaUser;
    private String tanggal;
    private String namaKodeBarang;
    private String kondisi_barang;
    private int jumlah;
    private String ruang;
    private String fotoBukti;
    private String status;
    private int idBarang;

    public VerifikasiDTO(int idPeminjaman, String namaUser, String tanggal,
                         String namaKodeBarang, String kondisi_barang, int jumlah, String ruang,
                         String status, int idBarang) {

        this.idPeminjaman = idPeminjaman;
        this.namaUser = namaUser;
        this.tanggal = tanggal;
        this.namaKodeBarang = namaKodeBarang;
        this.kondisi_barang = kondisi_barang;
        this.jumlah = jumlah;
        this.ruang = ruang;
        this.status = status;
        this.idBarang = idBarang; 
    }

    public int getIdPeminjaman() { 
        return idPeminjaman; 
    }
    public String getNamaUser() { 
        return namaUser; 
    }
    public String getTanggal() { 
        return tanggal; 
    }
    public String getNamaKodeBarang() { return namaKodeBarang; }
    public String getKondisiBarang() { return kondisi_barang; }
    public int getJumlah() { return jumlah; }
    public String getRuang() { return ruang; }
    public String getStatus() { return status; }
    public int getIdBarang() { return idBarang; }
    public String getFotoBukti() { return fotoBukti; }
    public void setFotoBukti(String fotoBukti) { this.fotoBukti = fotoBukti; }
}
