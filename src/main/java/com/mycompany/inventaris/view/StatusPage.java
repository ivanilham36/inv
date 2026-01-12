package com.mycompany.inventaris.view;

import com.mycompany.inventaris.dao.AuditTrailDAO;
import com.mycompany.inventaris.dao.StatusDAO;
import com.mycompany.inventaris.model.Riwayat;
import com.mycompany.inventaris.model.User;
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

public class StatusPage extends BorderPane {

    private TableView<Riwayat> table = new TableView<>();
    private StatusDAO statusDAO = new StatusDAO();
    private List<Riwayat> allData = new ArrayList<>();
    private User user;

    public StatusPage(User user) {
        this.user = user;
        initializeUI();
        loadData();
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    private void initializeUI() {
        VBox sidebar = createSidebar();

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30, 40, 30, 40));
        mainContent.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("STATUS BARANG");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

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

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> kategoriBox = new ComboBox<>();
        kategoriBox.getItems().addAll("Semua Kategori", "Permintaan", "Peminjaman", "Replacement");
        kategoriBox.setValue("Semua Kategori");
        kategoriBox.setStyle("-fx-font-size: 13px; -fx-padding: 6;");
        topBar.getChildren().add(kategoriBox);

        table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setTableMenuButtonVisible(false);
        table.setPlaceholder(new Label("TIDAK ADA STATUS BARANG."));

        TableColumn<Riwayat, String> noCol = new TableColumn<>("No.");
        noCol.setMinWidth(50);
        noCol.setMaxWidth(80);
        noCol.setStyle("-fx-alignment: CENTER;");
        noCol.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(table.getItems().indexOf(data.getValue()) + 1))
        );

        TableColumn<Riwayat, String> typeCol = new TableColumn<>("Tipe");
        typeCol.setMinWidth(120);
        typeCol.setMaxWidth(150);
        typeCol.setStyle("-fx-alignment: CENTER-LEFT;");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getType())));

        TableColumn<Riwayat, String> namaCol = new TableColumn<>("Nama Barang");
        namaCol.setMinWidth(180);
        namaCol.setStyle("-fx-alignment: CENTER-LEFT;");
        namaCol.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getNamaBarang())));

        TableColumn<Riwayat, String> kodeCol = new TableColumn<>("Kode Barang");
        kodeCol.setMinWidth(150);
        kodeCol.setStyle("-fx-alignment: CENTER-LEFT;");
        kodeCol.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getKodeBarang())));

        TableColumn<Riwayat, String> jumlahCol = new TableColumn<>("Jumlah Barang");
        jumlahCol.setMinWidth(130);
        jumlahCol.setStyle("-fx-alignment: CENTER-LEFT;");
        jumlahCol.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getJumlah()))
        );

        TableColumn<Riwayat, String> statusCol = new TableColumn<>("Status");
        statusCol.setMinWidth(180);
        statusCol.setStyle("-fx-alignment: CENTER-LEFT;");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getStatus())));

        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null || status.trim().isEmpty()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                String s = status.trim();

                Label badge = new Label(s);
                badge.setStyle(badgeStyleForStatus(s));

                HBox box = new HBox(badge);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
                setText(null);
            }

            private String badgeStyleForStatus(String status) {
                String s = status.toLowerCase();

                if (s.equals("menunggu") || s.equals("pending")) {
                    return "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; " +
                           "-fx-padding: 5 15; -fx-background-radius: 12; " +
                           "-fx-font-size: 11px; -fx-font-weight: bold;";
                }

                if (s.equals("dipinjam")) {
                    return "-fx-background-color: #dcfce7; -fx-text-fill: #166534; " +
                           "-fx-padding: 5 15; -fx-background-radius: 12; " +
                           "-fx-font-size: 11px; -fx-font-weight: bold;";
                }

                if (s.equals("pengembalian")) {
                    return "-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; " +
                           "-fx-padding: 5 15; -fx-background-radius: 12; " +
                           "-fx-font-size: 11px; -fx-font-weight: bold;";
                }

                if (s.equals("diproses")) {
                    return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; " +
                           "-fx-padding: 5 15; -fx-background-radius: 12; " +
                           "-fx-font-size: 11px; -fx-font-weight: bold;";
                }

                if (s.equals("ditolak")) {
                    return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; " +
                           "-fx-padding: 5 15; -fx-background-radius: 12; " +
                           "-fx-font-size: 11px; -fx-font-weight: bold;";
                }

                if (s.equals("dikembalikan") || s.equals("selesai")) {
                    return "-fx-background-color: #e0e7ff; -fx-text-fill: #3730a3; " +
                           "-fx-padding: 5 15; -fx-background-radius: 12; " +
                           "-fx-font-size: 11px; -fx-font-weight: bold;";
                }

                return "-fx-background-color: #e5e7eb; -fx-text-fill: #111827; " +
                       "-fx-padding: 5 15; -fx-background-radius: 12; " +
                       "-fx-font-size: 11px; -fx-font-weight: bold;";
            }
        });

        table.getColumns().addAll(noCol, typeCol, namaCol, kodeCol, jumlahCol, statusCol);

        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.Node headerBg = table.lookup(".column-header-background");
                    if (headerBg != null) headerBg.setStyle("-fx-background-color: #B71C1C;");

                    table.lookupAll(".column-header").forEach(node ->
                        node.setStyle("-fx-background-color: #B71C1C;")
                    );

                    table.lookupAll(".column-header > .label").forEach(node ->
                        node.setStyle("-fx-text-fill: white; -fx-font-weight: bold;")
                    );

                    javafx.scene.Node filler = table.lookup(".filler");
                    if (filler != null) filler.setStyle("-fx-background-color: #B71C1C;");
                });
            }
        });

        searchField.textProperty().addListener((obs, old, newVal) -> {
            applySearchAndFilter(searchField.getText(), kategoriBox.getValue());
        });

        kategoriBox.setOnAction(e -> {
            applySearchAndFilter(searchField.getText(), kategoriBox.getValue());
        });

        mainContent.getChildren().addAll(title, searchField, topBar, table);

        this.setLeft(sidebar);
        this.setCenter(mainContent);
    }

    private void applySearchAndFilter(String query, String kategori) {
        String keyword = (query == null) ? "" : query.trim().toLowerCase();
        String selected = (kategori == null) ? "Semua Kategori" : kategori;

        table.getItems().clear();

        allData.stream()
            .filter(d -> {
                if (selected.equals("Semua Kategori")) return true;
                return safe(d.getType()).equalsIgnoreCase(selected);
            })
            .filter(d -> {
                if (keyword.isEmpty()) return true;
                return safe(d.getNamaBarang()).toLowerCase().contains(keyword)
                    || safe(d.getKodeBarang()).toLowerCase().contains(keyword)
                    || safe(d.getType()).toLowerCase().contains(keyword)
                    || safe(d.getStatus()).toLowerCase().contains(keyword);
            })
            .forEach(table.getItems()::add);
    }

    private void loadData() {
        allData.clear();
        allData.addAll(statusDAO.getStatusByUser(user.getIdUser()));
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
            userPhoto = new Image(new java.io.ByteArrayInputStream(user.getPhoto()));
        } else {
            userPhoto = new Image(getClass().getResourceAsStream("/assets/user.png"));
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
        Button statusBtn = createMenuButton("📊  Status", true);
        Button riwayatBtn = createMenuButton("🕐  Riwayat", false);

        dashboardBtn.setOnAction(e -> {
            Stage currentStage = (Stage) dashboardBtn.getScene().getWindow();
            currentStage.setScene(new Scene(new UserPage(user), 1280, 720));
        });

        riwayatBtn.setOnAction(e -> {
            Stage currentStage = (Stage) riwayatBtn.getScene().getWindow();
            currentStage.setScene(new Scene(new RiwayatPage(user), 1280, 720));
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
            currentStage.setScene(new Scene(new MainPage(currentStage), 1280, 720));
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
}
