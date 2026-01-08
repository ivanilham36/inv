package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.model.AuditLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AuditTrailDAO {

    public static void log(
            int userId,
            String userName,
            String action,
            String description,
            String ipAddress,
            String status
    ) {

        String sql =
            "INSERT INTO audit_trail " +
            "(user_id, user_name, aksi, deskripsi, ip_address, status) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection conn = Koneksi.getKoneksi();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, userId);
            ps.setString(2, userName);
            ps.setString(3, action);
            ps.setString(4, description);
            ps.setString(5, ipAddress);
            ps.setString(6, status);

            int rows = ps.executeUpdate();

            System.out.println("Audit inserted, rows affected: " + rows);

        } catch (Exception e) {
            e.printStackTrace(); // DO NOT HIDE THIS
        }
    }

    public static List<AuditLog> getAll() {

        List<AuditLog> list = new ArrayList<>();

        String sql =
            "SELECT waktu, user_name, aksi, deskripsi, ip_address, status " +
            "FROM audit_trail ORDER BY waktu DESC";

        try {
            Connection conn = Koneksi.getKoneksi();
            ResultSet rs = conn.createStatement().executeQuery(sql);

            while (rs.next()) {
                list.add(new AuditLog(
                        rs.getString("waktu"),
                        rs.getString("user_name"),
                        rs.getString("aksi"),
                        rs.getString("deskripsi"),
                        rs.getString("ip_address"),
                        rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
