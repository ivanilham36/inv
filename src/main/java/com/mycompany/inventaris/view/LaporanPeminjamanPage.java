package com.mycompany.inventaris.view;

import com.mycompany.inventaris.model.User;
import com.mycompany.inventaris.dao.AuditTrailDAO;
import com.mycompany.inventaris.dao.PeminjamanDAO;
import com.mycompany.inventaris.model.LaporanPeminjamanDTO;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.print.PrinterJob;

public class LaporanPeminjamanPage extends BorderPane {
    
    private TableView<PeminjamanData> table;
    private List<PeminjamanData> allData;
    private User user;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<String> statusFilter;
    private Pagination pagination;
    private static final int ROWS_PER_PAGE = 14;
    private List<PeminjamanData> currentViewData = new ArrayList<>();
    private Label totalLabel;
    private TextField searchField;


    
    public LaporanPeminjamanPage(User user) {
        this.user = user;
        allData = new ArrayList<>();
        initializeUI();
        loadData();
    }
    
    private void updateTotalLabel(List<?> data) {
    totalLabel.setText("Total Data: " + data.size());
}
    
    private void setupPagination(List<PeminjamanData> data) {
    currentViewData = data;

    int pageCount = (int) Math.ceil((double) data.size() / ROWS_PER_PAGE);
    pagination.setPageCount(Math.max(pageCount, 1));

    pagination.setPageFactory(pageIndex -> {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, data.size());

        table.getItems().setAll(data.subList(fromIndex, toIndex));
        return new VBox(); // required, but unused
    });

    pagination.setVisible(data.size() > ROWS_PER_PAGE);
    pagination.setManaged(data.size() > ROWS_PER_PAGE);
}
    
    private void loadData() {
    PeminjamanDAO dao = new PeminjamanDAO();
    List<LaporanPeminjamanDTO> list = dao.getLaporanPeminjaman();
    table.getItems().clear();
    allData.clear();

    for (LaporanPeminjamanDTO d : list) {
        PeminjamanData data = new PeminjamanData(
            d.getIdPeminjaman(),
            d.getNamaPeminjam(),
            d.getRole(),
            d.getBarang(),
            String.valueOf(d.getJumlah()),
            d.getTglPinjam().toString(),
            d.getTglKembali() != null ? d.getTglKembali().toString() : "-",
            d.getStatusVerifikasi(),
            d.getStatusBarang()
        );

       allData.add(data);
    }
    setupPagination(allData);
    updateTotalLabel(allData);

}
private void refreshData() {
    // Reset filters visually
    startDatePicker.setValue(null);
    endDatePicker.setValue(null);
    statusFilter.setValue("Semua Status");

    // Reload from database
    loadData();

    // Reset pagination to first page
    pagination.setCurrentPageIndex(0);
}

    
    private void initializeUI() {
        VBox sidebar = createSidebar();

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30, 40, 30, 40));
        mainContent.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("Laporan Peminjaman");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Filter Section
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        // Date Range Filter
        VBox dateRangeBox = new VBox(5);
        Label dateLabel = new Label("Filter Tanggal:");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        
        HBox dateInputs = new HBox(10);
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Dari Tanggal");
        startDatePicker.setStyle("-fx-font-size: 12px;");
        
        Label toLabel = new Label("s/d");
        toLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        
        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Sampai Tanggal");
        endDatePicker.setStyle("-fx-font-size: 12px;");
        
        dateInputs.getChildren().addAll(startDatePicker, toLabel, endDatePicker);
        dateRangeBox.getChildren().addAll(dateLabel, dateInputs);

        // Status Filter
        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("Filter Status:");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Semua Status", "Approved", "Rejected", "Pending");
        statusFilter.setValue("Semua Status");
        statusFilter.setStyle("-fx-font-size: 12px;");
        
        statusBox.getChildren().addAll(statusLabel, statusFilter);

        Button applyFilterBtn = new Button("Terapkan Filter");
        applyFilterBtn.setStyle(
            "-fx-background-color: #3C4C79; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        applyFilterBtn.setOnAction(e -> applyFilters());

        Button resetFilterBtn = new Button("Reset");
        resetFilterBtn.setStyle(
            "-fx-background-color: #3C4C79; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        resetFilterBtn.setOnAction(e -> resetFilters());

        Button refreshBtn = new Button("Refresh");
refreshBtn.setStyle(
    "-fx-background-color: #0ea5e9;" +   // blue-cyan
    "-fx-text-fill: white;" +
    "-fx-padding: 10 20;" +
    "-fx-background-radius: 8;" +
    "-fx-font-size: 12px;" +
    "-fx-font-weight: bold;" +
    "-fx-cursor: hand;"
);

refreshBtn.setOnAction(e -> refreshData());

        
        // Search Field
searchField = new TextField();
searchField.setPromptText("Cari ID / Nama / Barang...");
searchField.setStyle(
    "-fx-font-size: 12px;" +
    "-fx-padding: 8 12;" +
    "-fx-background-radius: 8;"
);
searchField.setPrefWidth(220);

// Trigger search while typing
searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

// Spacer to push search to the right
Region searchSpacer = new Region();
HBox.setHgrow(searchSpacer, Priority.ALWAYS);

filterBar.getChildren().addAll(
    dateRangeBox,
    statusBox,
    applyFilterBtn,
    resetFilterBtn,
    refreshBtn,
    searchSpacer,
    searchField
);


        // Table
        table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Apply header styling
        this.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.Node headerBg = table.lookup(".column-header-background");
                    if (headerBg != null) {
                        headerBg.setStyle("-fx-background-color: #3C4C79;");
                    }
                    table.lookupAll(".column-header").forEach(node -> {
                        node.setStyle("-fx-background-color: #3C4C79;");
                    });
                    table.lookupAll(".column-header > .label").forEach(node -> {
                        node.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    });
                    javafx.scene.Node filler = table.lookup(".filler");
                    if (filler != null) {
                        filler.setStyle("-fx-background-color: #3C4C79;");
                    }
                });
            }
        });

        TableColumn<PeminjamanData, String> noCol = new TableColumn<>("No.");
        noCol.setMinWidth(50);
        noCol.setMaxWidth(50);
        noCol.setStyle("-fx-alignment: CENTER;");
        noCol.setCellValueFactory(data -> 
          new SimpleStringProperty(
    String.valueOf(
        pagination.getCurrentPageIndex() * ROWS_PER_PAGE
        + table.getItems().indexOf(data.getValue()) + 1
    )
));

        TableColumn<PeminjamanData, String> namaCol = new TableColumn<>("Nama Peminjam");
        namaCol.setMinWidth(150);
        namaCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNamaPeminjam()));

        TableColumn<PeminjamanData, String> roleCol = new TableColumn<>("Role");
        roleCol.setMinWidth(100);
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

        TableColumn<PeminjamanData, String> barangCol = new TableColumn<>("Barang");
        barangCol.setMinWidth(150);
        barangCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBarang()));

        TableColumn<PeminjamanData, String> jumlahCol = new TableColumn<>("Jumlah");
        jumlahCol.setMinWidth(80);
        jumlahCol.setMaxWidth(80);
        jumlahCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getJumlah()));

        TableColumn<PeminjamanData, String> tglPinjamCol = new TableColumn<>("Tgl Pinjam");
        tglPinjamCol.setMinWidth(120);
        tglPinjamCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTglPinjam()));

        TableColumn<PeminjamanData, String> tglKembaliCol = new TableColumn<>("Tgl Kembali");
        tglKembaliCol.setMinWidth(120);
        tglKembaliCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTglKembali()));

        pagination = new Pagination();
        pagination.setPageFactory(pageIndex -> new VBox());

       // Status Verifikasi column
TableColumn<PeminjamanData, String> statusVerifCol = new TableColumn<>("Status Verifikasi");
statusVerifCol.setMinWidth(130);
statusVerifCol.setCellFactory(col -> new TableCell<>() {
    @Override
    protected void updateItem(String status, boolean empty) {
        super.updateItem(status, empty);
        if (empty || status == null) {
            setGraphic(null);
        } else {
            Label statusLabel = new Label(status);

            String lower = status.toLowerCase();

            if (lower.equals("dikembalikan")) {
                // GREEN
               statusLabel.setStyle(
            "-fx-background-color: rgba(34,197,94,0.15);" +
            "-fx-border-color: #22c55e;" +
            "-fx-text-fill: #166534;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-padding: 4 12;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );

            } 
            else if (lower.equals("ditolak")) {
                // RED
              statusLabel.setStyle(
            "-fx-background-color: rgba(239,68,68,0.15);" +
          "-fx-border-color: #ef4444;" +
         "-fx-text-fill: #991b1b;" +
          "-fx-background-radius: 12;" +
         "-fx-border-radius: 12;" +
         "-fx-padding: 4 12;" +
         "-fx-font-size: 11px;" +
         "-fx-font-weight: bold;"
            );

            } 
            else if (lower.equals("dipinjam") || lower.equals("pending")) {
                // ORANGE
               statusLabel.setStyle(
               "-fx-background-color: rgba(245,158,11,0.15);" +
               "-fx-border-color: #f59e0b;" +
               "-fx-text-fill: #92400e;" +
               "-fx-background-radius: 12;" +
               "-fx-border-radius: 12;" +
               "-fx-padding: 4 12;" +
               "-fx-font-size: 11px;" +
               "-fx-font-weight: bold;"
);

            } 
            else {
                // DEFAULT (fallback)
                statusLabel.setStyle(
                "-fx-background-color: rgba(156,163,175,0.15);" +
                "-fx-border-color: #9ca3af;" +
                "-fx-text-fill: #374151;" +
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;" +
                "-fx-padding: 4 12;" +
                "-fx-font-size: 11px;"
            );

            }

            HBox box = new HBox(statusLabel);
            box.setAlignment(Pos.CENTER);
            setGraphic(box);
        }
    }
});
statusVerifCol.setCellValueFactory(
    data -> new SimpleStringProperty(data.getValue().getStatusVerifikasi())
);

        // Status Barang column
        TableColumn<PeminjamanData, String> statusBarangCol = new TableColumn<>("Status Barang");
        statusBarangCol.setMinWidth(120);
        statusBarangCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusBarang()));

        table.getColumns().addAll(noCol, namaCol, roleCol, barangCol, jumlahCol, 
                                  tglPinjamCol, tglKembaliCol, statusVerifCol, statusBarangCol);
        
        // Bottom action buttons
        HBox bottomBar = new HBox(15);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(15, 0, 0, 0));

        totalLabel = new Label("Total Data: 0");
        totalLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

         Button printBtn = new Button("Print");
         printBtn.setStyle(
         "-fx-background-color: #3b82f6;" +
        "-fx-text-fill: white;" +
        "-fx-padding: 10 25;" +
        "-fx-background-radius: 20;" +
        "-fx-font-size: 13px;" +
        "-fx-font-weight: bold;" +
        "-fx-cursor: hand;"
        );
        printBtn.setOnAction(e -> printTable());

        
        Button exportBtn = new Button("Export ke CSV");
        exportBtn.setStyle(
            "-fx-background-color: #22c55e; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 25; " +
            "-fx-background-radius: 20; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        exportBtn.setOnAction(e -> exportToCSV());

        bottomBar.getChildren().addAll(totalLabel, spacer, printBtn, exportBtn);

        mainContent.getChildren().addAll(title, filterBar, table, pagination, bottomBar);

        this.setLeft(sidebar);
        this.setCenter(mainContent);
    }

private void applyFilters() {
    List<PeminjamanData> filteredData = new ArrayList<>(allData);

    // 🔎 SEARCH FILTER
    String keyword = searchField != null ? searchField.getText().toLowerCase().trim() : "";

    if (!keyword.isEmpty()) {
        filteredData = filteredData.stream()
            .filter(d ->
                d.getIdPeminjaman().toLowerCase().contains(keyword) ||
                d.getNamaPeminjam().toLowerCase().contains(keyword) ||
                d.getBarang().toLowerCase().contains(keyword)
            )
            .collect(Collectors.toList());
    }

    // 📅 DATE FILTER
    LocalDate startDate = startDatePicker.getValue();
    LocalDate endDate = endDatePicker.getValue();

    if (startDate != null && endDate != null) {
        filteredData = filteredData.stream()
            .filter(d -> {
                try {
                    LocalDate tglPinjam = LocalDate.parse(d.getTglPinjam());
                    return !tglPinjam.isBefore(startDate)
                        && !tglPinjam.isAfter(endDate);
                } catch (Exception e) {
                    return true;
                }
            })
            .collect(Collectors.toList());
    }

    // 📌 STATUS FILTER
    String status = statusFilter.getValue();
    if (!status.equals("Semua Status")) {
        filteredData = filteredData.stream()
            .filter(d -> d.getStatusVerifikasi().equalsIgnoreCase(status))
            .collect(Collectors.toList());
    }

    setupPagination(filteredData);
    updateTotalLabel(filteredData);
}

private void resetFilters() {
    startDatePicker.setValue(null);
    endDatePicker.setValue(null);
    statusFilter.setValue("Semua Status");
    if (searchField != null) searchField.clear();

    setupPagination(allData);
    updateTotalLabel(allData);
}

    private void printTable() {
    PrinterJob job = PrinterJob.createPrinterJob();
    if (job == null) return;

    boolean proceed = job.showPrintDialog(this.getScene().getWindow());
    if (!proceed) return;

    // Create a temporary table for printing
    TableView<PeminjamanData> printTable = new TableView<>();
    printTable.getColumns().addAll(table.getColumns());

    // Print ALL filtered data (not just one page)
    printTable.getItems().setAll(currentViewData);

    printTable.setPrefWidth(table.getWidth());
    printTable.setScaleX(0.8);
    printTable.setScaleY(0.8);

    boolean success = job.printPage(printTable);
    if (success) {
        job.endJob();
    }
}


    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Laporan Peminjaman");
        fileChooser.setInitialFileName("laporan_peminjaman_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
       
        
        File file = fileChooser.showSaveDialog(this.getScene().getWindow());
        
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Write header
                writer.write("No,ID Peminjaman,Nama Peminjam,Role,Barang,Jumlah,Tgl Pinjam,Tgl Kembali,Status Verifikasi,Status Barang");
                writer.newLine();
                
                // Write data from current table view (filtered data)
                int no = 1;
                for (PeminjamanData data : table.getItems()) {
                    writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        no++,
                        data.getIdPeminjaman(),
                        data.getNamaPeminjam(),
                        data.getRole(),
                        data.getBarang(),
                        data.getJumlah(),
                        data.getTglPinjam(),
                        data.getTglKembali(),
                        data.getStatusVerifikasi(),
                        data.getStatusBarang()
                    ));
                    writer.newLine();
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Berhasil");
                alert.setHeaderText(null);
                alert.setContentText("Data berhasil diekspor ke:\n" + file.getAbsolutePath() +
                                   "\n\nTotal: " + table.getItems().size() + " data");
                alert.showAndWait();
                
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Gagal");
                alert.setHeaderText(null);
                alert.setContentText("Gagal mengekspor data: " + e.getMessage());
                alert.showAndWait();
            }
        }
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
        roleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");

        VBox textBox = new VBox(2, nameLabel, roleLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        HBox userBox = new HBox(10, userImage, textBox);
        userBox.setAlignment(Pos.CENTER_LEFT);
        userBox.setPadding(new Insets(10, 10, 20, 10));

        // MENU
        VBox menuBox = new VBox(8);

        // MAIN MENU
        Button dashboardBtn = createMenuButton("🏠  Dashboard", false);
        Button verifikasiBtn = createMenuButton("✓  Verifikasi", false);
        Button userBtn = createMenuButton("👤  User", false);
        Button manageDataBtn = createMenuButton("⚙  Manage Data", false);
        Button auditTrailBtn = createMenuButton("📜  Audit Trail", false);
        Button laporanBtn = createMenuButton("📊  Laporan ▼", false);

        // SUB MENU LAPORAN
        VBox laporanSubMenu = new VBox(5);
        laporanSubMenu.setPadding(new Insets(0, 0, 0, 20));
        laporanSubMenu.setVisible(false);
        laporanSubMenu.setManaged(false);
        
        boolean isLaporanPage = true;

        if (isLaporanPage) {
            laporanSubMenu.setVisible(true);
            laporanSubMenu.setManaged(true);
            laporanBtn.setText("📊  Laporan ▲");
        }


        Button laporanPinjamBtn =
                createMenuButton("Laporan Peminjaman", true);

        Button laporanGunaBtn =
                createMenuButton("Laporan Penggunaan", false);

        // ACTION
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
            Stage s = (Stage) verifikasiBtn.getScene().getWindow();
            s.setScene(new Scene(new VerifikasiPage(user), s.getWidth(), s.getHeight()));
            s.setMaximized(true);
        });
        
        userBtn.setOnAction(e -> {
            Stage currentStage = (Stage) userBtn.getScene().getWindow();
            Scene newScene = new Scene(new AdminUserPage(user), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(newScene);
            currentStage.setMaximized(true);
        });

        manageDataBtn.setOnAction(e -> {
            Stage s = (Stage) manageDataBtn.getScene().getWindow();
            s.setScene(new Scene(new ManageDataPage(user), s.getWidth(), s.getHeight()));
            s.setMaximized(true);
        });
        
        auditTrailBtn.setOnAction(e -> {
            Stage currentStage = (Stage) auditTrailBtn.getScene().getWindow();
            Scene newScene = new Scene(new AuditTrailPage(user), currentStage.getWidth(), currentStage.getHeight());
            currentStage.setScene(newScene);
            currentStage.setMaximized(true);
        });
        

        laporanGunaBtn.setOnAction(e -> {
            Stage s = (Stage) laporanGunaBtn.getScene().getWindow();
            s.setScene(new Scene(new LaporanPenggunaanPage(user), s.getWidth(), s.getHeight()));
            s.setMaximized(true);
        });

        // TOGGLE LAPORAN ▼ ▲
        laporanBtn.setOnAction(e -> {
            boolean open = laporanSubMenu.isVisible();
            laporanSubMenu.setVisible(!open);
            laporanSubMenu.setManaged(!open);
            laporanBtn.setText(open ? "📊  Laporan ▼" : "📊  Laporan ▲");
        });

        // MASUKKAN SUBMENU
        laporanSubMenu.getChildren().addAll(
                laporanPinjamBtn,
                laporanGunaBtn
        );

        // FINAL ADD
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

    // Inner class
    public static class PeminjamanData {
        private String idPeminjaman, namaPeminjam, role, barang, jumlah;
        private String tglPinjam, tglKembali, statusVerifikasi, statusBarang;
        
        public PeminjamanData(String idPeminjaman, String namaPeminjam, String role, 
                             String barang, String jumlah, String tglPinjam, String tglKembali,
                             String statusVerifikasi, String statusBarang) {
            this.idPeminjaman = idPeminjaman;
            this.namaPeminjam = namaPeminjam;
            this.role = role;
            this.barang = barang;
            this.jumlah = jumlah;
            this.tglPinjam = tglPinjam;
            this.tglKembali = tglKembali;
            this.statusVerifikasi = statusVerifikasi;
            this.statusBarang = statusBarang;
        }
        
        public String getIdPeminjaman() { return idPeminjaman; }
        public String getNamaPeminjam() { return namaPeminjam; }
        public String getRole() { return role; }
        public String getBarang() { return barang; }
        public String getJumlah() { return jumlah; }
        public String getTglPinjam() { return tglPinjam; }
        public String getTglKembali() { return tglKembali; }
        public String getStatusVerifikasi() { return statusVerifikasi; }
        public String getStatusBarang() { return statusBarang; }
    }
}