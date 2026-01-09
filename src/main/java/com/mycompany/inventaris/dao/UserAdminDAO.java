/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.model.User;
import com.mycompany.inventaris.Koneksi;
import com.mycompany.inventaris.dao.AuditTrailDAO;
import com.mycompany.inventaris.service.SessionManager;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAdminDAO {

    private Connection conn;

    public UserAdminDAO() {
        conn = Koneksi.getKoneksi();
    }

    // INSERT user
    public boolean insert(User user) {
    String sql = "INSERT INTO user (name, username, password, email, phone, birthPlace, " +
            "birthDate, identity_number, role, status, photo) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, user.getNama());
        ps.setString(2, user.getUsername());
        ps.setString(3, user.getPassword());
        ps.setString(4, user.getEmail());
        ps.setString(5, user.getPhone());
        ps.setString(6, user.getPlace());
        ps.setDate(7, new java.sql.Date(user.getBirth().getTime()));
        ps.setString(8, user.getIdentity());
        ps.setString(9, user.getRole());
        ps.setString(10, user.getStatus());
        ps.setBytes(11, user.getPhoto());

        int rows = ps.executeUpdate();
        boolean ok = rows > 0;

        AuditTrailDAO.log(
            SessionManager.getUserId(),
            SessionManager.getUsername(),
            "TAMBAH_USER",
            "Tambah user: username=" + user.getUsername()
                + ", role=" + user.getRole()
                + ", status=" + user.getStatus(),
            SessionManager.getIp(),
            ok ? "BERHASIL" : "GAGAL"
        );

        return ok;

    } catch (Exception e) {

        AuditTrailDAO.log(
            SessionManager.getUserId(),
            SessionManager.getUsername(),
            "TAMBAH_USER",
            "Error tambah user: username=" + user.getUsername()
                + " | error=" + e.getMessage(),
            SessionManager.getIp(),
            "GAGAL"
        );

        System.out.println("Insert User Error: " + e.getMessage());
        return false;
    }
}


    // GET ALL users
    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY created_at DESC";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                User u = new User();

                u.setIdUser(rs.getInt("id_user"));
                u.setNama(rs.getString("name"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setPhone(rs.getString("phone"));
                u.setPassword(rs.getString("password"));
                u.setIdentity(rs.getString("identity_number"));
                u.setRole(rs.getString("role"));
                u.setStatus(rs.getString("status"));
                u.setPlace(rs.getString("birthPlace"));
                u.setBirth(rs.getDate("birthDate"));
                u.setPhoto(rs.getBytes("photo"));

                // created_at
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    u.setCreatedAt(ts);
                }

                list.add(u);
            }

        } catch (Exception e) {
            System.out.println("Get All User Error: " + e.getMessage());
        }

        return list;
    }

    public boolean updateUserPhoto(int userId, byte[] photo) {
    String sql = "UPDATE user SET photo = ? WHERE id_user = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setBytes(1, photo);
        ps.setInt(2, userId);

        int rows = ps.executeUpdate();
        boolean ok = rows > 0;

        AuditTrailDAO.log(
            SessionManager.getUserId(),
            SessionManager.getUsername(),
            "UPDATE_FOTO_USER",
            ok ? "Update foto user id=" + userId
               : "Gagal update foto user (id tidak ditemukan?) id=" + userId,
            SessionManager.getIp(),
            ok ? "BERHASIL" : "GAGAL"
        );

        return ok;

    } catch (Exception e) {

        AuditTrailDAO.log(
            SessionManager.getUserId(),
            SessionManager.getUsername(),
            "UPDATE_FOTO_USER",
            "Error update foto user id=" + userId + " | error=" + e.getMessage(),
            SessionManager.getIp(),
            "GAGAL"
        );

        System.out.println("Update User Photo Error: " + e.getMessage());
        return false;
    }
}


    
    
public boolean update(User user) {
    String sql = "UPDATE user SET name=?, email=?, phone=?, password=?, role=?, status=?, " +
            "birthPlace=?, birthDate=?, identity_number=?, photo=? WHERE id_user=?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, user.getNama());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPhone());
        ps.setString(4, user.getPassword());
        ps.setString(5, user.getRole());
        ps.setString(6, user.getStatus());
        ps.setString(7, user.getPlace());
        ps.setDate(8, new java.sql.Date(user.getBirth().getTime()));
        ps.setString(9, user.getIdentity());
        ps.setBytes(10, user.getPhoto());
        ps.setInt(11, user.getIdUser());

        int rows = ps.executeUpdate();
        boolean ok = rows > 0;

        AuditTrailDAO.log(
            SessionManager.getUserId(),
            SessionManager.getUsername(),
            "UPDATE_USER",
            ok ? "Update user id=" + user.getIdUser()
                 + " | username=" + user.getUsername()
                 + " | role=" + user.getRole()
                 + " | status=" + user.getStatus()
               : "Gagal update user (id tidak ditemukan?) id=" + user.getIdUser(),
            SessionManager.getIp(),
            ok ? "BERHASIL" : "GAGAL"
        );

        return ok;

    } catch (Exception e) {

        AuditTrailDAO.log(
            SessionManager.getUserId(),
            SessionManager.getUsername(),
            "UPDATE_USER",
            "Error update user id=" + user.getIdUser()
                + " | error=" + e.getMessage(),
            SessionManager.getIp(),
            "GAGAL"
        );

        System.out.println("Update User Error: " + e.getMessage());
        return false;
    }
}



   public boolean delete(int id_user) {
    String sql = "DELETE FROM user WHERE id_user=?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, id_user);

        int rows = ps.executeUpdate();
        boolean ok = rows > 0;

        AuditTrailDAO.log(
            SessionManager.getUserId(),
            SessionManager.getUsername(),
            "HAPUS_USER",
            ok ? "Hapus user id=" + id_user
               : "Gagal hapus user (id tidak ditemukan?) id=" + id_user,
            SessionManager.getIp(),
            ok ? "BERHASIL" : "GAGAL"
        );

        return ok;

    } catch (Exception e) {

        AuditTrailDAO.log(
            SessionManager.getUserId(),
            SessionManager.getUsername(),
            "HAPUS_USER",
            "Error hapus user id=" + id_user + " | error=" + e.getMessage(),
            SessionManager.getIp(),
            "GAGAL"
        );

        System.out.println("Delete User Error: " + e.getMessage());
        return false;
    }
}

}

