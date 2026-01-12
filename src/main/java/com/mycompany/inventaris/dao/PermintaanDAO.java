package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.model.Permintaan;
import com.mycompany.inventaris.service.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PermintaanDAO {

    public boolean insert(Permintaan p) {
        String cekBarang = "SELECT stok, kategori FROM barang WHERE id_barang = ?";
        String insert = "INSERT INTO permintaan (id_user, id_barang, jumlah, tanggal, status) VALUES (?,?,?,?,?)";
        String updateStok = "UPDATE barang SET stok = stok - ? WHERE id_barang = ?";

        String kategori = "-";
        String status = "pending";

        try (Connection conn = Koneksi.getKoneksi()) {
            if (conn == null || conn.isClosed()) {
                throw new RuntimeException("Koneksi database tidak tersedia / sudah tertutup");
            }

            conn.setAutoCommit(false);

            int stok;

            try (PreparedStatement cek = conn.prepareStatement(cekBarang)) {
                cek.setInt(1, p.getIdBarang());
                try (ResultSet rs = cek.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Barang Tidak Ditemukan");
                    }
                    stok = rs.getInt("stok");
                    kategori = rs.getString("kategori");
                }
            }

            status = "pending";
            if ("consumable".equalsIgnoreCase(kategori)) {
                if (stok < p.getJumlah()) {
                    throw new RuntimeException("Stock tidak cukup");
                }
                status = "approved";
            }

            p.setStatus(status);

            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, p.getIdUser());
                ps.setInt(2, p.getIdBarang());
                ps.setInt(3, p.getJumlah());
                ps.setDate(4, new java.sql.Date(p.getTanggal().getTime()));
                ps.setString(5, status);
                ps.executeUpdate();
            }

            if ("consumable".equalsIgnoreCase(kategori)) {
                try (PreparedStatement ps2 = conn.prepareStatement(updateStok)) {
                    ps2.setInt(1, p.getJumlah());
                    ps2.setInt(2, p.getIdBarang());
                    ps2.executeUpdate();
                }
            }

            conn.commit();

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

        } catch (Exception e) {
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
            e.printStackTrace();
            return false;
        }
    }
}
