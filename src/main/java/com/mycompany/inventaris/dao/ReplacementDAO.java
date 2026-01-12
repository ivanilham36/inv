package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.model.Replacement;
import com.mycompany.inventaris.model.VerifikasiDTO;
import com.mycompany.inventaris.dao.AuditTrailDAO;
import com.mycompany.inventaris.service.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ReplacementDAO {

    // =========================
    // INSERT (AJUKAN REPLACEMENT)
    // =========================
    public static boolean insert(Replacement r) {
        String sql = """
            INSERT INTO replacement
            (id_user, id_barang, id_peminjaman, jumlah, alasan, kondisi_barang, foto_bukti, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'pending')
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, r.getIdUser());
            ps.setInt(2, r.getIdBarang());

            if (r.getIdPeminjaman() > 0) ps.setInt(3, r.getIdPeminjaman());
            else ps.setNull(3, Types.INTEGER);

            ps.setInt(4, r.getJumlah());

            String alasan = r.getAlasan();
            if (alasan == null || alasan.trim().isEmpty()) alasan = "-";
            ps.setString(5, alasan);

            String kondisi = r.getKondisiBarang();
            if (kondisi == null || kondisi.trim().isEmpty()) kondisi = "rusak ringan";
            ps.setString(6, kondisi);

            String foto = r.getFotoBukti();
            if (foto == null || foto.trim().isEmpty()) ps.setNull(7, Types.VARCHAR);
            else ps.setString(7, foto);

            int rows = ps.executeUpdate();
            boolean ok = rows > 0;

            AuditTrailDAO.log(
                SessionManager.getUserId(),
                SessionManager.getUsername(),
                "AJUKAN_REPLACEMENT",
                "Ajukan replacement: id_barang=" + r.getIdBarang()
                    + ", id_peminjaman=" + r.getIdPeminjaman()
                    + ", jumlah=" + r.getJumlah()
                    + ", kondisi=" + kondisi
                    + ", foto=" + (foto == null ? "-" : foto),
                SessionManager.getIp(),
                ok ? "BERHASIL" : "GAGAL"
            );

            return ok;

        } catch (Exception e) {
            AuditTrailDAO.log(
                SessionManager.getUserId(),
                SessionManager.getUsername(),
                "AJUKAN_REPLACEMENT",
                "Error ajukan replacement: " + e.getMessage(),
                SessionManager.getIp(),
                "GAGAL"
            );
            e.printStackTrace();
            return false;
        }
    }

    public List<VerifikasiDTO> getMenungguReplacement() {
        List<VerifikasiDTO> list = new ArrayList<>();

        String sql = """
            SELECT
                r.id_replacement AS id_peminjaman,
                b.id_barang AS id_barang,
                u.name AS nama_user,
                r.tanggal_pengajuan,
                CONCAT(b.nama_barang,' (',b.kode_barang,')') AS barang,
                r.jumlah,
                IFNULL(NULLIF(p.lokasi,''), '-') AS ruang,
                r.alasan,
                r.kondisi_barang,
                r.foto_bukti,
                r.status
            FROM replacement r
            JOIN user u ON r.id_user = u.id_user
            JOIN barang b ON r.id_barang = b.id_barang
            LEFT JOIN peminjaman p ON r.id_peminjaman = p.id_peminjaman
            WHERE r.status = 'pending'
            ORDER BY r.tanggal_pengajuan DESC
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                VerifikasiDTO dto = new VerifikasiDTO(
                    rs.getInt("id_peminjaman"),
                    rs.getString("nama_user"),
                    rs.getTimestamp("tanggal_pengajuan").toString(),
                    rs.getString("barang"),
                    rs.getString("kondisi_barang"),
                    rs.getInt("jumlah"),
                    rs.getString("ruang"),
                    rs.getString("status"),
                    rs.getInt("id_barang")
                );

                dto.setFotoBukti(rs.getString("foto_bukti"));

                list.add(dto);
            }

        } catch (Exception e) {
            System.out.println("Get Menunggu Replacement Error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }


    public boolean setujuiReplacement(int idReplacement) {
        return setujuiReplacement(idReplacement, null, null);
    }

    public boolean tolakReplacement(int idReplacement) {
        return tolakReplacement(idReplacement, null);
    }

    public boolean setujuiReplacement(int idReplacement, String keputusanAdmin, String catatanAdmin) {

        String cek = """
            SELECT id_barang
            FROM replacement
            WHERE id_replacement = ?
              AND status = 'pending'
            FOR UPDATE
        """;

        String updReplacement = """
            UPDATE replacement
            SET status = 'approved',
                id_admin = ?,
                tanggal_verifikasi = NOW(),
                keputusan_admin = ?,
                catatan_admin = ?
            WHERE id_replacement = ?
              AND status = 'pending'
        """;

        String updBarang = """
            UPDATE barang
            SET kondisi = 'rusak',
                status  = 'rusak'
            WHERE id_barang = ?
        """;

        Connection conn = null;
        try {
            conn = Koneksi.getKoneksi();
            conn.setAutoCommit(false);

            int idBarang;

            // 1) lock + ambil id_barang
            try (PreparedStatement ps = conn.prepareStatement(cek)) {
                ps.setInt(1, idReplacement);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        AuditTrailDAO.log(SessionManager.getUserId(), SessionManager.getUsername(),
                            "VERIFIKASI_REPLACEMENT_SETUJU",
                            "Gagal: replacement tidak ditemukan / bukan pending (id=" + idReplacement + ")",
                            SessionManager.getIp(), "GAGAL");
                        return false;
                    }
                    idBarang = rs.getInt("id_barang");
                }
            }

            // fallback default
            String keputusan = (keputusanAdmin == null || keputusanAdmin.isBlank()) ? null : keputusanAdmin;
            String catatan  = (catatanAdmin == null || catatanAdmin.isBlank()) ? "-" : catatanAdmin;

            // 2) update replacement + keputusan/catatan admin
            int a;
            try (PreparedStatement ps = conn.prepareStatement(updReplacement)) {
                ps.setInt(1, SessionManager.getUserId());
                ps.setString(2, keputusan);
                ps.setString(3, catatan);
                ps.setInt(4, idReplacement);
                a = ps.executeUpdate();
            }

            if (a == 0) {
                conn.rollback();
                AuditTrailDAO.log(SessionManager.getUserId(), SessionManager.getUsername(),
                    "VERIFIKASI_REPLACEMENT_SETUJU",
                    "Gagal: update replacement tidak memenuhi syarat (id=" + idReplacement + ")",
                    SessionManager.getIp(), "GAGAL");
                return false;
            }

            // 3) update barang jadi rusak
            try (PreparedStatement ps = conn.prepareStatement(updBarang)) {
                ps.setInt(1, idBarang);
                ps.executeUpdate();
            }

            conn.commit();

            AuditTrailDAO.log(SessionManager.getUserId(), SessionManager.getUsername(),
                "VERIFIKASI_REPLACEMENT_SETUJU",
                "Setujui replacement id=" + idReplacement
                    + " | keputusan_admin=" + (keputusan == null ? "-" : keputusan)
                    + " | id_barang=" + idBarang,
                SessionManager.getIp(), "BERHASIL");

            return true;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}

            AuditTrailDAO.log(SessionManager.getUserId(), SessionManager.getUsername(),
                "VERIFIKASI_REPLACEMENT_SETUJU",
                "Error setujui replacement id=" + idReplacement + " | " + e.getMessage(),
                SessionManager.getIp(), "GAGAL");

            e.printStackTrace();
            return false;

        } finally {
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }

    public boolean tolakReplacement(int idReplacement, String catatanAdmin) {
        String sql = """
            UPDATE replacement
            SET status = 'rejected',
                id_admin = ?,
                tanggal_verifikasi = NOW(),
                catatan_admin = ?
            WHERE id_replacement = ?
              AND status = 'pending'
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, SessionManager.getUserId());
            ps.setString(2, (catatanAdmin == null || catatanAdmin.isBlank()) ? "-" : catatanAdmin);
            ps.setInt(3, idReplacement);

            int rows = ps.executeUpdate();
            boolean ok = rows > 0;

            AuditTrailDAO.log(
                SessionManager.getUserId(),
                SessionManager.getUsername(),
                "VERIFIKASI_REPLACEMENT_TOLAK",
                ok ? "Tolak replacement id=" + idReplacement
                   : "Gagal tolak replacement (status bukan pending?) id=" + idReplacement,
                SessionManager.getIp(),
                ok ? "BERHASIL" : "GAGAL"
            );

            return ok;

        } catch (Exception e) {
            AuditTrailDAO.log(
                SessionManager.getUserId(),
                SessionManager.getUsername(),
                "VERIFIKASI_REPLACEMENT_TOLAK",
                "Error tolak replacement id=" + idReplacement + " | " + e.getMessage(),
                SessionManager.getIp(),
                "GAGAL"
            );
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // DETAIL
    // =========================
    public Replacement getDetail(int idReplacement) {
        String sql = """
            SELECT
                r.id_replacement,
                r.id_user,
                r.id_barang,
                r.id_peminjaman,
                r.jumlah,
                r.alasan,
                r.kondisi_barang,
                r.foto_bukti,
                r.tanggal_pengajuan,
                r.status,
                r.keputusan_admin,
                r.catatan_admin,
                IFNULL(NULLIF(p.lokasi,''), '-') AS lokasi,
                b.nama_barang,
                b.kode_barang
            FROM replacement r
            JOIN barang b ON r.id_barang = b.id_barang
            LEFT JOIN peminjaman p ON r.id_peminjaman = p.id_peminjaman
            WHERE r.id_replacement = ?
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idReplacement);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Replacement r = new Replacement();
                r.setIdReplacement(rs.getInt("id_replacement"));
                r.setIdUser(rs.getInt("id_user"));
                r.setIdBarang(rs.getInt("id_barang"));
                r.setIdPeminjaman(rs.getInt("id_peminjaman"));
                r.setJumlah(rs.getInt("jumlah"));
                r.setAlasan(rs.getString("alasan"));
                r.setKondisiBarang(rs.getString("kondisi_barang"));
                r.setStatus(rs.getString("status"));
                r.setFotoBukti(rs.getString("foto_bukti"));
                r.setNamaBarang(rs.getString("nama_barang"));
                r.setKodeBarang(rs.getString("kode_barang"));
                r.setLokasi(rs.getString("lokasi"));

                return r;
            }

        } catch (Exception e) {
            System.out.println("Get Detail Replacement Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
