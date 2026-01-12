package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.model.Peminjaman;
import com.mycompany.inventaris.model.LaporanPeminjamanDTO;
import com.mycompany.inventaris.model.LaporanPenggunaanDTO;
import com.mycompany.inventaris.model.PeminjamanChoice;
import com.mycompany.inventaris.model.VerifikasiDTO;
import com.mycompany.inventaris.service.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PeminjamanDAO {

    // =========================
    // INSERT PEMINJAMAN
    // =========================
    public boolean insert(Peminjaman pn) {
        String sql = """
            INSERT INTO peminjaman
            (id_user, id_barang, jumlah, tanggal_peminjaman, tanggal_kembali, lokasi, status)
            VALUES (?, ?, ?, ?, ?, ?, 'pending')
        """;

        String lokasi = null;

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

            try { lokasi = pn.getLokasi(); } catch (Exception ignore) {}
            if (lokasi == null || lokasi.trim().isEmpty()) lokasi = "-";
            ps.setString(6, lokasi);

            int rows = ps.executeUpdate();
            boolean ok = rows > 0;

            AuditTrailDAO.log(
                SessionManager.getUserId(),
                SessionManager.getUsername(),
                "AJUKAN_PEMINJAMAN",
                "Ajukan peminjaman: id_barang=" + pn.getIdBarang()
                    + ", jumlah=" + pn.getJumlah()
                    + ", lokasi=" + lokasi,
                SessionManager.getIp(),
                ok ? "BERHASIL" : "GAGAL"
            );

            return ok;

        } catch (Exception e) {
            AuditTrailDAO.log(
                SessionManager.getUserId(),
                SessionManager.getUsername(),
                "AJUKAN_PEMINJAMAN",
                "Gagal ajukan peminjaman: id_barang=" + pn.getIdBarang()
                    + ", jumlah=" + pn.getJumlah()
                    + ", lokasi=" + (lokasi == null ? "-" : lokasi)
                    + " | error=" + e.getMessage(),
                SessionManager.getIp(),
                "GAGAL"
            );

            System.out.println("Insert Peminjaman Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<LaporanPeminjamanDTO> getLaporanPeminjaman() {
        List<LaporanPeminjamanDTO> list = new ArrayList<>();

        String sql = """
            SELECT 
                p.id_peminjaman AS id_peminjaman,
                u.name          AS nama_peminjam,
                u.role          AS role,
                b.nama_barang   AS nama_barang,
                p.jumlah        AS jumlah,
                p.tanggal_peminjaman AS tanggal_peminjaman,
                p.tanggal_kembali    AS tanggal_kembali,
                p.status        AS status_verifikasi,
                b.status        AS status_barang
            FROM peminjaman p
            JOIN user u  ON p.id_user = u.id_user
            JOIN barang b ON p.id_barang = b.id_barang

            UNION ALL

            SELECT
                r.id_permintaan AS id_peminjaman,
                u.name          AS nama_peminjam,
                u.role          AS role,
                b.nama_barang   AS nama_barang,
                r.jumlah        AS jumlah,
                r.tanggal       AS tanggal_peminjaman,
                NULL            AS tanggal_kembali,
                r.status        AS status_verifikasi,
                b.status        AS status_barang
            FROM permintaan r
            JOIN user u  ON r.id_user = u.id_user
            JOIN barang b ON r.id_barang = b.id_barang
            WHERE r.status = 'approved'

            ORDER BY tanggal_peminjaman DESC
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
                b.kondisi,
                b.deskripsi
            FROM peminjaman p
            JOIN barang b ON p.id_barang = b.id_barang
            JOIN user u ON p.id_user = u.id_user
            
            UNION ALL
            
            SELECT
                r.id_permintaan AS id_penggunaan,
                b.nama_barang,
                b.kategori,
                u.name AS nama_pengguna,
                u.role,
                r.tanggal AS tanggal_peminjaman,
                NULL AS tanggal_kembali,
                0 AS durasi_hari,
                b.kondisi,
                b.deskripsi
            FROM permintaan r
            JOIN barang b ON r.id_barang = b.id_barang
            JOIN user u ON r.id_user = u.id_user
            
            ORDER BY tanggal_peminjaman DESC;
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
                b.kondisi AS kondisi_barang,
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
                    rs.getString("kondisi_barang"),
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
    // =========================
    public boolean verifikasiSetuju(int idPeminjaman) {

        String cek = """
            SELECT 
                p.id_barang, p.jumlah, p.lokasi,
                u.name AS nama_user,
                b.stok, b.kode_barang
            FROM peminjaman p
            JOIN barang b ON b.id_barang = p.id_barang
            JOIN user u ON u.id_user = p.id_user
            WHERE p.id_peminjaman = ?
            FOR UPDATE
        """;

        String updateStatus = """
            UPDATE peminjaman
            SET status = 'dipinjam'
            WHERE id_peminjaman = ?
              AND status = 'pending'
        """;

        // stok berkurang (tetap)
        String updateStok = """
            UPDATE barang
            SET stok = stok - ?
            WHERE id_barang = ?
        """;

        // CATAT barang_keluar
        // NOTE: kolom tujuan boleh disesuaikan kalau field kamu beda
        String insertBarangKeluar = """
            INSERT INTO barang_keluar (id_barang, tanggal_keluar, jumlah, lokasi, tujuan, id_user)
            VALUES (?, NOW(), ?, ?, ?, ?)
        """;

        Connection conn = null;

        try {
            conn = Koneksi.getKoneksi();
            conn.setAutoCommit(false);

            int idBarang;
            int jumlah;
            int stok;
            String lokasi;
            String kodeBarang;
            String namaPeminjam;

            // 1) cek stok + lock row
            try (PreparedStatement ps = conn.prepareStatement(cek)) {
                ps.setInt(1, idPeminjaman);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();

                        AuditTrailDAO.log(
                            SessionManager.getUserId(),
                            SessionManager.getUsername(),
                            "VERIFIKASI_PINJAM_SETUJU",
                            "Gagal setujui: peminjaman id=" + idPeminjaman + " (data tidak ditemukan)",
                            SessionManager.getIp(),
                            "GAGAL"
                        );
                        return false;
                    }

                    idBarang = rs.getInt("id_barang");
                    jumlah = rs.getInt("jumlah");
                    stok = rs.getInt("stok");

                    lokasi = rs.getString("lokasi");
                    if (lokasi == null || lokasi.trim().isEmpty()) lokasi = "-";

                    kodeBarang = rs.getString("kode_barang");
                    namaPeminjam = rs.getString("nama_user");
                    if (namaPeminjam == null || namaPeminjam.trim().isEmpty()) namaPeminjam = "-";
                }
            }

            if (stok < jumlah) {
                conn.rollback();

                AuditTrailDAO.log(
                    SessionManager.getUserId(),
                    SessionManager.getUsername(),
                    "VERIFIKASI_PINJAM_SETUJU",
                    "Gagal setujui: stok kurang (id_peminjaman=" + idPeminjaman +
                        ", id_barang=" + idBarang + ", stok=" + stok + ", minta=" + jumlah + ")",
                    SessionManager.getIp(),
                    "GAGAL"
                );
                return false;
            }

            // 2) update status
            int updated;
            try (PreparedStatement ps1 = conn.prepareStatement(updateStatus)) {
                ps1.setInt(1, idPeminjaman);
                updated = ps1.executeUpdate();
            }

            if (updated == 0) {
                conn.rollback();

                AuditTrailDAO.log(
                    SessionManager.getUserId(),
                    SessionManager.getUsername(),
                    "VERIFIKASI_PINJAM_SETUJU",
                    "Gagal setujui: status bukan pending (id_peminjaman=" + idPeminjaman + ")",
                    SessionManager.getIp(),
                    "GAGAL"
                );
                return false;
            }

            // 3) kurangi stok
            try (PreparedStatement ps2 = conn.prepareStatement(updateStok)) {
                ps2.setInt(1, jumlah);
                ps2.setInt(2, idBarang);
                ps2.executeUpdate();
            }

            // 4) CATAT barang_keluar
            String tujuan = "Dipinjam oleh " + namaPeminjam + " (kode=" + kodeBarang + ")";

            try (PreparedStatement ps3 = conn.prepareStatement(insertBarangKeluar)) {
                ps3.setInt(1, idBarang);
                ps3.setInt(2, jumlah);
                ps3.setString(3, lokasi);
                ps3.setString(4, tujuan);
                ps3.setInt(5, SessionManager.getUserId()); 
                ps3.executeUpdate();
            }

            conn.commit();

            AuditTrailDAO.log(
                SessionManager.getUserId(),
                SessionManager.getUsername(),
                "VERIFIKASI_PINJAM_SETUJU",
                "Setujui peminjaman id=" + idPeminjaman +
                    " | id_barang=" + idBarang +
                    " | jumlah=" + jumlah +
                    " | catat_barang_keluar=YA",
                SessionManager.getIp(),
                "BERHASIL"
            );

            return true;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}

            AuditTrailDAO.log(
                SessionManager.getUserId(),
                SessionManager.getUsername(),
                "VERIFIKASI_PINJAM_SETUJU",
                "Error setujui peminjaman id=" + idPeminjaman + " | error=" + e.getMessage(),
                SessionManager.getIp(),
                "GAGAL"
            );

            System.out.println("Verifikasi Setuju Error: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
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

            int rows = ps.executeUpdate();
            boolean ok = rows > 0;

            AuditTrailDAO.log(
                SessionManager.getUserId(),
                SessionManager.getUsername(),
                "VERIFIKASI_PINJAM_TOLAK",
                ok
                  ? "Tolak peminjaman id=" + idPeminjaman
                  : "Gagal tolak (status bukan pending?) id=" + idPeminjaman,
                SessionManager.getIp(),
                ok ? "BERHASIL" : "GAGAL"
            );

            return ok;

        } catch (Exception e) {

            AuditTrailDAO.log(
                SessionManager.getUserId(),
                SessionManager.getUsername(),
                "VERIFIKASI_PINJAM_TOLAK",
                "Error tolak peminjaman id=" + idPeminjaman + " | error=" + e.getMessage(),
                SessionManager.getIp(),
                "GAGAL"
            );

            System.out.println("Verifikasi Tolak Error: " + e.getMessage());
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
    
    public static boolean updateStatusById(int idPeminjaman, String status) {
        String sql = "UPDATE peminjaman SET status = ? WHERE id_peminjaman = ?";
        try (Connection c = Koneksi.getKoneksi();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, idPeminjaman);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
