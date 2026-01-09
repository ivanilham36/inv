/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.model.Barang;
import com.mycompany.inventaris.view.ManageDataPage;
import com.mycompany.inventaris.view.ManageDataPage.DetailBarang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BarangDAO {
  public static List<Barang> getAll(){
      List<Barang> list = new ArrayList<>();
      
      String sql = "select*from barang";
      try{
          Connection conn = Koneksi.getKoneksi();
          Statement st = conn.createStatement();
          ResultSet rs = st.executeQuery(sql);
          
          while(rs.next()){
              list.add(new Barang(
                      rs.getInt("id_barang"),
                      rs.getString("kode_barang"),
                      rs.getString("nama_barang"),
                      rs.getString("kategori"),
                      rs.getInt("stok"),
                      rs.getString("kondisi"),
                      rs.getString("lokasi"),
                      rs.getString("status")
              ));
          }
      }catch(Exception e){
          e.printStackTrace();
      }
      
      return list;
    }
  
  public static boolean insertBarang(Barang barang) {
    String sql = "INSERT INTO barang " +
                 "(kode_barang, nama_barang, kategori, stok, kondisi, lokasi, `status`) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?)";

    try {
        Connection conn = Koneksi.getKoneksi();
        var ps = conn.prepareStatement(sql);

        ps.setString(1, barang.getKode());
        ps.setString(2, barang.getNama());
        ps.setString(3, barang.getKategori());
        ps.setInt(4, barang.getStok());
        ps.setString(5, barang.getKondisi());
        ps.setString(6, barang.getLokasi());
        ps.setString(7, barang.getStatus());

        return ps.executeUpdate() > 0;

    } catch (Exception e) {
        System.out.println("Insert Barang Error: " + e.getMessage());
        return false;
    }
}

  
    //UPDATE BARANG
    public static boolean reduceStok(int idBarang, int jumlah) {
    String sql = "UPDATE barang SET stok = stok - ? WHERE id_barang = ? AND CAST(stok AS UNSIGNED) >= ?";
        try {
            Connection conn = Koneksi.getKoneksi();
            var ps = conn.prepareStatement(sql);
            ps.setInt(1, jumlah);
            ps.setInt(2, idBarang);
            ps.setInt(3, jumlah);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Update Stok Error: " + e.getMessage());
            return false;
        }
    }
    
    public static void tambahBarangMasuk(String kode, int jumlah, String lokasi, String keterangan, int idUser) {
    // Insert ke barang_masuk
        String sql = "INSERT INTO barang_masuk(id_barang, tanggal_masuk, jumlah, lokasi, keterangan, id_user) VALUES (?, NOW(), ?, ?, ?, ?)";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, kode);
            stmt.setInt(2, jumlah);
            stmt.setString(3, lokasi);
            stmt.setString(4, keterangan);
            stmt.setInt(5, idUser);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Update stok di tabel barang
        sql = "UPDATE barang SET stok = stok + ? WHERE kode = ?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jumlah);
            stmt.setString(2, kode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void tambahBarangKeluar(String kode, int jumlah, String lokasi, String tujuan, int idUser) {
        // Insert ke barang_keluar
        String sql = "INSERT INTO barang_keluar(id_barang, tanggal_keluar, jumlah, lokasi, tujuan, id_user) VALUES (?, NOW(), ?, ?, ?, ?)";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, kode);
            stmt.setInt(2, jumlah);
            stmt.setString(3, lokasi);
            stmt.setString(4, tujuan);
            stmt.setInt(5, idUser);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Kurangi stok di tabel barang
        sql = "UPDATE barang SET stok = stok - ? WHERE kode = ?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jumlah);
            stmt.setString(2, kode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<DetailBarang> getDetailBarangByLokasi(String lokasi) {
        List<DetailBarang> list = new ArrayList<>();
        String sql = "SELECT id_barang, nama, lokasi, stok FROM barang WHERE lokasi = ?";
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, lokasi);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new ManageDataPage.DetailBarang(
                    rs.getString("lokasi"),
                    rs.getString("id_barang"),
                    rs.getString("nama"),
                    String.valueOf(rs.getInt("stok"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
   public static List<ManageDataPage.DetailBarang> getBarangByLokasi(String lokasi) {
    List<ManageDataPage.DetailBarang> list = new ArrayList<>();

    String sql = "SELECT kode, nama, lokasi, stok FROM barang WHERE lokasi = ?";

    try (Connection c = Koneksi.getKoneksi();
         PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setString(1, lokasi);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(new ManageDataPage.DetailBarang(
                rs.getString("lokasi"),
                rs.getString("kode"),
                rs.getString("nama"),
                String.valueOf(rs.getInt("stok"))
            ));
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return list;
}



    
}