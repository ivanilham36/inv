/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris;

/**
 *
 * @author Amy
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.mycompany.inventaris.view.MainPage;
import javafx.scene.image.Image;

public class AppLauncher extends Application {

    @Override
    public void start(Stage stage) {
        stage.getIcons().add(
            new Image(getClass().getResourceAsStream("/assets/asaindo.png"))
        );
        stage.setTitle("Ngetes doang bjirr");
        stage.setResizable(true);

        MainPage root = new MainPage(stage);

        stage.setScene(new Scene(root));
         stage.setMaximized(true);
        stage.show();
    }
}
