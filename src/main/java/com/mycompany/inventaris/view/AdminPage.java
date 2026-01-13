/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.view;

/**
 *
 * @author Amy
 */

import com.mycompany.inventaris.model.User;
import com.mycompany.inventaris.dao.AuditTrailDAO;
import com.mycompany.inventaris.dao.UserAdminDAO;
import com.mycompany.inventaris.dao.DashboardDAO;
import java.io.ByteArrayInputStream;
import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class AdminPage extends BorderPane {

    private Stage stage;
    private User admin;
    
    public AdminPage(User admin){
        this.admin = admin;
        initializeUI();
    }
    
    private void initializeUI() {

        // SIDEBAR
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setAlignment(Pos.TOP_LEFT);
        sidebar.setPrefWidth(200);
        sidebar.setStyle(
                "-fx-background-color: white;" +
                "-fx-padding: 20 10;" +
                "-fx-border-width: 0 1 0 0;" +
                "-fx-border-color: #e5e7eb;"
        );


        // Logo
        ImageView logo = new ImageView(
                new Image(getClass().getResourceAsStream("/assets/logoAsa.png"))
        );
        logo.setFitHeight(70);
        logo.setPreserveRatio(true);

        VBox logoBox = new VBox(logo);
        logoBox.setAlignment(Pos.TOP_LEFT);
        logoBox.setPadding(new Insets(0,0,0,0));


        // USER PROFILE
    Image userPhoto;

    byte[] photoBytes = admin.getPhoto();

    if (photoBytes != null && photoBytes.length > 0) {
    userPhoto = new Image(new ByteArrayInputStream(photoBytes));
    } else {
    // absolute safety fallback (should rarely happen if NOT NULL)
    userPhoto = new Image(
        getClass().getResourceAsStream("/assets/user.png")
    );
        }
        ImageView userImage = new ImageView(userPhoto);
        userImage.setFitWidth(40);
        userImage.setFitHeight(40);
        userImage.setPreserveRatio(true);
        Circle clipCircle = new Circle(20, 20, 20);
        userImage.setClip(clipCircle);

        Label nameLabel = new Label(admin.getNama());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label roleLabel = new Label(admin.getRole().toUpperCase());
        roleLabel.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: #9ca3af;" +
                "-fx-font-weight: normal;"
        );

        VBox textBox = new VBox(2, nameLabel, roleLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox userBox = new HBox(10, userImage, textBox);
        userBox.setAlignment(Pos.CENTER_LEFT);
        userBox.setPadding(new Insets(10, 10, 20, 10));


        // MENU
        VBox menuBox = new VBox(8);

        // MAIN MENU
        Button dashboardBtn = createMenuButton("🏠  Dashboard", true);
        Button verifikasiBtn = createMenuButton("✓  Verifikasi", false);
        Button manageDataBtn = createMenuButton("⚙  Manage Data", false);
        Button laporanBtn = createMenuButton("📊  Laporan ▼", false);

        // SUB MENU LAPORAN
        VBox laporanSubMenu = new VBox(5);
        laporanSubMenu.setPadding(new Insets(0, 0, 0, 20));
        laporanSubMenu.setVisible(false);
        laporanSubMenu.setManaged(false);

        Button laporanPinjamBtn =
                createMenuButton("Laporan Peminjaman", false);

        Button laporanGunaBtn =
                createMenuButton("Laporan Penggunaan", false);

        // ACTION
        verifikasiBtn.setOnAction(e -> {
            Stage s = (Stage) verifikasiBtn.getScene().getWindow();
            s.setScene(new Scene(new VerifikasiPage(admin), s.getWidth(), s.getHeight()));
            s.setMaximized(true);
        });

        manageDataBtn.setOnAction(e -> {
            Stage s = (Stage) manageDataBtn.getScene().getWindow();
            s.setScene(new Scene(new ManageDataPage(admin), s.getWidth(), s.getHeight()));
            s.setMaximized(true);
        });

        laporanPinjamBtn.setOnAction(e -> {
            Stage s = (Stage) laporanBtn.getScene().getWindow();
            s.setScene(new Scene(new LaporanPeminjamanPage(admin), s.getWidth(), s.getHeight()));
            s.setMaximized(true);
        });

        laporanGunaBtn.setOnAction(e -> {
            Stage s = (Stage) laporanGunaBtn.getScene().getWindow();
            s.setScene(new Scene(new LaporanPenggunaanPage(admin), s.getWidth(), s.getHeight()));
            s.setMaximized(true);
        });

        // TOGGLE LAPORAN ▼ ▲
        laporanBtn.setOnAction(e -> {
            boolean open = laporanSubMenu.isVisible();
            laporanSubMenu.setVisible(!open);
            laporanSubMenu.setManaged(!open);
            laporanBtn.setText(open ? "📊  Laporan ▼" : "📊  Laporan ▲");
        });

        // MASUKKAN SUBMENU
        laporanSubMenu.getChildren().addAll(
                laporanPinjamBtn,
                laporanGunaBtn
        );

        // FINAL ADD
        menuBox.getChildren().addAll(
                dashboardBtn,
                verifikasiBtn,
                manageDataBtn,
                laporanBtn,
                laporanSubMenu
        );



        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("↩  Logout");
        logoutBtn.setAlignment(Pos.CENTER_LEFT);
        logoutBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-font-size: 13px; " +
            "-fx-text-fill: #475569; " +
            "-fx-padding: 12 10; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        logoutBtn.setOnAction(e -> {
    String ip = "UNKNOWN";
    try {
        ip = java.net.InetAddress.getLocalHost().getHostAddress();
    } catch (Exception ex) {
        ex.printStackTrace();
    }

    AuditTrailDAO.log(
        admin.getIdUser(),          
        admin.getUsername(),         
        "LOGOUT",
        "Pengguna keluar dari sistem",
        ip,
        "BERHASIL"
    ); 
        Stage currentStage = (Stage) logoutBtn.getScene().getWindow();
    Scene newScene = new Scene(new MainPage(currentStage), currentStage.getWidth(), currentStage.getHeight());
    currentStage.setScene(newScene);
    stage.setMaximized(true);
       });

        sidebar.getChildren().addAll(logoBox, userBox, menuBox, spacer, logoutBtn);
        
        // MAIN CONTENT
        StackPane mainContent = new StackPane();
        mainContent.setStyle("-fx-background-color: #f8fafc;");

        VBox centerBox = new VBox(40);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(60));

        Label halo = new Label("Halo, " + admin.getNama() + " !!");
        halo.setStyle(
            "-fx-font-size: 40px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #334155;"
        );

        // CARD CONTENT
        HBox cardBox = new HBox(20);
        cardBox.setAlignment(Pos.CENTER);

        int permintaanCount = DashboardDAO.getPermintaanCount();
        int peminjamanCount = DashboardDAO.getPeminjamanCount();
        int pengembalianCount = DashboardDAO.getPengembalianCount();
        int replacementCount = DashboardDAO.getReplacementCount();
        
        VBox cardPeminjaman = createStatCard( "Data Peminjaman",String.valueOf(peminjamanCount), "#fee2e2");
        VBox cardPermintaan = createStatCard("Data Permintaan", String.valueOf(permintaanCount), "#e0f2fe");
        VBox cardPengembalian = createStatCard("Data Pengembalian", String.valueOf(pengembalianCount), "#fee2e2");
        VBox cardReplacement = createStatCard("Data Replacement", String.valueOf(replacementCount), "#e0f2fe");

        cardBox.getChildren().addAll(
            cardPeminjaman,
            cardPermintaan,
            cardPengembalian,
            cardReplacement
        );

        centerBox.getChildren().addAll(halo, cardBox);
        mainContent.getChildren().add(centerBox);

        this.setLeft(sidebar);
        this.setCenter(mainContent);
    }
    private VBox createStatCard(String title, String value, String bgColor) {

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" 
        );

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;"
        );

        VBox card = new VBox(8, titleLabel, valueLabel);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-background-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4);"
        );

        return card;
    }

    private Button createMenuButton(String text, boolean isActive) {
        Button btn = new Button(text);
        
        btn.setWrapText(true);
        btn.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(Region.USE_COMPUTED_SIZE);
    
        if (isActive) {
            btn.setStyle(
                    "-fx-background-color: rgba(164,35,35,0.10);" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #111827;" +
                    "-fx-padding: 10 15;" +
                    "-fx-background-radius: 6;" +
                    "-fx-font-size: 13px;" +
                    "-fx-alignment: center-left;" +
                    "-fx-cursor: hand;"
            );
        } else {
            btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-font-size: 13px;" +
                    "-fx-text-fill: #475569;" +
                    "-fx-padding: 10 15;" +
                    "-fx-font-weight: bold;" +
                    "-fx-alignment: center-left;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;"
            );
        }

        return btn;
    }
}
