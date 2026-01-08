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
import com.mycompany.inventaris.model.AuditLog;
import com.mycompany.inventaris.dao.UserAdminDAO;
import com.mycompany.inventaris.dao.AuditTrailDAO;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class AuditTrailPage extends BorderPane {
    private String currentSearchKeyword = "";
    private ComboBox<String> userCombo;
    private TableView<AuditLog> table;
    private List<AuditLog> allData;
    private User superadmin;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Pagination pagination;
    private static final int ROWS_PER_PAGE = 10;
    private List<AuditLog> filteredData = new ArrayList<>();
    private VBox createPage(int pageIndex) {
    int fromIndex = pageIndex * ROWS_PER_PAGE;
    int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredData.size());

    table.getItems().setAll(filteredData.subList(fromIndex, toIndex));

    return new VBox(); // required by Pagination
}
    private void updatePagination() {
    int pageCount = (int) Math.ceil((double) filteredData.size() / ROWS_PER_PAGE);
    pagination.setPageCount(Math.max(pageCount, 1));
    pagination.setCurrentPageIndex(0);
    createPage(0);
}


    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private byte[] fileToBytes(File file) {
    try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
        return fis.readAllBytes();
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
};
    
    private Image centerCropSquare(Image source) {
    double size = Math.min(source.getWidth(), source.getHeight());

    double x = (source.getWidth() - size) / 2;
    double y = (source.getHeight() - size) / 2;

    javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
    params.setViewport(new javafx.geometry.Rectangle2D(x, y, size, size));

    ImageView iv = new ImageView(source);
    iv.setFitWidth(300);
    iv.setFitHeight(300);
    iv.setPreserveRatio(true);

    return iv.snapshot(params, null);
}

    
    
private void openProfilePhotoDialog(ImageView sidebarImage) {

    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.initStyle(StageStyle.UNDECORATED);

    VBox root = new VBox(15);
    root.setPadding(new Insets(20));
    root.setAlignment(Pos.CENTER);
    root.setStyle(
        "-fx-background-color: white;" +
        "-fx-border-radius: 12;" +
        "-fx-background-radius: 12;" +
        "-fx-border-color: #e5e7eb;"
    );

    // Preview image
    ImageView preview = new ImageView();
    preview.setFitWidth(120);
    preview.setFitHeight(120);
    preview.setPreserveRatio(true);

    Circle clip = new Circle(60, 60, 60);
    preview.setClip(clip);

    // Load existing photo
    if (superadmin.getPhoto() != null && superadmin.getPhoto().length > 0) {
        preview.setImage(
            new Image(new java.io.ByteArrayInputStream(superadmin.getPhoto()), 300, 300, true, true)
        );
    } else {
        preview.setImage(new Image(getClass().getResourceAsStream("/assets/user.png")));
    }

    final File[] selectedFile = new File[1];

    Button chooseBtn = new Button("Choose Image");
    Label sizeHint = new Label("Max file size: 5MB");
    sizeHint.setStyle(
    "-fx-font-size: 11px; " +
    "-fx-text-fill: #64748b;"
    );

    chooseBtn.setOnAction(e -> {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.jfif")
        );

        File file = fc.showOpenDialog(dialog);
        if (file == null) return;

        if (file.length() > MAX_IMAGE_SIZE) {
            new Alert(Alert.AlertType.ERROR, "Image size must be under 5MB").show();
            return;
        }

        Image img = new Image(file.toURI().toString(), 400, 400, true, true);
        Image cropped = centerCropSquare(img);

        preview.setImage(cropped);
        selectedFile[0] = file;
    });

    Button saveBtn = new Button("Save");
    Button cancelBtn = new Button("Cancel");

    HBox buttons = new HBox(10, saveBtn, cancelBtn);
    buttons.setAlignment(Pos.CENTER);

    saveBtn.setOnAction(e -> {

        if (selectedFile[0] == null) {
            dialog.close();
            return;
        }

        try {
            byte[] bytes = fileToBytes(selectedFile[0]);

            UserAdminDAO dao = new UserAdminDAO();
            boolean success = dao.updateUserPhoto(superadmin.getIdUser(), bytes);

            if (success) {
                superadmin.setPhoto(bytes);

                // reload page
                Stage stage = (Stage) sidebarImage.getScene().getWindow();
                stage.setScene(new Scene(new AdminUserPage(superadmin), 1280, 720));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        dialog.close();
    });

    cancelBtn.setOnAction(e -> dialog.close());

    root.getChildren().addAll(preview, chooseBtn, buttons);

    Scene scene = new Scene(root);
    dialog.setScene(scene);
    dialog.showAndWait();
}

    
    public AuditTrailPage(User superadmin) {
        this.superadmin = superadmin;
        allData = AuditTrailDAO.getAll();
        initializeUI();
    }

    private void initializeUI() {
        VBox sidebar = createSidebar();

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30, 40, 30, 40));
        mainContent.setStyle("-fx-background-color: #f8fafc;");

        // Header
        Label title = new Label("Jejak Audit");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Filter Section
        VBox filterSection = new VBox(15);
        filterSection.setPadding(new Insets(20));
        filterSection.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );

        Label filterLabel = new Label("Filter");
        filterLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Filter Row 1: Date Range & User
        HBox filterRow1 = new HBox(15);
        filterRow1.setAlignment(Pos.CENTER_LEFT);

        VBox startDateBox = new VBox(5);
        Label startLabel = new Label("Tanggal Mulai");
        startLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Pilih tanggal mulai");
        startDatePicker.setPrefWidth(180);
        startDateBox.getChildren().addAll(startLabel, startDatePicker);

        VBox endDateBox = new VBox(5);
        Label endLabel = new Label("Tanggal Akhir");
        endLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Pilih tanggal akhir");
        endDatePicker.setPrefWidth(180);
        endDateBox.getChildren().addAll(endLabel, endDatePicker);

        VBox userFilterBox = new VBox(5);
        Label userLabel = new Label("Pengguna");
        userLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
       userCombo = new ComboBox<>();
userCombo.setPrefWidth(180);

userCombo.getItems().add("Semua Pengguna");

UserAdminDAO userDao = new UserAdminDAO();
List<User> users = userDao.getAll();

for (User u : users) {
    userCombo.getItems().add(
        u.getUsername() + " - " + u.getRole()
    );
}

userCombo.setValue("Semua Pengguna");


        userCombo.setPrefWidth(180);
        userFilterBox.getChildren().addAll(userLabel, userCombo);

        VBox actionFilterBox = new VBox(5);
        Label actionLabel = new Label("Jenis Aksi");
        actionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        ComboBox<String> actionCombo = new ComboBox<>();
        actionCombo.getItems().addAll("Semua Aksi", "LOGIN", "LOGOUT", "EDIT_USER", "TAMBAH_USER", "EDIT_BARANG", "HAPUS_BARANG", "TAMBAH_BARANG", "PEMINJAMAN", "PENGEMBALIAN", "PENGGUNAAN", "VERIFIKASI", "EKSPOR_DATA");
        actionCombo.setValue("Semua Aksi");
        actionCombo.setPrefWidth(180);
        actionFilterBox.getChildren().addAll(actionLabel, actionCombo);

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
        applyFilterBtn.setOnAction(e -> applyFilters(userCombo.getValue(), actionCombo.getValue()));

        Button resetBtn = new Button("Reset");
        resetBtn.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #3C4C79; " +
            "-fx-border-width: 2; " +
            "-fx-text-fill: #3C4C79; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 8; " +
            "-fx-border-radius: 8; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        resetBtn.setOnAction(e -> {
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            userCombo.setValue("Semua Pengguna");
            actionCombo.setValue("Semua Aksi");
        });

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        filterRow1.getChildren().addAll(startDateBox, endDateBox, userFilterBox, actionFilterBox, spacer1, applyFilterBtn, resetBtn);

        filterSection.getChildren().addAll(filterLabel, filterRow1);

        // Search & Export
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Cari log...");
        searchField.setPrefWidth(350);
        searchField.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 8 15;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

    Button refreshBtn = new Button("🔄 Refresh Data");
    refreshBtn.setStyle(
    "-fx-background-color: #0ea5e9; " +
    "-fx-text-fill: white; " +
    "-fx-padding: 10 20; " +
    "-fx-background-radius: 8; " +
    "-fx-font-size: 13px; " +
    "-fx-font-weight: bold; " +
    "-fx-cursor: hand;"
    );

    refreshBtn.setOnAction(e -> reloadAuditData());

        
        Button exportBtn = new Button("📥 Ekspor ke CSV");
        exportBtn.setStyle(
            "-fx-background-color: #22c55e; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 8; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        exportBtn.setOnAction(e -> showExportDialog());

        topBar.getChildren().addAll(searchField, spacer, refreshBtn, exportBtn);

        // Table
        table = new TableView<>();
        pagination = new Pagination();
        pagination.setPageFactory(this::createPage);
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setTableMenuButtonVisible(false);

        TableColumn<AuditLog, String> timestampCol = new TableColumn<>("Waktu");
        timestampCol.setMinWidth(150);
        timestampCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTimestamp()));

        TableColumn<AuditLog, String> userCol = new TableColumn<>("Pengguna");
        userCol.setMinWidth(150);
        userCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserName()));

        TableColumn<AuditLog, String> actionCol = new TableColumn<>("Aksi");
        actionCol.setMinWidth(140);
        actionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String action, boolean empty) {
                super.updateItem(action, empty);
                if (empty || action == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(action);
                    String color = getActionColor(action);
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
        actionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAction()));

        TableColumn<AuditLog, String> descCol = new TableColumn<>("Deskripsi");
        descCol.setMinWidth(250);
        descCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        TableColumn<AuditLog, String> ipCol = new TableColumn<>("Alamat IP");
        ipCol.setMinWidth(130);
        ipCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIpAddress()));

        TableColumn<AuditLog, String> statusCol = new TableColumn<>("Status");
        statusCol.setMinWidth(100);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    if (status.equals("BERHASIL")) {
                        badge.setStyle(
                            "-fx-background-color: #22c55e; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 4 10; " +
                            "-fx-background-radius: 12; " +
                            "-fx-font-size: 10px; " +
                            "-fx-font-weight: bold;"
                        );
                    } else {
                        badge.setStyle(
                            "-fx-background-color: #dc2626; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 4 10; " +
                            "-fx-background-radius: 12; " +
                            "-fx-font-size: 10px; " +
                            "-fx-font-weight: bold;"
                        );
                    }
                    HBox box = new HBox(badge);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        table.getColumns().addAll(timestampCol, userCol, actionCol, descCol, ipCol, statusCol);
        refreshTable(allData);

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
                        node.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
                    });
                    javafx.scene.Node filler = table.lookup(".filler");
                    if (filler != null) {
                        filler.setStyle("-fx-background-color: #3C4C79;");
                    }
                });
            }
        });

searchField.textProperty().addListener((obs, old, newVal) -> {
    currentSearchKeyword = newVal == null ? "" : newVal.trim().toLowerCase();

    if (pagination != null) {
        applyFilters(userCombo.getValue(), actionCombo.getValue());
    }
});



        // Stats Card
        HBox statsCard = createStatsCard();
        
mainContent.getChildren().addAll(
        title,
        filterSection,
        statsCard,
        topBar,
        table,
        pagination
);

        this.setLeft(sidebar);
        this.setCenter(mainContent);
    }

    private HBox createStatsCard() {
        HBox statsCard = new HBox(20);
        statsCard.setPadding(new Insets(20));
        statsCard.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );

        long totalLogs = allData.size();
        long successLogs = allData.stream().filter(log -> log.getStatus().equals("BERHASIL")).count();
        long failedLogs = allData.stream().filter(log -> log.getStatus().equals("GAGAL")).count();
        long todayLogs = allData.stream()
            .filter(log -> log.getTimestamp().startsWith(LocalDate.now().toString()))
            .count();

        VBox totalBox = createStatBox("Total Log", String.valueOf(totalLogs), "#3C4C79");
        VBox successBox = createStatBox("Berhasil", String.valueOf(successLogs), "#22c55e");
        VBox failedBox = createStatBox("Gagal", String.valueOf(failedLogs), "#dc2626");
        VBox todayBox = createStatBox("Hari Ini", String.valueOf(todayLogs), "#f59e0b");

        statsCard.getChildren().addAll(totalBox, successBox, failedBox, todayBox);
        return statsCard;
    }

    private VBox createStatBox(String label, String value, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(10, 20, 10, 20));
        box.setStyle("-fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8;");
        HBox.setHgrow(box, Priority.ALWAYS);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        box.getChildren().addAll(valueLabel, labelText);
        return box;
    }

    private String getActionColor(String action) {
        switch (action) {
            case "LOGIN": return "#3b82f6";
            case "LOGOUT": return "#6b7280";
            case "TAMBAH_USER": case "TAMBAH_BARANG": return "#22c55e";
            case "EDIT_BARANG": case "EDIT_USER": return "#f59e0b";
            case "HAPUS_BARANG": case "HAPUS_USER": return "#dc2626";
            case "PEMINJAMAN": return "#8b5cf6";
            case "PENGEMBALIAN": return "#14b8a6";
            case "PENGGUNAAN": return "#f97316";
            case "VERIFIKASI": return "#06b6d4";
            case "EKSPOR_DATA": return "#a855f7";
            default: return "#64748b";
        }
    }

  private void applyFilters(String user, String action) {

    List<AuditLog> filtered = new ArrayList<>(allData);

    // 1. Date range
    LocalDate startDate = startDatePicker.getValue();
    LocalDate endDate = endDatePicker.getValue();
    if (startDate != null && endDate != null) {
        filtered = filtered.stream()
            .filter(log -> {
                LocalDate logDate = LocalDate.parse(log.getTimestamp().substring(0, 10));
                return !logDate.isBefore(startDate) && !logDate.isAfter(endDate);
            })
            .collect(Collectors.toList());
    }

if (user != null && !user.equals("Semua Pengguna")) {
        String selectedUsername = user.split(" - ")[0];
        filtered = filtered.stream()
            .filter(log -> log.getUserName().equals(selectedUsername))
            .collect(Collectors.toList());
    }

    // 3. Action dropdown
    if (action != null && !action.equals("Semua Aksi")) {
        filtered = filtered.stream()
            .filter(log -> log.getAction().equals(action))
            .collect(Collectors.toList());
    }

    // 4. SEARCH BAR (combined!)
    if (!currentSearchKeyword.isEmpty()) {
        filtered = filtered.stream()
            .filter(log ->
                log.getUserName().toLowerCase().contains(currentSearchKeyword) ||
                log.getAction().toLowerCase().contains(currentSearchKeyword) ||
                log.getDescription().toLowerCase().contains(currentSearchKeyword) ||
                log.getIpAddress().toLowerCase().contains(currentSearchKeyword) ||
                log.getStatus().toLowerCase().contains(currentSearchKeyword)
            )
            .collect(Collectors.toList());
    }

    refreshTable(filtered);
}


private void refreshTable(List<AuditLog> data) {
   filteredData.clear();
   filteredData.addAll(data);   
   updatePagination();

}

private void reloadAuditData() {
    // Re-fetch from database
    allData = AuditTrailDAO.getAll();

    // Clear filters
    startDatePicker.setValue(null);
    endDatePicker.setValue(null);
    userCombo.setValue("Semua Pengguna");
    currentSearchKeyword = "";

    // Refresh table + pagination
    refreshTable(allData);
}


    private void showExportDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Ekspor ke CSV");

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Ekspor Log Audit");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox rangeBox = new VBox(10);
        Label rangeLabel = new Label("Pilih Rentang Tanggal:");
        rangeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        HBox dateBox = new HBox(10);
        DatePicker exportStartDate = new DatePicker();
        exportStartDate.setPromptText("Tanggal Mulai");
        DatePicker exportEndDate = new DatePicker();
        exportEndDate.setPromptText("Tanggal Akhir");
        dateBox.getChildren().addAll(exportStartDate, exportEndDate);

        CheckBox allDataCheck = new CheckBox("Ekspor Semua Data (Abaikan Rentang Tanggal)");
        allDataCheck.setStyle("-fx-font-size: 12px;");

        rangeBox.getChildren().addAll(rangeLabel, dateBox, allDataCheck);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Batal");
        cancelBtn.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #3C4C79; " +
            "-fx-border-width: 2; " +
            "-fx-text-fill: #3C4C79; " +
            "-fx-padding: 8 20; " +
            "-fx-background-radius: 8; " +
            "-fx-border-radius: 8; " +
            "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialog.close());

        Button exportButton = new Button("Ekspor");
        exportButton.setStyle(
            "-fx-background-color: #22c55e; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 8 20; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-font-weight: bold;"
        );
        exportButton.setOnAction(e -> {
            exportToCSV(exportStartDate.getValue(), exportEndDate.getValue(), allDataCheck.isSelected());
            dialog.close();
        });

        buttonBox.getChildren().addAll(cancelBtn, exportButton);

        content.getChildren().addAll(title, rangeBox, buttonBox);

        Scene scene = new Scene(content, 450, 250);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void exportToCSV(LocalDate startDate, LocalDate endDate, boolean exportAll) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.setInitialFileName("audit_trail_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(this.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Header
                writer.write("Waktu,Pengguna,Aksi,Deskripsi,Alamat IP,Status\n");

                // Filter data
                List<AuditLog> dataToExport = allData;
                if (!exportAll && startDate != null && endDate != null) {
                    dataToExport = allData.stream()
                        .filter(log -> {
                            LocalDate logDate = LocalDate.parse(log.getTimestamp().substring(0, 10));
                            return !logDate.isBefore(startDate) && !logDate.isAfter(endDate);
                        })
                        .collect(Collectors.toList());
                }

                // Data rows
                for (AuditLog log : dataToExport) {
                    writer.write(String.format("%s,%s,%s,\"%s\",%s,%s\n",
                        log.getTimestamp(),
                        log.getUserName(),
                        log.getAction(),
                        log.getDescription().replace("\"", "\"\""),
                        log.getIpAddress(),
                        log.getStatus()
                    ));
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ekspor Berhasil");
                alert.setHeaderText(null);
                alert.setContentText("Log audit berhasil diekspor ke:\n" + file.getAbsolutePath());
                alert.showAndWait();

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ekspor Gagal");
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

    if (superadmin.getPhoto() != null && superadmin.getPhoto().length > 0) {
        userPhoto = new Image(
        new java.io.ByteArrayInputStream(superadmin.getPhoto())
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
        userImage.setCursor(javafx.scene.Cursor.HAND);
        userImage.setOnMouseClicked(e -> openProfilePhotoDialog(userImage));

        

        Label nameLabel = new Label(superadmin.getNama());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label roleLabel = new Label(superadmin.getRole().toUpperCase());
        roleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");

        VBox textBox = new VBox(2, nameLabel, roleLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        HBox userBox = new HBox(10, userImage, textBox);
        userBox.setAlignment(Pos.CENTER_LEFT);
        userBox.setPadding(new Insets(10, 10, 20, 10));

        VBox menuBox = new VBox(8);
        Button dashboardBtn = createMenuButton("🏠  Dashboard", false);
        Button userBtn = createMenuButton("👤  User", false);
        Button manageDataBtn = createMenuButton("⚙  Manage Data", false);
        Button auditTrailBtn = createMenuButton("📜  Audit Trail", true);
        Button laporanBtn = createMenuButton("📊  Laporan ▼", false);
        
        VBox laporanSubMenu = new VBox(5);
        laporanSubMenu.setPadding(new Insets(0, 0, 0, 20));
        laporanSubMenu.setVisible(false);
        laporanSubMenu.setManaged(false);

        Button laporanPinjamBtn =
                createMenuButton("Laporan Peminjaman", false);

        Button laporanGunaBtn =
                createMenuButton("Laporan Penggunaan", false);

        dashboardBtn.setOnAction(e -> {
            Stage currentStage = (Stage) dashboardBtn.getScene().getWindow();
            Scene newScene = new Scene(new SuperAdminPage(superadmin), 1280, 720);
            currentStage.setScene(newScene);
        });

        userBtn.setOnAction(e -> {
            Stage currentStage = (Stage) userBtn.getScene().getWindow();
            Scene newScene = new Scene(new AdminUserPage(superadmin), 1280, 720);
            currentStage.setScene(newScene);
        });
        
        manageDataBtn.setOnAction(e -> {
            Stage currentStage = (Stage) manageDataBtn.getScene().getWindow();
            Scene newScene = new Scene(new ManageDataPage(superadmin), 1280, 720);
            currentStage.setScene(newScene);
        });
        
        laporanPinjamBtn.setOnAction(e -> {
            Stage s = (Stage) laporanBtn.getScene().getWindow();
            s.setScene(new Scene(new LaporanPeminjamanPage(superadmin), 1280, 720));
        });

        laporanGunaBtn.setOnAction(e -> {
            Stage s = (Stage) laporanGunaBtn.getScene().getWindow();
            s.setScene(new Scene(new LaporanPenggunaanPage(superadmin), 1280, 720));
        });
        
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

        menuBox.getChildren().addAll(dashboardBtn, userBtn, manageDataBtn, auditTrailBtn, laporanBtn, laporanSubMenu);

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
        superadmin.getIdUser(),          // SAME as login success
        superadmin.getUsername(),        // SAME as login success
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
    }}