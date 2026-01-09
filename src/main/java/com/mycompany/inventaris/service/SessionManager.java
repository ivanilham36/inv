/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.service;
import com.mycompany.inventaris.model.User;
/**
 *
 * @author pnady
 */
public class SessionManager {
    private static User currentUser;
    private static String ipAddress;
    
    public static void set(User user, String ip) {
        currentUser = user;
        ipAddress = ip;
    }

    public static User getUser() {
        return currentUser;
    }

    public static int getUserId() {
        return currentUser != null ? currentUser.getIdUser() : 0;
    }

    public static String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "-";
    }

    public static String getNama() {
        return currentUser != null ? currentUser.getNama() : "-";
    }

    public static String getRole() {
        return currentUser != null ? currentUser.getRole() : "-";
    }

    public static String getIp() {
        return ipAddress != null ? ipAddress : "-";
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static boolean isSuperAdmin() {
        return currentUser != null && currentUser.isSuperAdmin();
    }

    // dipanggil saat logout
    public static void clear() {
        currentUser = null;
        ipAddress = null;
    }
}
