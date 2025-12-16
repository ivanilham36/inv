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
import java.io.File;
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

public class PenggantianBarangPage extends BorderPane {
    
    private TableView<BarangRusakData> table;
    private List<BarangRusakData> allData = new ArrayList<>();
    private List<BarangRusakData> selectedItems = new ArrayList<>();
    private User user;
    
    public PenggantianBarangPage(User user) {
        this.user = user;
        initializeUI();
        loadDummyData();
    }
    
    private void loadDummyData() {
        // Dummy data - nanti diganti dari database
        allData.add(new BarangRusakData("BR001", "Spidol", "RL001", "Medi Pribadi", "26/11/2025", "Hilang", "Menunggu"));
        allData.add(new BarangRusakData("BR002", "Laptop Asus", "LP003", "Ahmad Fauzi", "25/11/2025", "Rusak Berat", "Disetujui"));
        allData.add(new BarangRusakData("BR003", "Proyektor", "NC002", "Siti Nurhaliza", "24/11/2025", "Rusak Ringan", "Ditolak"));
        allData.add(new BarangRusakData("BR004", "Mouse Logitech", "NC005", "Fadlia Mahasiswa", "23/11/2025", "Hilang", "Menunggu"));
        
        table.getItems().setAll(allData);
    }
    
    private void initializeUI() {
        // SIDEBAR
        VBox sidebar = createSidebar();
        
        // MAIN CONTENT
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30, 40, 30, 40));
        mainContent.setStyle("-fx-background-color: #f8fafc;");
        
        // Header
        Label title = new Label("PENGGANTIAN BARANG RUSAK");
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
        
        // Top Bar
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Semua Status", "Menunggu", "Disetujui", "Ditolak");
        statusBox.setValue("Semua Status");
        statusBox.setStyle("-fx-font-size: 13px; -fx-padding: 6;");
        
        ComboBox<String> kondisiBox = new ComboBox<>();
        kondisiBox.getItems().addAll("Semua Kondisi", "Hilang", "Rusak Berat", "Rusak Ringan");
        kondisiBox.setValue("Semua Kondisi");
        kondisiBox.setStyle("-fx-font-size: 13px; -fx-padding: 6;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button ajukanBtn = new Button("Ajukan Penggantian");
        ajukanBtn.setStyle(
            "-fx-background-color: #dc2626; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 25; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        ajukanBtn.setOnAction(e -> showFormPopup());
        
        topBar.getChildren().addAll(statusBox, kondisiBox, spacer, ajukanBtn);
        
        // Table
        table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setTableMenuButtonVisible(false);
        
        TableColumn<BarangRusakData, String> noCol = new TableColumn<>("No.");
        noCol.setMinWidth(50);
        noCol.setMaxWidth(80);
        noCol.setStyle("-fx-alignment: CENTER;");
        noCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(table.getItems().indexOf(data.getValue()) + 1)));
        
        TableColumn<BarangRusakData, String> idCol = new TableColumn<>("ID Laporan");
        idCol.setMinWidth(100);
        idCol.setStyle("-fx-alignment: CENTER-LEFT;");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIdLaporan()));
        
        TableColumn<BarangRusakData, String> namaCol = new TableColumn<>("Nama Barang");
        namaCol.setMinWidth(150);
        namaCol.setStyle("-fx-alignment: CENTER-LEFT;");
        namaCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNamaBarang()));
        
        TableColumn<BarangRusakData, String> kodeCol = new TableColumn<>("Kode Barang");
        kodeCol.setMinWidth(100);
        kodeCol.setStyle("-fx-alignment: CENTER-LEFT;");
        kodeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKodeBarang()));
        
        TableColumn<BarangRusakData, String> peminjamCol = new TableColumn<>("Peminjam");
        peminjamCol.setMinWidth(150);
        peminjamCol.setStyle("-fx-alignment: CENTER-LEFT;");
        peminjamCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNamaPeminjam()));
        
        TableColumn<BarangRusakData, String> tanggalCol = new TableColumn<>("Tanggal Laporan");
        tanggalCol.setMinWidth(130);
        tanggalCol.setStyle("-fx-alignment: CENTER-LEFT;");
        tanggalCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTanggalLaporan()));
        
        TableColumn<BarangRusakData, String> kondisiCol = new TableColumn<>("Kondisi");
        kondisiCol.setMinWidth(120);
        kondisiCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String kondisi, boolean empty) {
                super.updateItem(kondisi, empty);
                if (empty || kondisi == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(kondisi);
                    String color;
                    if (kondisi.equals("Hilang")) {
                        color = "#ef4444"; // red
                    } else if (kondisi.equals("Rusak Berat")) {
                        color = "#f97316"; // orange
                    } else {
                        color = "#f59e0b"; // yellow
                    }
                    badge.setStyle(
                        "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 4 10; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 10px; " +
                        "-fx-font-weight: bold;"
                    );
                    HBox box = new HBox(badge);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });
        kondisiCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKondisi()));
        
        TableColumn<BarangRusakData, String> statusCol = new TableColumn<>("Status");
        statusCol.setMinWidth(120);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    String color;
                    if (status.equals("Menunggu")) {
                        color = "#fbbf24"; // yellow
                    } else if (status.equals("Disetujui")) {
                        color = "#22c55e"; // green
                    } else {
                        color = "#dc2626"; // red
                    }
                    badge.setStyle(
                        "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 4 10; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-size: 10px; " +
                        "-fx-font-weight: bold;"
                    );
                    HBox box = new HBox(badge);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
               
        table.getColumns().addAll(noCol, idCol, namaCol, kodeCol, peminjamCol, tanggalCol, kondisiCol, statusCol);
        
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
                table.getItems().addAll(allData);
            } else {
                String keyword = newVal.toLowerCase();
                allData.stream()
                    .filter(data -> 
                        data.getNamaBarang().toLowerCase().contains(keyword) ||
                        data.getKodeBarang().toLowerCase().contains(keyword) ||
                        data.getNamaPeminjam().toLowerCase().contains(keyword))
                    .forEach(data -> table.getItems().add(data));
            }
        });
        
        // Filter by status
        statusBox.setOnAction(e -> applyFilters(statusBox.getValue(), kondisiBox.getValue()));
        kondisiBox.setOnAction(e -> applyFilters(statusBox.getValue(), kondisiBox.getValue()));
        
        mainContent.getChildren().addAll(title, searchField, topBar, table);
        
        this.setLeft(sidebar);
        this.setCenter(mainContent);
    }
    
    private void applyFilters(String status, String kondisi) {
        table.getItems().clear();
        
        List<BarangRusakData> filtered = new ArrayList<>(allData);
        
        if (!status.equals("Semua Status")) {
            filtered = filtered.stream()
                .filter(data -> data.getStatus().equals(status))
                .toList();
        }
        
        if (!kondisi.equals("Semua Kondisi")) {
            filtered = filtered.stream()
                .filter(data -> data.getKondisi().equals(kondisi))
                .toList();
        }
        
        table.getItems().addAll(filtered);
    }
    
    private void showDetailPopup(BarangRusakData data) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.UNDECORATED);
        
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 5);"
        );
        
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/assets/logoAsa.png")));
        logo.setFitHeight(40);
        logo.setPreserveRatio(true);
        
        Button closeBtn = new Button("×");
        closeBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #999; " +
            "-fx-font-size: 24px; " +
            "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> popup.close());
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        header.getChildren().addAll(leftSpacer, logo, rightSpacer, closeBtn);
        
        Label title = new Label("Detail Laporan Barang Rusak");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        VBox details = new VBox(12);
        details.getChildren().addAll(
            createDetailRow("ID Laporan", data.getIdLaporan()),
            createDetailRow("Nama Barang", data.getNamaBarang()),
            createDetailRow("Kode Barang", data.getKodeBarang()),
            createDetailRow("Nama Peminjam", data.getNamaPeminjam()),
            createDetailRow("Tanggal Laporan", data.getTanggalLaporan()),
            createDetailRow("Kondisi", data.getKondisi()),
            createDetailRow("Status", data.getStatus())
        );
        
        Button okBtn = new Button("Tutup");
        okBtn.setStyle(
            "-fx-background-color: #3C4C79; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 30; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        okBtn.setOnAction(e -> popup.close());
        
        container.getChildren().addAll(header, title, details, okBtn);
        
        StackPane root = new StackPane(container);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        
        Scene scene = new Scene(root, 450, 520);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.show();
    }
    
    private VBox createDetailRow(String label, String value) {
        VBox box = new VBox(5);
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        
        Label lblValue = new Label(value);
        lblValue.setStyle(
            "-fx-font-size: 13px; " +
            "-fx-text-fill: #1e293b; " +
            "-fx-padding: 8 10; " +
            "-fx-background-color: #f8fafc; " +
            "-fx-background-radius: 6;"
        );
        
        box.getChildren().addAll(lblLabel, lblValue);
        return box;
    }
    
    private void showFormPopup() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.UNDECORATED);
        
        VBox container = new VBox(13);
        container.setPadding(new Insets(25));
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 5);"
        );
        
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/assets/logoAsa.png")));
        logo.setFitHeight(50);
        logo.setPreserveRatio(true);
        
        Button closeBtn = new Button("×");
        closeBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #999; " +
            "-fx-font-size: 24px; " +
            "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> popup.close());
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        header.getChildren().addAll(leftSpacer, logo, rightSpacer, closeBtn);
        
        Label title = new Label("Formulir Penggantian Barang");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        VBox fields = new VBox(12);
        
        TextField namaPelapor = new TextField(user.getNama());
        namaPelapor.setEditable(false);
        VBox namaField = createField("Nama Pelapor", namaPelapor);
        
        TextField namaBarang = new TextField();
        namaBarang.setPromptText("Masukkan nama barang");
        VBox namaBarangField = createField("Nama Barang", namaBarang);
        
        TextField kodeBarang = new TextField();
        kodeBarang.setPromptText("Masukkan kode barang");
        VBox kodeField = createField("Kode Barang", kodeBarang);
        
        ComboBox<String> kondisiCombo = new ComboBox<>();
        kondisiCombo.getItems().addAll("Hilang", "Rusak Berat", "Rusak Ringan");
        kondisiCombo.setPromptText("Pilih kondisi");
        kondisiCombo.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 10;"
        );
        VBox kondisiField = createFieldCombo("Kondisi Barang", kondisiCombo);
        
        TextArea keterangan = new TextArea();
        keterangan.setPromptText("Jelaskan kronologi kerusakan/kehilangan");
        keterangan.setPrefRowCount(3);
        keterangan.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 10;"
        );
        VBox keteranganField = createFieldArea("Keterangan", keterangan);
        
        fields.getChildren().addAll(namaField, namaBarangField, kodeField, kondisiField, keteranganField);
        
        Button submitBtn = new Button("Ajukan Penggantian");
        submitBtn.setStyle(
            "-fx-background-color: #dc2626; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 350;"
        );
        submitBtn.setOnAction(e -> {
            if (namaBarang.getText().isEmpty() || kodeBarang.getText().isEmpty() || 
                kondisiCombo.getValue() == null || keterangan.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Semua field harus diisi!");
                alert.showAndWait();
            } else {
                // TODO: Save to database
                popup.hide();
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    showSuccessPopup();
                });
            }
        });
        
        container.getChildren().addAll(header, title, fields, submitBtn);
        
        StackPane root = new StackPane(container);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        
        Scene scene = new Scene(root, 450, 600);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.showAndWait();
    }
    
    private VBox createField(String label, TextField field) {
        VBox box = new VBox(5);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        field.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 10;"
        );
        box.getChildren().addAll(lbl, field);
        return box;
    }
    
    private VBox createFieldCombo(String label, ComboBox<String> combo) {
        VBox box = new VBox(5);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        box.getChildren().addAll(lbl, combo);
        return box;
    }
    
    private VBox createFieldArea(String label, TextArea area) {
        VBox box = new VBox(5);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        box.getChildren().addAll(lbl, area);
        return box;
    }
    
    private void showSuccessPopup() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.UNDECORATED);
        
        VBox container = new VBox(20);
        container.setPadding(new Insets(40));
        container.setAlignment(Pos.CENTER);
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 5);"
        );
        
        Circle circle = new Circle(40, Color.web("#22c55e"));
        Label checkmark = new Label("✓");
        checkmark.setStyle("-fx-font-size: 40px; -fx-text-fill: white; -fx-font-weight: bold;");
        StackPane icon = new StackPane(circle, checkmark);
        
        Label title = new Label("Sukses !!");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label message = new Label("Laporan penggantian barang berhasil diajukan!\nMohon tunggu konfirmasi dari admin.");
        message.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-text-alignment: center;");
        message.setWrapText(true);
        
        Button okBtn = new Button("Kembali ke Dashboard");
        okBtn.setStyle(
            "-fx-background-color: #3C4C79; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 25; " +
            "-fx-background-radius: 20; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        okBtn.setOnAction(e -> {
            popup.close();
            Stage currentStage = (Stage) this.getScene().getWindow();
            Scene newScene = new Scene(new UserPage(user), 1280, 720);
            currentStage.setScene(newScene);
        });
        
        container.getChildren().addAll(icon, title, message, okBtn);
        
        StackPane root = new StackPane(container);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        
        Scene scene = new Scene(root, 450, 350);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.showAndWait();
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
        
        Image userPhoto;
        if (user.getPhoto() != null && !user.getPhoto().isEmpty()
                && new File(user.getPhoto()).exists()) {
            userPhoto = new Image(
                new File(user.getPhoto()).toURI().toString(),
                false
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
        roleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        
        VBox textBox = new VBox(2, nameLabel, roleLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        HBox userBox = new HBox(10, userImage, textBox);
        userBox.setAlignment(Pos.CENTER_LEFT);
        userBox.setPadding(new Insets(10, 10, 20, 10));
        
        VBox menuBox = new VBox(8);
        Button dashboardBtn = createMenuButton("🏠  Dashboard", false);
        Button statusBtn = createMenuButton("📊  Status", false);
        Button riwayatBtn = createMenuButton("🕐  Riwayat", false);
        
        dashboardBtn.setOnAction(e -> {
            Stage currentStage = (Stage) dashboardBtn.getScene().getWindow();
            Scene newScene = new Scene(new UserPage(user), 1280, 720);
            currentStage.setScene(newScene);
        });
        
        statusBtn.setOnAction(e -> {
            Stage currentStage = (Stage) statusBtn.getScene().getWindow();
            Scene newScene = new Scene(new StatusPage(user), 1280, 720);
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
    
    // Inner class for BarangRusakData
    public static class BarangRusakData {
        private String idLaporan;
        private String namaBarang;
        private String kodeBarang;
        private String namaPeminjam;
        private String tanggalLaporan;
        private String kondisi;
        private String status;
        
        public BarangRusakData(String idLaporan, String namaBarang, String kodeBarang, 
                              String namaPeminjam, String tanggalLaporan, String kondisi, String status) {
            this.idLaporan = idLaporan;
            this.namaBarang = namaBarang;
            this.kodeBarang = kodeBarang;
            this.namaPeminjam = namaPeminjam;
            this.tanggalLaporan = tanggalLaporan;
            this.kondisi = kondisi;
            this.status = status;
        }
        
        public String getIdLaporan() { return idLaporan; }
        public String getNamaBarang() { return namaBarang; }
        public String getKodeBarang() { return kodeBarang; }
        public String getNamaPeminjam() { return namaPeminjam; }
        public String getTanggalLaporan() { return tanggalLaporan; }
        public String getKondisi() { return kondisi; }
        public String getStatus() { return status; }
    }
}