
package com.cis296.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameClientFX extends Application{

    public static void main(String[] args) {
        launch(args);
    }//end main
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GameClientUI.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        stage.setTitle("Game Client UI");
        stage.setScene(scene);
        stage.show();
        
    }//end start
    
}
