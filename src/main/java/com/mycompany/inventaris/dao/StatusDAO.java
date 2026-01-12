package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.model.Riwayat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StatusDAO {

    public List<Riwayat> getStatusByUser(int id_user) {
        List<Riwayat> list = new ArrayList<>();

        String sql = """
            SELECT 
                'Peminjaman' AS type,
                b.nama_barang,
                b.kode_barang,
                p.jumlah,
                p.tanggal_peminjaman AS tanggal_pengajuan,
                p.tanggal_kembali AS tanggal_pengembalian,
                CASE
                    WHEN p.status = 'pending' THEN 'Pending'
                    WHEN p.status = 'dipinjam' THEN 'Dipinjam'
                    WHEN p.status = 'pengembalian' THEN 'Pengembalian'
                    ELSE p.status
                END AS status
            FROM peminjaman p
            JOIN barang b ON p.id_barang = b.id_barang
            WHERE p.id_user = ?
              AND p.status IN ('pending','dipinjam','pengembalian')

            UNION ALL

            SELECT
                'Permintaan' AS type,
                b.nama_barang,
                b.kode_barang,
                r.jumlah,
                r.tanggal AS tanggal_pengajuan,
                NULL AS tanggal_pengembalian,
                CASE
                    WHEN r.status = 'pending' THEN 'Pending'
                    WHEN r.status = 'diproses' THEN 'Diproses'
                    ELSE r.status
                END AS status
            FROM permintaan r
            JOIN barang b ON r.id_barang = b.id_barang
            WHERE r.id_user = ?
              AND r.status IN ('pending','diproses')

            UNION ALL

            SELECT
                'Replacement' AS type,
                b.nama_barang,
                b.kode_barang,
                rp.jumlah,
                rp.tanggal_pengajuan AS tanggal_pengajuan,
                NULL AS tanggal_pengembalian,
                'Pending' AS status
            FROM replacement rp
            JOIN barang b ON rp.id_barang = b.id_barang
            WHERE rp.id_user = ?
              AND rp.status = 'pending'

            ORDER BY tanggal_pengajuan DESC
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
            System.out.println("Get Status Error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
}
