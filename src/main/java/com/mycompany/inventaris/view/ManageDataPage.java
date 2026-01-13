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
import com.mycompany.inventaris.Koneksi;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.print.PrinterJob;
import java.util.Optional;

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
                    String.valueOf(b.getIdBarang()),
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

        VBox tableContainer = new VBox();
        tableContainer.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10;"
        );

        table = new TableView<>();
        table.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-table-cell-border-color: #e5e7eb;"
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
        
        TableColumn<BarangData, Void> actionCol = new TableColumn<>("Action");
        actionCol.setMinWidth(150);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);

            {
                pane.setAlignment(Pos.CENTER);
                editBtn.setStyle(
                    "-fx-background-color: #3b82f6; " +
                    "-fx-text-fill: white; " +
                    "-fx-padding: 5 12; " +
                    "-fx-background-radius: 6; " +
                    "-fx-font-size: 11px; " +
                    "-fx-cursor: hand;"
                );
                deleteBtn.setStyle(
                    "-fx-background-color: #dc2626; " +
                    "-fx-text-fill: white; " +
                    "-fx-padding: 5 12; " +
                    "-fx-background-radius: 6; " +
                    "-fx-font-size: 11px; " +
                    "-fx-cursor: hand;"
                );

                editBtn.setOnAction(e -> {
                    BarangData data = getTableView().getItems().get(getIndex());
                    showEditBarangPopup(data);
                });

                deleteBtn.setOnAction(e -> {
                    BarangData data = getTableView().getItems().get(getIndex());
                    handleDelete(data);
                });

                if (user.isAdmin()) {
                    editBtn.setVisible(false);
                    deleteBtn.setVisible(false);
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

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

        table.getColumns().addAll(idCol, namaCol, lokasiCol, jumlahCol, actionCol);  
        allData.forEach(data -> table.getItems().add(data));

        tableContainer.getChildren().add(table);

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

        HBox bottomBar = new HBox(15);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(15, 0, 0, 0));

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

        if (user.isAdmin()) {
            pindahBtn.setDisable(true);
            pindahBtn.setVisible(false);
        }

        pindahBtn.setOnAction(e -> showPindahBarangPopup());
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

        if (user.isAdmin()) {
            addBtn.setDisable(true);
            addBtn.setVisible(false);
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
        Stage popup = new Stage();
        popup.setTitle("Tambah Barang");

        TextField kodeField = new TextField();
        kodeField.setPromptText("Kode Barang");

        TextField namaField = new TextField();
        namaField.setPromptText("Nama Barang");

        ComboBox<String> kategoriCombo = new ComboBox<>();
        kategoriCombo.getItems().addAll(
            "consumable",
            "non_consumable",
            "reusable"
        );
        kategoriCombo.setPromptText("Kategori");

        TextField stokField = new TextField();
        stokField.setPromptText("Stok");
        stokField.setTextFormatter(new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            return text.matches("\\d*") ? change : null;
        }));

        ComboBox<String> kondisiCombo = new ComboBox<>();
        kondisiCombo.getItems().addAll(
            "baik",
            "rusak",
            "digunakan"
        );
        kondisiCombo.setPromptText("Kondisi");

        ComboBox<String> lokasiCombo = new ComboBox<>();  
        lokasiCombo.getItems().addAll(                    
            "Gudang", "Lab SI", "Lab TI", "Lab Umum",     
            "Ruang 105", "Ruang 106", "Ruang 201"         
        );                                                
        lokasiCombo.setPromptText("Pilih Lokasi");       

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(
            "tersedia",
            "rusak",
            "dipinjam"
        );
        statusCombo.setPromptText("Status");

        Button simpanBtn = new Button("Simpan");
        Button batalBtn = new Button("Batal");

        simpanBtn.setOnAction(e -> handleAdd(
            kodeField.getText(),
            namaField.getText(),
            kategoriCombo.getValue(),
            stokField.getText(),
            kondisiCombo.getValue(),
            lokasiCombo.getValue(),
            statusCombo.getValue(),
            popup
        ));
        
        
        batalBtn.setOnAction(e -> popup.close());

        HBox buttonBox = new HBox(10, simpanBtn, batalBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(12,
            new Label("Kode Barang"), kodeField,
            new Label("Nama Barang"), namaField,
            new Label("Kategori"), kategoriCombo,
            new Label("Stok"), stokField,
            new Label("Kondisi"), kondisiCombo,
            new Label("Lokasi"), lokasiCombo,
            new Label("Status"), statusCombo,
            buttonBox
        );

        layout.setPadding(new Insets(20));

        popup.setScene(new Scene(layout, 460, 600));
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
        Stage popup
    ) {
        if (kode.isEmpty() || nama.isEmpty() || stokText.isEmpty()
            || kategori == null || kondisi == null || lokasi.isEmpty() || status == null) {

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validasi");
        alert.setHeaderText(null);
        alert.setContentText("Semua field wajib diisi!");
        alert.showAndWait();
        return;
    }

        int stok;
        try {
            stok = Integer.parseInt(stokText);
            if (stok <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validasi");
            alert.setHeaderText(null);
            alert.setContentText("Stok harus berupa angka lebih dari 0!");
            alert.showAndWait();
            return;
        }
        
        try {
        String sql = "SELECT COUNT(*) AS total FROM barang WHERE kode_barang = ?";
        Connection conn = Koneksi.getKoneksi();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, kode);
        ResultSet rs = ps.executeQuery();

        if (rs.next() && rs.getInt("total") > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Duplikasi");
            alert.setHeaderText(null);
            alert.setContentText("Kode barang sudah terdaftar! Gunakan kode lain.");
            alert.showAndWait();
            return;
        }
    } catch (Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Terjadi kesalahan saat memeriksa kode barang!");
        alert.showAndWait();
        return;
    }
        
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

        if (BarangDAO.insertBarang(barang)) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sukses");
            alert.setHeaderText(null);
            alert.setContentText("Barang berhasil ditambahkan!");
            alert.showAndWait();
            
            String ip = "UNKNOWN";
            try { ip = java.net.InetAddress.getLocalHost().getHostAddress(); } catch (Exception ex) {}

            AuditTrailDAO.log(
                user.getIdUser(),
                user.getUsername(),
                "TAMBAH BARANG",
                "Menambahkan barang: " + nama + " (" + kode + ")",
                ip,
                "BERHASIL"
            );

            refreshTable();
            popup.close();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Gagal");
            alert.setHeaderText(null);
            alert.setContentText("Gagal menambahkan barang. Periksa input atau koneksi database.");
            alert.showAndWait();
        }
    }
    
            private void showEditBarangPopup(BarangData data) {
            Stage popup = new Stage();
            popup.setTitle("Edit Barang");

            int idBarang = Integer.parseInt(data.getId());
            Barang barang = BarangDAO.getById(idBarang);

            if (barang == null) {
                showAlert("Error", "Barang tidak ditemukan!", Alert.AlertType.ERROR);
                return;
            }

            TextField kodeField = new TextField(barang.getKode());
            TextField namaField = new TextField(barang.getNama());

            ComboBox<String> kategoriCombo = new ComboBox<>();
            kategoriCombo.getItems().addAll("consumable", "non_consumable", "reusable");
            kategoriCombo.setValue(barang.getKategori());

            TextField stokField = new TextField(String.valueOf(barang.getStok()));
            stokField.setTextFormatter(new TextFormatter<>(change -> {
                String text = change.getControlNewText();
                return text.matches("\\d*") ? change : null;
            }));

            ComboBox<String> kondisiCombo = new ComboBox<>();
            kondisiCombo.getItems().addAll("baik", "rusak", "digunakan");
            kondisiCombo.setValue(barang.getKondisi());

            ComboBox<String> lokasiCombo = new ComboBox<>();         
            lokasiCombo.getItems().addAll(                          
                "Gudang", "Lab SI", "Lab TI", "Lab Umum",            
                "Ruang 105", "Ruang 106", "Ruang 201"                
            );                                                       
            lokasiCombo.setValue(barang.getLokasi());                

            ComboBox<String> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll("tersedia", "rusak", "dipinjam");
            statusCombo.setValue(barang.getStatus());

            Button simpanBtn = new Button("Update");
            simpanBtn.setStyle(
                "-fx-background-color: #3b82f6; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10 25; " +
                "-fx-background-radius: 8; " +
                "-fx-font-weight: bold; " +
                "-fx-cursor: hand;"
            );

            Button batalBtn = new Button("Batal");
            batalBtn.setStyle(
                "-fx-background-color: #64748b; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 10 25; " +
                "-fx-background-radius: 8; " +
                "-fx-font-weight: bold; " +
                "-fx-cursor: hand;"
            );

            simpanBtn.setOnAction(e -> {
                if (kodeField.getText().isEmpty() || namaField.getText().isEmpty() || 
                    stokField.getText().isEmpty() || kategoriCombo.getValue() == null || 
                    kondisiCombo.getValue() == null || statusCombo.getValue() == null) {
                    showAlert("Validasi", "Semua field harus diisi!", Alert.AlertType.WARNING);
                    return;
                }

                barang.setKode(kodeField.getText());
                barang.setNama(namaField.getText());
                barang.setKategori(kategoriCombo.getValue());
                barang.setStok(Integer.parseInt(stokField.getText()));
                barang.setKondisi(kondisiCombo.getValue());
                barang.setLokasi(lokasiCombo.getValue());
                barang.setStatus(statusCombo.getValue());

                if (BarangDAO.updateBarang(barang)) {
                    showAlert("Sukses", "Barang berhasil diupdate!", Alert.AlertType.INFORMATION);
                    refreshTable();
                    popup.close();
                    String ip = "UNKNOWN";
                    try { ip = java.net.InetAddress.getLocalHost().getHostAddress(); } catch (Exception ex) {}

                    AuditTrailDAO.log(
                        user.getIdUser(),
                        user.getUsername(),
                        "EDIT BARANG",
                        "Mengubah barang: " + barang.getNama() + " (" + barang.getKode() + ")",
                        ip,
                        "BERHASIL"
                    );
                } else {
                    showAlert("Error", "Gagal mengupdate barang", Alert.AlertType.ERROR);
                }
            });

            batalBtn.setOnAction(e -> popup.close());

            HBox buttonBox = new HBox(10, simpanBtn, batalBtn);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);

            VBox layout = new VBox(12,
                new Label("Kode Barang"), kodeField,
                new Label("Nama Barang"), namaField,
                new Label("Kategori"), kategoriCombo,
                new Label("Stok"), stokField,
                new Label("Kondisi"), kondisiCombo,
                new Label("Lokasi"), lokasiCombo,
                new Label("Status"), statusCombo,
                buttonBox
            );

            layout.setPadding(new Insets(20));
            layout.setStyle("-fx-background-color: white;");

            popup.setScene(new Scene(layout, 460, 600));
            popup.show();
        }

        private void handleDelete(BarangData data) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus Barang");
        confirm.setContentText("Apakah Anda yakin ingin menghapus barang:\n" + 
                              data.getBarang() + " (" + data.getIdBarang() + ")?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int idBarang = Integer.parseInt(data.getId());

            if (BarangDAO.deleteBarang(idBarang)) {
                showAlert("Sukses", "Barang berhasil dihapus!", Alert.AlertType.INFORMATION);
                refreshTable();
                String ip = "UNKNOWN";
                try { ip = java.net.InetAddress.getLocalHost().getHostAddress(); } catch (Exception ex) {}

                AuditTrailDAO.log(
                    user.getIdUser(),
                    user.getUsername(),
                    "HAPUS BARANG",
                    "Menghapus barang: " + data.getBarang() + " (" + data.getIdBarang() + ")",
                    ip,
                    "BERHASIL"
                );
            } else {
                showAlert("Error", "Gagal menghapus barang", Alert.AlertType.ERROR);
            }
        }
    }
    
    private void refreshTable() {
        table.getItems().clear();
        allData.clear();

        List<Barang> barangList = BarangDAO.getAll();
        if (barangList == null) return;

        for (Barang b : barangList) {
            allData.add(new BarangData(
                String.valueOf(b.getIdBarang()),
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

    /* ===========================
       PINDAH BARANG POPUP - FIX
       =========================== */
    private void showPindahBarangPopup() {
        Stage popup = new Stage();
        popup.setTitle("Pindah Barang Stock");

        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: white;");

        Label title = new Label("Pindah Barang Stock");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label dariLabel = new Label("Dari Lokasi");
        ComboBox<String> dariLokasiCombo = new ComboBox<>();
        dariLokasiCombo.getItems().addAll(
            "Gudang", "Lab SI", "Lab TI", "Lab Umum",
            "Ruang 105", "Ruang 106", "Ruang 201", "Rusak"
        );
        dariLokasiCombo.setPromptText("Pilih lokasi");
        dariLokasiCombo.setMaxWidth(Double.MAX_VALUE);

        Label keLabel = new Label("Ke Lokasi");
        ComboBox<String> keLokasiCombo = new ComboBox<>();
        keLokasiCombo.getItems().addAll(
            "Gudang", "Lab SI", "Lab TI", "Lab Umum",
            "Ruang 105", "Ruang 106", "Ruang 201"
        );
        keLokasiCombo.setPromptText("Pilih lokasi");
        keLokasiCombo.setMaxWidth(Double.MAX_VALUE);

        TableView<DetailBarang> detailTable = new TableView<>();
        detailTable.setPrefHeight(250);

        TableColumn<DetailBarang, String> lokasiCol = new TableColumn<>("Lokasi");
        lokasiCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLokasi()));

        TableColumn<DetailBarang, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdBarang())));

        TableColumn<DetailBarang, String> namaCol = new TableColumn<>("Nama Barang");
        namaCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNamaBarang()));

        TableColumn<DetailBarang, String> qtyCol = new TableColumn<>("Stok");
        qtyCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQty()));

        detailTable.getColumns().addAll(lokasiCol, idCol, namaCol, qtyCol);

        /* ===============================
           FIX: LOAD DATA MENGGUNAKAN ID
           =============================== */
        dariLokasiCombo.setOnAction(e -> {
            detailTable.getItems().clear();

            if (dariLokasiCombo.getValue() != null) {
                List<Barang> barangList = BarangDAO.getByLokasi(dariLokasiCombo.getValue());

                for (Barang b : barangList) {
                    detailTable.getItems().add(
                        new DetailBarang(
                            b.getIdBarang(),      // ID ASLI
                            b.getLokasi(),
                            b.getNama(),
                            String.valueOf(b.getStok())
                        )
                    );
                }
            }
        });

        Label qtyLabel = new Label("Jumlah yang akan dipindah:");
        TextField qtyField = new TextField();
        qtyField.setPromptText("Masukkan jumlah");
        qtyField.setTextFormatter(new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            return text.matches("\\d*") ? change : null;
        }));

        Button simpanBtn = new Button("Pindahkan");
        simpanBtn.setStyle(
            "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 10 25;" +
            "-fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;"
        );

        Button cancelBtn = new Button("Batal");
        cancelBtn.setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-padding: 10 25;" +
            "-fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;"
        );

        /* ===========================
           FIX: PINDAH BARANG DENGAN ID
           =========================== */
        simpanBtn.setOnAction(e -> {
            DetailBarang selected = detailTable.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showAlert("Validasi", "Pilih barang yang akan dipindah!", Alert.AlertType.WARNING);
                return;
            }

            if (keLokasiCombo.getValue() == null) {
                showAlert("Validasi", "Pilih lokasi tujuan!", Alert.AlertType.WARNING);
                return;
            }

            if (qtyField.getText().isEmpty()) {
                showAlert("Validasi", "Masukkan jumlah barang!", Alert.AlertType.WARNING);
                return;
            }

            int qty = Integer.parseInt(qtyField.getText());
            int idBarang = selected.getIdBarang();   // FIX: ID BARANG
            int stokTersedia = Integer.parseInt(selected.getQty());

            if (qty > stokTersedia) {
                showAlert("Validasi", "Jumlah melebihi stok tersedia!", Alert.AlertType.WARNING);
                return;
            }

            if (qty <= 0) {
                showAlert("Validasi", "Jumlah harus lebih dari 0!", Alert.AlertType.WARNING);
                return;
            }

            boolean success = BarangDAO.pindahBarang(
                idBarang,
                qty,
                keLokasiCombo.getValue()
            );

            if (success) {
                showAlert("Sukses", "Barang berhasil dipindahkan!", Alert.AlertType.INFORMATION);
                refreshTable();
                popup.close();
                String ip = "UNKNOWN";
                try { ip = java.net.InetAddress.getLocalHost().getHostAddress(); } catch (Exception ex) {}

                AuditTrailDAO.log(
                    user.getIdUser(),
                    user.getUsername(),
                    "PINDAH BARANG",
                    "Memindahkan barang: ID " + idBarang +
                    " dari " + dariLokasiCombo.getValue() +
                    " ke " + keLokasiCombo.getValue() +
                    " sejumlah " + qty,
                    ip,
                    "BERHASIL"
                );
            } else {
                showAlert("Error", "Gagal memindahkan barang", Alert.AlertType.ERROR);
            }
        });

        cancelBtn.setOnAction(e -> popup.close());

        HBox buttonBox = new HBox(10, simpanBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);

        container.getChildren().addAll(
            title,
            dariLabel, dariLokasiCombo,
            keLabel, keLokasiCombo,
            new Label("Pilih Barang:"), detailTable,
            qtyLabel, qtyField,
            buttonBox
        );

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 600, 650);
        popup.setScene(scene);
        popup.show();
    }
    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
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
                s.setScene(new Scene(new SuperAdminPage(user), s.getWidth(), s.getHeight()));
                s.setMaximized(true);
            } else {
                s.setScene(new Scene(new AdminPage(user), s.getWidth(), s.getHeight()));
                s.setMaximized(true);
            }
        });

        verifikasiBtn.setOnAction(e -> {
            Stage currentStage = (Stage) verifikasiBtn.getScene().getWindow();
            Scene newScene = new Scene(new VerifikasiPage(user), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(newScene);
            currentStage.setMaximized(true);
        });

        userBtn.setOnAction(e -> {
            Stage currentStage = (Stage) userBtn.getScene().getWindow();
            Scene newScene = new Scene(new AdminUserPage(user), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(newScene);
            currentStage.setMaximized(true);
        });

        auditTrailBtn.setOnAction(e -> {
            Stage currentStage = (Stage) auditTrailBtn.getScene().getWindow();
            Scene newScene = new Scene(new AuditTrailPage(user), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(newScene);
            currentStage.setMaximized(true);
        });

        laporanPinjamBtn.setOnAction(e -> {
            Stage s = (Stage) laporanBtn.getScene().getWindow();
            s.setScene(new Scene(new LaporanPeminjamanPage(user), s.getWidth(), s.getHeight()));
            s.setMaximized(true);
        });

        laporanGunaBtn.setOnAction(e -> {
            Stage s = (Stage) laporanGunaBtn.getScene().getWindow();
            s.setScene(new Scene(new LaporanPenggunaanPage(user), s.getWidth(), s.getHeight()));
            s.setMaximized(true);
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

    /* ======================
       DATA TABLE UTAMA
       ====================== */
    public static class BarangData {
        private String id;
        private String idBarang;
        private String barang;
        private String lokasi;
        private String jumlah;

        public BarangData(String id, String idBarang, String barang, String lokasi, String jumlah) {
            this.id = id;
            this.idBarang = idBarang;
            this.barang = barang;
            this.lokasi = lokasi;
            this.jumlah = jumlah;
        }
        
        public String getId() { return id; }
        public String getIdBarang() { return idBarang; }
        public String getBarang() { return barang; }
        public String getLokasi() { return lokasi; }
        public String getJumlah() { return jumlah; }
    }

    /* ======================
       DETAIL BARANG (POPUP)
       SUDAH DIFIX PAKAI ID
       ====================== */
    public static class DetailBarang {
        private int idBarang;        // ID BARANG ASLI DARI DB
        private String lokasi;
        private String namaBarang;
        private String qty;

        public DetailBarang(int idBarang, String lokasi, String namaBarang, String qty) {
            this.idBarang = idBarang;
            this.lokasi = lokasi;
            this.namaBarang = namaBarang;
            this.qty = qty;
        }

        public int getIdBarang() { return idBarang; }
        public String getLokasi() { return lokasi; }
        public String getNamaBarang() { return namaBarang; }
        public String getQty() { return qty; }
    }
}
