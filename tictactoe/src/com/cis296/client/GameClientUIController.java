//Heba Sayed 
//Tic Tac Toe Game
//CIS 296 Project 4
//GameClientUIController.java

package com.cis296.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;

public class GameClientUIController implements Initializable {

    @FXML
    private Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9; //for the game board
    
    @FXML
    private Text winnerText;  // to display the winner/tie

    @FXML
    private TextArea taReceived;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private static final int PORT = 5454;
    private static final String IP = "localhost";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the socket connection here
        try {
            socket = new Socket(IP, 5454);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Start a separate thread to listen for messages from the server
            new Thread(this::receiveMessages).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
@FXML
    private void buttonHandler(ActionEvent event) {
        // Handle button clicks on the game board
        Button clickedButton = (Button) event.getSource();
        int position = Integer.parseInt(clickedButton.getId().substring(3)) - 1; // Button IDs are btn1, btn2, ..., btn9
        writer.println(String.valueOf(position));
        
    } 

    private void receiveMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.startsWith("BOARD:")) {
                    // update the game board based on the received message
                    updateBoard(message.substring(6));
                } else if (message.startsWith("WINNER:") || message.equals("TIE")) {
                    // display the winner or tie 
                    displayResult(message);
                    
                } else {
                    // append non-board messages to the text area
                    appendMessageToTextArea(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateBoard(String boardState) {
         // update the game board GUI 
        Platform.runLater(() -> {
            for (int i = 0; i < 9; i++) {
                char symbol = boardState.charAt(i);
                updateButton(i + 1, String.valueOf(symbol));
            }
        });
    }

    private void updateButton(int buttonNumber, String symbol) {
        // update the button with symbol 
        String buttonId = "btn" + buttonNumber;
        Button button = (Button) btn1.getScene().lookup("#" + buttonId);
        button.setText(symbol);
    }

    private void displayResult(String result) {
    // display the winner or tie message on the GUI
    if (result.startsWith("WINNER:")) {
        String winner = result.substring(7);
        winnerText.setText("Winner: " + winner);
        appendMessageToTextArea("Game Over! Player " + winner + " Wins!");
    } else if (result.equals("TIE")) {
        appendMessageToTextArea("Game Over! It's a tie!");
        winnerText.setText("It's a Tie!");
    } 
}

    private void appendMessageToTextArea(String message) {
         //Append non-board messages to the text area
        taReceived.appendText(message + "\n");
    }
}

