/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.view;

/**
 *
 * @author Amy
 */

import com.mycompany.inventaris.dao.UserAdminDAO;
import com.mycompany.inventaris.dao.AuditTrailDAO;
import com.mycompany.inventaris.model.User;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class AdminUserPage extends BorderPane {
    
    private TableView<User> table;
    private List<User> allData;
    private User superadmin;
    private static final int ROWS_PER_PAGE = 8;
    private int currentPage = 0;
    private Pagination pagination;

    private byte[] fileToBytes(File file) {
    try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
        return fis.readAllBytes();
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
};
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
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

    root.getChildren().addAll(preview, chooseBtn, sizeHint, buttons);

    Scene scene = new Scene(root);
    dialog.setScene(scene);
    dialog.showAndWait();
}

    
    public AdminUserPage(User superadmin) {
        this.superadmin = superadmin;
        UserAdminDAO userAdminDAO = new UserAdminDAO();
        allData = userAdminDAO.getAll();
        initializeUI();
    }

    private Pagination createPagination() {

    int pageCount = (int) Math.ceil((double) allData.size() / ROWS_PER_PAGE);

    Pagination pagination = new Pagination(pageCount, 0);
    pagination.setStyle("-fx-page-information-visible: false;");

    pagination.setPageFactory(pageIndex -> {
        currentPage = pageIndex;
        updateTablePage();
        return new VBox(); // dummy node
    });

    return pagination;
}

private void updateTablePage() {
    table.getItems().clear();

    int fromIndex = currentPage * ROWS_PER_PAGE;
    int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allData.size());

    if (fromIndex <= toIndex) {
        table.getItems().addAll(allData.subList(fromIndex, toIndex));
    }
}


    
    private void initializeUI() {
        VBox sidebar = createSidebar();

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30, 40, 30, 40));
        mainContent.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("User");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Search here...");
        searchField.setPrefWidth(350);
        searchField.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-radius: 25; " +
            "-fx-background-radius: 25; " +
            "-fx-padding: 8 20;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ComboBox<String> sortBox = new ComboBox<>();
        sortBox.getItems().addAll("Newest", "Oldest", "A-Z", "Z-A");
        sortBox.setValue("Newest");
        sortBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #3C4C79; " +
            "-fx-border-radius: 20; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 6 15;"
        );

        Button newUserBtn = new Button("+ New User");
        newUserBtn.setStyle(
            "-fx-background-color: #3C4C79; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 25; " +
            "-fx-background-radius: 20; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        newUserBtn.setOnAction(e -> showAddUserForm());

        topBar.getChildren().addAll(searchField, spacer, sortBox, newUserBtn);

        // Table
        table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setTableMenuButtonVisible(false);

  TableColumn<User, byte[]> photoCol = new TableColumn<>("Photo");
        photoCol.setMinWidth(60);
        photoCol.setMaxWidth(60);
        photoCol.setPrefWidth(60);

        photoCol.setCellValueFactory(
        data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPhoto())
        );
        photoCol.setCellFactory(col -> new TableCell<>() {

    private final ImageView imageView = new ImageView();

    @Override
    protected void updateItem(byte[] photo, boolean empty) {
        super.updateItem(photo, empty);

        if (empty) {
            setGraphic(null);
            return;
        }

        User user = getTableRow().getItem();
        if (user == null) {
            setGraphic(null);
            return;
        }

        StackPane avatar;

        // ✅ PHOTO EXISTS
        if (photo != null && photo.length > 0) {

            Image img = new Image(
                new java.io.ByteArrayInputStream(photo),
                40, 40, true, true // resize on decode (IMPORTANT)
            );

            imageView.setImage(img);
            imageView.setFitWidth(40);
            imageView.setFitHeight(40);
            imageView.setPreserveRatio(true);

            Circle clip = new Circle(20, 20, 20);
            imageView.setClip(clip);

            avatar = new StackPane(imageView);

        }
        // ❌ NO PHOTO → INITIAL
        else {
            Circle circle = new Circle(20);
            circle.setFill(Color.web("#94a3b8"));

            String name = user.getNama();
            Label initial = new Label(
                (name != null && !name.isEmpty())
                    ? name.substring(0, 1).toUpperCase()
                    : "?"
            );
            initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            avatar = new StackPane(circle, initial);
        }

        setGraphic(avatar);
        setText(null); // ⬅ VERY IMPORTANT (prevents b@ output)
    }
});

        
        // Avatar + Name column
       TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setMinWidth(250);

        nameCol.setCellValueFactory(
        data -> new SimpleStringProperty(data.getValue().getNama())
        );

        nameCol.setCellFactory(col -> new TableCell<>() {
        @Override
        protected void updateItem(String name, boolean empty) {
        super.updateItem(name, empty);

        if (empty || name == null) {
            setText(null);
        } else {
            setText(name);
            setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b;");
            }
        }
    });

        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNama()));

        
        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setMinWidth(120);
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIdentity()));
        
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setMinWidth(150);
        usernameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));

        TableColumn<User, String> dateCol = new TableColumn<>("Date");
        dateCol.setMinWidth(150);
        dateCol.setCellValueFactory(data -> {
            Timestamp ts = data.getValue().getCreatedAt();
            return new SimpleStringProperty(
                ts != null ? ts.toLocalDateTime().toLocalDate().toString() : "-"
            );
        });
        TableColumn<User, String> positionCol = new TableColumn<>("Position");
        positionCol.setMinWidth(130);
        positionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

        // Phone column
TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
phoneCol.setMinWidth(140);
phoneCol.setCellValueFactory(
    data -> new SimpleStringProperty(data.getValue().getPhone())
);


        // Status column
        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setMinWidth(120);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(status);
                    if (status.equals("Aktif") || status.equals("Active")) {
                        statusLabel.setStyle(
                            "-fx-background-color: #3C4C79; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 5 15; " +
                            "-fx-background-radius: 12; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: bold;"
                        );
                    } else {
                        statusLabel.setStyle(
                            "-fx-background-color: #dc2626; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 5 15; " +
                            "-fx-background-radius: 12; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: bold;"
                        );
                    }
                    HBox box = new HBox(statusLabel);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        TableColumn<User, Void> actionCol = new TableColumn<>("Aksi");
        actionCol.setMinWidth(90);
        actionCol.setPrefWidth(90);
        actionCol.setMaxWidth(90);
        actionCol.setResizable(false);

actionCol.setCellFactory(col -> new TableCell<>() {

    private final Button actionBtn = new Button("⋮");

    {
        actionBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #64748b; " +
            "-fx-font-size: 20px; " +
            "-fx-padding: 4 10; " +   
            "-fx-cursor: hand;"
        );

        actionBtn.setOnAction(e -> {
            User user = getTableView().getItems().get(getIndex());
            showActionMenu(actionBtn, user);
        });
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            setGraphic(actionBtn);
            setAlignment(Pos.CENTER);    
        }
    }
});


        table.getColumns().addAll(photoCol, nameCol, usernameCol, idCol, dateCol, positionCol, phoneCol, statusCol, actionCol);
        pagination = createPagination();
        updateTablePage();

        // Apply header styling
        this.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.Node headerBg = table.lookup(".column-header-background");
                    if (headerBg != null) {
                        headerBg.setStyle("-fx-background-color: white;");
                    }
                    table.lookupAll(".column-header").forEach(node -> {
                        node.setStyle("-fx-background-color: white;");
                    });
                    table.lookupAll(".column-header > .label").forEach(node -> {
                        node.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 12px;");
                    });
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
                        data.getNama().toLowerCase().contains(keyword) ||
                        data.getIdentity().toLowerCase().contains(keyword) ||
                        data.getRole().toLowerCase().contains(keyword))
                    .forEach(data -> table.getItems().add(data));
            }
        });
        
        // Sort functionality
        sortBox.setOnAction(e -> {
            String selected = sortBox.getValue();
            List<User> sortedList = new ArrayList<>(table.getItems());
            
            switch(selected) {
                case "Newest":
                    sortedList = new ArrayList<>(allData);
                    break;
                case "Oldest":
                    java.util.Collections.reverse(sortedList);
                    break;
                case "A-Z":
                    sortedList.sort((a, b) -> a.getNama().compareToIgnoreCase(b.getNama()));
                    break;
                case "Z-A":
                    sortedList.sort((a, b) -> b.getNama().compareToIgnoreCase(a.getNama()));
                    break;
            }
            
            table.getItems().clear();
            sortedList.forEach(data -> table.getItems().add(data));
        });

        mainContent.getChildren().addAll(title, topBar, table, pagination);

        this.setLeft(sidebar);
        this.setCenter(mainContent);
    }



    private void showActionMenu(Button sourceBtn, User user) {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        MenuItem viewItem = new MenuItem("View Details");
        
        editItem.setOnAction(e -> showEditForm(user));
        deleteItem.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete User");
            confirm.setHeaderText("Are you sure ??");
            confirm.setContentText("User \"" + user.getNama() + "\" will be deleted.");
            
            confirm.showAndWait().ifPresent(result -> {
               if (result == ButtonType.OK) {

    UserAdminDAO dao = new UserAdminDAO();
    boolean success = dao.delete(user.getIdUser());

    if (success) {

        // 🔹 Audit log
        String ip = "UNKNOWN";
        try {
            ip = java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        AuditTrailDAO.log(
                superadmin.getIdUser(),
                superadmin.getUsername(),
                "HAPUS_USER",
                "Username " + user.getUsername()
                        + " bernama " + user.getNama() + " dihapus",
                ip,
                "BERHASIL"
        );

        allData.remove(user);
        table.getItems().remove(user);

    } else {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setHeaderText("Delete Failed");
        error.setContentText("Failed to delete user");
        error.show();
    }
}

            });
        });
        viewItem.setOnAction(e -> showUserDetail(user));
        
        contextMenu.getItems().addAll(viewItem, editItem, deleteItem);
        contextMenu.show(sourceBtn, javafx.geometry.Side.BOTTOM, 0, 0);
    }
    
    private void showUserDetail(User user) {
    Stage detailPopup = new Stage();
    detailPopup.initModality(Modality.APPLICATION_MODAL);
    detailPopup.setTitle("User Details");

    VBox box = new VBox(15);
    box.setPadding(new Insets(25));

    box.getChildren().addAll(
        new Label("Name : " + user.getNama()),
        new Label("Email : " + user.getEmail()),
        new Label("Phone : " + user.getPhone()),
        new Label("Role : " + user.getRole()),
        new Label("Status : " + user.getStatus()),
        new Label("ID Number : " + user.getIdentity())
    );

    Button close = new Button("Close");
    close.setOnAction(e -> detailPopup.close());
    box.getChildren().add(close);

    Scene scene = new Scene(box, 350, 300);
    detailPopup.setScene(scene);
    detailPopup.show();
}

    private void showEditForm(User user) {
    Stage editPopup = new Stage();
    editPopup.initModality(Modality.APPLICATION_MODAL);
    editPopup.setTitle("Edit User");

    VBox box = new VBox(15);
    box.setPadding(new Insets(25));

    TextField nameField = new TextField(user.getNama());
    TextField emailField = new TextField(user.getEmail());
    TextField phoneField = new TextField(user.getPhone());
    TextField passwordField = new TextField(user.getPassword());
    passwordField.setPromptText("Password");


    ComboBox<String> roleBox = new ComboBox<>();
    roleBox.getItems().addAll("Mahasiswa", "Karyawan", "Dosen", "Admin", "Superadmin");
    roleBox.setValue(user.getRole());

    ComboBox<String> statusBox = new ComboBox<>();
    statusBox.getItems().addAll("Aktif", "Nonaktif");
    statusBox.setValue(user.getStatus());

    Button save = new Button("Save");
    save.setOnAction(e -> {

    // 1️⃣ Capture OLD values
    String oldName = user.getNama();
    String oldEmail = user.getEmail();
    String oldPhone = user.getPhone();
    String oldPassword = user.getPassword();
    String oldRole = user.getRole();
    String oldStatus = user.getStatus();

    // 2️⃣ Apply NEW values
    user.setNama(nameField.getText());
    user.setEmail(emailField.getText());
    user.setPhone(phoneField.getText());
    user.setPassword(passwordField.getText());
    user.setRole(roleBox.getValue());
    user.setStatus(statusBox.getValue());

    boolean success = new UserAdminDAO().update(user);

    if (success) {

        // 3️⃣ Detect what changed
        List<String> changes = new ArrayList<>();

        if (!oldName.equals(user.getNama())) changes.add("Nama");
        if (!oldEmail.equals(user.getEmail())) changes.add("Email");
        if (!oldPhone.equals(user.getPhone())) changes.add("Phone");
        if (!oldPassword.equals(passwordField.getText())) changes.add("Password");
        if (!oldRole.equals(user.getRole())) changes.add("Role");
        if (!oldStatus.equals(user.getStatus())) changes.add("Status");

        String deskripsi = changes.isEmpty()
                ? "Data user diperbarui tanpa perubahan"
                : "Data user diubah: " + String.join(", ", changes);

        // 4️⃣ Log audit
        String ip = "UNKNOWN";
        try {
            ip = java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        AuditTrailDAO.log(
                superadmin.getIdUser(),
                superadmin.getUsername(),
                "EDIT_USER",
                deskripsi,
                ip,
                "BERHASIL"
        );

        table.refresh();
        editPopup.close();
    }
});


    Button cancel = new Button("Cancel");
    cancel.setOnAction(e -> editPopup.close());

    box.getChildren().addAll(
        new Label("Edit User"),
        nameField, emailField, phoneField, passwordField,
        roleBox, statusBox,
        new HBox(10, save, cancel)
    );

    Scene scene = new Scene(box, 350, 350);
    editPopup.setScene(scene);
    editPopup.show();
}

    
    private void showAddUserForm() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.DECORATED);
        popup.setTitle("Add New User");

        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: white;");

        Label title = new Label("Add New User");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // User Details Section
        Label sectionTitle = new Label("User Details");
        sectionTitle.setStyle(
            "-fx-background-color: #B71C1C; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 12 20; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8 8 0 0;"
        );

        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setStyle(
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-width: 0 1 1 1; " +
            "-fx-border-radius: 0 0 8 8;"
        );

        // Form fields
        HBox photoAndNameRow = new HBox(20);
        photoAndNameRow.setAlignment(Pos.TOP_LEFT);
        
        // Photo upload
        VBox photoBox = new VBox(8);
        Label photoLabel = new Label("Photo *");
        photoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        
        VBox uploadArea = new VBox(10);
        uploadArea.setAlignment(Pos.CENTER);
        uploadArea.setPadding(new Insets(30));
        uploadArea.setPrefSize(150, 150);
        uploadArea.setStyle(
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-width: 2; " +
            "-fx-border-style: dashed; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand;"
        );
        
        Label uploadText = new Label("Drag and drop or\nclick here to select file");
        uploadText.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px; -fx-text-alignment: center;");
        uploadText.setWrapText(true);
        
        final File[] selectedPhoto = new File[1];
        ImageView preview = new ImageView();
        preview.setFitWidth(120);
        preview.setFitHeight(120);
        preview.setPreserveRatio(true);
        preview.setVisible(false);

        uploadArea.getChildren().addAll(preview, uploadText);
        uploadArea.setOnMouseClicked(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Photo");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File file = fileChooser.showOpenDialog(popup);
            if (file != null) {
                selectedPhoto[0] = file;

                Image img = new Image(file.toURI().toString());
                preview.setImage(img);
                preview.setVisible(true);

                uploadText.setText(file.getName());
            }

        });
        
        photoBox.getChildren().addAll(photoLabel, uploadArea);
        
        // Name fields
        VBox nameFields = new VBox(15);
        HBox.setHgrow(nameFields, Priority.ALWAYS);
        
        HBox nameRow = new HBox(15);
        VBox firstNameBox = new VBox(5);
        VBox lastNameBox = new VBox(5);
        HBox.setHgrow(firstNameBox, Priority.ALWAYS);
        HBox.setHgrow(lastNameBox, Priority.ALWAYS);
        
        TextField firstNameField = createFormField();
        firstNameField.setPromptText("Saya");
        TextField lastNameField = createFormField();
        lastNameField.setPromptText("Siapa");
        
        firstNameBox.getChildren().addAll(createFieldLabel("First Name *"), firstNameField);
        lastNameBox.getChildren().addAll(createFieldLabel("Last Name *"), lastNameField);
        nameRow.getChildren().addAll(firstNameBox, lastNameBox);
        
        // Date & Place of Birth
        HBox dateIdRow = new HBox(15);
        VBox dateAndPlaceBox = new VBox(5);
        VBox idBox = new VBox(5);
        HBox.setHgrow(dateAndPlaceBox, Priority.ALWAYS);
        HBox.setHgrow(idBox, Priority.ALWAYS);
        
        Label dateAndPlaceLabel = createFieldLabel("Date & Place of Birth *");
        
        // Date dan Place HORIZONTAL
        HBox dateAndPlaceFields = new HBox(10);
        
        DatePicker dobPicker = new DatePicker();
        dobPicker.setPromptText("24 Februari 1997");
        dobPicker.setPrefWidth(200);
        
        TextField pobField = createFormField();
        pobField.setPromptText("Jakarta");
        HBox.setHgrow(pobField, Priority.ALWAYS);
        
        dateAndPlaceFields.getChildren().addAll(dobPicker, pobField);
        
        TextField idField = createFormField();
        idField.setPromptText("92456789");
        
        dateAndPlaceBox.getChildren().addAll(dateAndPlaceLabel, dateAndPlaceFields);
        idBox.getChildren().addAll(createFieldLabel("ID *"), idField);
        dateIdRow.getChildren().addAll(dateAndPlaceBox, idBox);
        
        // Email & Phone Row
        HBox contactRow = new HBox(15);
        VBox emailBox = new VBox(5);
        VBox phoneBox = new VBox(5);
        HBox.setHgrow(emailBox, Priority.ALWAYS);
        HBox.setHgrow(phoneBox, Priority.ALWAYS);
        
        TextField emailField = createFormField();
        emailField.setPromptText("wilson@gmail.com");
        TextField phoneField = createFormField();
        phoneField.setPromptText("+123456789");
        
        emailBox.getChildren().addAll(createFieldLabel("Email *"), emailField);
        phoneBox.getChildren().addAll(createFieldLabel("Phone *"), phoneField);
        contactRow.getChildren().addAll(emailBox, phoneBox);
        
        // Position
        HBox positionRow = new HBox(15);
        VBox positionBox = new VBox(5);
        Region positionSpacer = new Region();
        HBox.setHgrow(positionBox, Priority.ALWAYS);
        HBox.setHgrow(positionSpacer, Priority.ALWAYS);
        
        ComboBox<String> positionCombo = new ComboBox<>();
        positionCombo.getItems().addAll("Mahasiswa", "Dosen", "Karyawan", "Admin", "Superadmin");
        positionCombo.setPromptText("Select position");
        positionCombo.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 10 15;"
        );
        positionCombo.setMaxWidth(Double.MAX_VALUE);
        positionBox.getChildren().addAll(createFieldLabel("Position *"), positionCombo);
        
        positionRow.getChildren().addAll(positionBox, positionSpacer);
        
        nameFields.getChildren().addAll(nameRow, dateIdRow, contactRow, positionRow);
        photoAndNameRow.getChildren().addAll(photoBox, nameFields);
        
        formBox.getChildren().add(photoAndNameRow);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
             
        Button submitBtn = new Button("Submit");
        submitBtn.setStyle(
            "-fx-background-color: #3C4C79; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 35; " +
            "-fx-background-radius: 20; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        
        submitBtn.setOnAction(e -> {
            if (firstNameField.getText().trim().isEmpty()
                || dobPicker.getValue() == null
                || pobField.getText().trim().isEmpty()
                || positionCombo.getValue() == null) {

                System.out.println("Field wajib belum diisi");
                return;
            }
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String fullName = firstName + " " + lastName;
            
            String username = firstName.toLowerCase().replaceAll("\\s+", "") + (int)(Math.random()*1000);
            java.sql.Date dob = java.sql.Date.valueOf(dobPicker.getValue());
            String placeOfBirth = pobField.getText().trim();
            
            User user = new User();
            user.setNama(fullName);
            user.setUsername(username);
            user.setPassword("12345");
            user.setEmail(emailField.getText());
            user.setPhone(phoneField.getText());
            user.setBirth(dob);
            user.setPlace(placeOfBirth);
            user.setIdentity(idField.getText());
            user.setRole(positionCombo.getValue());
            user.setStatus("Aktif");
            
            if(selectedPhoto[0] != null){
                user.setPhoto(fileToBytes(selectedPhoto[0]));
            }else {   
            user.setPhoto(new byte[0]);
             }
            
           UserAdminDAO dao = new UserAdminDAO();
boolean success = dao.insert(user);

// 🔹 IP Address
String ip = "UNKNOWN";
try {
    ip = java.net.InetAddress.getLocalHost().getHostAddress();
} catch (Exception ex) {
    ex.printStackTrace();
}

if (success) {
    AuditTrailDAO.log(
        superadmin.getIdUser(),
        superadmin.getUsername(),
        "TAMBAH_USER",
        "Menambahkan user baru: "
            + user.getUsername()
            + " (" + user.getNama() + ")",
        ip,
        "BERHASIL"
    );

    allData = dao.getAll();
    table.getItems().clear();
    table.getItems().addAll(allData);
    popup.close();

} else {

    AuditTrailDAO.log(
        superadmin.getIdUser(),
        superadmin.getUsername(),
        "TAMBAH_USER",
        "Gagal menambahkan user baru: "
            + user.getUsername()
            + " (" + user.getNama() + ")",
        ip,
        "GAGAL"
    );

    System.out.println("Gagal Insert Data");
}       });
        
        buttonBox.getChildren().addAll(submitBtn);

        VBox formContainer = new VBox(0);
        formContainer.getChildren().addAll(sectionTitle, formBox);

        container.getChildren().addAll(title, formContainer, buttonBox);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(scrollPane, 900, 700);
        popup.setScene(scene);
        popup.show();
    }

    private TextField createFormField() {
        TextField field = new TextField();
        field.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e5e7eb; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 10 15;"
        );
        return field;
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        return label;
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
    }   else {
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
        Button userBtn = createMenuButton("👤  User", true);
        Button manageDataBtn = createMenuButton("⚙  Manage Data", false);
        Button auditTrailBtn = createMenuButton("📜  Audit Trail", false);
        Button laporanBtn = createMenuButton("📊  Laporan ▼", false);
        
        // Submenu Laporan
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
        
        manageDataBtn.setOnAction(e -> {
            Stage currentStage = (Stage) manageDataBtn.getScene().getWindow();
            Scene newScene = new Scene(new ManageDataPage(superadmin), 1280, 720);
            currentStage.setScene(newScene);
        });
        
        auditTrailBtn.setOnAction(e -> {
            Stage currentStage = (Stage) auditTrailBtn.getScene().getWindow();
            Scene newScene = new Scene(new AuditTrailPage(superadmin), 1280, 720);
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
        superadmin.getIdUser(),          
        superadmin.getUsername(),         
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