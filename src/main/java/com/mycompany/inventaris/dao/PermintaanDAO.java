/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.model.Permintaan;
import com.mycompany.inventaris.dao.AuditTrailDAO;
import com.mycompany.inventaris.service.SessionManager;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
public class PermintaanDAO {
    private Connection conn;
    
    public PermintaanDAO(){
        conn = Koneksi.getKoneksi();
    }
    
    //insert
    public boolean insert(Permintaan p){
    String cekBarang = "select stok, kategori from barang where id_barang=?";
    String insert = "insert into permintaan (id_user, id_barang, jumlah, tanggal, status) values (?,?,?,?,?)";
    String updateStok = "UPDATE barang SET stok = stok - ? WHERE id_barang = ?";

    String kategori = "-";
    String status = "pending";

    try {
        conn.setAutoCommit(false);

        // cek barang
        PreparedStatement cek = conn.prepareStatement(cekBarang);
        cek.setInt(1, p.getIdBarang());
        ResultSet rs = cek.executeQuery();

        if(!rs.next()){
            throw new RuntimeException("Barang Tidak Ditemukan");
        }

        int stok = rs.getInt("stok");
        kategori = rs.getString("kategori");

        status = "pending";
        if("consumable".equalsIgnoreCase(kategori)){
            if(stok < p.getJumlah()){
                throw new RuntimeException("Stock tidak cukup");
            }
            status = "approved";  // auto-approved
        }

        p.setStatus(status);

        // insert permintaan
        PreparedStatement ps = conn.prepareStatement(insert);
        ps.setInt(1, p.getIdUser());
        ps.setInt(2, p.getIdBarang());
        ps.setInt(3, p.getJumlah());
        ps.setDate(4, new java.sql.Date(p.getTanggal().getTime()));
        ps.setString(5, status);
        ps.executeUpdate();

        // update stok jika consumable
        if("consumable".equalsIgnoreCase(kategori)){
            PreparedStatement ps2 = conn.prepareStatement(updateStok);
            ps2.setInt(1, p.getJumlah());
            ps2.setInt(2, p.getIdBarang());
            ps2.executeUpdate();
        }

        conn.commit();

        // ✅ AUDIT SUKSES (setelah commit)
        String ket = "Ajukan permintaan consumable: id_barang=" + p.getIdBarang()
                   + ", jumlah=" + p.getJumlah()
                   + ", kategori=" + kategori
                   + ", status=" + status;

        if ("consumable".equalsIgnoreCase(kategori) && "approved".equalsIgnoreCase(status)) {
            ket += " (AUTO-APPROVED: consumable langsung mengurangi stok)";
        } else {
            ket += " (MENUNGGU VERIFIKASI ADMIN)";
        }

        AuditTrailDAO.log(
            SessionManager.getUserId(),
            SessionManager.getUsername(),
            "AJUKAN_PERMINTAAN",
            ket,
            SessionManager.getIp(),
            "BERHASIL"
        );

        return true;

    } catch(Exception e){
        try { conn.rollback(); } catch (Exception ex){ ex.printStackTrace(); }
        AuditTrailDAO.log(
            SessionManager.getUserId(),
            SessionManager.getUsername(),
            "AJUKAN_PERMINTAAN",
            "Gagal ajukan permintaan: id_barang=" + p.getIdBarang()
                + ", jumlah=" + p.getJumlah()
                + ", kategori=" + kategori
                + ", status=" + status
                + " | error=" + e.getMessage(),
            SessionManager.getIp(),
            "GAGAL"
        );

        System.out.println("Insert Permintaan Error: " + e.getMessage());
        return false;

    } finally {
        try { conn.setAutoCommit(true); } catch(Exception ex){ ex.printStackTrace(); }
    }
}


}
