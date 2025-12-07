package pl.edu.pwr.lifyentsyev.gui;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import pl.edu.pwr.lifyentsyev.core.Board;
import pl.edu.pwr.lifyentsyev.core.Creator;
import pl.edu.pwr.lifyentsyev.core.Field;

public class SimulationController {
    @FXML
    private TextField inputWidth;
    @FXML
    private TextField inputHeight;
    @FXML
    private TextField inputTreasures;
    @FXML
    private TextField inputScavengers;
    @FXML
    private TextField inputShooter;
    @FXML
    private TextField inputBulldozer;
    @FXML
    private Button startButton;
    @FXML
    private Label lblTreasure;
    @FXML
    private Label lblScavenger;
    @FXML
    private Label lblShooter;
    @FXML
    private Label lblBulldozer;
    @FXML
    private Label lblShots;
    @FXML
    private Label lblKills;
    @FXML
    private Label lblTransformations;
    @FXML
    private GridPane boardGrid;

    private Board board;
    private Creator creator;
    private Label[][] gridLabels;
    private AnimationTimer timer;
    private boolean isSimulationStarted = false;

    String baseStyle = "-fx-border-color: #cccccc; -fx-alignment: center; -fx-font-weight: bold;";
    @FXML
    public void onStartClicked(){
        if(!isSimulationStarted){
            try {
                int width = Integer.parseInt(inputWidth.getText());
                int height = Integer.parseInt(inputHeight.getText());
                int treasures = Integer.parseInt(inputTreasures.getText());
                int scavengers = Integer.parseInt(inputScavengers.getText());
                int shooter = Integer.parseInt(inputShooter.getText());
                int bulldozer = Integer.parseInt(inputBulldozer.getText());

                if(width <= 0 || height <= 0){
                    showError("Wymiary planszy muszą być większe od 0!");
                    return;
                }

                if(treasures < 0 || scavengers < 0 || shooter < 0 || bulldozer < 0){
                    showError("Liczba figur i skarbów nie może być ujemna!");
                    return;
                }

                int total_objects = scavengers + shooter + bulldozer;
                int boardSize = width * height;

                if(total_objects > boardSize || treasures > boardSize){
                    showError("Za dużo elementów! Plansza ma tylko " + boardSize + " pól.");
                    return;
                }

                initializeSimulation(width, height, treasures, scavengers, shooter, bulldozer);

                this.timer = new AnimationTimer() {
                    @Override
                    public void handle(long now) {
                        lblTreasure.setText("Skarby: " + board.treasureCount.get());
                        lblScavenger.setText("Szperacze: " + board.scavengerCount.get());
                        lblShooter.setText("Strzelcy: " + board.shooterCount.get());
                        lblBulldozer.setText("Spychacze: " + board.bulldozerCount.get());
                        lblShots.setText("Strzały: " + board.shotsFired.get());
                        lblKills.setText("Zabójstwa: " + board.kills.get());
                        lblTransformations.setText("Transformacje: " + board.transformations.get());

                        for(int x=0; x<width; x++){
                            for(int y=0; y<height; y++){
                                Field field = board.getField(x, y);
                                Label label = gridLabels[x][y];

                                if(field.isOccupied()){
                                    switch(field.getOccupant().getSymbol()){
                                        case 'S':
                                            label.setText("🕵️");
                                            // Niebieskie tło dla Szperacza
                                            label.setStyle(baseStyle + "-fx-background-color: #add8e6; -fx-text-fill: blue;");
                                            break;
                                        case 'H':
                                            label.setText("🔫");
                                            // Czerwone tło dla Strzelca
                                            label.setStyle(baseStyle + "-fx-background-color: #ffcccc; -fx-text-fill: darkred;");
                                            break;
                                        case 'B':
                                            label.setText("🚜");
                                            // Brązowe tło dla Spychacza
                                            label.setStyle(baseStyle + "-fx-background-color: #deb887; -fx-text-fill: #5a3a22;");
                                            break;
                                    }
                                } else if (field.hasTreasure()) {
                                    label.setText("💰");
                                    // Żółte tło dla Skarbu
                                    label.setStyle(baseStyle + "-fx-background-color: #ffeb3b; -fx-text-fill: #f57f17;");
                                }else {
                                    label.setText("");
                                    // Białe tło dla pustego pola
                                    label.setStyle(baseStyle + "-fx-background-color: white;");
                                }
                            }
                        }
                    }
                };
                this.timer.start();
                isSimulationStarted = true;
                startButton.setText("Pauza");

                inputWidth.setDisable(true);
                inputHeight.setDisable(true);
                inputScavengers.setDisable(true);
                inputShooter.setDisable(true);
                inputBulldozer.setDisable(true);
                inputTreasures.setDisable(true);
            }catch(NumberFormatException e){
                showError("W polach muszą być tylko liczby całkowite!");
            }
        }else{
            if(board.isPaused()){
                board.setPaused(false);
                timer.start();
                startButton.setText("Pauza");
            }else{
                board.setPaused(true);
                timer.stop();
                startButton.setText("Start");
            }
        }
    }

    private void initializeSimulation(int width, int height, int treasures, int scavengers, int shooter, int bulldozer){
        board = new Board(width, height);
        creator = new Creator(board, treasures, scavengers, shooter, bulldozer);

        new Thread(creator).start();
        boardGrid.getChildren().clear();
        gridLabels = new Label[width][height];

        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                Label label = new Label();
                label.setMinSize(25, 25);
                label.setStyle("-fx-border-color: black; -fx-alignment: center;");
                boardGrid.add(label, x, y);
                gridLabels[x][y] = label;
            }
        }
    }

    private void showError(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd danych");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
