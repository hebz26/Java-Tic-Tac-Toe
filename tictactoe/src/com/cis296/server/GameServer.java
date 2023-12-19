//Heba Sayed 
//Tic Tac Toe Game
//CIS 296 Project 4
//GameServer.java

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class GameServer {

    private static final int PORT = 5454;
    private static final Set<GameClientHandler> clients = new HashSet<>();
    private static final String[] playerSymbols = {"X", "O"};
    private static int currentPlayerIndex = 0;
    private static final int MAX_PLAYERS = 2;
    private static final int BOARD_SIZE = 9; 
    private static final char[] board = new char[BOARD_SIZE];
    private static boolean gameOver = false; 

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            //empty tic tac toe board
            initializeBoard();
            
            //making sure only 2 people can join at a time
            while (clients.size() < MAX_PLAYERS) { 
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                // Create a new thread to handle the client
                GameClientHandler clientHandler = new GameClientHandler(clientSocket, playerSymbols[currentPlayerIndex]);
                clients.add(clientHandler);
                new Thread(clientHandler).start();

                // Switch to the next player
                currentPlayerIndex = (currentPlayerIndex + 1) % playerSymbols.length;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    //empty tic tac toe board
    private static void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[i] = ' ';
        }
    }

    private static class GameClientHandler implements Runnable {
        
        private final Socket clientSocket;
        private final String playerSymbol;
        private PrintWriter writer;
        private BufferedReader reader;

        public GameClientHandler(Socket clientSocket, String playerSymbol) {
            this.clientSocket = clientSocket;
            this.playerSymbol = playerSymbol;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                // notify the client about its assigned mark
                writer.println("You are player " + playerSymbol);

                // broadcast the assigned mark to all clients
                broadcast(playerSymbol + " has joined the game");

                // Send the initial board state
                sendBoardState();

                String message;
                while ((message = reader.readLine()) != null) {
                    handleMove(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Remove client when it disconnects
                clients.remove(this);
                broadcast(playerSymbol + " has left the game");
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        
        private void handleMove(String move) {
    try {
        int position = Integer.parseInt(move);
        if (isValidMove(position) && playerSymbol.equals(playerSymbols[currentPlayerIndex])) {
            
            // Only allow the current player to make a move
            board[position] = playerSymbol.charAt(0);
            if(gameOver == false) //when the game is over, players cannot place symbols
            {
            sendBoardState();
            }

            if (checkForWinner()) {
                gameOver = true; 
                broadcast("WINNER:" + playerSymbol);
                
            } else if (checkForTie()) {
                gameOver = true; 
                broadcast("TIE");
                
            } else {
                if(  gameOver == false )
                {
                switchPlayer();
                broadcast("BOARD:" + new String(board));
                broadcast("Player " + playerSymbols[currentPlayerIndex] + ", it's your turn!");
                }
            }
        } else {
             if(  gameOver == false ){
            sendMessage("Wait your turn or \n Choose empty space");
             }
        }
    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
        sendMessage("Invalid move. Please try again.");
    }
}


        private boolean isValidMove(int position) {
            
            return position >= 0 && position < BOARD_SIZE && board[position] == ' ';
        }
        
        

        private boolean checkForWinner() {
            // Check for a winner (8 possible winning combinations)
            return checkLine(0, 1, 2) || checkLine(3, 4, 5) || checkLine(6, 7, 8) ||
                    checkLine(0, 3, 6) || checkLine(1, 4, 7) || checkLine(2, 5, 8) ||
                    checkLine(0, 4, 8) || checkLine(2, 4, 6);
        }
   

        private boolean checkLine(int a, int b, int c) {
            return board[a] != ' ' && board[a] == board[b] && board[a] == board[c];
        }

        private boolean checkForTie() {
            // Check if the board is full, and there is no winner
            for (int i = 0; i < BOARD_SIZE; i++) {
                if (board[i] == ' ') {
                    return false;
                }
            }
            return true;
        }

        private void switchPlayer() {
            currentPlayerIndex = (currentPlayerIndex + 1) % 2;
        }

        private void sendBoardState() {
            broadcast("BOARD:" + new String(board));
        }

        private void broadcast(String message) {
            for (GameClientHandler client : clients) {
                client.sendMessage(message);
            }
        }

        private void sendMessage(String message) {
            writer.println(message);
        }
    }
 
}
