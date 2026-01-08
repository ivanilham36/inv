package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.model.Replacement;
import com.mycompany.inventaris.model.VerifikasiDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ReplacementDAO {
    public static boolean insert(Replacement r) {
        String sql = """
            INSERT INTO replacement
            (id_user, id_barang, id_peminjaman, jumlah, alasan, kondisi_barang, tanggal_pengajuan, status)
            VALUES (?, ?, ?, ?, ?, ?, NOW(), 'pending')
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, r.getIdUser());
            ps.setInt(2, r.getIdBarang());
            ps.setInt(3, r.getIdPeminjaman());
            ps.setInt(4, r.getJumlah());

            // alasan aman
            String alasan = r.getAlasan();
            if (alasan == null || alasan.trim().isEmpty()) alasan = "-";
            ps.setString(5, alasan);

            // kondisi barang harus cocok dengan enum DB kamu
            String kondisi = r.getKondisiBarang();
            if (kondisi == null || kondisi.trim().isEmpty()) kondisi = "rusak ringan";
            ps.setString(6, kondisi);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Insert Replacement Error: " + e.getMessage());
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
            list.add(new VerifikasiDTO(
                rs.getInt("id_peminjaman"),
                rs.getString("nama_user"),
                rs.getDate("tanggal_pengajuan").toString(),
                rs.getString("barang"),
                rs.getInt("jumlah"),
                rs.getString("ruang"),
                rs.getString("status"),
                rs.getInt("id_barang")
            ));

            }

        } catch (Exception e) {
            System.out.println("Get Menunggu Replacement Error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    public boolean setujuiReplacement(int idReplacement) {
        String sql = """
            UPDATE replacement
            SET status = 'approved'
            WHERE id_replacement = ?
              AND status = 'pending'
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idReplacement);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Setujui Replacement Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean tolakReplacement(int idReplacement) {
        String sql = """
            UPDATE replacement
            SET status = 'rejected'
            WHERE id_replacement = ?
              AND status = 'pending'
        """;

        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idReplacement);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Tolak Replacement Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
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
                r.tanggal_pengajuan,
                r.status,
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
                return r;
            }

        } catch (Exception e) {
            System.out.println("Get Detail Replacement Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
