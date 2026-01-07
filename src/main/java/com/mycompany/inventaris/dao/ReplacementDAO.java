package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.model.Replacement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReplacementDAO {

    // ================= INSERT =================
    public static boolean insert(Replacement r) {
        String sql = """
            INSERT INTO replacement
            (id_user, id_barang, id_peminjaman, kondisi_barang, keterangan, status, tanggal_pengajuan)
            VALUES (?, ?, ?, ?, ?, 'PENDING', NOW())
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, r.getIdUser());
            ps.setInt(2, r.getIdBarang());

            if (r.getIdPeminjaman() == 0) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, r.getIdPeminjaman());
            }

            ps.setString(4, r.getKondisi());
            ps.setString(5, r.getAlasan());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Insert Replacement Error: " + e.getMessage());
            return false;
        }
    }

    

    // ================= VERIFIKASI ADMIN =================
    public static boolean verifikasi(int id_replacement, String status, String keterangan) {
        String sql = """
            UPDATE replacement
            SET status = ?, keterangan = ?, tanggal_verifikasi = NOW()
            WHERE id_replacement = ?
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);        // APPROVED / REJECTED
            ps.setString(2, keterangan);
            ps.setInt(3, id_replacement);   // ✅ FI

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Verifikasi Replacement Error: " + e.getMessage());
            return false;
        }
    }

   
}
