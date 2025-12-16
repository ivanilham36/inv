/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.view;

/**
 *
 * @author Amy
 */

import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.prefs.Preferences;

import com.mycompany.inventaris.dao.LoginDAO;
import com.mycompany.inventaris.model.User;

public class LoginPage extends StackPane {

    private Stage stage;
    private CheckBox rememberMe;
    private TextField usernameField;
    private PasswordField passwordHidden;
    private TextField passwordVisible;


    public LoginPage(Stage stage) {
        this.stage = stage;
         javafx.scene.text.Font.loadFont(
        getClass().getResourceAsStream("/fonts/Poppins-Regular.ttf"), 12);

        javafx.scene.text.Font.loadFont(
        getClass().getResourceAsStream("/fonts/Poppins-Bold.ttf"), 12);

        initializeUI();
    }
    
    private Preferences prefs =
        Preferences.userNodeForPackage(LoginPage.class);

    private void initializeUI() {

        // BACKGROUND
        ImageView background = new ImageView(
                new Image(getClass().getResourceAsStream("/assets/login-bg.png"))
        );
        background.setPreserveRatio(false);
        background.fitWidthProperty().bind(this.widthProperty());
        background.fitHeightProperty().bind(this.heightProperty());


        // CARD CONTAINER
        VBox loginCard = new VBox(15);
        loginCard.setAlignment(Pos.TOP_LEFT);

        // ukuran fix
        loginCard.setPrefWidth(360);
        loginCard.setMaxWidth(360);
        loginCard.setMinWidth(360);

        loginCard.setPrefHeight(480);
        loginCard.setMaxHeight(480);
        loginCard.setMinHeight(480);

        loginCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 25, 0, 0, 6);" +
                "-fx-padding: 30 40 30 40;"
        );



        // LOGO
        ImageView logo = new ImageView(
                new Image(getClass().getResourceAsStream("/assets/logoAsa.png"))
        );
        logo.setFitHeight(70);
        logo.setPreserveRatio(true);
        
        StackPane logoBox = new StackPane(logo);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setMaxWidth(Double.MAX_VALUE);
        // TITLE
        Label title = new Label("MASUK");
        title.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-family: 'Poppins';" +
                "-fx-text-fill: #1e293b;"
        );

        // USERNAME
        Label labelUser = new Label("Nama Pengguna");
        labelUser.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        usernameField = new TextField();
        usernameField.setPromptText("Ketik username");
        usernameField.setStyle(
                "-fx-font-family: 'Poppins';" +
                "-fx-background-color: white;" +
                "-fx-border-color: #d5dbe4;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 10;" +
                "-fx-font-size: 12px;"
        );

        // PASSWORD FIELD
        Label passLabel = new Label("Kata Sandi");
        passLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        StackPane pwStack = new StackPane();
        pwStack.setAlignment(Pos.CENTER_LEFT);

        passwordHidden = new PasswordField();
        passwordHidden.setPromptText("Masukkan password");
        passwordHidden.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #d5dbe4;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 32 8 10;" +   // tambah space kanan buat icon mata
                "-fx-font-size: 12px;"
        );

        passwordVisible = new TextField();
        passwordVisible.setPromptText("Masukkan password");
        passwordVisible.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #d5dbe4;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 32 8 10;" +
                "-fx-font-size: 12px;"
        );
        passwordVisible.setVisible(false);
        passwordVisible.setManaged(false);

        Button toggleBtn = new Button("👁");
        toggleBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-font-size: 12px;" +
                "-fx-padding: 0;" +
                "-fx-text-fill: #64748b;" +
                "-fx-cursor: hand;"
        );

        StackPane.setAlignment(toggleBtn, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggleBtn, new Insets(0, 8, 0, 0));

        final boolean[] isShowing = {false};
        toggleBtn.setOnAction(e -> {
            isShowing[0] = !isShowing[0];
            if (isShowing[0]) {
                passwordVisible.setText(passwordHidden.getText());
                passwordVisible.setVisible(true);
                passwordVisible.setManaged(true);
                passwordHidden.setVisible(false);
                passwordHidden.setManaged(false);
                toggleBtn.setText("👁‍🗨");
            } else {
                passwordHidden.setText(passwordVisible.getText());
                passwordHidden.setVisible(true);
                passwordHidden.setManaged(true);
                passwordVisible.setVisible(false);
                passwordVisible.setManaged(false);
                toggleBtn.setText("👁");
            }
        });

        pwStack.getChildren().addAll(passwordHidden, passwordVisible, toggleBtn);


        rememberMe = new CheckBox("Ingat saya");
        rememberMe.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");
        
        // Load Username
        String savedUsername = prefs.get("remember_username", "");
        boolean remember = prefs.getBoolean("remember_checked", false);

        if (remember) {
            usernameField.setText(savedUsername);
            rememberMe.setSelected(true);
        }

        //BUTTON LOGIN 
        Button loginBtn = new Button("MASUK");
        loginBtn.setStyle(
                "-fx-background-color: #dc2626;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 10 0;" +
                "-fx-cursor: hand;" +
                "-fx-max-width: infinity;"
        );

        loginBtn.setOnAction(e -> {
            String pw = isShowing[0] ? passwordVisible.getText() : passwordHidden.getText();
            handleLogin(usernameField.getText(), pw);
        });
                this.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    String pw = isShowing[0] ? passwordVisible.getText() : passwordHidden.getText();
                    handleLogin(usernameField.getText(), pw);
                    break;
                default:
                    break;
            }
        });


        // ADD COMPONENT
        loginCard.getChildren().addAll(
                logoBox,
                title,
                labelUser,
                usernameField,
                passLabel,
                pwStack,      
                rememberMe,
                loginBtn
        );

        StackPane.setAlignment(loginCard, Pos.CENTER);
        this.getChildren().addAll(background, loginCard);
        
        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.application.Platform.runLater(() -> {
                    if (!usernameField.getText().isEmpty()) {
                            passwordHidden.requestFocus();
                        } else {
                            usernameField.requestFocus();
                        }
                });
            }
        });
    }

    private void handleLogin(String user, String pw) {
        if (user.isEmpty() || pw.isEmpty()) {
            show("Login Gagal", "Username dan password harus diisi!");
            return;
        }

        User u = LoginDAO.login(user, pw);
        if (u == null) {    
            show("Login Gagal", "Username atau password salah!");
            return;
        } 
        
        if (rememberMe.isSelected()) {
            prefs.put("remember_username", user);
            prefs.putBoolean("remember_checked", true);
        } else {
            prefs.remove("remember_username");
            prefs.putBoolean("remember_checked", false);
        }

        Scene scene;
        switch (u.getRole().toLowerCase()){
            case "mahasiswa":
            case "dosen":
            case "karyawan":
                scene = new Scene(new UserPage(u), 1280, 720);
                break;
                
            case "admin":
                scene = new Scene(new AdminPage(u), 1280, 720);
                break;
                
            case "superadmin":
                scene = new Scene(new SuperAdminPage(u), 1280, 720);
                break;
                
            default:
                show("Error", "Role Tidak Dikenali !!");
                return;
        }
        stage.setScene(scene);
    }

    private void show(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
