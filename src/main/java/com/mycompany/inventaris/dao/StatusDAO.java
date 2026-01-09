package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.model.Riwayat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StatusDAO {

    public List<Riwayat> getStatusByUser(int id_user) {
        List<Riwayat> list = new ArrayList<>();

        String sql =
            "SELECT 'Peminjaman' AS type, " +
            "b.nama_barang, " +
            "b.kode_barang, " +
            "p.jumlah, " +
            "p.tanggal_peminjaman AS tanggal_pengajuan, " +
            "p.tanggal_kembali AS tanggal_pengembalian, " +
            "p.status " +
            "FROM peminjaman p " +
            "JOIN barang b ON p.id_barang = b.id_barang " +
            "WHERE p.id_user = ? AND p.status IN ('pending','dipinjam') " +

            "UNION ALL " +

            "SELECT 'Permintaan' AS type, " +
            "b.nama_barang, " +
            "b.kode_barang, " +
            "r.jumlah, " +
            "r.tanggal AS tanggal_pengajuan, " +
            "NULL AS tanggal_pengembalian, " +
            "r.status " +
            "FROM permintaan r " +
            "JOIN barang b ON r.id_barang = b.id_barang " +
            "WHERE r.id_user = ? AND r.status IN ('pending','diproses') " +

            "ORDER BY tanggal_pengajuan DESC";

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_user);
            ps.setInt(2, id_user);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Riwayat(
                        rs.getString("type"),
                        rs.getString("nama_barang"),
                        rs.getString("kode_barang"),
                        rs.getInt("jumlah"),
                        rs.getDate("tanggal_pengajuan"),
                        rs.getDate("tanggal_pengembalian"),
                        rs.getString("status")
                    ));
                }
            }

        } catch (Exception e) {
            System.out.println("Get Status Error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
}
