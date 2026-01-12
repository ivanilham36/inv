package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.model.Barang;
import com.mycompany.inventaris.service.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BarangDAO {

    private static String safeUsername() {
        try {
            return SessionManager.getUsername();
        } catch (Exception e) {
            return "-";
        }
    }

    private static String safeIp() {
        try {
            return SessionManager.getIp();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private static String norm(String s) {
        if (s == null) return "-";
        String t = s.trim();
        return t.isEmpty() ? "-" : t;
    }

    public static List<Barang> getAll() {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT * FROM barang ORDER BY id_barang DESC";

        try (Connection conn = Koneksi.getKoneksi();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean insertBarang(Barang barang, String deskripsi) {
        String sql = """
            INSERT INTO barang
            (kode_barang, nama_barang, kategori, stok, kondisi, lokasi, deskripsi, `status`)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        String lok = norm(barang.getLokasi());
        String desc = norm(deskripsi);

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, barang.getKode());
            ps.setString(2, barang.getNama());
            ps.setString(3, barang.getKategori());
            ps.setInt(4, barang.getStok());
            ps.setString(5, barang.getKondisi());
            ps.setString(6, lok);
            ps.setString(7, desc);
            ps.setString(8, barang.getStatus());

            boolean ok = ps.executeUpdate() > 0;

            AuditTrailDAO.log(
                    SessionManager.getUserId(),
                    safeUsername(),
                    "TAMBAH_BARANG",
                    "Tambah barang: kode=" + barang.getKode()
                            + ", nama=" + barang.getNama()
                            + ", kategori=" + barang.getKategori()
                            + ", stok=" + barang.getStok()
                            + ", kondisi=" + barang.getKondisi()
                            + ", lokasi=" + lok
                            + ", deskripsi=" + desc
                            + ", status=" + barang.getStatus(),
                    safeIp(),
                    ok ? "BERHASIL" : "GAGAL"
            );

            return ok;

        } catch (Exception e) {
            AuditTrailDAO.log(
                    SessionManager.getUserId(),
                    safeUsername(),
                    "TAMBAH_BARANG",
                    "Gagal tambah barang: kode=" + barang.getKode() + " | error=" + e.getMessage(),
                    safeIp(),
                    "GAGAL"
            );
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateBarangByKode(String kodeBarang, String namaBarang, String lokasi, int stok, String status, String kondisi) {
        String sql = "UPDATE barang SET nama_barang = ?, lokasi = ?, stok = ?, status = ?, kondisi = ? WHERE kode_barang = ?";
        boolean ok;
        String lok = norm(lokasi);
        String st = norm(status);
        String kd = norm(kondisi);

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, namaBarang);
            ps.setString(2, lok);
            ps.setInt(3, stok);
            ps.setString(4, st);
            ps.setString(5, kd);
            ps.setString(6, kodeBarang);

            ok = ps.executeUpdate() > 0;

            AuditTrailDAO.log(
                    SessionManager.getUserId(),
                    safeUsername(),
                    "UPDATE_BARANG",
                    "Update barang: kode=" + kodeBarang
                            + ", nama=" + namaBarang
                            + ", lokasi=" + lok
                            + ", stok=" + stok
                            + ", status=" + st
                            + ", kondisi=" + kd,
                    safeIp(),
                    ok ? "BERHASIL" : "GAGAL"
            );

            return ok;

        } catch (Exception e) {
            AuditTrailDAO.log(
                    SessionManager.getUserId(),
                    safeUsername(),
                    "UPDATE_BARANG",
                    "Gagal update barang: kode=" + kodeBarang + " | error=" + e.getMessage(),
                    safeIp(),
                    "GAGAL"
            );
            e.printStackTrace();
            return false;
        }
    }



    public static boolean deleteBarangByKode(String kodeBarang) {
        String sql = "DELETE FROM barang WHERE kode_barang = ?";

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kodeBarang);
            boolean ok = ps.executeUpdate() > 0;

            AuditTrailDAO.log(
                    SessionManager.getUserId(),
                    safeUsername(),
                    "DELETE_BARANG",
                    "Hapus barang: kode=" + kodeBarang,
                    safeIp(),
                    ok ? "BERHASIL" : "GAGAL"
            );

            return ok;

        } catch (Exception e) {
            AuditTrailDAO.log(
                    SessionManager.getUserId(),
                    safeUsername(),
                    "DELETE_BARANG",
                    "Gagal hapus barang: kode=" + kodeBarang + " | error=" + e.getMessage(),
                    safeIp(),
                    "GAGAL"
            );
            e.printStackTrace();
            return false;
        }
    }

    public static boolean tambahBarangMasuk(String kodeBarang, int jumlah, String lokasi, String keterangan) {
        return tambahBarangMasuk(kodeBarang, jumlah, lokasi, keterangan, SessionManager.getUserId());
    }

    public static boolean tambahBarangMasuk(String kodeBarang, int jumlah, String lokasi, String keterangan, int idUser) {
        Integer idBarang = getIdBarangByKode(kodeBarang);
        if (idBarang == null) {
            AuditTrailDAO.log(
                    idUser,
                    safeUsername(),
                    "BARANG_MASUK",
                    "Gagal: kode_barang tidak ditemukan (" + kodeBarang + ")",
                    safeIp(),
                    "GAGAL"
            );
            return false;
        }

        String insert = """
            INSERT INTO barang_masuk(id_barang, tanggal_masuk, jumlah, lokasi, keterangan, id_user)
            VALUES (?, NOW(), ?, ?, ?, ?)
        """;

        String update = "UPDATE barang SET stok = stok + ? WHERE id_barang = ?";

        String lok = norm(lokasi);
        String ket = norm(keterangan);

        try (Connection conn = Koneksi.getKoneksi()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, idBarang);
                ps.setInt(2, jumlah);
                ps.setString(3, lok);
                ps.setString(4, ket);
                ps.setInt(5, idUser);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setInt(1, jumlah);
                ps.setInt(2, idBarang);
                ps.executeUpdate();
            }

            conn.commit();

            AuditTrailDAO.log(
                    idUser,
                    safeUsername(),
                    "BARANG_MASUK",
                    "Barang masuk: kode=" + kodeBarang
                            + ", id_barang=" + idBarang
                            + ", jumlah=" + jumlah
                            + ", lokasi=" + lok
                            + ", ket=" + ket,
                    safeIp(),
                    "BERHASIL"
            );

            return true;

        } catch (Exception e) {
            AuditTrailDAO.log(
                    idUser,
                    safeUsername(),
                    "BARANG_MASUK",
                    "Error barang masuk: kode=" + kodeBarang
                            + ", id_barang=" + idBarang
                            + ", jumlah=" + jumlah
                            + " | error=" + e.getMessage(),
                    safeIp(),
                    "GAGAL"
            );
            e.printStackTrace();
            return false;
        }
    }

    public static boolean tambahBarangKeluar(String kodeBarang, int jumlah, String lokasi, String tujuan) {
        return tambahBarangKeluar(kodeBarang, jumlah, lokasi, tujuan, SessionManager.getUserId());
    }

    public static boolean tambahBarangKeluar(String kodeBarang, int jumlah, String lokasi, String tujuan, int idUser) {
        Integer idBarang = getIdBarangByKode(kodeBarang);
        if (idBarang == null) {
            AuditTrailDAO.log(
                    idUser,
                    safeUsername(),
                    "BARANG_KELUAR",
                    "Gagal: kode_barang tidak ditemukan (" + kodeBarang + ")",
                    safeIp(),
                    "GAGAL"
            );
            return false;
        }

        String lok = norm(lokasi);
        String tuj = norm(tujuan);

        String update = "UPDATE barang SET stok = stok - ? WHERE id_barang = ? AND stok >= ?";
        String insert = """
            INSERT INTO barang_keluar(id_barang, tanggal_keluar, jumlah, lokasi, tujuan, id_user)
            VALUES (?, NOW(), ?, ?, ?, ?)
        """;

        try (Connection conn = Koneksi.getKoneksi()) {
            conn.setAutoCommit(false);

            int updated;
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setInt(1, jumlah);
                ps.setInt(2, idBarang);
                ps.setInt(3, jumlah);
                updated = ps.executeUpdate();
            }

            if (updated <= 0) {
                conn.rollback();

                AuditTrailDAO.log(
                        idUser,
                        safeUsername(),
                        "BARANG_KELUAR",
                        "Gagal barang keluar (stok tidak cukup): kode=" + kodeBarang
                                + ", id_barang=" + idBarang
                                + ", minta=" + jumlah
                                + ", lokasi=" + lok
                                + ", tujuan=" + tuj,
                        safeIp(),
                        "GAGAL"
                );
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, idBarang);
                ps.setInt(2, jumlah);
                ps.setString(3, lok);
                ps.setString(4, tuj);
                ps.setInt(5, idUser);
                ps.executeUpdate();
            }

            conn.commit();

            AuditTrailDAO.log(
                    idUser,
                    safeUsername(),
                    "BARANG_KELUAR",
                    "Barang keluar: kode=" + kodeBarang
                            + ", id_barang=" + idBarang
                            + ", jumlah=" + jumlah
                            + ", lokasi=" + lok
                            + ", tujuan=" + tuj,
                    safeIp(),
                    "BERHASIL"
            );

            return true;

        } catch (Exception e) {
            AuditTrailDAO.log(
                    idUser,
                    safeUsername(),
                    "BARANG_KELUAR",
                    "Error barang keluar: kode=" + kodeBarang
                            + ", id_barang=" + idBarang
                            + ", jumlah=" + jumlah
                            + " | error=" + e.getMessage(),
                    safeIp(),
                    "GAGAL"
            );
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getAllLokasi() {
        List<String> list = new ArrayList<>();
        String sql = """
            SELECT DISTINCT lokasi
            FROM barang
            WHERE lokasi IS NOT NULL AND lokasi <> ''
            ORDER BY lokasi ASC
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("lokasi"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static Integer getIdBarangByKode(String kodeBarang) {
        String sql = "SELECT id_barang FROM barang WHERE kode_barang = ? LIMIT 1";

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kodeBarang);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id_barang");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
