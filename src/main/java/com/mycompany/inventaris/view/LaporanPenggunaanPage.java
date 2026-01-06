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
import com.mycompany.inventaris.dao.AuditTrailDAO;
import com.mycompany.inventaris.dao.PeminjamanDAO;
import com.mycompany.inventaris.model.LaporanPenggunaanDTO;
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
import javafx.scene.chart.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import javafx.print.PrinterJob;

public class LaporanPenggunaanPage extends BorderPane {
    private User user;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<String> kategoriFilter;
    private TextField searchField;
    private PieChart kondisiChart;
    private BarChart<String, Number> kategoriChart;
    private Label totalPenggunaanLabel;
    private Label barangTerbanyakLabel;
    private Label avgDurasiLabel;
    private TableView<LaporanPenggunaanDTO> table;
    private List<LaporanPenggunaanDTO> allData;
    private static final int ROWS_PER_PAGE = 14;
    private Pagination pagination;
    private List<LaporanPenggunaanDTO> currentData;


    
public LaporanPenggunaanPage(User user) {
    this.user = user;

    PeminjamanDAO dao = new PeminjamanDAO();
    allData = dao.getLaporanPenggunaan();

    System.out.println("LaporanPenggunaan rows = " + allData.size()); 

    initializeUI();
}


private VBox createPage(int pageIndex) {
    int fromIndex = pageIndex * ROWS_PER_PAGE;
    int toIndex = Math.min(
        fromIndex + ROWS_PER_PAGE,
        currentData.size()
    );

    table.getItems().setAll(
        currentData.subList(fromIndex, toIndex)
    );

    return new VBox(); 
}


  private Pagination createPagination() {
    int pageCount = (int) Math.ceil(
        (double) currentData.size() / ROWS_PER_PAGE
    );

    Pagination p = new Pagination(pageCount, 0);
    p.setVisible(currentData.size() > ROWS_PER_PAGE);

    p.setPageFactory(this::createPage);
    return p;
}

  
    private void initializeUI() {
        VBox sidebar = createSidebar();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc;");
        
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30, 40, 30, 40));
        mainContent.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("Laporan Penggunaan Barang");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // STATISTIK CARDS
        HBox statsBox = createStatsCards();

        // GRAFIK SECTION
        HBox chartsBox = new HBox(20);
        chartsBox.setPrefHeight(300);
        
        // Pie Chart - Kondisi Barang
        VBox pieChartBox = new VBox(10);
        pieChartBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        Label pieTitle = new Label("Kondisi Barang");
        pieTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        kondisiChart = new PieChart();
        kondisiChart.setLegendVisible(true);
        kondisiChart.setLabelsVisible(true);
        kondisiChart.setStyle("-fx-font-size: 11px;");
        updateKondisiChart(allData);
        
        pieChartBox.getChildren().addAll(pieTitle, kondisiChart);
        HBox.setHgrow(pieChartBox, Priority.ALWAYS);
        
        // Bar Chart - Penggunaan per Kategori
        VBox barChartBox = new VBox(10);
        barChartBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        Label barTitle = new Label("Penggunaan per Kategori");
        barTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Jumlah");
        kategoriChart = new BarChart<>(xAxis, yAxis);
        kategoriChart.setLegendVisible(false);
        kategoriChart.setStyle("-fx-font-size: 11px;");
        updateKategoriChart(allData);
        
        barChartBox.getChildren().addAll(barTitle, kategoriChart);
        HBox.setHgrow(barChartBox, Priority.ALWAYS);
        
        chartsBox.getChildren().addAll(pieChartBox, barChartBox);

        // FILTER & SEARCH SECTION
        VBox filterSection = new VBox(15);
        filterSection.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        
        Label filterTitle = new Label("Filter & Pencarian");
        filterTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        // Search Box
        VBox searchBox = new VBox(5);
        Label searchLabel = new Label("Cari Barang/Pengguna:");
        searchLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        
        searchField = new TextField();
        searchField.setPromptText("Ketik untuk mencari...");
        searchField.setStyle("-fx-font-size: 12px; -fx-pref-width: 200;");
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
        
        searchBox.getChildren().addAll(searchLabel, searchField);

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

        // Kategori Filter
        VBox kategoriBox = new VBox(5);
        Label kategoriLabel = new Label("Filter Kategori:");
        kategoriLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        
        kategoriFilter = new ComboBox<>();
        kategoriFilter.getItems().addAll("Semua Kategori", "Consumable", "Non Consumable");
        kategoriFilter.setValue("Semua Kategori");
        kategoriFilter.setStyle("-fx-font-size: 12px;");
        
        kategoriBox.getChildren().addAll(kategoriLabel, kategoriFilter);

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
            "-fx-background-color: #64748b; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        resetFilterBtn.setOnAction(e -> resetFilters());

        filterBar.getChildren().addAll(searchBox, dateRangeBox, kategoriBox, applyFilterBtn, resetFilterBtn);
        filterSection.getChildren().addAll(filterTitle, filterBar);

        // TABLE
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

TableColumn<LaporanPenggunaanDTO, String> noCol = new TableColumn<>("No");
noCol.setMinWidth(50);
noCol.setCellValueFactory(cell ->
    new SimpleStringProperty(
        String.valueOf(table.getItems().indexOf(cell.getValue()) + 1)
    )
);

TableColumn<LaporanPenggunaanDTO, String> barangCol =
        new TableColumn<>("Nama Barang");
        barangCol.setMinWidth(160);
barangCol.setCellValueFactory(cell ->
    new SimpleStringProperty(cell.getValue().getNamaBarang())
);

TableColumn<LaporanPenggunaanDTO, String> kategoriCol =
        new TableColumn<>("Kategori");
        kategoriCol.setMinWidth(120);
kategoriCol.setCellValueFactory(cell ->
    new SimpleStringProperty(cell.getValue().getKategori())
);

TableColumn<LaporanPenggunaanDTO, String> penggunaCol =
        new TableColumn<>("Pengguna");
        penggunaCol.setMinWidth(160);
penggunaCol.setCellValueFactory(cell ->
    new SimpleStringProperty(cell.getValue().getNamaPengguna())
);

TableColumn<LaporanPenggunaanDTO, String> roleCol =
        new TableColumn<>("Role");
        roleCol.setMinWidth(100);
roleCol.setCellValueFactory(cell ->
    new SimpleStringProperty(cell.getValue().getRole())
);

TableColumn<LaporanPenggunaanDTO, String> tglMulaiCol =
        new TableColumn<>("Tgl Mulai");
        tglMulaiCol.setMinWidth(120);
tglMulaiCol.setCellValueFactory(cell ->
    new SimpleStringProperty(
        cell.getValue().getTanggalPeminjaman().toString()
    )
);

TableColumn<LaporanPenggunaanDTO, String> tglSelesaiCol =
        new TableColumn<>("Tgl Selesai");
        tglSelesaiCol.setMinWidth(120);
tglSelesaiCol.setCellValueFactory(cell -> {
    if (cell.getValue().getTanggalKembali() == null) {
        return new SimpleStringProperty("-");
    }
    return new SimpleStringProperty(
        cell.getValue().getTanggalKembali().toString()
    );
});

TableColumn<LaporanPenggunaanDTO, String> durasiCol =
        new TableColumn<>("Durasi");
        durasiCol.setMinWidth(100);
durasiCol.setCellValueFactory(cell ->
    new SimpleStringProperty(
        cell.getValue().getDurasiHari() + " hari"
    )
);


TableColumn<LaporanPenggunaanDTO, String> kondisiCol =
        new TableColumn<>("Kondisi");
kondisiCol.setMinWidth(120);

kondisiCol.setCellFactory(col -> new TableCell<>() {
    @Override
    protected void updateItem(String kondisi, boolean empty) {
        super.updateItem(kondisi, empty);

        if (empty || kondisi == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        Label label = new Label(kondisi);
        String lower = kondisi.trim().toLowerCase();

        if (lower.equals("baik")) {
            // GREEN
            label.setStyle(
                "-fx-background-color: rgba(34,197,94,0.15);" +
                "-fx-border-color: #22c55e;" +
                "-fx-text-fill: #166534;" +
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;" +
                "-fx-padding: 4 12;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
            );

        } else if (lower.equals("digunakan")) {
            // ORANGE
            label.setStyle(
                "-fx-background-color: rgba(245,158,11,0.15);" +
                "-fx-border-color: #f59e0b;" +
                "-fx-text-fill: #92400e;" +
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;" +
                "-fx-padding: 4 12;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
            );

        } else if (lower.equals("rusak")) {
            // RED
            label.setStyle(
                "-fx-background-color: rgba(239,68,68,0.15);" +
                "-fx-border-color: #ef4444;" +
                "-fx-text-fill: #991b1b;" +
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;" +
                "-fx-padding: 4 12;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
            );

        } else {
            // FALLBACK
            label.setStyle(
                "-fx-background-color: rgba(156,163,175,0.15);" +
                "-fx-border-color: #9ca3af;" +
                "-fx-text-fill: #374151;" +
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;" +
                "-fx-padding: 4 12;" +
                "-fx-font-size: 11px;"
            );
        }

        HBox box = new HBox(label);
        box.setAlignment(Pos.CENTER);
        setGraphic(box);
        setText(null);
    }
});

kondisiCol.setCellValueFactory(
    data -> new SimpleStringProperty(data.getValue().getKondisi())
);


TableColumn<LaporanPenggunaanDTO, String> deskripsiCol =
        new TableColumn<>("Deskripsi");
deskripsiCol.setMinWidth(250);

/* Wrap text so long descriptions don't break layout */
deskripsiCol.setCellFactory(col -> new TableCell<>() {
    private final Label label = new Label();

    {
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #374151;");
        setGraphic(label);
        setPrefHeight(Control.USE_COMPUTED_SIZE);
    }

    @Override
    protected void updateItem(String text, boolean empty) {
        super.updateItem(text, empty);
        if (empty || text == null || text.isBlank()) {
            label.setText("-");
        } else {
            label.setText(text);
        }
    }
});

deskripsiCol.setCellValueFactory(
    data -> new SimpleStringProperty(data.getValue().getDeskripsi())
);


table.getColumns().addAll(
    noCol,
    barangCol,
    kategoriCol,
    penggunaCol,
    roleCol,
    tglMulaiCol,
    tglSelesaiCol,
    durasiCol,
    kondisiCol,
    deskripsiCol
);

table.setPrefHeight(Region.USE_COMPUTED_SIZE);
table.setMaxHeight(Double.MAX_VALUE);


table.setPrefHeight(500); // or whatever fits your UI
table.setMinHeight(500);
table.setMaxHeight(Region.USE_COMPUTED_SIZE);
      

currentData = new ArrayList<>(allData);
pagination = createPagination();
createPage(0);

        // Bottom action buttons
        HBox bottomBar = new HBox(15);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(15, 0, 0, 0));

        Label totalLabel = new Label("Total Data: " + allData.size());
        totalLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄 Refresh Data");
refreshBtn.setStyle(
    "-fx-background-color: #0ea5e9; " +
    "-fx-text-fill: white; " +
    "-fx-padding: 10 25; " +
    "-fx-background-radius: 20; " +
    "-fx-font-size: 13px; " +
    "-fx-font-weight: bold; " +
    "-fx-cursor: hand;"
);

refreshBtn.setOnAction(e -> refreshDataFromDatabase());

        
        Button printBtn = new Button("🖨 Cetak Laporan");
        printBtn.setStyle(
            "-fx-background-color: #3b82f6; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 25; " +
            "-fx-background-radius: 20; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        printBtn.setOnAction(e -> handlePrint());

        Button exportBtn = new Button("📥 Export ke CSV");
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

        bottomBar.getChildren().addAll(totalLabel, spacer, refreshBtn, printBtn, exportBtn);

    VBox tableSection = new VBox(10, table, pagination);

    mainContent.getChildren().addAll(
    title,
    statsBox,
    chartsBox,
    filterSection,
    tableSection,
    bottomBar
    );
        scrollPane.setContent(mainContent);

        this.setLeft(sidebar);
        this.setCenter(scrollPane);
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(20);
        statsBox.setPrefHeight(120);
        
        VBox card1 = createStatCard("📊", "Total Penggunaan",
        String.valueOf(allData.size()), "#3b82f6", "total");

        VBox card2 = createStatCard("🏆", "Barang Terbanyak",
                getBarangTerbanyak(), "#10b981", "barang");

        VBox card3 = createStatCard("⏱", "Rata-rata Durasi",
                getAverageDuration(), "#f59e0b", "durasi");

        VBox card4 = createStatCard("✓", "Kondisi Baik",
        allData.stream()
        .filter(d -> d.getKondisi() != null)
        .filter(d -> d.getKondisi().trim().equalsIgnoreCase("baik"))
        .count() + " barang",
        "#22c55e", null);


        
        statsBox.getChildren().addAll(card1, card2, card3, card4);
        return statsBox;
    }

            private VBox createStatCard(
        String icon,
        String title,
        String value,
        String color,
        String type
        ) {
            VBox card = new VBox(10);
            card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
            );

            Label iconLabel = new Label(icon);
            iconLabel.setStyle("-fx-font-size: 32px;");

            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

            Label valueLabel = new Label(value);
            valueLabel.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + color + ";"
            );

            // SIMPAN REFERENSI LABEL
            if ("total".equals(type)) {
                totalPenggunaanLabel = valueLabel;
            } else if ("barang".equals(type)) {
                barangTerbanyakLabel = valueLabel;
            } else if ("durasi".equals(type)) {
                avgDurasiLabel = valueLabel;
            }

            VBox textBox = new VBox(5, titleLabel, valueLabel);
            HBox content = new HBox(15, iconLabel, textBox);
            content.setAlignment(Pos.CENTER_LEFT);

            card.getChildren().add(content);
            return card;
        }


    private String getBarangTerbanyak() {
        Map<String, Long> barangCount = allData.stream()
            .collect(Collectors.groupingBy(LaporanPenggunaanDTO::getNamaBarang, Collectors.counting()));
        
        return barangCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(e -> e.getKey() + " (" + e.getValue() + "x)")
            .orElse("-");
    }

    private String getAverageDuration() {
    double avg = allData.stream()
        .mapToInt(LaporanPenggunaanDTO::getDurasiHari)
        .average()
        .orElse(0);

    return String.format("%.1f hari", avg);
}

private void updateKondisiChart(List<LaporanPenggunaanDTO> data) {
    Map<String, Long> kondisiCount = data.stream()
        .filter(d -> d.getKondisi() != null)
        .collect(Collectors.groupingBy(
            d -> d.getKondisi().trim().toUpperCase(),
            Collectors.counting()
        ));

    kondisiChart.getData().clear();
    kondisiCount.forEach((kondisi, count) -> {
        kondisiChart.getData().add(
            new PieChart.Data(kondisi + " (" + count + ")", count)
        );
    });
}


    private void updateKategoriChart(List<LaporanPenggunaanDTO> data) {
        Map<String, Long> kategoriCount = data.stream()
            .collect(Collectors.groupingBy(LaporanPenggunaanDTO::getKategori, Collectors.counting()));
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        kategoriCount.forEach((kategori, count) -> {
            series.getData().add(new XYChart.Data<>(kategori, count));
        });
        
        kategoriChart.getData().clear();
        kategoriChart.getData().add(series);
    }

    private void updateStatsCards(List<LaporanPenggunaanDTO> data) {
        totalPenggunaanLabel.setText(String.valueOf(data.size()));
        
        // Update barang terbanyak
        Map<String, Long> barangCount = data.stream()
            .collect(Collectors.groupingBy(LaporanPenggunaanDTO::getNamaBarang, Collectors.counting()));
        String barangTerbanyak = barangCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(e -> e.getKey() + " (" + e.getValue() + "x)")
            .orElse("-");
        barangTerbanyakLabel.setText(barangTerbanyak);
        
        // Update rata-rata durasi
       double avg = data.stream()
    .mapToInt(LaporanPenggunaanDTO::getDurasiHari)
    .average()
    .orElse(0);

    avgDurasiLabel.setText(String.format("%.1f hari", avg));

    }

    private void applyFilters() {
        List<LaporanPenggunaanDTO> filteredData = new ArrayList<>(allData);

        // Filter by search
        String searchText = searchField.getText().toLowerCase();
        if (!searchText.isEmpty()) {
            filteredData = filteredData.stream()
                .filter(data -> 
                    data.getNamaBarang().toLowerCase().contains(searchText) ||
                    data.getNamaPengguna().toLowerCase().contains(searchText) ||
                    data.getIdPenggunaan().toLowerCase().contains(searchText)
                )
                .collect(Collectors.toList());
        }

        // Filter by date range
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate != null && endDate != null) {
            filteredData = filteredData.stream()
                .filter(data -> {
                    try {
                    LocalDate tglMulai = data.getTanggalPeminjaman().toLocalDate();
                        return !tglMulai.isBefore(startDate) && !tglMulai.isAfter(endDate);
                    } catch (Exception e) {
                        return true;
                    }
                })
                .collect(Collectors.toList());
        }

     String kategoriUI = kategoriFilter.getValue();
if (!"Semua Kategori".equals(kategoriUI)) {

    String kategoriDB;
    if ("Consumable".equalsIgnoreCase(kategoriUI)) {
        kategoriDB = "consumable";
    } else if ("Non Consumable".equalsIgnoreCase(kategoriUI)) {
        kategoriDB = "non_consumable";
    } else {
        kategoriDB = kategoriUI.toLowerCase();
    }

    filteredData = filteredData.stream()
        .filter(d -> d.getKategori() != null)
        .filter(d -> d.getKategori().equalsIgnoreCase(kategoriDB))
        .collect(Collectors.toList());
}


currentData = filteredData;

pagination.setPageCount(
    (int) Math.ceil((double) currentData.size() / ROWS_PER_PAGE)
);
pagination.setCurrentPageIndex(0);
pagination.setVisible(currentData.size() > ROWS_PER_PAGE);

createPage(0);
        
        // Update charts and stats
        updateKondisiChart(filteredData);
        updateKategoriChart(filteredData);
        updateStatsCards(filteredData);
    }

    private void refreshDataFromDatabase() {
    PeminjamanDAO dao = new PeminjamanDAO();

    // Re-fetch from DB
    allData = dao.getLaporanPenggunaan();
    currentData = new ArrayList<>(allData);

    // Reset filters UI
    searchField.clear();
    startDatePicker.setValue(null);
    endDatePicker.setValue(null);
    kategoriFilter.setValue("Semua Kategori");

    // Update pagination
    pagination.setPageCount(
        (int) Math.ceil((double) currentData.size() / ROWS_PER_PAGE)
    );
    pagination.setCurrentPageIndex(0);
    pagination.setVisible(currentData.size() > ROWS_PER_PAGE);

    createPage(0);

    // Update charts & stats
    updateKondisiChart(allData);
    updateKategoriChart(allData);
    updateStatsCards(allData);

    System.out.println("🔄 Laporan Penggunaan refreshed. Rows = " + allData.size());
}
    
    private void resetFilters() {
        searchField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        kategoriFilter.setValue("Semua Kategori");
        currentData = new ArrayList<>(allData);

        pagination.setPageCount(
        (int) Math.ceil((double) currentData.size() / ROWS_PER_PAGE)
        );
        pagination.setCurrentPageIndex(0);
        pagination.setVisible(currentData.size() > ROWS_PER_PAGE);

        createPage(0);

        
        // Reset charts and stats
        updateKondisiChart(allData);
        updateKategoriChart(allData);
        updateStatsCards(allData);
    }

    private void handlePrint() {
    Stage previewStage = new Stage();
    previewStage.setTitle("Print Preview");
    previewStage.setWidth(800);
    previewStage.setHeight(600);

    // Ambil snapshot TableView
    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
    params.setTransform(javafx.scene.transform.Transform.scale(0.75, 0.75)); 
    Image snapshot = table.snapshot(params, null);

    ImageView previewImage = new ImageView(snapshot);
    previewImage.setPreserveRatio(true);
    previewImage.setFitWidth(760);

    ScrollPane scrollPane = new ScrollPane(previewImage);
    scrollPane.setFitToWidth(true);

    // Tombol print
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

    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Laporan Penggunaan");
        fileChooser.setInitialFileName("laporan_penggunaan_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(this.getScene().getWindow());
        
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Write header
                writer.write("No,ID Penggunaan,Nama Barang,Kategori,Pengguna,Role,Tgl Mulai,Tgl Selesai,Durasi,Kondisi,Deskripsi");
                writer.newLine();
                
                // Write data from current table view (filtered data)
                int no = 1;
                for (LaporanPenggunaanDTO data : table.getItems()) {
                    writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        no++,
                        data.getIdPenggunaan(),
                        data.getNamaBarang(),
                        data.getKategori(),
                        data.getNamaPengguna(),
                        data.getRole(),
                        data.getTanggalPeminjaman(),
                        data.getTanggalKembali(),
                        data.getDurasiHari() + " hari",
                        data.getKondisi(),
                        data.getDeskripsi()
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
        
        Button laporanPinjamBtn = createMenuButton("Laporan Peminjaman", false);
        Button laporanPenggunaanBtn = createMenuButton("Laporan Penggunaan", true);

        // ACTION
        dashboardBtn.setOnAction(e -> {
                Stage s = (Stage) sidebar.getScene().getWindow();
            if (user.isSuperAdmin()) {
                s.setScene(new Scene(new SuperAdminPage(user), 1280, 720));
            } else {
                s.setScene(new Scene(new AdminPage(user), 1280, 720));
            }
        });
        
        verifikasiBtn.setOnAction(e -> {
            Stage s = (Stage) verifikasiBtn.getScene().getWindow();
            s.setScene(new Scene(new VerifikasiPage(user), 1280, 720));
        });
        
        userBtn.setOnAction(e -> {
            Stage currentStage = (Stage) userBtn.getScene().getWindow();
            Scene newScene = new Scene(new AdminUserPage(user), 1280, 720);
            currentStage.setScene(newScene);
        });

        manageDataBtn.setOnAction(e -> {
            Stage s = (Stage) manageDataBtn.getScene().getWindow();
            s.setScene(new Scene(new ManageDataPage(user), 1280, 720));
        });
        
        auditTrailBtn.setOnAction(e -> {
            Stage currentStage = (Stage) auditTrailBtn.getScene().getWindow();
            Scene newScene = new Scene(new AuditTrailPage(user), 1280, 720);
            currentStage.setScene(newScene);
        });

        laporanPinjamBtn.setOnAction(e -> {
            Stage s = (Stage) laporanPinjamBtn.getScene().getWindow();
            s.setScene(new Scene(new LaporanPeminjamanPage(user), 1280, 720));
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
                laporanPenggunaanBtn
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
}