package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.model.Barang;
import com.mycompany.inventaris.view.ManageDataPage.DetailBarang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BarangDAO {

    // =======================
    // GET ALL BARANG
    // =======================
    public static List<Barang> getAll() {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT * FROM barang";

        try {
            Connection conn = Koneksi.getKoneksi();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                list.add(new Barang(
                        rs.getInt("id_barang"),
                        rs.getString("kode_barang"),
                        rs.getString("nama_barang"),
                        rs.getString("kategori"),
                        rs.getInt("stok"),
                        rs.getString("kondisi"),
                        rs.getString("lokasi"),
                        rs.getString("status"),
                        rs.getString("deskripsi")
                        
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // =======================
    // GET BY ID
    // =======================
    public static Barang getById(int idBarang) {
        String sql = "SELECT * FROM barang WHERE id_barang = ?";

        try {
            Connection conn = Koneksi.getKoneksi();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idBarang);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Barang(
                        rs.getInt("id_barang"),
                        rs.getString("kode_barang"),
                        rs.getString("nama_barang"),
                        rs.getString("kategori"),
                        rs.getInt("stok"),
                        rs.getString("kondisi"),
                        rs.getString("lokasi"),
                        rs.getString("status")
                );
            }

        } catch (Exception e) {
            System.out.println("Get Barang By ID Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // =======================
    // GET BY LOKASI
    // =======================
    public static List<Barang> getByLokasi(String lokasi) {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT * FROM barang WHERE lokasi = ?";

        try {
            Connection conn = Koneksi.getKoneksi();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, lokasi);
            ResultSet rs = ps.executeQuery();

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

    // =======================
    // INSERT (TAMBAH)
    // =======================
    public static boolean insertBarang(Barang barang) {
    String sql = "INSERT INTO barang " +
            "(kode_barang, nama_barang, kategori, stok, kondisi, lokasi, status, deskripsi) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection conn = Koneksi.getKoneksi();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, barang.getKode());
            ps.setString(2, barang.getNama());
            ps.setString(3, barang.getKategori());
            ps.setInt(4, barang.getStok());
            ps.setString(5, barang.getKondisi());
            ps.setString(6, barang.getLokasi());
            ps.setString(7, barang.getStatus());
            ps.setString(8, "");

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Insert Barang Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =======================
    // UPDATE (EDIT)
    // =======================
    public static boolean updateBarang(Barang barang) {
        String sql = "UPDATE barang SET " +
                "kode_barang = ?, nama_barang = ?, kategori = ?, stok = ?, kondisi = ?, lokasi = ?, status = ? " +
                "WHERE id_barang = ?";

        try {
            Connection conn = Koneksi.getKoneksi();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, barang.getKode());
            ps.setString(2, barang.getNama());
            ps.setString(3, barang.getKategori());
            ps.setInt(4, barang.getStok());
            ps.setString(5, barang.getKondisi());
            ps.setString(6, barang.getLokasi());
            ps.setString(7, barang.getStatus());
            ps.setInt(8, barang.getIdBarang());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Update Barang Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =======================
    // DELETE
    // =======================
    public static boolean deleteBarang(int idBarang) {
        String sql = "DELETE FROM barang WHERE id_barang = ?";

        try {
            Connection conn = Koneksi.getKoneksi();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idBarang);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Delete Barang Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =======================
    // PINDAH BARANG (BENAR)
    // =======================
    public static boolean pindahBarang(int idBarang, int jumlah, String lokasiTujuan) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Koneksi.getKoneksi();
            conn.setAutoCommit(false);

            String checkSql = "SELECT stok, lokasi FROM barang WHERE id_barang = ?";
            ps = conn.prepareStatement(checkSql);
            ps.setInt(1, idBarang);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                conn.rollback();
                return false;
            }

            int stokSekarang = rs.getInt("stok");
            String lokasiAsal = rs.getString("lokasi");

            if (stokSekarang < jumlah) {
                conn.rollback();
                return false;
            }

            // kurangi stok asal
            String updateStokSql = "UPDATE barang SET stok = stok - ? WHERE id_barang = ?";
            ps = conn.prepareStatement(updateStokSql);
            ps.setInt(1, jumlah);
            ps.setInt(2, idBarang);
            ps.executeUpdate();

            // cek barang tujuan
            String checkTargetSql =
                    "SELECT id_barang, stok FROM barang " +
                    "WHERE kode_barang = (SELECT kode_barang FROM barang WHERE id_barang = ?) " +
                    "AND lokasi = ?";
            ps = conn.prepareStatement(checkTargetSql);
            ps.setInt(1, idBarang);
            ps.setString(2, lokasiTujuan);
            ResultSet rsTarget = ps.executeQuery();

            if (rsTarget.next()) {
                int idTarget = rsTarget.getInt("id_barang");

                String addStokSql = "UPDATE barang SET stok = stok + ? WHERE id_barang = ?";
                ps = conn.prepareStatement(addStokSql);
                ps.setInt(1, jumlah);
                ps.setInt(2, idTarget);
                ps.executeUpdate();

            } else {
                String insertSql = "INSERT INTO barang (kode_barang, nama_barang, kategori, stok, kondisi, lokasi, status, deskripsi) " +
                   "SELECT kode_barang, nama_barang, kategori, ?, kondisi, ?, status, deskripsi " +
                   "FROM barang WHERE id_barang = ?";
                ps = conn.prepareStatement(insertSql);
                ps.setInt(1, jumlah);
                ps.setString(2, lokasiTujuan);
                ps.setInt(3, idBarang);
                ps.executeUpdate();
            }

            // hapus stok 0
            String deleteSql = "DELETE FROM barang WHERE id_barang = ? AND stok = 0";
            ps = conn.prepareStatement(deleteSql);
            ps.setInt(1, idBarang);
            ps.executeUpdate();

            conn.commit();
            return true;

        } catch (Exception e) {
            System.out.println("Pindah Barang Error: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // =======================
    // REDUCE STOK
    // =======================
    public static boolean reduceStok(int idBarang, int jumlah) {
        String sql = "UPDATE barang SET stok = stok - ? WHERE id_barang = ? AND stok >= ?";

        try {
            Connection conn = Koneksi.getKoneksi();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, jumlah);
            ps.setInt(2, idBarang);
            ps.setInt(3, jumlah);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Reduce Stok Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =======================
    // TAMBAH BARANG MASUK
    // =======================
    public static void tambahBarangMasuk(String kode, int jumlah, String lokasi, String keterangan, int idUser) {
        try {
            Connection conn = Koneksi.getKoneksi();

            String sqlMasuk =
                    "INSERT INTO barang_masuk(id_barang, tanggal_masuk, jumlah, lokasi, keterangan, id_user) " +
                    "VALUES (?, NOW(), ?, ?, ?, ?)";
            PreparedStatement psMasuk = conn.prepareStatement(sqlMasuk);
            psMasuk.setString(1, kode);
            psMasuk.setInt(2, jumlah);
            psMasuk.setString(3, lokasi);
            psMasuk.setString(4, keterangan);
            psMasuk.setInt(5, idUser);
            psMasuk.executeUpdate();

            String sqlUpdate = "UPDATE barang SET stok = stok + ? WHERE kode_barang = ?";
            PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
            psUpdate.setInt(1, jumlah);
            psUpdate.setString(2, kode);
            psUpdate.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =======================
    // TAMBAH BARANG KELUAR
    // =======================
    public static void tambahBarangKeluar(String kode, int jumlah, String lokasi, String tujuan, int idUser) {
        try {
            Connection conn = Koneksi.getKoneksi();

            String sqlKeluar =
                    "INSERT INTO barang_keluar(id_barang, tanggal_keluar, jumlah, lokasi, tujuan, id_user) " +
                    "VALUES (?, NOW(), ?, ?, ?, ?)";
            PreparedStatement psKeluar = conn.prepareStatement(sqlKeluar);
            psKeluar.setString(1, kode);
            psKeluar.setInt(2, jumlah);
            psKeluar.setString(3, lokasi);
            psKeluar.setString(4, tujuan);
            psKeluar.setInt(5, idUser);
            psKeluar.executeUpdate();

            String sqlUpdate = "UPDATE barang SET stok = stok - ? WHERE kode_barang = ?";
            PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
            psUpdate.setInt(1, jumlah);
            psUpdate.setString(2, kode);
            psUpdate.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =======================
    // FIX DEPRECATED METHOD
    // =======================
    @Deprecated
    public static List<DetailBarang> getDetailBarangByLokasi(String lokasi) {
        List<DetailBarang> list = new ArrayList<>();
        List<Barang> barangList = getByLokasi(lokasi);

        for (Barang b : barangList) {
            list.add(new DetailBarang(
                    b.getIdBarang(),          // FIX — INT ID
                    b.getLokasi(),            // FIX — posisi benar
                    b.getNama(),
                    String.valueOf(b.getStok())
            ));
        }

        return list;
    }
}
