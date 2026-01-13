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
import javafx.scene.layout.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;    
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class MainPage extends StackPane {

    private Stage stage;
    
    public MainPage(Stage stage) {
        this.stage = stage;
        initializeUI();
    }

    private void initializeUI() {
        // Background Shape
        Pane bgShapes = new Pane();

        Circle topLeft = new Circle();
        topLeft.radiusProperty().bind(this.widthProperty().multiply(0.13));
        topLeft.centerXProperty().bind(this.widthProperty().multiply(0.39));
        topLeft.centerYProperty().bind(this.heightProperty().multiply(-0.06));
        topLeft.setFill(Color.web("#931717"));
        topLeft.setMouseTransparent(true);

        Circle smallBlue = new Circle();
        smallBlue.radiusProperty().bind(this.widthProperty().multiply(0.027));
        smallBlue.centerXProperty().bind(this.widthProperty().multiply(0.51));
        smallBlue.centerYProperty().bind(this.heightProperty().multiply(0.19));
        smallBlue.setFill(Color.web("#3C4C79"));
        smallBlue.setMouseTransparent(true);

        Circle topRight = new Circle();
        topRight.radiusProperty().bind(this.widthProperty().multiply(0.12));
        topRight.centerXProperty().bind(this.widthProperty().multiply(0.98));
        topRight.centerYProperty().bind(this.heightProperty().multiply(0.06));
        topRight.setFill(Color.web("#A42323"));
        topRight.setMouseTransparent(true);

        Circle bottomLeft = new Circle();
        bottomLeft.radiusProperty().bind(this.widthProperty().multiply(0.18));
        bottomLeft.centerXProperty().bind(this.widthProperty().multiply(0.14));
        bottomLeft.centerYProperty().bind(this.heightProperty().multiply(1.04));
        bottomLeft.setFill(Color.web("#A42323"));
        bottomLeft.setMouseTransparent(true);

        bgShapes.getChildren().addAll(topLeft, smallBlue, topRight, bottomLeft);

        // Navbar
        BorderPane navbar = new BorderPane();
        navbar.setStyle("-fx-padding: 25 60; -fx-background-color: transparent; -fx-font-family: 'Poppins';");

        // LOGO (kiri)
        ImageView logo = new ImageView(
            new Image(getClass().getResourceAsStream("/assets/logoAsa.png"))
        );
        logo.setFitHeight(70);
        logo.setPreserveRatio(true);
        navbar.setLeft(logo);

        // Text Kiri
        Label title = new Label("Sistem Inventaris Barang Kampus");
        title.setStyle(
            "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-family: 'Poppins';"
        );

        Label subtitle = new Label(
            "Kelola dan pantau aset kampus secara\n" +
            "efisien, cepat, dan terintegrasi."
        );
        subtitle.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: normal; -fx-text-fill: black; -fx-font-family: 'Poppins';"
        );
        subtitle.setLineSpacing(4);

        Button loginBtn = new Button("Masuk ke Sistem");
        loginBtn.setStyle(
            "-fx-background-color: #A42323; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 12 40; " +
            "-fx-background-radius: 20; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: bold; " +
            "-fx-font-family: 'Poppins'; " +
            "-fx-cursor: hand;"
        );
        loginBtn.setOnAction(e -> {
            Scene newScene = new Scene(new LoginPage(stage));
            stage.setScene(newScene);
            stage.setMaximized(true);
        });

        VBox leftContent = new VBox(title, subtitle, loginBtn);
        leftContent.setSpacing(25);
        leftContent.setAlignment(Pos.CENTER_LEFT);

        // Logo
        ImageView heroImage = new ImageView(
            new Image(getClass().getResourceAsStream("/assets/logoInv.png"))
        );
        heroImage.setFitWidth(420);
        heroImage.setPreserveRatio(true);

        HBox hero = new HBox(leftContent, heroImage);
        hero.setSpacing(120);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.setStyle("-fx-padding: 60 80;");

        VBox content = new VBox(navbar, hero);
        content.setAlignment(Pos.TOP_CENTER);

        // Stackpane
        this.getChildren().addAll(bgShapes, content);
    }
}