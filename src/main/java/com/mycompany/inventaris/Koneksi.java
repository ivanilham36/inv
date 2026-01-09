package com.mycompany.inventaris;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    private static Connection connDb;

    public static Connection getKoneksi() {
        String url = "jdbc:mysql://3.0.41.12:3306/db_inventaris?useSSL=false&serverTimezone=Asia/Jakarta";
        String username = "inv";
        String password = "kosong";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            if (connDb == null || connDb.isClosed() || !connDb.isValid(2)) {
                connDb = DriverManager.getConnection(url, username, password);
                System.out.println("Connection Successful");
            }

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Connection Failed: " + e.getMessage());
            e.printStackTrace();
        }

        return connDb;
    }
}
