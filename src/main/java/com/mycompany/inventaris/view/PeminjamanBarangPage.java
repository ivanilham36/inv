/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.view;

/**
 *
 * @author Amy
 */

import com.mycompany.inventaris.model.Barang;
import com.mycompany.inventaris.dao.BarangDAO;
import com.mycompany.inventaris.dao.PeminjamanDAO;
import com.mycompany.inventaris.dao.PermintaanDAO;
import com.mycompany.inventaris.dao.AuditTrailDAO;
import com.mycompany.inventaris.model.Peminjaman;
import com.mycompany.inventaris.model.Permintaan;
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
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.Node;

public class PeminjamanBarangPage extends BorderPane {

    private TableView<BarangRow> table;
    private List<Barang> allData;
    private List<BarangRow> selectedItems = new ArrayList<>();
    private User user;
    private PermintaanDAO permintaanDAO;
    private PeminjamanDAO peminjamanDAO;

    public PeminjamanBarangPage(User user) {
        allData = BarangDAO.getAll();
        permintaanDAO = new PermintaanDAO();
        peminjamanDAO = new PeminjamanDAO();
        this.user = user;
        initializeUI();
            Platform.runLater(() -> {
        Node headerBg = table.lookup(".column-header-background");
        if (headerBg != null) headerBg.setStyle("-fx-background-color: #B71C1C;");

        table.lookupAll(".column-header").forEach(node -> {
            node.setStyle("-fx-background-color: #B71C1C;");
        });

        table.lookupAll(".column-header > .label").forEach(node -> {
            node.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        });

        Node filler = table.lookup(".filler");
        if (filler != null) filler.setStyle("-fx-background-color: #B71C1C;");
    });

    }

    private void initializeUI() {
        // SIDEBAR
        VBox sidebar = createSidebar();

        // MAIN CONTENT
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30, 40, 30, 40));
        mainContent.setStyle("-fx-background-color: #f8fafc;");

        // Header
        Label title = new Label("PEMINJAMAN BARANG");
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

        ComboBox<String> kategoriBox = new ComboBox<>();
        kategoriBox.getItems().addAll("Semua Kategori", "Reusable", "Consumable", "Non Consumable");
        kategoriBox.setValue("Semua Kategori");
        kategoriBox.setStyle("-fx-font-size: 13px; -fx-padding: 6;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button meminjamBtn = new Button("Meminjam Barang");
        meminjamBtn.setStyle(
            "-fx-background-color: #3C4C79; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 25; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        meminjamBtn.setOnAction(e -> showFormPopup());

        topBar.getChildren().addAll(kategoriBox, spacer, meminjamBtn);

        // Table
        table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<BarangRow, String> noCol = new TableColumn<>("No.");
        noCol.setMinWidth(50);
        noCol.setMaxWidth(50);
        noCol.setStyle("-fx-alignment: CENTER;");
        noCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(table.getItems().indexOf(data.getValue()) + 1)));

        TableColumn<BarangRow, String> idCol = new TableColumn<>("Kode Barang");
        idCol.setMinWidth(100);
        idCol.setMaxWidth(120);
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().barang.getKode()));

        TableColumn<BarangRow, String> nameCol = new TableColumn<>("Nama Barang");
        nameCol.setMinWidth(200);
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().barang.getNama()));

        TableColumn<BarangRow, String> catCol = new TableColumn<>("Kategori");
        catCol.setMinWidth(150);
        catCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().barang.getKategori()));

        TableColumn<BarangRow, String> stokCol = new TableColumn<>("Stok Barang");
        stokCol.setMinWidth(100);
        stokCol.setMaxWidth(120);
        stokCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().barang.getStok() + " pcs"));

        TableColumn<BarangRow, Void> actionCol = new TableColumn<>("Aksi");
        actionCol.setMinWidth(180);
        actionCol.setMaxWidth(200);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private HBox actionBox = new HBox(8);
            private Button plusBtn = new Button("+");
            private Label countLabel = new Label("0");
            private Button minusBtn = new Button("-");
            private Button infoBtn = new Button("⋮");

            {
                plusBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc2626; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand;");
                minusBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #264065; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand;");
                countLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                infoBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 0;");

                plusBtn.setOnAction(e -> {
                    BarangRow row = getRowSafe();
                    if(row == null) return;
                    
                    if(isDisabled(row)) return;
                    
                    if(row.quantity >= row.barang.getStok()) return;
                    
                    row.quantity++;
                    countLabel.setText(String.valueOf(row.quantity));
                    if(!selectedItems.contains(row)) selectedItems.add(row);
                });

                minusBtn.setOnAction(e -> {
                    BarangRow row = getRowSafe();
                    if (row == null) return;

                    if (row.quantity > 0) {
                        row.quantity--;
                        countLabel.setText(String.valueOf(row.quantity));
                        if (row.quantity == 0) selectedItems.remove(row);
                    }
                });

                infoBtn.setOnAction(e -> {
                    BarangRow row = getRowSafe();
                    if(row == null) return;
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Info Barang");
                    alert.setHeaderText(row.barang.getNama());
                    alert.setContentText(
                        "ID: " + row.barang.getKode() + "\n" +
                        "Kategori: " + row.barang.getKategori() + "\n" +
                        "Stok: " + row.barang.getStok() + " pcs\n" +
                        "Kondisi: " + row.barang.getKondisi() + "\n" +
                        "Lokasi: " + row.barang.getLokasi()
                    );
                    alert.showAndWait();
                });

                actionBox.getChildren().addAll(plusBtn, countLabel, minusBtn, infoBtn);
                actionBox.setAlignment(Pos.CENTER);
            }
            private BarangRow getRowSafe(){
                if(getTableRow() == null) return null;
                Object item = getTableRow().getItem();
                if (item == null) return null;
                return (BarangRow) item;
            }
            
            private boolean isDisabled(BarangRow row){
                String status = row.barang.getStatus();
                String kondisi = row.barang.getKondisi();
                
                boolean disabled = false;
                if(status != null){
                    String s = status.toLowerCase();
                    if(s.equals("rusak") || s.equals("dipinjam")) disabled = true;
                }
                
                if(kondisi != null){
                    String k = kondisi.toLowerCase();
                    if(k.equals("rusak")) disabled = true;
                }
                
                if(row.barang.getStok() <= 0) disabled = true;
                return disabled;
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                }
                
                BarangRow row = getRowSafe();
                if (row == null) {
                    setGraphic(null);
                    return;
                }

                countLabel.setText(String.valueOf(row.quantity));

                boolean disabled = isDisabled(row);
                plusBtn.setDisable(disabled);
                minusBtn.setDisable(disabled);
                actionBox.setOpacity(disabled ? 0.4 : 1.0);

                if (disabled && row.quantity > 0) {
                    row.quantity = 0;
                    selectedItems.remove(row);
                    countLabel.setText("0");
                }

                setGraphic(actionBox);
            }
        });

        table.getColumns().addAll(noCol, idCol, nameCol, catCol, stokCol, actionCol);
        allData.forEach(b -> table.getItems().add(new BarangRow(b)));

        // Search functionality
        searchField.textProperty().addListener((obs, old, newVal) -> {
            table.getItems().clear();
            if (newVal.isEmpty()) {
                allData.forEach(b -> table.getItems().add(new BarangRow(b)));
            } else {
                String keyword = newVal.toLowerCase();
                allData.stream()
                    .filter(b -> 
                        b.getKode().toLowerCase().contains(keyword) ||
                        b.getNama().toLowerCase().contains(keyword))
                    .forEach(b -> table.getItems().add(new BarangRow(b)));
            }
        });

        kategoriBox.setOnAction(e -> {
            table.getItems().clear();
            String selected = kategoriBox.getValue();
            if (selected.equals("Semua Kategori")) {
                allData.forEach(b -> table.getItems().add(new BarangRow(b)));
            } else {
                allData.stream()
                    .filter(b -> b.getKategori().equals(selected))
                    .forEach(b -> table.getItems().add(new BarangRow(b)));
            }
        });

        mainContent.getChildren().addAll(title, searchField, topBar, table);

        this.setLeft(sidebar);
        this.setCenter(mainContent);
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
            Scene newScene = new Scene(new UserPage(user), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(newScene);
            currentStage.setMaximized(true);
        });
        
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

    private void showFormPopup() {
        if (selectedItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Peringatan");
            alert.setHeaderText(null);
            alert.setContentText("Silakan pilih barang terlebih dahulu!");
            alert.showAndWait();
            return;
        }

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

        Label title = new Label("Formulir Peminjaman");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        VBox fields = new VBox(12);

        TextField namaPeminjam = new TextField(user.getNama());
        VBox namaField = createField("Nama Peminjam", namaPeminjam);

        TextField lokasiPengambilan = new TextField();
        lokasiPengambilan.setPromptText("Ketik lokasi disini");
        VBox lokasiField = createField("Lokasi Pengiriman", lokasiPengambilan);

        TextField namaKodeBarang = new TextField();
        String barangList = selectedItems.stream()
            .map(r -> r.barang.getNama() + " (" + r.barang.getKode() + ")")
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
        namaKodeBarang.setText(barangList);
        VBox namaKodeField = createField("Nama & Kode Barang", namaKodeBarang);

        ComboBox<String> jenisBarang = new ComboBox<>();
        jenisBarang.getItems().addAll("Reusable", "Consumable", "Non Consumable");
        if (!selectedItems.isEmpty()) {
            jenisBarang.setValue(selectedItems.get(0).barang.getKategori());
        }
        jenisBarang.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 10;"
        );
        VBox jenisField = createFieldCombo("Jenis Barang", jenisBarang);

        TextField jumlahBarang = new TextField();
        jumlahBarang.setText(String.valueOf(selectedItems.stream().mapToInt(r -> r.quantity).sum()) + " pcs");
        jumlahBarang.setEditable(false);
        VBox jumlahField = createField("Jumlah Barang", jumlahBarang);

        Label statusLabel = new Label("Status");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        
        Label statusBadge = new Label("Tersedia");
        statusBadge.setStyle(
            "-fx-background-color: #22c55e; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 4 12; " +
            "-fx-background-radius: 12; " +
            "-fx-font-size: 10px; " +
            "-fx-font-weight: bold;"
        );
        VBox statusBox = new VBox(5, statusLabel, statusBadge);

        fields.getChildren().addAll(namaField, lokasiField, namaKodeField, jenisField, jumlahField, statusBox);

        Button submitBtn = new Button("Ajukan Peminjaman");
        submitBtn.setStyle(
            "-fx-background-color: #3C4C79; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand; " +
            "-fx-pref-width: 350;"
        );
        submitBtn.setOnAction(e -> {
    if (namaPeminjam.getText().isEmpty() || lokasiPengambilan.getText().isEmpty()) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Semua field harus diisi!");
        alert.showAndWait();
        return;
    }

    try {
        for (BarangRow row : selectedItems) {
            if (row.quantity > 0) {
                String kategori = row.barang.getKategori();
                Date now = new Date();

                if ("consumable".equalsIgnoreCase(kategori)) {
                    Permintaan p = new Permintaan();
                    p.setIdUser(user.getIdUser());
                    p.setIdBarang(row.barang.getIdBarang());
                    p.setJumlah(row.quantity);
                    p.setTanggal(now);

                    boolean sukses = permintaanDAO.insert(p);
                    if (!sukses) {
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Gagal menyimpan permintaan untuk " + row.barang.getNama());
                        alert.showAndWait();
                        return;
                    }

                } else {
                    String lokasi = lokasiPengambilan.getText();
                    if (lokasi == null || lokasi.trim().isEmpty()) lokasi = "-";
                    
                    Peminjaman pm = new Peminjaman();
                    pm.setIdUser(user.getIdUser());
                    pm.setIdBarang(row.barang.getIdBarang());
                    pm.setJumlah(row.quantity);
                    pm.setTanggalPeminjaman(now);
                    pm.setStatus("pending");
                    pm.setLokasi(lokasi); 

                    boolean sukses = peminjamanDAO.insert(pm);
                    if (!sukses) {
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Gagal menyimpan peminjaman untuk " + row.barang.getNama());
                        alert.showAndWait();
                        return;
                    }
                }
            }
        }

        popup.close();
        showSuccessPopup();

    } catch (Exception ex) {
        ex.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR, "Terjadi kesalahan: " + ex.getMessage());
        alert.showAndWait();
    }
});


        container.getChildren().addAll(header, title, fields, submitBtn);

        StackPane root = new StackPane(container);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        Scene scene = new Scene(root, 450, 550);
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

        Label message = new Label("Terima kasih telah meminjam barang,\nmohon ditunggu ya !!");
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
            Scene newScene = new Scene(new UserPage(user), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(newScene);
            currentStage.setMaximized(true);
        });

        HBox btnBox = new HBox(15, okBtn);
        btnBox.setAlignment(Pos.CENTER);

        container.getChildren().addAll(icon, title, message, btnBox);

        StackPane root = new StackPane(container);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        Scene scene = new Scene(root, 450, 350);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.showAndWait();

        selectedItems.clear();
        table.getItems().forEach(row -> row.quantity = 0);
        table.refresh();
    }

    static class BarangRow {
        Barang barang;
        int quantity = 0;

        BarangRow(Barang barang) {
            this.barang = barang;
        }
    }
}