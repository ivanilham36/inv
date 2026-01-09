package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.model.Riwayat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RiwayatDAO {

    public List<Riwayat> getByUser(int id_user) {
        List<Riwayat> list = new ArrayList<>();

        String sql = """
            SELECT * FROM (
                -- RIWAYAT PEMINJAMAN (selesai)
                SELECT 
                    'Peminjaman' AS type,
                    b.nama_barang,
                    b.kode_barang,
                    p.jumlah,
                    DATE(p.tanggal_peminjaman) AS tanggal_pengajuan,
                    DATE(p.tanggal_kembali)    AS tanggal_pengembalian,
                    CASE 
                        WHEN p.status = 'dikembalikan' THEN 'Dikembalikan'
                        ELSE p.status
                    END AS status
                FROM peminjaman p
                JOIN barang b ON p.id_barang = b.id_barang
                WHERE p.id_user = ?
                  AND p.status = 'dikembalikan'

                UNION ALL

                -- RIWAYAT PERMINTAAN
                SELECT
                    'Permintaan' AS type,
                    b.nama_barang,
                    b.kode_barang,
                    r.jumlah,
                    DATE(r.tanggal) AS tanggal_pengajuan,
                    NULL            AS tanggal_pengembalian,
                    CASE
                        WHEN r.status = 'approved' THEN 'Disetujui'
                        WHEN r.status = 'rejected' THEN 'Ditolak'
                        ELSE r.status
                    END AS status
                FROM permintaan r
                JOIN barang b ON r.id_barang = b.id_barang
                WHERE r.id_user = ?
                  AND r.status IN ('approved', 'rejected')

                UNION ALL

                -- RIWAYAT REPLACEMENT
                SELECT
                    'Replacement' AS type,
                    b.nama_barang,
                    b.kode_barang,
                    rp.jumlah,
                    DATE(rp.tanggal_pengajuan)   AS tanggal_pengajuan,
                    DATE(rp.tanggal_verifikasi)  AS tanggal_pengembalian,
                    CASE
                        WHEN rp.status = 'approved' THEN 'Disetujui'
                        WHEN rp.status = 'rejected' THEN 'Ditolak'
                        ELSE rp.status
                    END AS status
                FROM replacement rp
                JOIN barang b ON rp.id_barang = b.id_barang
                WHERE rp.id_user = ?
                  AND rp.status IN ('approved', 'rejected')
            ) x
            ORDER BY x.tanggal_pengajuan DESC
        """;


        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_user);
            ps.setInt(2, id_user);
            ps.setInt(3, id_user);

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
            System.out.println("Get Riwayat Error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
}
