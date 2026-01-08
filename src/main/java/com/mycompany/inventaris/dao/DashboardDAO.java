package com.mycompany.inventaris.dao;

import com.mycompany.inventaris.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardDAO {

    private static int getCount(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;

        try {
            Connection conn = Koneksi.getKoneksi();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static int getPermintaanCount() {
        return getCount("permintaan");
    }

    public static int getPeminjamanCount() {
        return getCount("peminjaman");
    }

    public static int getPengembalianCount() {
        return getCount("pengembalian");
    }

    public static int getReplacementCount() {
        return getCount("replacement");
    }
}
