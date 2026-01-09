package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.dao.AuditTrailDAO;
import com.mycompany.inventaris.service.SessionManager;
import com.mycompany.inventaris.model.VerifikasiDTO;

import java.sql.*;
import java.util.*;

public class PengembalianDAO {

    public boolean ajukanPengembalian(int idPeminjaman, String lokasi) {

    String cek = """
        SELECT p.id_user, p.id_barang, p.jumlah
        FROM peminjaman p
        WHERE p.id_peminjaman = ?
          AND p.status = 'dipinjam'
    """;

    String cekPending = """
        SELECT 1
        FROM pengembalian
        WHERE id_peminjaman = ?
          AND status = 'pending'
        LIMIT 1
    """;

    String insert = """
        INSERT INTO pengembalian
        (id_peminjaman, id_user, id_barang, lokasi, jumlah, tanggal_kembali, status)
        VALUES (?, ?, ?, ?, ?, CURDATE(), 'pending')
    """;

    try (Connection conn = Koneksi.getKoneksi()) {
        conn.setAutoCommit(false);

        // 1) pastikan belum ada pending
        try (PreparedStatement ps = conn.prepareStatement(cekPending)) {
            ps.setInt(1, idPeminjaman);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    conn.rollback();

                    AuditTrailDAO.log(
                        SessionManager.getUserId(),
                        SessionManager.getUsername(),
                        "AJUKAN_PENGEMBALIAN",
                        "Gagal: sudah ada pengembalian pending (id_peminjaman=" + idPeminjaman + ")",
                        SessionManager.getIp(),
                        "GAGAL"
                    );

                    return false;
                }
            }
        }

        int idUser, idBarang, jumlah;

        // 2) cek peminjaman harus dipinjam
        try (PreparedStatement ps = conn.prepareStatement(cek)) {
            ps.setInt(1, idPeminjaman);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    conn.rollback();

                    AuditTrailDAO.log(
                        SessionManager.getUserId(),
                        SessionManager.getUsername(),
                        "AJUKAN_PENGEMBALIAN",
                        "Gagal: peminjaman tidak ditemukan / bukan dipinjam (id_peminjaman=" + idPeminjaman + ")",
                        SessionManager.getIp(),
                        "GAGAL"
                    );

                    return false;
                }
                idUser = rs.getInt("id_user");
                idBarang = rs.getInt("id_barang");
                jumlah = rs.getInt("jumlah");
            }
        }

        if (lokasi == null || lokasi.trim().isEmpty()) lokasi = "-";

        // 3) insert ke pengembalian
        int rows;
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setInt(1, idPeminjaman);
            ps.setInt(2, idUser);
            ps.setInt(3, idBarang);
            ps.setString(4, lokasi);
            ps.setInt(5, jumlah);
            rows = ps.executeUpdate();
        }

        conn.commit();
        boolean ok = rows > 0;

        AuditTrailDAO.log(
            SessionManager.getUserId(),
            SessionManager.getUsername(),
            "AJUKAN_PENGEMBALIAN",
            "Ajukan pengembalian: id_peminjaman=" + idPeminjaman +
                ", id_barang=" + idBarang +
                ", jumlah=" + jumlah +
                ", lokasi=" + lokasi,
            SessionManager.getIp(),
            ok ? "BERHASIL" : "GAGAL"
        );

        return ok;

    } catch (Exception e) {
        AuditTrailDAO.log(
            SessionManager.getUserId(),
            SessionManager.getUsername(),
            "AJUKAN_PENGEMBALIAN",
            "Error ajukan pengembalian id_peminjaman=" + idPeminjaman + " | " + e.getMessage(),
            SessionManager.getIp(),
            "GAGAL"
        );
        e.printStackTrace();
        return false;
    }
}

    
        public List<VerifikasiDTO> getMenungguPengembalian() {
        List<VerifikasiDTO> list = new ArrayList<>();

        String sql = """
            SELECT
                k.id_pengembalian AS id_peminjaman,
                u.name AS nama_user,
                k.tanggal_kembali AS tanggal_pengajuan,
                CONCAT(b.nama_barang,' (',b.kode_barang,')') AS nama_kode_barang,
                k.jumlah,
                IFNULL(NULLIF(k.lokasi,''), '-') AS ruang,
                k.status,
                k.id_barang
            FROM pengembalian k
            JOIN user u ON k.id_user = u.id_user
            JOIN barang b ON k.id_barang = b.id_barang
            WHERE k.status = 'pending'
            ORDER BY k.created_at DESC
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new VerifikasiDTO(
                    rs.getInt("id_peminjaman"),                
                    rs.getString("nama_user"),
                    rs.getDate("tanggal_pengajuan").toString(),
                    rs.getString("nama_kode_barang"),
                    rs.getInt("jumlah"),
                    rs.getString("ruang"),
                    rs.getString("status"),
                    rs.getInt("id_barang")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
        
        public boolean setujuiPengembalian(int idPengembalian) {
        String cek = """
            SELECT id_peminjaman, id_barang, jumlah
            FROM pengembalian
            WHERE id_pengembalian = ?
              AND status = 'pending'
            FOR UPDATE
        """;

        String updPengembalian = """
            UPDATE pengembalian
            SET status = 'dikembalikan'
            WHERE id_pengembalian = ?
              AND status = 'pending'
        """;

        String updPeminjaman = """
            UPDATE peminjaman
            SET status = 'dikembalikan', tanggal_kembali = CURDATE()
            WHERE id_peminjaman = ?
              AND status = 'dipinjam'
        """;

        String updStok = """
            UPDATE barang
            SET stok = stok + ?
            WHERE id_barang = ?
        """;

        try (Connection conn = Koneksi.getKoneksi()) {
            conn.setAutoCommit(false);

            int idPeminjaman, idBarang, jumlah;

            try (PreparedStatement ps = conn.prepareStatement(cek)) {
                ps.setInt(1, idPengembalian);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        AuditTrailDAO.log(SessionManager.getUserId(), SessionManager.getUsername(),
                            "VERIFIKASI_KEMBALI_SETUJU",
                            "Gagal: data pengembalian tidak ditemukan / bukan pending (id_pengembalian=" + idPengembalian + ")",
                            SessionManager.getIp(), "GAGAL");
                        return false;
                    }
                    idPeminjaman = rs.getInt("id_peminjaman");
                    idBarang = rs.getInt("id_barang");
                    jumlah = rs.getInt("jumlah");
                }
            }

            int a,b;
            try (PreparedStatement ps = conn.prepareStatement(updPengembalian)) {
                ps.setInt(1, idPengembalian);
                a = ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(updPeminjaman)) {
                ps.setInt(1, idPeminjaman);
                b = ps.executeUpdate();
            }
            if (a == 0 || b == 0) {
                conn.rollback();
                AuditTrailDAO.log(SessionManager.getUserId(), SessionManager.getUsername(),
                    "VERIFIKASI_KEMBALI_SETUJU",
                    "Gagal: update status tidak memenuhi syarat (id_pengembalian=" + idPengembalian + ", id_peminjaman=" + idPeminjaman + ")",
                    SessionManager.getIp(), "GAGAL");
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(updStok)) {
                ps.setInt(1, jumlah);
                ps.setInt(2, idBarang);
                ps.executeUpdate();
            }

            conn.commit();

            AuditTrailDAO.log(SessionManager.getUserId(), SessionManager.getUsername(),
                "VERIFIKASI_KEMBALI_SETUJU",
                "Setujui pengembalian: id_pengembalian=" + idPengembalian + ", id_peminjaman=" + idPeminjaman + ", id_barang=" + idBarang + ", jumlah=" + jumlah,
                SessionManager.getIp(), "BERHASIL");

            return true;

        } catch (Exception e) {
            AuditTrailDAO.log(SessionManager.getUserId(), SessionManager.getUsername(),
                "VERIFIKASI_KEMBALI_SETUJU",
                "Error setujui pengembalian id_pengembalian=" + idPengembalian + " | " + e.getMessage(),
                SessionManager.getIp(), "GAGAL");
            e.printStackTrace();
            return false;
        }
    }

        public boolean tolakPengembalian(int idPengembalian) {
        String sql = """
            UPDATE pengembalian
            SET status = 'ditolak'
            WHERE id_pengembalian = ?
              AND status = 'pending'
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPengembalian);
            int rows = ps.executeUpdate();
            boolean ok = rows > 0;

            AuditTrailDAO.log(
                SessionManager.getUserId(),
                SessionManager.getUsername(),
                "VERIFIKASI_KEMBALI_TOLAK",
                ok ? "Tolak pengembalian id_pengembalian=" + idPengembalian
                   : "Gagal tolak pengembalian (bukan pending?) id_pengembalian=" + idPengembalian,
                SessionManager.getIp(),
                ok ? "BERHASIL" : "GAGAL"
            );

            return ok;

        } catch (Exception e) {
            AuditTrailDAO.log(
                SessionManager.getUserId(),
                SessionManager.getUsername(),
                "VERIFIKASI_KEMBALI_TOLAK",
                "Error tolak pengembalian id_pengembalian=" + idPengembalian + " | " + e.getMessage(),
                SessionManager.getIp(),
                "GAGAL"
            );
            e.printStackTrace();
            return false;
        }
    }

        

}