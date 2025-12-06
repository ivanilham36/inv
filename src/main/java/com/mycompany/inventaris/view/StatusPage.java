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
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.ArrayList;
import java.util.List;

public class StatusPage extends BorderPane {
    
    private TableView<StatusBarangData> table;
    private List<StatusBarangData> allData;
    private User user;
    
    public StatusPage(User user) {
        this.user = user;
        allData = new ArrayList<>();
        // Dummy data
        allData.add(new StatusBarangData("RL001", "Spidol", "Reusable", "1 pcs", "Belum Diverifikasi"));
        allData.add(new StatusBarangData("NC002", "Proyektor", "Electronics", "1 pcs", "Diverifikasi"));
        allData.add(new StatusBarangData("LP003", "Laptop Asus", "Electronics", "1 pcs", "Ditolak"));
        
        initializeUI();
    }

    private void initializeUI() {
        // SIDEBAR
        VBox sidebar = createSidebar();

        // MAIN CONTENT
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30, 40, 30, 40));
        mainContent.setStyle("-fx-background-color: #f8fafc;");

        // Header
        Label title = new Label("STATUS BARANG");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Search Bar
        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Pencarian");
        searchField.setPrefWidth(400);
        searchField.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 10 15;"
        );

        // Top Bar with Filter
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> kategoriBox = new ComboBox<>();
        kategoriBox.getItems().addAll("Semua Kategori", "Reusable", "Electronics", "Furniture");
        kategoriBox.setValue("Semua Kategori");
        kategoriBox.setStyle("-fx-font-size: 13px; -fx-padding: 6;");

        topBar.getChildren().add(kategoriBox);

        // Table
        table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setTableMenuButtonVisible(false);
        table.setPlaceholder(new Label("TIDAK ADA STATUS BARANG."));

        TableColumn<StatusBarangData, String> noCol = new TableColumn<>("No.");
        noCol.setMinWidth(50);
        noCol.setMaxWidth(80);
        noCol.setStyle("-fx-alignment: CENTER;");
        noCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(table.getItems().indexOf(data.getValue()) + 1)));

        TableColumn<StatusBarangData, String> idCol = new TableColumn<>("ID Barang");
        idCol.setMinWidth(120);
        idCol.setMaxWidth(150);
        idCol.setStyle("-fx-alignment: CENTER-LEFT;");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIdBarang()));

        TableColumn<StatusBarangData, String> namaCol = new TableColumn<>("Nama Barang");
        namaCol.setMinWidth(180);
        namaCol.setStyle("-fx-alignment: CENTER-LEFT;");
        namaCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNamaBarang()));

        TableColumn<StatusBarangData, String> kategoriCol = new TableColumn<>("Kategori");
        kategoriCol.setMinWidth(150);
        kategoriCol.setStyle("-fx-alignment: CENTER-LEFT;");
        kategoriCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKategori()));

        TableColumn<StatusBarangData, String> jumlahCol = new TableColumn<>("Jumlah Barang");
        jumlahCol.setMinWidth(130);
        jumlahCol.setStyle("-fx-alignment: CENTER-LEFT;");
        jumlahCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getJumlahBarang()));

        TableColumn<StatusBarangData, String> statusCol = new TableColumn<>("Status");
        statusCol.setMinWidth(180);
        statusCol.setStyle("-fx-alignment: CENTER-LEFT;");
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label statusLabel = new Label(status);
                    
                    if (status.equals("Belum Diverifikasi")) {
                        statusLabel.setStyle(
                            "-fx-background-color: #fef3c7; " +
                            "-fx-text-fill: #92400e; " +
                            "-fx-padding: 5 15; " +
                            "-fx-background-radius: 12; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: bold;"
                        );
                    } else if (status.equals("Diverifikasi")) {
                        statusLabel.setStyle(
                            "-fx-background-color: #dcfce7; " +
                            "-fx-text-fill: #166534; " +
                            "-fx-padding: 5 15; " +
                            "-fx-background-radius: 12; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: bold;"
                        );
                    } else if (status.equals("Ditolak")) {
                        statusLabel.setStyle(
                            "-fx-background-color: #fee2e2; " +
                            "-fx-text-fill: #991b1b; " +
                            "-fx-padding: 5 15; " +
                            "-fx-background-radius: 12; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: bold;"
                        );
                    }
                    
                    HBox box = new HBox(statusLabel);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                    setText(null);
                }
            }
        });
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        table.getColumns().addAll(noCol, idCol, namaCol, kategoriCol, jumlahCol, statusCol);
        allData.forEach(data -> table.getItems().add(data));

        // Apply header styling
        this.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.Node headerBg = table.lookup(".column-header-background");
                    if (headerBg != null) {
                        headerBg.setStyle("-fx-background-color: #B71C1C;");
                    }
                    
                    table.lookupAll(".column-header").forEach(node -> {
                        node.setStyle("-fx-background-color: #B71C1C;");
                    });
                    
                    table.lookupAll(".column-header > .label").forEach(node -> {
                        node.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    });
                    
                    javafx.scene.Node filler = table.lookup(".filler");
                    if (filler != null) {
                        filler.setStyle("-fx-background-color: #B71C1C;");
                    }
                });
            }
        });

        // Search functionality
        searchField.textProperty().addListener((obs, old, newVal) -> {
            table.getItems().clear();
            if (newVal.isEmpty()) {
                allData.forEach(data -> table.getItems().add(data));
            } else {
                String keyword = newVal.toLowerCase();
                allData.stream()
                    .filter(data -> 
                        data.getIdBarang().toLowerCase().contains(keyword) ||
                        data.getNamaBarang().toLowerCase().contains(keyword) ||
                        data.getKategori().toLowerCase().contains(keyword))
                    .forEach(data -> table.getItems().add(data));
            }
        });

        // Filter by kategori
        kategoriBox.setOnAction(e -> {
            table.getItems().clear();
            String selected = kategoriBox.getValue();
            if (selected.equals("Semua Kategori")) {
                allData.forEach(data -> table.getItems().add(data));
            } else {
                allData.stream()
                    .filter(data -> data.getKategori().equals(selected))
                    .forEach(data -> table.getItems().add(data));
            }
        });

        mainContent.getChildren().addAll(title, searchField, topBar, table);

        this.setLeft(sidebar);
        this.setCenter(mainContent);
    }

    private void showActionMenu(Button button, StatusBarangData data) {
        // Method ini tidak digunakan lagi karena tombol menu sudah dihapus
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setAlignment(Pos.TOP_LEFT);
        sidebar.setPrefWidth(200);
        sidebar.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-width: 0 1 0 0; " +
            "-fx-border-color: #e5e7eb;"
        );

        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/assets/logoAsa.png")));
        logo.setFitHeight(70);
        logo.setPreserveRatio(true);
        VBox logoBox = new VBox(logo);
        logoBox.setAlignment(Pos.TOP_LEFT);

        Image userPhoto = new Image(getClass().getResourceAsStream("/assets/user.png"));
        ImageView userImage = new ImageView(userPhoto);
        userImage.setFitWidth(40);
        userImage.setFitHeight(40);
        userImage.setPreserveRatio(true);
        Circle clipCircle = new Circle(20, 20, 20);
        userImage.setClip(clipCircle);

        String fullName = user.getNama().toUpperCase();
        Label nameLabel = new Label(fullName);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label roleLabel = new Label(user.getRole().toUpperCase());
        roleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");

        VBox textBox = new VBox(2, nameLabel, roleLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        HBox userBox = new HBox(10, userImage, textBox);
        userBox.setAlignment(Pos.CENTER_LEFT);
        userBox.setPadding(new Insets(10, 10, 20, 10));

        VBox menuBox = new VBox(8);
        Button dashboardBtn = createMenuButton("🏠  Dashboard", false);
        Button statusBtn = createMenuButton("📊  Status", true);
        Button riwayatBtn = createMenuButton("🕐  Riwayat", false);

        dashboardBtn.setOnAction(e -> {
            Stage currentStage = (Stage) dashboardBtn.getScene().getWindow();
            Scene newScene = new Scene(new UserPage(user), 1280, 720);
            currentStage.setScene(newScene);
        });

        riwayatBtn.setOnAction(e -> {
            Stage currentStage = (Stage) riwayatBtn.getScene().getWindow();
            Scene newScene = new Scene(new RiwayatPage(user), 1280, 720);
            currentStage.setScene(newScene);
        });

        menuBox.getChildren().addAll(dashboardBtn, statusBtn, riwayatBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("↩ Logout");
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
            Stage currentStage = (Stage) logoutBtn.getScene().getWindow();
            Scene newScene = new Scene(new MainPage(currentStage), 1280, 720);
            currentStage.setScene(newScene);
        });

        sidebar.getChildren().addAll(logoBox, userBox, menuBox, spacer, logoutBtn);
        return sidebar;
    }

    private Button createMenuButton(String text, boolean isActive) {
        Button btn = new Button(text);
        if (isActive) {
            btn.setStyle(
                "-fx-background-color: rgba(164,35,35,0.10); " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #111827; " +
                "-fx-padding: 10 15; " +
                "-fx-background-radius: 6; " +
                "-fx-font-size: 13px; " +
                "-fx-alignment: center-left; " +
                "-fx-cursor: hand;"
            );
        } else {
            btn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-font-size: 13px; " +
                "-fx-text-fill: #475569; " +
                "-fx-padding: 10 15; " +
                "-fx-font-weight: bold; " +
                "-fx-alignment: center-left; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand;"
            );
        }
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    // Inner class untuk data status barang
    public static class StatusBarangData {
        private String idBarang;
        private String namaBarang;
        private String kategori;
        private String jumlahBarang;
        private String status;
        
        public StatusBarangData(String idBarang, String namaBarang, String kategori, 
                               String jumlahBarang, String status) {
            this.idBarang = idBarang;
            this.namaBarang = namaBarang;
            this.kategori = kategori;
            this.jumlahBarang = jumlahBarang;
            this.status = status;
        }
        
        public String getIdBarang() { return idBarang; }
        public String getNamaBarang() { return namaBarang; }
        public String getKategori() { return kategori; }
        public String getJumlahBarang() { return jumlahBarang; }
        public String getStatus() { return status; }
        
        public void setStatus(String status) { this.status = status; }
    }
}