/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.view;

/**
 *
 * @author Amy
 */

import com.mycompany.inventaris.dao.RiwayatDAO;
import com.mycompany.inventaris.dao.StatusDAO;
import com.mycompany.inventaris.dao.AuditTrailDAO;
import com.mycompany.inventaris.model.Riwayat;
import com.mycompany.inventaris.model.User;
import java.io.File;
import java.text.SimpleDateFormat;
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

public class RiwayatPage extends BorderPane {
    
    private TableView<Riwayat> table = new TableView<>();
    private RiwayatDAO riwayatDAO = new RiwayatDAO();
    private List<Riwayat> allData = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private User user;
    
    public RiwayatPage(User user) {
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
        Label title = new Label("RIWAYAT BARANG");
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
        kategoriBox.getItems().addAll("Semua Kategori", "Disetujui", "Dikembalikan");
        kategoriBox.setValue("Semua Kategori");
        kategoriBox.setStyle("-fx-font-size: 13px; -fx-padding: 6;");

        topBar.getChildren().add(kategoriBox);

        // Table
        table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<Riwayat, String> noCol = new TableColumn<>("No.");
        noCol.setMinWidth(50);
        noCol.setMaxWidth(50);
        noCol.setStyle("-fx-alignment: CENTER;");
        noCol.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(table.getItems().indexOf(data.getValue()) + 1)));

        TableColumn<Riwayat, String> idCol = new TableColumn<>("Tipe");
        idCol.setMinWidth(120);
        idCol.setMaxWidth(150);
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));

        TableColumn<Riwayat, String> namaCol = new TableColumn<>("Nama Barang");
        namaCol.setMinWidth(150);
        namaCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNamaBarang()));

        TableColumn<Riwayat, String> barangCol = new TableColumn<>("Kode Barang");
        barangCol.setMinWidth(180);
        barangCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKodeBarang()));

        TableColumn<Riwayat, String> tglPinjamCol = new TableColumn<>("Tanggal Peminjaman");
        tglPinjamCol.setMinWidth(140);
        tglPinjamCol.setCellValueFactory(data -> new SimpleObjectProperty(data.getValue().getTanggalPengajuan()));

        TableColumn<Riwayat, String> tglKembaliCol = new TableColumn<>("Tanggal Pengembalian");
        tglKembaliCol.setMinWidth(150);
        tglKembaliCol.setCellValueFactory(data -> new SimpleObjectProperty(data.getValue().getTanggalPengembalian()));

        TableColumn<Riwayat, String> jumlahCol = new TableColumn<>("Jumlah Barang");
        jumlahCol.setMinWidth(120);
        jumlahCol.setMaxWidth(130);
        jumlahCol.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getJumlah()))
        );
        
        TableColumn<Riwayat, String> statusCol = new TableColumn<>("Status");
        statusCol.setMinWidth(130);
        statusCol.setMaxWidth(150);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label statusLabel = new Label(status);
                    if (status.equals("Dipinjam")) {
                        statusLabel.setStyle(
                            "-fx-background-color: #fef3c7; " +
                            "-fx-text-fill: #92400e; " +
                            "-fx-padding: 5 15; " +
                            "-fx-background-radius: 12; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: bold;"
                        );
                    } else {
                        statusLabel.setStyle(
                            "-fx-background-color: #dcfce7; " +
                            "-fx-text-fill: #166534; " +
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

        table.getColumns().addAll(noCol, idCol, namaCol, barangCol, tglPinjamCol, tglKembaliCol, jumlahCol, statusCol);
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
                        data.getNamaBarang().toLowerCase().contains(keyword) ||
//                        data.getNamaPeminjam().toLowerCase().contains(keyword) ||
                        data.getNamaBarang().toLowerCase().contains(keyword))
                    .forEach(data -> table.getItems().add(data));
            }
        });

        // Filter by status
        kategoriBox.setOnAction(e -> {
            table.getItems().clear();
            String selected = kategoriBox.getValue();
            if (selected.equals("Semua Kategori")) {
                allData.forEach(data -> table.getItems().add(data));
            } else {
                allData.stream()
                    .filter(data -> data.getStatus().equals(selected))
                    .forEach(data -> table.getItems().add(data));
            }
        });

        mainContent.getChildren().addAll(title, searchField, topBar, table);

        this.setLeft(sidebar);
        this.setCenter(mainContent);
    }

    private void loadData(){
        allData.clear();
        allData.addAll(riwayatDAO.getByUser(user.getIdUser()));
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
        roleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");

        VBox textBox = new VBox(2, nameLabel, roleLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        HBox userBox = new HBox(10, userImage, textBox);
        userBox.setAlignment(Pos.CENTER_LEFT);
        userBox.setPadding(new Insets(10, 10, 20, 10));

        VBox menuBox = new VBox(8);
        Button dashboardBtn = createMenuButton("🏠  Dashboard", false);
        Button statusBtn = createMenuButton("📊  Status", false);
        Button riwayatBtn = createMenuButton("🕐  Riwayat", true);

        dashboardBtn.setOnAction(e -> {
            Stage currentStage = (Stage) dashboardBtn.getScene().getWindow();
            Scene newScene = new Scene(new UserPage(user), 1280, 720);
            currentStage.setScene(newScene);
        });
        statusBtn.setOnAction(e -> {
            Scene newScene = new Scene(new StatusPage(user), 1280, 720);
            Stage currentStage = (Stage) statusBtn.getScene().getWindow();
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

    private void showDetailPopup(Riwayat data) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.TRANSPARENT);

        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12;"
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

        Label title = new Label("Detail Peminjaman");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        VBox details = new VBox(12);
        
        String tanggalPengajuan = data.getTanggalPengajuan() != null ? sdf.format(data.getTanggalPengajuan()) : "-";
        String tanggalKembali = data.getTanggalPengembalian() != null ? sdf.format(data.getTanggalPengembalian()) : "-";

        
        details.getChildren().addAll(
            createDetailRow("Tipe", data.getType()),
            createDetailRow("Nama Barang", data.getNamaBarang()),
            createDetailRow("Kode Barang", data.getKodeBarang()),
            createDetailRow("Tanggal Pengajuan", tanggalPengajuan),
            createDetailRow("Tanggal Pengembalian", tanggalKembali),
            createDetailRow("Jumlah Barang", String.valueOf(data.getJumlah())),
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

        Scene scene = new Scene(root, 450, 480);
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
}