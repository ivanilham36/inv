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
import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class UserPage extends BorderPane {

    private Stage stage;
    private User user;

    public UserPage(User user) {
        this.user = user;
        initializeUI();
    }

    private void initializeUI() {

        // SIDEBAR 
        VBox sidebar = new VBox(10);
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


        Image userPhoto;

    if (user.getPhoto() != null && user.getPhoto().length > 0) {
        userPhoto = new Image(
        new java.io.ByteArrayInputStream(user.getPhoto())
        );
    } else {
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

        Label nameLabel = new Label(user.getNama());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label roleLabel = new Label(user.getRole().toUpperCase());
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
        menuBox.setAlignment(Pos.TOP_CENTER);

        Button dashboardBtn = createMenuButton("🏠  Dashboard", true);
        Button statusBtn = createMenuButton("📊  Status", false);
        Button riwayatBtn = createMenuButton("🕐  Riwayat", false);

        statusBtn.setOnAction(e -> {
            Stage currentStage = (Stage) statusBtn.getScene().getWindow();
            Scene newScene = new Scene(new StatusPage(user), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(newScene);
            currentStage.setMaximized(true);
        });
        
        riwayatBtn.setOnAction(e -> {
            Stage currentStage = (Stage) riwayatBtn.getScene().getWindow();
            Scene newScene = new Scene(new RiwayatPage(user), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(newScene);
            currentStage.setMaximized(true);
        });

        menuBox.getChildren().addAll(dashboardBtn, statusBtn, riwayatBtn);


        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("↩ Logout");
        logoutBtn.setAlignment(Pos.CENTER_LEFT);
        logoutBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #475569;" +
                "-fx-padding: 12 10;" +
                "-fx-font-weight: bold;" +
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
        user.getIdUser(),          
        user.getUsername(),         
        "LOGOUT",
        "Pengguna keluar dari sistem",
        ip,
        "BERHASIL"
    ); 
        Stage currentStage = (Stage) logoutBtn.getScene().getWindow();
    Scene newScene = new Scene(new MainPage(currentStage), currentStage.getWidth(), currentStage.getHeight());
    currentStage.setScene(newScene);
    currentStage.setMaximized(true);
       });

        sidebar.getChildren().addAll(logoBox, userBox, menuBox, spacer, logoutBtn);



        // MAIN CONTENT 
        StackPane mainContent = new StackPane();
        mainContent.setStyle("-fx-background-color: #f8fafc;");

        VBox centerBox = new VBox(40);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(60));

        Label halo = new Label("HALO, " + user.getNama() + " !!");
        halo.setStyle(
                "-fx-font-size: 40px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #334155;"
        );


        Button peminjamanBtn = new Button("PEMINJAMAN BARANG");
        peminjamanBtn.setStyle(
                "-fx-background-color: #A42323;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 26;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
        );

        peminjamanBtn.setOnAction(e -> {
            Stage currentStage = (Stage) peminjamanBtn.getScene().getWindow();
            Scene newScene = new Scene(new PeminjamanBarangPage(user), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(newScene);
            currentStage.setMaximized(true);
        });


        Button pengembalianBtn = new Button("PENGEMBALIAN BARANG");
        pengembalianBtn.setStyle(
                "-fx-background-color: #3C4C79;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 26;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
        );
        
        pengembalianBtn.setOnAction(e -> {
            Stage currentStage = (Stage) pengembalianBtn.getScene().getWindow();
            Scene newScene = new Scene(new PengembalianBarangPage(user), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(newScene);
            currentStage.setMaximized(true);
        });
        
        Button replacementBtn = new Button("PENGGANTIAN BARANG");
        replacementBtn.setStyle(
                "-fx-background-color: #3C4C79;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 26;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
        );
        
        replacementBtn.setOnAction(e -> {
            Stage currentStage = (Stage) replacementBtn.getScene().getWindow();
            Scene newScene = new Scene(new PenggantianBarangPage(user), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(newScene);
            currentStage.setMaximized(true);
        });


        HBox buttonBox = new HBox(30, peminjamanBtn, pengembalianBtn, replacementBtn);
        buttonBox.setAlignment(Pos.CENTER);

        centerBox.getChildren().addAll(halo, buttonBox);
        mainContent.getChildren().add(centerBox);

        this.setLeft(sidebar);
        this.setCenter(mainContent);
    }


    private Button createMenuButton(String text, boolean isActive) {
        Button btn = new Button(text);

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

        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }
}
