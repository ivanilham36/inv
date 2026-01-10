/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.view;

/**
 *
 * @author Amy
 */

import com.mycompany.inventaris.dao.BarangDAO;
import com.mycompany.inventaris.dao.AuditTrailDAO;
import com.mycompany.inventaris.model.Barang;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.print.PrinterJob;

public class ManageDataPage extends BorderPane {

    private TableView<BarangData> table;
    private List<BarangData> allData;
    private User user;

    public ManageDataPage(User user) {
        this.user = user;
        allData = new ArrayList<>();

        try {
            for (Barang b : BarangDAO.getAll()) {
                allData.add(new BarangData(
                        b.getKode(),
                        b.getNama(),
                        b.getLokasi(),
                        String.valueOf(b.getStok())
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initializeUI();
    }

    private void initializeUI() {
        VBox sidebar = createSidebar();

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30, 40, 30, 40));
        mainContent.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("Master Data Management");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_RIGHT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Cari Nama / ID barang");
        searchField.setPrefWidth(300);
        searchField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-radius: 25; " +
                        "-fx-background-radius: 25; " +
                        "-fx-padding: 8 20;"
        );

        topBar.getChildren().add(searchField);

        // Table Container
        VBox tableContainer = new VBox();
        tableContainer.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10;"
        );

        // Table
        table = new TableView<>();
        table.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-table-cell-border-color: #e5e7eb;"
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Apply RED header styling
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
                        node.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
                    });
                    javafx.scene.Node filler = table.lookup(".filler");
                    if (filler != null) {
                        filler.setStyle("-fx-background-color: #B71C1C;");
                    }
                });
            }
        });

        TableColumn<BarangData, String> idCol = new TableColumn<>("ID Barang");
        idCol.setMinWidth(100);
        idCol.setStyle("-fx-alignment: CENTER;");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIdBarang()));

        TableColumn<BarangData, String> namaCol = new TableColumn<>("Barang");
        namaCol.setMinWidth(300);
        namaCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBarang()));

        TableColumn<BarangData, String> lokasiCol = new TableColumn<>("Lokasi");
        lokasiCol.setMinWidth(150);
        lokasiCol.setStyle("-fx-alignment: CENTER;");
        lokasiCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLokasi()));

        TableColumn<BarangData, String> jumlahCol = new TableColumn<>("Jumlah");
        jumlahCol.setMinWidth(100);
        jumlahCol.setStyle("-fx-alignment: CENTER;");
        jumlahCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getJumlah()));

        table.getColumns().addAll(idCol, namaCol, lokasiCol, jumlahCol);
        allData.forEach(data -> table.getItems().add(data));

        tableContainer.getChildren().add(table);

        // DOUBLE CLICK ROW = EDIT/DELETE
        table.setRowFactory(tv -> {
            TableRow<BarangData> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (!user.isSuperAdmin()) return; // <--- ini penting
                if (!row.isEmpty() && ev.getClickCount() == 2) {
                    BarangData selected = row.getItem();
                    Alert pilih = new Alert(Alert.AlertType.CONFIRMATION);
                    pilih.setTitle("Pilih Aksi");
                    pilih.setHeaderText("Mau ngapain dengan barang ini?");
                    ButtonType editBtn = new ButtonType("Edit / Delete");
                    ButtonType tambahStokBtn = new ButtonType("Tambah Stok");
                    ButtonType batal = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);

                    pilih.getButtonTypes().setAll(editBtn, tambahStokBtn, batal);

                    pilih.showAndWait().ifPresent(res -> {
                        if (res == editBtn) {
                            showEditBarangPopup(selected);
                        } else if (res == tambahStokBtn) {
                            showTambahStokPopup(selected);
                        }
                    });
                }
            });
            return row;
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
                                        data.getBarang().toLowerCase().contains(keyword) ||
                                        data.getLokasi().toLowerCase().contains(keyword))
                        .forEach(data -> table.getItems().add(data));
            }
        });

        // Bottom buttons and pagination
        HBox bottomBar = new HBox(15);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(15, 0, 0, 0));

        // ===== FIX: fitur pindah barang TIDAK DIPAKAI, jadi sembunyikan SELALU (tanpa ubah layout) =====
        Button pindahBtn = new Button("Pindah Barang");
        pindahBtn.setStyle(
                "-fx-background-color: #3C4C79; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10 25; " +
                        "-fx-background-radius: 20; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
        );
        pindahBtn.setDisable(true);
        pindahBtn.setVisible(false);
        pindahBtn.setManaged(false); // biar tidak nyisain space

        // kalau kamu mau tetap keep handler (ga kepake), biarin; tapi aman juga kalau dibiarkan.
        // pindahBtn.setOnAction(e -> showPindahBarangPopup());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.setStyle(
                "-fx-background-color: #3C4C79; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10 30; " +
                        "-fx-background-radius: 20; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
        );

        // FIX: Add hanya untuk SuperAdmin (kalau mau CRUD penuh untuk SuperAdmin)
        if (!user.isSuperAdmin()) {
            addBtn.setDisable(true);
            addBtn.setVisible(false);
            addBtn.setManaged(false);
        }

        addBtn.setOnAction(e -> showAddBarangPopup());

        Button printBtn = new Button("Print");
        printBtn.setStyle(
                "-fx-background-color: #3C4C79; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10 30; " +
                        "-fx-background-radius: 20; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
        );
        printBtn.setOnAction(e -> handlePrint());

        Button exportBtn = new Button("Export");
        exportBtn.setStyle(
                "-fx-background-color: #3C4C79; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10 30; " +
                        "-fx-background-radius: 20; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
        );
        exportBtn.setOnAction(e -> handleExportCSV());

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // Pagination
        HBox pagination = new HBox(10);
        pagination.setAlignment(Pos.CENTER_RIGHT);

        Label pageInfo = new Label("Showing 1-10 from 100 data");
        pageInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        Button prevBtn = createPaginationButton("◀");
        Button page1Btn = createPaginationButton("1");
        page1Btn.setStyle(page1Btn.getStyle() + "-fx-background-color: #3C4C79; -fx-text-fill: white;");
        Button page2Btn = createPaginationButton("2");
        Button page3Btn = createPaginationButton("3");
        Button nextBtn = createPaginationButton("▶");

        pagination.getChildren().addAll(pageInfo, prevBtn, page1Btn, page2Btn, page3Btn, nextBtn);

        bottomBar.getChildren().addAll(pindahBtn, spacer, addBtn, printBtn, exportBtn, spacer2, pagination);

        mainContent.getChildren().addAll(title, topBar, tableContainer, bottomBar);

        this.setLeft(sidebar);
        this.setCenter(mainContent);
    }

    private void showAddBarangPopup() {
        if (!user.isSuperAdmin()) return;

        Stage popup = new Stage();
        popup.setTitle("Tambah Barang");

        // INPUT FIELDS
        TextField kodeField = new TextField();
        kodeField.setPromptText("Kode Barang");

        TextField namaField = new TextField();
        namaField.setPromptText("Nama Barang");

        // ENUM: kategori
        ComboBox<String> kategoriCombo = new ComboBox<>();
        kategoriCombo.getItems().addAll(
                "consumable",
                "non_consumable",
                "reusable"
        );
        kategoriCombo.setPromptText("Kategori");

        // Stok (numeric only)
        TextField stokField = new TextField();
        stokField.setPromptText("Stok");
        stokField.setTextFormatter(new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            return text.matches("\\d*") ? change : null;
        }));

        // ENUM: kondisi
        ComboBox<String> kondisiCombo = new ComboBox<>();
        kondisiCombo.getItems().addAll(
                "baik",
                "rusak",
                "digunakan"
        );
        kondisiCombo.setPromptText("Kondisi");

        TextField lokasiField = new TextField();
        lokasiField.setPromptText("Lokasi");

        // Deskripsi
        TextArea deskripsiArea = new TextArea();
        deskripsiArea.setPromptText("Deskripsi barang");
        deskripsiArea.setWrapText(true);
        deskripsiArea.setPrefRowCount(3);

        // ENUM: status
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(
                "tersedia",
                "rusak",
                "dipinjam"
        );
        statusCombo.setPromptText("Status");

        // BUTTONS
        Button simpanBtn = new Button("Simpan");
        Button batalBtn = new Button("Batal");

        simpanBtn.setOnAction(e -> handleAdd(
                kodeField.getText(),
                namaField.getText(),
                kategoriCombo.getValue(),
                stokField.getText(),
                kondisiCombo.getValue(),
                lokasiField.getText(),
                statusCombo.getValue(),
                deskripsiArea.getText(), 
                popup
        ));

        batalBtn.setOnAction(e -> popup.close());

        HBox buttonBox = new HBox(10, simpanBtn, batalBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(12,
                new Label("Kode Barang"), kodeField,   // ✅ hapus duplikat kodeField
                new Label("Nama Barang"), namaField,
                new Label("Kategori"), kategoriCombo,
                new Label("Stok"), stokField,
                new Label("Kondisi"), kondisiCombo,
                new Label("Lokasi"), lokasiField,
                new Label("Deskripsi"), deskripsiArea,
                new Label("Status"), statusCombo,
                buttonBox
        );

        layout.setPadding(new Insets(20));

        popup.setScene(new Scene(layout, 350, 500));
        popup.show();
    }


    private void showEditBarangPopup(BarangData data) {
        if (!user.isSuperAdmin()) return;

        Stage popup = new Stage();
        popup.setTitle("Edit / Delete Barang");

        TextField kodeField = new TextField(data.getIdBarang());
        kodeField.setDisable(true);

        TextField namaField = new TextField(data.getBarang());

        TextField lokasiField = new TextField(data.getLokasi());

        TextField stokField = new TextField(data.getJumlah());
        stokField.setTextFormatter(new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            return text.matches("\\d*") ? change : null;
        }));

        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button cancelBtn = new Button("Cancel");

        updateBtn.setOnAction(e -> {
            if (namaField.getText().trim().isEmpty() || stokField.getText().trim().isEmpty()) return;

            boolean ok = BarangDAO.updateBarangByKode(
                    kodeField.getText(),
                    namaField.getText(),
                    lokasiField.getText(),
                    Integer.parseInt(stokField.getText())
            );

            if (ok) {
                refreshTable();
                popup.close();
            } else {
                new Alert(Alert.AlertType.ERROR, "Gagal update barang").showAndWait();
            }
        });

        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Yakin hapus barang " + kodeField.getText() + " ?",
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();

            if (confirm.getResult() == ButtonType.YES) {
                boolean ok = BarangDAO.deleteBarangByKode(kodeField.getText());
                if (ok) {
                    refreshTable();
                    popup.close();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Gagal hapus barang").showAndWait();
                }
            }
        });

        cancelBtn.setOnAction(e -> popup.close());

        HBox btns = new HBox(10, updateBtn, deleteBtn, cancelBtn);
        btns.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(12,
                new Label("Kode Barang"), kodeField,
                new Label("Nama Barang"), namaField,
                new Label("Lokasi"), lokasiField,
                new Label("Stok"), stokField,
                btns
        );
        layout.setPadding(new Insets(20));

        popup.setScene(new Scene(layout, 350, 320));
        popup.show();
    }
    
    private void showTambahStokPopup(BarangData data) {
        if (!user.isSuperAdmin()) return;

        Stage popup = new Stage();
        popup.setTitle("Tambah Stok (Barang Masuk)");

        TextField kodeField = new TextField(data.getIdBarang());
        kodeField.setDisable(true);

        TextField namaField = new TextField(data.getBarang());
        namaField.setDisable(true);

        TextField lokasiField = new TextField(data.getLokasi());
        lokasiField.setPromptText("Lokasi");

        TextField jumlahField = new TextField();
        jumlahField.setPromptText("Jumlah tambah");
        jumlahField.setTextFormatter(new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            return text.matches("\\d*") ? change : null;
        }));

        TextArea ketArea = new TextArea();
        ketArea.setPromptText("Keterangan (contoh: restock / pengadaan / koreksi)");
        ketArea.setWrapText(true);
        ketArea.setPrefRowCount(3);

        Button tambahBtn = new Button("Tambah");
        Button batalBtn = new Button("Batal");

        tambahBtn.setOnAction(e -> {
            if (jumlahField.getText() == null || jumlahField.getText().trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Jumlah tambah wajib diisi!").showAndWait();
                return;
            }

            int jumlahTambah = Integer.parseInt(jumlahField.getText());
            if (jumlahTambah <= 0) {
                new Alert(Alert.AlertType.WARNING, "Jumlah tambah harus > 0!").showAndWait();
                return;
            }

            String lokasi = lokasiField.getText();
            String ket = ketArea.getText();
            if (lokasi == null || lokasi.trim().isEmpty()) lokasi = "-";
            if (ket == null || ket.trim().isEmpty()) ket = "Restock / Tambah stok";

            boolean ok = BarangDAO.tambahBarangMasuk(
                    kodeField.getText(),
                    jumlahTambah,
                    lokasi,
                    ket
            );

            if (ok) {
                refreshTable();
                popup.close();
                new Alert(Alert.AlertType.INFORMATION, "Stok berhasil ditambah & tercatat di barang_masuk.").showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, "Gagal tambah stok. Cek koneksi/DB.").showAndWait();
            }
        });

        batalBtn.setOnAction(e -> popup.close());

        HBox btns = new HBox(10, tambahBtn, batalBtn);
        btns.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(12,
                new Label("Kode Barang"), kodeField,
                new Label("Nama Barang"), namaField,
                new Label("Lokasi"), lokasiField,
                new Label("Jumlah Tambah"), jumlahField,
                new Label("Keterangan"), ketArea,
                btns
        );
        layout.setPadding(new Insets(20));

        popup.setScene(new Scene(layout, 380, 420));
        popup.show();
    }


    private void handleAdd(
            String kode,
            String nama,
            String kategori,
            String stokText,
            String kondisi,
            String lokasi,
            String status,
            String deskripsi,
            Stage popup
    ) {
        if (!user.isSuperAdmin()) return;

        if (kode == null || kode.trim().isEmpty()
                || nama == null || nama.trim().isEmpty()
                || stokText == null || stokText.trim().isEmpty()
                || kategori == null || kondisi == null || status == null) {
            new Alert(Alert.AlertType.WARNING, "Lengkapi semua field!").showAndWait();
            return;
        }

        int stok = Integer.parseInt(stokText);

        Barang barang = new Barang(
                0,
                kode,
                nama,
                kategori,
                stok,
                kondisi,
                lokasi,
                status
        );

        if (BarangDAO.insertBarang(barang, deskripsi)) {
            refreshTable();
            popup.close();
        } else {
            new Alert(Alert.AlertType.ERROR, "Gagal tambah barang").showAndWait();
        }
    }

    private void refreshTable() {
        table.getItems().clear();
        allData.clear();

        List<Barang> barangList = BarangDAO.getAll();
        if (barangList == null) return;

        for (Barang b : barangList) {
            allData.add(new BarangData(
                    b.getKode(),
                    b.getNama(),
                    b.getLokasi(),
                    String.valueOf(b.getStok())
            ));
        }

        table.getItems().addAll(allData);
    }

    private void handlePrint() {
        Stage previewStage = new Stage();
        previewStage.setTitle("Print Preview");
        previewStage.setWidth(800);
        previewStage.setHeight(600);

        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        params.setTransform(javafx.scene.transform.Transform.scale(0.75, 0.75));
        Image snapshot = table.snapshot(params, null);

        ImageView previewImage = new ImageView(snapshot);
        previewImage.setPreserveRatio(true);
        previewImage.setFitWidth(760);

        ScrollPane scrollPane = new ScrollPane(previewImage);
        scrollPane.setFitToWidth(true);

        Button printButton = new Button("Print");
        printButton.setStyle(
                "-fx-background-color: #3C4C79; -fx-text-fill: white; -fx-padding: 10 25; " +
                        "-fx-background-radius: 10; -fx-font-weight: bold;"
        );

        printButton.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(previewStage)) {
                boolean success = job.printPage(table);
                if (success) {
                    job.endJob();
                    previewStage.close();
                }
            }
        });

        Button closeButton = new Button("Close");
        closeButton.setStyle(
                "-fx-background-color: #B71C1C; -fx-text-fill: white; " +
                        "-fx-padding: 10 20; -fx-background-radius: 10;"
        );
        closeButton.setOnAction(e -> previewStage.close());

        HBox buttons = new HBox(15, printButton, closeButton);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(15));

        VBox layout = new VBox(10, scrollPane, buttons);
        layout.setPadding(new Insets(10));

        Scene previewScene = new Scene(layout);
        previewStage.setScene(previewScene);
        previewStage.show();
    }

    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to CSV");
        fileChooser.setInitialFileName("master_data_management.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(this.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("ID Barang,Barang,Lokasi,Jumlah");
                writer.newLine();

                for (BarangData data : table.getItems()) {
                    writer.write(String.format("%s,%s,%s,%s",
                            data.getIdBarang(),
                            data.getBarang(),
                            data.getLokasi(),
                            data.getJumlah()
                    ));
                    writer.newLine();
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Success");
                alert.setHeaderText(null);
                alert.setContentText("Data berhasil diekspor ke:\n" + file.getAbsolutePath());
                alert.showAndWait();

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Failed");
                alert.setHeaderText(null);
                alert.setContentText("Gagal mengekspor data: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void showPindahBarangPopup() {
      
    }

    // Inner class for DetailBarang in popup (tetap dipertahankan biar compile kalau ada referensi lama)
    public static class DetailBarang {
        private String lokasi;
        private String idBarang;
        private String namaBarang;
        private String qty;

        public DetailBarang(String lokasi, String idBarang, String namaBarang, String qty) {
            this.lokasi = lokasi;
            this.idBarang = idBarang;
            this.namaBarang = namaBarang;
            this.qty = qty;
        }

        public String getLokasi() { return lokasi; }
        public String getIdBarang() { return idBarang; }
        public String getNamaBarang() { return namaBarang; }
        public String getQty() { return qty; }
    }

    private Button createPaginationButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 8 12; " +
                        "-fx-font-size: 12px; " +
                        "-fx-cursor: hand;"
        );
        btn.setMinWidth(40);
        return btn;
    }

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

        VBox menuBox = new VBox(8);
        Button dashboardBtn = createMenuButton("🏠  Dashboard", false);
        Button verifikasiBtn = createMenuButton("✓  Verifikasi", false);
        Button userBtn = createMenuButton("👤  User", false);
        Button manageDataBtn = createMenuButton("⚙  Manage Data", true);
        Button auditTrailBtn = createMenuButton("📜  Audit Trail", false);
        Button laporanBtn = createMenuButton("📊  Laporan ▼", false);

        VBox laporanSubMenu = new VBox(5);
        laporanSubMenu.setPadding(new Insets(0, 0, 0, 20));
        laporanSubMenu.setVisible(false);
        laporanSubMenu.setManaged(false);

        Button laporanPinjamBtn = createMenuButton("Laporan Peminjaman", false);
        Button laporanGunaBtn = createMenuButton("Laporan Penggunaan", false);

        dashboardBtn.setOnAction(e -> {
            Stage s = (Stage) sidebar.getScene().getWindow();
            if (user.isSuperAdmin()) {
                s.setScene(new Scene(new SuperAdminPage(user), 1280, 720));
            } else {
                s.setScene(new Scene(new AdminPage(user), 1280, 720));
            }
        });

        verifikasiBtn.setOnAction(e -> {
            Stage currentStage = (Stage) verifikasiBtn.getScene().getWindow();
            Scene newScene = new Scene(new VerifikasiPage(user), 1280, 720);
            currentStage.setScene(newScene);
        });

        userBtn.setOnAction(e -> {
            Stage currentStage = (Stage) userBtn.getScene().getWindow();
            Scene newScene = new Scene(new AdminUserPage(user), 1280, 720);
            currentStage.setScene(newScene);
        });

        auditTrailBtn.setOnAction(e -> {
            Stage currentStage = (Stage) auditTrailBtn.getScene().getWindow();
            Scene newScene = new Scene(new AuditTrailPage(user), 1280, 720);
            currentStage.setScene(newScene);
        });

        laporanPinjamBtn.setOnAction(e -> {
            Stage s = (Stage) laporanBtn.getScene().getWindow();
            s.setScene(new Scene(new LaporanPeminjamanPage(user), 1280, 720));
        });

        laporanGunaBtn.setOnAction(e -> {
            Stage s = (Stage) laporanGunaBtn.getScene().getWindow();
            s.setScene(new Scene(new LaporanPenggunaanPage(user), 1280, 720));
        });

        laporanBtn.setOnAction(e -> {
            boolean open = laporanSubMenu.isVisible();
            laporanSubMenu.setVisible(!open);
            laporanSubMenu.setManaged(!open);
            laporanBtn.setText(open ? "📊  Laporan ▼" : "📊  Laporan ▲");
        });

        laporanSubMenu.getChildren().addAll(laporanPinjamBtn, laporanGunaBtn);

        menuBox.getChildren().add(dashboardBtn);

        if (user.isAdmin()) {
            menuBox.getChildren().add(verifikasiBtn);
        }

        if (user.isSuperAdmin()) {
            menuBox.getChildren().add(userBtn);
        }

        menuBox.getChildren().add(manageDataBtn);

        if (user.isSuperAdmin()) {
            menuBox.getChildren().add(auditTrailBtn);
        }

        menuBox.getChildren().addAll(laporanBtn, laporanSubMenu);

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

    // Inner class for BarangData
    public static class BarangData {
        private String idBarang;
        private String barang;
        private String lokasi;
        private String jumlah;

        public BarangData(String idBarang, String barang, String lokasi, String jumlah) {
            this.idBarang = idBarang;
            this.barang = barang;
            this.lokasi = lokasi;
            this.jumlah = jumlah;
        }

        public String getIdBarang() { return idBarang; }
        public String getBarang() { return barang; }
        public String getLokasi() { return lokasi; }
        public String getJumlah() { return jumlah; }
    }
}
