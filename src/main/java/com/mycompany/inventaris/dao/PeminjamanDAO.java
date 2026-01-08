package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.model.Peminjaman;
import com.mycompany.inventaris.model.LaporanPeminjamanDTO;
import com.mycompany.inventaris.model.LaporanPenggunaanDTO;
import com.mycompany.inventaris.model.PeminjamanChoice;
import com.mycompany.inventaris.model.VerifikasiDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PeminjamanDAO {

    // =========================
    // INSERT PEMINJAMAN
    // status default: 'pending'
    // stok TIDAK dikurangi di sini
    // lokasi wajib (kalau kosong -> "-")
    // =========================
    public boolean insert(Peminjaman pn) {

        String sql = """
            INSERT INTO peminjaman
            (id_user, id_barang, jumlah, tanggal_peminjaman, tanggal_kembali, lokasi, status)
            VALUES (?, ?, ?, ?, ?, ?, 'pending')
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pn.getIdUser());
            ps.setInt(2, pn.getIdBarang());
            ps.setInt(3, pn.getJumlah());
            ps.setDate(4, new java.sql.Date(pn.getTanggalPeminjaman().getTime()));

            if (pn.getTanggalKembali() != null) {
                ps.setDate(5, new java.sql.Date(pn.getTanggalKembali().getTime()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            String lokasi = null;
            try { lokasi = pn.getLokasi(); } catch (Exception ignore) {}
            if (lokasi == null || lokasi.trim().isEmpty()) lokasi = "-";
            ps.setString(6, lokasi);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Insert Peminjaman Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // LAPORAN PEMINJAMAN (ADMIN)
    // =========================
    public List<LaporanPeminjamanDTO> getLaporanPeminjaman() {
        List<LaporanPeminjamanDTO> list = new ArrayList<>();

        String sql = """
            SELECT 
                p.id_peminjaman,
                u.name AS nama_peminjam,
                u.role,
                b.nama_barang,
                p.jumlah,
                p.tanggal_peminjaman,
                p.tanggal_kembali,
                p.status AS status_verifikasi,
                b.status AS status_barang
            FROM peminjaman p
            JOIN user u ON p.id_user = u.id_user
            JOIN barang b ON p.id_barang = b.id_barang
            ORDER BY p.tanggal_peminjaman DESC
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new LaporanPeminjamanDTO(
                    rs.getString("id_peminjaman"),
                    rs.getString("nama_peminjam"),
                    rs.getString("role"),
                    rs.getString("nama_barang"),
                    rs.getInt("jumlah"),
                    rs.getDate("tanggal_peminjaman"),
                    rs.getDate("tanggal_kembali"),
                    rs.getString("status_verifikasi"),
                    rs.getString("status_barang")
                ));
            }

        } catch (Exception e) {
            System.out.println("Get Laporan Peminjaman Error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    // =========================
    // LAPORAN PENGGUNAAN
    // =========================
    public List<LaporanPenggunaanDTO> getLaporanPenggunaan() {
        List<LaporanPenggunaanDTO> list = new ArrayList<>();

        String sql = """
            SELECT
                p.id_peminjaman AS id_penggunaan,
                b.nama_barang,
                b.kategori,
                u.name AS nama_pengguna,
                u.role,
                p.tanggal_peminjaman,
                p.tanggal_kembali,
                CASE
                    WHEN p.tanggal_kembali IS NULL THEN
                        DATEDIFF(CURDATE(), p.tanggal_peminjaman)
                    ELSE
                        DATEDIFF(p.tanggal_kembali, p.tanggal_peminjaman)
                END AS durasi_hari,
                b.kondisi AS kondisi,
                b.deskripsi
            FROM peminjaman p
            JOIN barang b ON p.id_barang = b.id_barang
            JOIN user u ON p.id_user = u.id_user
            ORDER BY p.tanggal_peminjaman DESC
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new LaporanPenggunaanDTO(
                    rs.getString("id_penggunaan"),
                    rs.getString("nama_barang"),
                    rs.getString("kategori"),
                    rs.getString("nama_pengguna"),
                    rs.getString("role"),
                    rs.getDate("tanggal_peminjaman"),
                    rs.getDate("tanggal_kembali"),
                    rs.getInt("durasi_hari"),
                    rs.getString("kondisi"),
                    rs.getString("deskripsi")
                ));
            }

        } catch (Exception e) {
            System.out.println("Get Laporan Penggunaan Error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    // =========================
    // GET PEMINJAMAN BY USER (yang masih dipinjam)
    // =========================
    public List<Peminjaman> getByUser(int id_user) {
        List<Peminjaman> list = new ArrayList<>();

        String sql = """
            SELECT p.id_peminjaman, p.id_user, p.id_barang,
                   b.nama_barang, b.kode_barang,
                   p.jumlah,
                   p.tanggal_peminjaman,
                   p.tanggal_kembali,
                   p.status,
                   p.lokasi
            FROM peminjaman p
            JOIN barang b ON p.id_barang = b.id_barang
            WHERE p.id_user = ?
              AND p.status = 'dipinjam'
            ORDER BY p.id_peminjaman DESC
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_user);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Peminjaman p = new Peminjaman(
                        rs.getInt("id_peminjaman"),
                        rs.getInt("id_user"),
                        rs.getInt("id_barang"),
                        rs.getString("nama_barang"),
                        rs.getString("kode_barang"),
                        rs.getInt("jumlah"),
                        rs.getDate("tanggal_peminjaman"),
                        rs.getDate("tanggal_kembali"),
                        rs.getString("status")
                    );

                    try { p.setLokasi(rs.getString("lokasi")); } catch (Exception ignore) {}
                    list.add(p);
                }
            }

        } catch (Exception e) {
            System.out.println("Get Peminjaman Error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    // =========================
    // GET MENUNGGU VERIFIKASI (ADMIN)
    // =========================
    public List<VerifikasiDTO> getMenungguVerifikasi() {
        List<VerifikasiDTO> list = new ArrayList<>();

        String sql = """
            SELECT 
                p.id_peminjaman,
                u.name AS nama_user,
                p.tanggal_peminjaman,
                CONCAT(b.nama_barang, ' (', b.kode_barang, ')') AS nama_kode_barang,
                p.jumlah,
                IFNULL(NULLIF(p.lokasi,''), '-') AS ruang,
                p.status,
                p.id_barang
            FROM peminjaman p
            JOIN user u ON p.id_user = u.id_user
            JOIN barang b ON p.id_barang = b.id_barang
            WHERE p.status = 'pending'
            ORDER BY p.tanggal_peminjaman DESC
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new VerifikasiDTO(
                    rs.getInt("id_peminjaman"),
                    rs.getString("nama_user"),
                    rs.getDate("tanggal_peminjaman").toString(),
                    rs.getString("nama_kode_barang"),
                    rs.getInt("jumlah"),
                    rs.getString("ruang"),
                    rs.getString("status"),
                    rs.getInt("id_barang")
                ));
            }

        } catch (Exception e) {
            System.out.println("Get Menunggu Verifikasi Error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    // =========================
    // VERIFIKASI SETUJU (ADMIN)
    // stok berkurang DI SINI SAJA
    // amanin double approve: AND status='pending'
    // =========================
    public boolean verifikasiSetuju(int idPeminjaman) {

        String cek = """
            SELECT p.id_barang, p.jumlah, b.stok
            FROM peminjaman p
            JOIN barang b ON b.id_barang = p.id_barang
            WHERE p.id_peminjaman = ?
            FOR UPDATE
        """;

        String updateStatus = """
            UPDATE peminjaman
            SET status = 'dipinjam'
            WHERE id_peminjaman = ?
              AND status = 'pending'
        """;

        String updateStok = """
            UPDATE barang
            SET stok = stok - ?
            WHERE id_barang = ?
        """;

        try (Connection conn = Koneksi.getKoneksi()) {
            conn.setAutoCommit(false);

            int idBarang;
            int jumlah;
            int stok;

            // 1) cek stok + lock row
            try (PreparedStatement ps = conn.prepareStatement(cek)) {
                ps.setInt(1, idPeminjaman);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    idBarang = rs.getInt("id_barang");
                    jumlah = rs.getInt("jumlah");
                    stok = rs.getInt("stok");
                }
            }

            if (stok < jumlah) {
                conn.rollback();
                return false;
            }

            // 2) update status (harus pending)
            int updated;
            try (PreparedStatement ps1 = conn.prepareStatement(updateStatus)) {
                ps1.setInt(1, idPeminjaman);
                updated = ps1.executeUpdate();
            }
            if (updated == 0) {
                conn.rollback();
                return false;
            }

            // 3) kurangi stok
            try (PreparedStatement ps2 = conn.prepareStatement(updateStok)) {
                ps2.setInt(1, jumlah);
                ps2.setInt(2, idBarang);
                ps2.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (Exception e) {
            System.out.println("Verifikasi Setuju Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // VERIFIKASI TOLAK (ADMIN)
    // =========================
    public boolean verifikasiTolak(int idPeminjaman) {
        String sql = """
            UPDATE peminjaman
            SET status = 'ditolak'
            WHERE id_peminjaman = ?
              AND status = 'pending'
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPeminjaman);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Verifikasi Tolak Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // USER: AJUKAN PENGEMBALIAN
    // =========================
    public boolean ajukanPengembalian(int idPeminjaman) {
        String sql = """
            UPDATE peminjaman
            SET status = 'pengembalian'
            WHERE id_peminjaman = ?
              AND status = 'dipinjam'
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPeminjaman);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Ajukan Pengembalian Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // ADMIN: LIST MENUNGGU PENGEMBALIAN
    // =========================
    public List<VerifikasiDTO> getMenungguPengembalian() {
        List<VerifikasiDTO> list = new ArrayList<>();

        String sql = """
            SELECT 
                p.id_peminjaman,
                u.name AS nama_user,
                p.tanggal_peminjaman,
                CONCAT(b.nama_barang, ' (', b.kode_barang, ')') AS nama_kode_barang,
                p.jumlah,
                IFNULL(NULLIF(p.lokasi,''), '-') AS ruang,
                p.status,
                p.id_barang
            FROM peminjaman p
            JOIN user u ON p.id_user = u.id_user
            JOIN barang b ON p.id_barang = b.id_barang
            WHERE p.status = 'pengembalian'
            ORDER BY p.tanggal_peminjaman DESC
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new VerifikasiDTO(
                    rs.getInt("id_peminjaman"),
                    rs.getString("nama_user"),
                    rs.getDate("tanggal_peminjaman").toString(),
                    rs.getString("nama_kode_barang"),
                    rs.getInt("jumlah"),
                    rs.getString("ruang"),
                    rs.getString("status"),
                    rs.getInt("id_barang")
                ));
            }

        } catch (Exception e) {
            System.out.println("Get Menunggu Pengembalian Error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    // =========================
    // ADMIN: SETUJUI PENGEMBALIAN
    // stok bertambah DI SINI
    // =========================
    public boolean verifikasiPengembalianSetuju(int idPeminjaman) {

        String cek = """
            SELECT p.id_barang, p.jumlah
            FROM peminjaman p
            WHERE p.id_peminjaman = ?
            FOR UPDATE
        """;

        String updatePeminjaman = """
            UPDATE peminjaman
            SET status = 'dikembalikan',
                tanggal_kembali = NOW()
            WHERE id_peminjaman = ?
              AND status = 'pengembalian'
        """;

        String updateStok = """
            UPDATE barang
            SET stok = stok + ?
            WHERE id_barang = ?
        """;

        try (Connection conn = Koneksi.getKoneksi()) {
            conn.setAutoCommit(false);

            int idBarang;
            int jumlah;

            // 1) lock row peminjaman
            try (PreparedStatement ps = conn.prepareStatement(cek)) {
                ps.setInt(1, idPeminjaman);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    idBarang = rs.getInt("id_barang");
                    jumlah = rs.getInt("jumlah");
                }
            }

            // 2) update peminjaman
            int updated;
            try (PreparedStatement ps = conn.prepareStatement(updatePeminjaman)) {
                ps.setInt(1, idPeminjaman);
                updated = ps.executeUpdate();
            }
            if (updated == 0) {
                conn.rollback();
                return false;
            }

            // 3) update stok (+)
            try (PreparedStatement ps = conn.prepareStatement(updateStok)) {
                ps.setInt(1, jumlah);
                ps.setInt(2, idBarang);
                ps.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (Exception e) {
            System.out.println("Verifikasi Pengembalian Setuju Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // ADMIN: TOLAK PENGEMBALIAN
    // balik ke 'dipinjam'
    // =========================
    public boolean verifikasiPengembalianTolak(int idPeminjaman) {
        String sql = """
            UPDATE peminjaman
            SET status = 'dipinjam'
            WHERE id_peminjaman = ?
              AND status = 'pengembalian'
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPeminjaman);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Verifikasi Pengembalian Tolak Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // COMBOBOX: BARANG YANG SEDANG DIPINJAM USER
    // =========================
    public static List<PeminjamanChoice> getDipinjamByUser(int idUser) {
        List<PeminjamanChoice> list = new ArrayList<>();

        String sql = """
            SELECT p.id_peminjaman, p.id_barang, p.jumlah, b.kode_barang, b.nama_barang
            FROM peminjaman p
            JOIN barang b ON b.id_barang = p.id_barang
            WHERE p.id_user = ?
              AND p.status = 'dipinjam'
            ORDER BY p.id_peminjaman DESC
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUser);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PeminjamanChoice(
                        rs.getInt("id_peminjaman"),
                        rs.getInt("id_barang"),
                        rs.getString("kode_barang"),
                        rs.getString("nama_barang"),
                        rs.getInt("jumlah")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
