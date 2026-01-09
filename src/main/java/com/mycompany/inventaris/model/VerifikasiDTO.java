package com.mycompany.inventaris.model;

public class VerifikasiDTO {
    private int idPeminjaman;
    private String namaUser;
    private String tanggal;
    private String namaKodeBarang;
    private int jumlah;
    private String ruang;
    private String status;
    private int idBarang;

    public VerifikasiDTO(int idPeminjaman, String namaUser, String tanggal,
                         String namaKodeBarang, int jumlah, String ruang,
                         String status, int idBarang) {

        this.idPeminjaman = idPeminjaman;
        this.namaUser = namaUser;
        this.tanggal = tanggal;
        this.namaKodeBarang = namaKodeBarang;
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
    public int getJumlah() { return jumlah; }
    public String getRuang() { return ruang; }
    public String getStatus() { return status; }
    public int getIdBarang() { return idBarang; }
}
