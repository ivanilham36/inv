package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.service.SessionManager;
import com.mycompany.inventaris.model.VerifikasiDTO;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class PengembalianDAO {

    // =========================
    // AJUKAN PENGEMBALIAN (USER)
    // =========================
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
            VALUES (?, ?, ?, ?, ?, ?, 'pending')
        """;

        Connection conn = null;

        try {
            conn = Koneksi.getKoneksi();
            conn.setAutoCommit(false);

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

        
            LocalDate todayWib = LocalDate.now(ZoneId.of("Asia/Jakarta"));
            int rows;
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, idPeminjaman);
                ps.setInt(2, idUser);
                ps.setInt(3, idBarang);
                ps.setString(4, lokasi);
                ps.setInt(5, jumlah);
                ps.setDate(6, java.sql.Date.valueOf(todayWib));
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
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}

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

        } finally {
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }

    // =========================
    // LIST PENDING (ADMIN)
    // =========================
    public List<VerifikasiDTO> getMenungguPengembalian() {
        List<VerifikasiDTO> list = new ArrayList<>();

        String sql = """
            SELECT
                k.id_pengembalian AS id_peminjaman,
                u.name AS nama_user,
                DATE(k.tanggal_kembali) AS tanggal_pengajuan,
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

    // =========================
    // SETUJUI (ADMIN)
    // =========================
    public boolean setujuiPengembalian(int idPengembalian) {

        // join ke peminjaman biar sekalian dapat tanggal_peminjaman
        String cek = """
            SELECT k.id_peminjaman, k.id_barang, k.jumlah, p.tanggal_peminjaman
            FROM pengembalian k
            JOIN peminjaman p ON p.id_peminjaman = k.id_peminjaman
            WHERE k.id_pengembalian = ?
              AND k.status = 'pending'
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
            SET status = 'dikembalikan',
                tanggal_kembali = ?
            WHERE id_peminjaman = ?
              AND status = 'dipinjam'
        """;

        String updStok = """
            UPDATE barang
            SET stok = stok + ?
            WHERE id_barang = ?
        """;

        Connection conn = null;

        try {
            conn = Koneksi.getKoneksi();
            conn.setAutoCommit(false);

            int idPeminjaman, idBarang, jumlah;
            LocalDate tglPinjam;

            // 1) lock & ambil data
            try (PreparedStatement ps = conn.prepareStatement(cek)) {
                ps.setInt(1, idPengembalian);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();

                        AuditTrailDAO.log(
                            SessionManager.getUserId(), SessionManager.getUsername(),
                            "VERIFIKASI_KEMBALI_SETUJU",
                            "Gagal: data pengembalian tidak ditemukan / bukan pending (id_pengembalian=" + idPengembalian + ")",
                            SessionManager.getIp(), "GAGAL"
                        );
                        return false;
                    }
                    idPeminjaman = rs.getInt("id_peminjaman");
                    idBarang = rs.getInt("id_barang");
                    jumlah = rs.getInt("jumlah");

                    Date dPinjam = rs.getDate("tanggal_peminjaman");
                    tglPinjam = (dPinjam == null) ? null : ((java.sql.Date) dPinjam).toLocalDate();
                }
            }

            // 2) update pengembalian
            int a;
            try (PreparedStatement ps = conn.prepareStatement(updPengembalian)) {
                ps.setInt(1, idPengembalian);
                a = ps.executeUpdate();
            }

            // 3) tanggal kembali
            LocalDate todayWib = LocalDate.now(ZoneId.of("Asia/Jakarta"));
            LocalDate finalKembali = todayWib;

            if (tglPinjam != null && todayWib.isBefore(tglPinjam)) {
                finalKembali = tglPinjam;
            }

            int b;
            try (PreparedStatement ps = conn.prepareStatement(updPeminjaman)) {
                ps.setDate(1, java.sql.Date.valueOf(finalKembali));
                ps.setInt(2, idPeminjaman);
                b = ps.executeUpdate();
            }

            if (a == 0 || b == 0) {
                conn.rollback();

                AuditTrailDAO.log(
                    SessionManager.getUserId(), SessionManager.getUsername(),
                    "VERIFIKASI_KEMBALI_SETUJU",
                    "Gagal: update status tidak memenuhi syarat (id_pengembalian=" + idPengembalian + ", id_peminjaman=" + idPeminjaman + ")",
                    SessionManager.getIp(), "GAGAL"
                );
                return false;
            }

            // 4) stok +jumlah
            try (PreparedStatement ps = conn.prepareStatement(updStok)) {
                ps.setInt(1, jumlah);
                ps.setInt(2, idBarang);
                ps.executeUpdate();
            }

            conn.commit();

            AuditTrailDAO.log(
                SessionManager.getUserId(), SessionManager.getUsername(),
                "VERIFIKASI_KEMBALI_SETUJU",
                "Setujui pengembalian: id_pengembalian=" + idPengembalian +
                    ", id_peminjaman=" + idPeminjaman +
                    ", id_barang=" + idBarang +
                    ", jumlah=" + jumlah +
                    ", tanggal_kembali=" + finalKembali,
                SessionManager.getIp(), "BERHASIL"
            );

            return true;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}

            AuditTrailDAO.log(
                SessionManager.getUserId(), SessionManager.getUsername(),
                "VERIFIKASI_KEMBALI_SETUJU",
                "Error setujui pengembalian id_pengembalian=" + idPengembalian + " | " + e.getMessage(),
                SessionManager.getIp(), "GAGAL"
            );

            e.printStackTrace();
            return false;

        } finally {
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }

    // =========================
    // TOLAK (ADMIN)
    // =========================
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
