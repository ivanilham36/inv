package com.mycompany.inventaris.view;

import com.mycompany.inventaris.model.User;
import com.mycompany.inventaris.dao.AuditTrailDAO;
import com.mycompany.inventaris.dao.PeminjamanDAO;
import com.mycompany.inventaris.dao.PengembalianDAO;
import com.mycompany.inventaris.dao.ReplacementDAO;
import com.mycompany.inventaris.model.VerifikasiDTO;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class VerifikasiPage extends BorderPane {

    private TableView<VerifikasiDTO> table;
    private List<VerifikasiDTO> allData = new ArrayList<>();

    private final PeminjamanDAO peminjamanDAO = new PeminjamanDAO();
    private final ReplacementDAO replacementDAO = new ReplacementDAO();
    private final PengembalianDAO pengembalianDAO = new PengembalianDAO();


    private User admin;

    // simpan pilihan mode aktif
    private String modeAktif = "Peminjaman"; // default

    public VerifikasiPage(User admin) {
        this.admin = admin;
        initializeUI();
        loadData(); // default: peminjaman pending
    }

    private void initializeUI() {
        VBox sidebar = createSidebar();

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30, 40, 30, 40));
        mainContent.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("Verifikasi");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Pencarian");
        searchField.setPrefWidth(300);
        searchField.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 8 15;"
        );

        // ✅ kategoriBox jadi pilihan data verifikasi (layout tetap)
        ComboBox<String> kategoriBox = new ComboBox<>();
        kategoriBox.getItems().addAll("Peminjaman", "Pengembalian", "Replacement");
        kategoriBox.setValue("Peminjaman");
        kategoriBox.setStyle("-fx-font-size: 13px; -fx-padding: 6;");
        kategoriBox.setOnAction(e -> {
            modeAktif = kategoriBox.getValue();
            loadData();
            searchField.clear();
        });

        topBar.getChildren().addAll(searchField, kategoriBox);

        // Table
        table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Apply RED header
        this.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.Node headerBg = table.lookup(".column-header-background");
                    if (headerBg != null) headerBg.setStyle("-fx-background-color: #B71C1C;");
                    table.lookupAll(".column-header").forEach(node -> node.setStyle("-fx-background-color: #B71C1C;"));
                    table.lookupAll(".column-header > .label").forEach(node -> node.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"));
                    javafx.scene.Node filler = table.lookup(".filler");
                    if (filler != null) filler.setStyle("-fx-background-color: #B71C1C;");
                });
            }
        });

        TableColumn<VerifikasiDTO, String> noCol = new TableColumn<>("No.");
        noCol.setMinWidth(50);
        noCol.setMaxWidth(50);
        noCol.setStyle("-fx-alignment: CENTER;");
        noCol.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(table.getItems().indexOf(data.getValue()) + 1)));

        TableColumn<VerifikasiDTO, String> namaCol = new TableColumn<>("Nama Pengguna");
        namaCol.setMinWidth(150);
        namaCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNamaUser()));

        // label kolom tanggal tetap sama (layout tidak berubah)
        TableColumn<VerifikasiDTO, String> tanggalCol = new TableColumn<>("Tanggal Peminjaman");
        tanggalCol.setMinWidth(150);
        tanggalCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTanggal()));

        TableColumn<VerifikasiDTO, String> barangCol = new TableColumn<>("Nama & Kode Barang");
        barangCol.setMinWidth(200);
        barangCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNamaKodeBarang()));

        TableColumn<VerifikasiDTO, String> jumlahCol = new TableColumn<>("Jumlah Barang");
        jumlahCol.setMinWidth(120);
        jumlahCol.setMaxWidth(120);
        jumlahCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getJumlah())));

        TableColumn<VerifikasiDTO, Void> aksiCol = new TableColumn<>("Aksi");
        aksiCol.setMinWidth(150);
        aksiCol.setMaxWidth(150);
        aksiCol.setCellFactory(col -> new TableCell<>() {
            private HBox actionBox = new HBox(8);
            private Button approveBtn = new Button("✓");
            private Button rejectBtn = new Button("✕");
            private Button menuBtn = new Button("⋮");

            {
                approveBtn.setStyle(
                    "-fx-background-color: #22c55e; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 16px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 5 12; " +
                    "-fx-background-radius: 50; " +
                    "-fx-cursor: hand;"
                );

                rejectBtn.setStyle(
                    "-fx-background-color: #dc2626; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 16px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 5 12; " +
                    "-fx-background-radius: 50; " +
                    "-fx-cursor: hand;"
                );

                menuBtn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #64748b; " +
                    "-fx-font-size: 18px; " +
                    "-fx-cursor: hand;"
                );

                approveBtn.setOnAction(e -> {
                    VerifikasiDTO data = getTableView().getItems().get(getIndex());
                    handleApprove(data);
                });

                rejectBtn.setOnAction(e -> {
                    VerifikasiDTO data = getTableView().getItems().get(getIndex());
                    handleReject(data);
                });

                menuBtn.setOnAction(e -> {
                    VerifikasiDTO data = getTableView().getItems().get(getIndex());
                    showDetailPopup(data);
                });

                actionBox.getChildren().addAll(approveBtn, rejectBtn, menuBtn);
                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });

        table.getColumns().addAll(noCol, namaCol, tanggalCol, barangCol, jumlahCol, aksiCol);

        // Search functionality (tetap sama)
        searchField.textProperty().addListener((obs, old, newVal) -> {
            table.getItems().clear();
            if (newVal == null || newVal.isEmpty()) {
                table.getItems().addAll(allData);
            } else {
                String keyword = newVal.toLowerCase();
                allData.stream()
                    .filter(data ->
                        (data.getNamaUser() != null && data.getNamaUser().toLowerCase().contains(keyword)) ||
                        (data.getNamaKodeBarang() != null && data.getNamaKodeBarang().toLowerCase().contains(keyword))
                    )
                    .forEach(data -> table.getItems().add(data));
            }
        });

        mainContent.getChildren().addAll(title, topBar, table);

        this.setLeft(sidebar);
        this.setCenter(mainContent);
    }

    private void handleApprove(VerifikasiDTO v) {
        String teks = switch (modeAktif) {
            case "Pengembalian" -> "menyetujui pengembalian dari ";
            case "Replacement" -> "menyetujui replacement dari ";
            default -> "menyetujui peminjaman dari ";
        };

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Apakah Anda yakin ingin " + teks + v.getNamaUser() + "?",
            ButtonType.OK, ButtonType.CANCEL);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                boolean success;
                if ("Pengembalian".equals(modeAktif)) {
                    success = pengembalianDAO.setujuiPengembalian(v.getIdPeminjaman());
                } else if ("Replacement".equals(modeAktif)) {
                    // idReplacement kita taruh di idPeminjaman DTO
                    success = replacementDAO.setujuiReplacement(v.getIdPeminjaman());
                } else {
                    success = peminjamanDAO.verifikasiSetuju(v.getIdPeminjaman());
                }

                if (success) {
                    new Alert(Alert.AlertType.INFORMATION, "Berhasil!").showAndWait();
                    loadData();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Gagal! Cek status / data di DB.").showAndWait();
                }
            }
        });
    }

    private void handleReject(VerifikasiDTO v) {
        String teks = switch (modeAktif) {
            case "Pengembalian" -> "menolak pengembalian dari ";
            case "Replacement" -> "menolak replacement dari ";
            default -> "menolak peminjaman dari ";
        };

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Apakah Anda yakin ingin " + teks + v.getNamaUser() + "?",
            ButtonType.OK, ButtonType.CANCEL);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                boolean success;
                if ("Pengembalian".equals(modeAktif)) {
                    success = pengembalianDAO.tolakPengembalian(v.getIdPeminjaman());
                } else if ("Replacement".equals(modeAktif)) {
                    success = replacementDAO.tolakReplacement(v.getIdPeminjaman());
                } else {
                    success = peminjamanDAO.verifikasiTolak(v.getIdPeminjaman());
                }

                if (success) {
                    new Alert(Alert.AlertType.INFORMATION, "Berhasil!").showAndWait();
                    loadData();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Gagal!").showAndWait();
                }
            }
        });
    }

    private void loadData() {
        table.getItems().clear();

        if ("Pengembalian".equals(modeAktif)) {
            allData = pengembalianDAO.getMenungguPengembalian(); 
        } else if ("Replacement".equals(modeAktif)) {
            allData = replacementDAO.getMenungguReplacement(); 
        } else {
            allData = peminjamanDAO.getMenungguVerifikasi();
        }

        table.getItems().addAll(allData);
    }

    private void showDetailPopup(VerifikasiDTO data) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detail Permintaan");
        alert.setHeaderText("Informasi Lengkap");
        alert.setContentText(
            "Nama: " + data.getNamaUser() + "\n" +
            "Tanggal: " + data.getTanggal() + "\n" +
            "Barang: " + data.getNamaKodeBarang() + "\n" +
            "Jumlah: " + data.getJumlah() + "\n" +
            "Ruang: " + (data.getRuang() == null ? "-" : data.getRuang())
        );
        alert.showAndWait();
    }

    // =========================
    // SIDEBAR (KODE KAMU — TIDAK DIUBAH)
    // =========================
    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
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
        if (admin.getPhoto() != null && admin.getPhoto().length > 0) {
            userPhoto = new Image(new java.io.ByteArrayInputStream(admin.getPhoto()));
        } else {
            userPhoto = new Image(getClass().getResourceAsStream("/assets/user.png"));
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

        VBox menuBox = new VBox(8);
        Button dashboardBtn = createMenuButton("🏠  Dashboard", false);
        Button verifikasiBtn = createMenuButton("✓  Verifikasi", true);
        Button manageDataBtn = createMenuButton("⚙  Manage Data", false);
        Button laporanBtn = createMenuButton("📊  Laporan ▼", false);

        VBox laporanSubMenu = new VBox(5);
        laporanSubMenu.setPadding(new Insets(0, 0, 0, 20));
        laporanSubMenu.setVisible(false);
        laporanSubMenu.setManaged(false);

        Button laporanPinjamBtn = createMenuButton("Laporan Peminjaman", false);
        Button laporanGunaBtn = createMenuButton("Laporan Penggunaan", false);

        dashboardBtn.setOnAction(e -> {
            Stage currentStage = (Stage) dashboardBtn.getScene().getWindow();
            Scene newScene = new Scene(new AdminPage(admin), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(newScene);
            currentStage.setMaximized(true);
        });

        manageDataBtn.setOnAction(e -> {
            Stage currentStage = (Stage) manageDataBtn.getScene().getWindow();
            Scene newScene = new Scene(new ManageDataPage(admin), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(newScene);
            currentStage.setMaximized(true);
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

        laporanBtn.setOnAction(e -> {
            boolean open = laporanSubMenu.isVisible();
            laporanSubMenu.setVisible(!open);
            laporanSubMenu.setManaged(!open);
            laporanBtn.setText(open ? "📊  Laporan ▼" : "📊  Laporan ▲");
        });

        laporanSubMenu.getChildren().addAll(laporanPinjamBtn, laporanGunaBtn);
        menuBox.getChildren().addAll(dashboardBtn, verifikasiBtn, manageDataBtn, laporanBtn, laporanSubMenu);

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
            try { ip = java.net.InetAddress.getLocalHost().getHostAddress(); }
            catch (Exception ex) { ex.printStackTrace(); }

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
            currentStage.setMaximized(true);
        });

        sidebar.getChildren().addAll(logoBox, userBox, menuBox, spacer, logoutBtn);
        return sidebar;
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
