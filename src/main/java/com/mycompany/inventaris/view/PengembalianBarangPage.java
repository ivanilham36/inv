/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.view;

/**
 *
 * @author Amy
 */

import com.mycompany.inventaris.dao.PeminjamanDAO;
import com.mycompany.inventaris.dao.PengembalianDAO;
import com.mycompany.inventaris.model.Peminjaman;
import com.mycompany.inventaris.model.Pengembalian;
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
import javafx.beans.property.SimpleObjectProperty;

public class PengembalianBarangPage extends BorderPane {
    
    private TableView<Peminjaman> table;
    private List<Peminjaman> allData = new ArrayList<>();
    private List<Peminjaman> selectedItems = new ArrayList<>();
    private PeminjamanDAO peminjamanDAO = new PeminjamanDAO();
    private PengembalianDAO pengembalianDAO = new PengembalianDAO();
    private User user;
    
    public PengembalianBarangPage(User user) {
        this.user = user;
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        // SIDEBAR
        VBox sidebar = createSidebar();

        // MAIN CONTENT
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30, 40, 30, 40));
        mainContent.setStyle("-fx-background-color: #f8fafc;");

        // Header
        Label title = new Label("PENGEMBALIAN BARANG");
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

        Button kembalikanBtn = new Button("Kembalikan Barang");
        kembalikanBtn.setStyle(
            "-fx-background-color: #3C4C79; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 25; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        kembalikanBtn.setOnAction(e -> showFormPopup());

        topBar.getChildren().addAll(kategoriBox, spacer, kembalikanBtn);

        // Table
        table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setTableMenuButtonVisible(false);

        TableColumn<Peminjaman, String> noCol = new TableColumn<>("No.");
        noCol.setMinWidth(50);
        noCol.setMaxWidth(80);
        noCol.setStyle("-fx-alignment: CENTER;");
        noCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(table.getItems().indexOf(data.getValue()) + 1)));

        TableColumn<Peminjaman, String> idCol = new TableColumn<>("Nama Barang");
        idCol.setMinWidth(120);
        idCol.setStyle("-fx-alignment: CENTER-LEFT;");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNamaBarang()));

        TableColumn<Peminjaman, String> namaCol = new TableColumn<>("Kode Barang");
        namaCol.setMinWidth(200);
        namaCol.setStyle("-fx-alignment: CENTER-LEFT;");
        namaCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKodeBarang()));

        TableColumn<Peminjaman, String> tanggalCol = new TableColumn<>("Tanggal Peminjaman");
        tanggalCol.setMinWidth(150);
        tanggalCol.setStyle("-fx-alignment: CENTER-LEFT;");
        tanggalCol.setCellValueFactory(data -> new SimpleObjectProperty(data.getValue().getTanggalPeminjaman()));
        
        TableColumn<Peminjaman, String> jumlahCol = new TableColumn<>("Jumlah Barang");
        jumlahCol.setMinWidth(130);
        jumlahCol.setStyle("-fx-alignment: CENTER-LEFT;");
        jumlahCol.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getJumlah()))
        );
        
        TableColumn<Peminjaman, Void> aksiCol = new TableColumn<>("Aksi");
        aksiCol.setMinWidth(120);
        aksiCol.setStyle("-fx-alignment: CENTER-LEFT;");
        aksiCol.setCellFactory(col -> new TableCell<>() {
            private CheckBox checkBox = new CheckBox();
            private Button infoBtn = new Button("⋮");

            {
                checkBox.setStyle("-fx-cursor: hand;");
                infoBtn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #64748b; " +
                    "-fx-font-size: 18px; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 0 5;"
                );

                checkBox.setOnAction(e -> {
                    Peminjaman row = getTableView().getItems().get(getIndex());
                    if (checkBox.isSelected()) {
                        if (!selectedItems.contains(row)) {
                            selectedItems.add(row);
                        }
                    } else {
                        selectedItems.remove(row);
                    }
                });

                infoBtn.setOnAction(e -> {
                    Peminjaman row = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Info Barang");
                    alert.setHeaderText(row.getNamaBarang());
                    alert.setContentText(
                        "ID: " + row.getIdBarang() + "\n" +
                        "Tanggal Pinjam: " + row.getTanggalPeminjaman() + "\n" +
                        "Jumlah: " + row.getJumlah()
                    );
                    alert.showAndWait();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10, checkBox, infoBtn);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(noCol, idCol, namaCol, tanggalCol, jumlahCol, aksiCol);
        allData.forEach(row -> table.getItems().add(row));

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
                allData.forEach(row -> table.getItems().add(row));
            } else {
                String keyword = newVal.toLowerCase();
                allData.stream()
                    .filter(row -> 
                        row.getKodeBarang().toLowerCase().contains(keyword) ||
                        row.getNamaBarang().toLowerCase().contains(keyword))
                    .forEach(row -> table.getItems().add(row));
            }
        });

        mainContent.getChildren().addAll(title, searchField, topBar, table);

        this.setLeft(sidebar);
        this.setCenter(mainContent);
    }
    
    private void loadData(){
        allData.clear();
        allData.addAll(peminjamanDAO.getByUser(user.getIdUser()));
        table.getItems().setAll(allData);
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
        }else {
            // fallback kalau user belum upload foto
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

    private void showFormPopup() {
        if (selectedItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Peringatan");
            alert.setHeaderText(null);
            alert.setContentText("Silakan pilih barang yang akan dikembalikan terlebih dahulu!");
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

        Label title = new Label("Formulir Pengembalian");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        VBox fields = new VBox(12);

        TextField namaPeminjam = new TextField(user.getNama());
        VBox namaField = createField("Nama Peminjam", namaPeminjam);

        TextField lokasiPengembalian = new TextField();
        lokasiPengembalian.setPromptText("Ketik lokasi disini");
        VBox lokasiField = createField("Lokasi Pengembalian", lokasiPengembalian);

        TextField namaKodeBarang = new TextField();
        String barangList = selectedItems.stream()
            .map(r -> r.getNamaBarang() + " (" + r.getIdBarang() + ")")
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
        namaKodeBarang.setText(barangList);
        namaKodeBarang.setEditable(false);
        VBox namaKodeField = createField("Nama & Kode Barang", namaKodeBarang);

        ComboBox<String> jenisBarang = new ComboBox<>();
        jenisBarang.getItems().addAll("Reusable", "Consumable", "Non Consumable");
        jenisBarang.setValue("Reusable");
        jenisBarang.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 10;"
        );
        VBox jenisField = createFieldCombo("Jenis Barang", jenisBarang);

        TextField jumlahBarang = new TextField();
        jumlahBarang.setText(String.valueOf(selectedItems.get(0).getJumlah()));
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

        Button submitBtn = new Button("Ajukan Pengembalian");
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
            if (namaPeminjam.getText().isEmpty() || lokasiPengembalian.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Semua field harus diisi!");
                alert.showAndWait();
            } 
            
            if (selectedItems.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Pilih barang terlebih dahulu").show();
                return;
            }
            Peminjaman selected = selectedItems.get(0);
            Pengembalian pengembalian = new Pengembalian();
            pengembalian.setIdPeminjaman(selected.getIdPeminjaman());
            pengembalian.setIdUser(user.getIdUser()); 
            pengembalian.setIdBarang(selected.getIdBarang());
            pengembalian.setLokasi(lokasiPengembalian.getText());
            pengembalian.setJumlah(selected.getJumlah());
            pengembalian.setTanggalKembali(new java.util.Date());
            pengembalian.setStatus("selesai"); // 
            boolean success = pengembalianDAO.insert(pengembalian);
            
             if (success) {
                popup.close();
                showSuccessPopup();
                loadData();       
                selectedItems.clear();
            } else {
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

        Label message = new Label("Terima kasih telah mengembalikan barang!");
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

        Button riwayatBtn = new Button("Lihat Riwayat");
        riwayatBtn.setStyle(
            "-fx-background-color: #dc2626; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 25; " +
            "-fx-background-radius: 20; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        riwayatBtn.setOnAction(e -> {
            popup.close();
            Stage currentStage = (Stage) this.getScene().getWindow();
            Scene newScene = new Scene(new RiwayatPage(user), 1280, 720);
            currentStage.setScene(newScene);
        });

        HBox btnBox = new HBox(15, okBtn, riwayatBtn);
        btnBox.setAlignment(Pos.CENTER);

        container.getChildren().addAll(icon, title, message, btnBox);

        StackPane root = new StackPane(container);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        Scene scene = new Scene(root, 450, 350);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.showAndWait();

        // Reset selection
        selectedItems.clear();
        table.refresh();
    }
}