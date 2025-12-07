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

/**
 * Kontroler widoku głównego aplikacji (JavaFX).
 * Odpowiada za:
 * 1. Odbieranie danych wejściowych od użytkownika (parametry symulacji).
 * 2. Inicjalizację logiki biznesowej (Board, Creator).
 * 3. Cykliczne odświeżanie widoku (renderowanie stanu planszy) w pętli AnimationTimer.
 * 4. Obsługę interakcji (Start, Pauza, Wznów).
 */
public class SimulationController {
    //Pola formularza (konfiguracja symulacji)
    @FXML private TextField inputWidth;
    @FXML private TextField inputHeight;
    @FXML private TextField inputTreasures;
    @FXML private TextField inputScavengers;
    @FXML private TextField inputShooter;
    @FXML private TextField inputBulldozer;
    /** Przycisk sterujący stanem symulacji (Start/Pauza/Wznów). */
    @FXML private Button startButton;
    //Etykiety statystyk (wyświetlanie stanu liczników)
    @FXML private Label lblTreasure;
    @FXML private Label lblScavenger;
    @FXML private Label lblShooter;
    @FXML private Label lblBulldozer;
    @FXML private Label lblShots;
    @FXML private Label lblKills;
    @FXML private Label lblTransformations;
    /** Główny kontener siatki, w którym rysowana jest plansza. */
    @FXML private GridPane boardGrid;

    private Board board; /** Referencja do modelu planszy (Backend). */
    private Creator creator; /** Referencja do Kreatora (generatora figur). */

    /** * Tablica pomocnicza przechowująca referencje do etykiet wizualnych (Frontend).
     * Pozwala na szybki dostęp do konkretnego pola widoku [x][y] bez przeszukiwania dzieci GridPane.
     */
    private Label[][] gridLabels;

    private AnimationTimer timer; /** Pętla renderowania JavaFX. */
    private boolean isSimulationStarted = false; /** Flaga określająca, czy symulacja została już zainicjalizowana. */

    String baseStyle = "-fx-border-color: #cccccc; -fx-alignment: center; -fx-font-weight: bold;";

    /**
     * Główna metoda obsługująca kliknięcie przycisku akcji.
     * Metoda działa w trybie maszyny stanów:
     * 1. Jeśli symulacja nie ruszyła: Waliduje dane, tworzy planszę i uruchamia timer.
     * 2. Jeśli symulacja trwa: Przełącza między stanem PAUZA a WZNÓW.
     */
    @FXML
    public void onStartClicked(){
        if(!isSimulationStarted){
            // ETAP 1: Walidacja i Start
            try {
                // Parsowanie danych wejściowych
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

                // Inicjalizacja Backendu i Frontendu
                initializeSimulation(width, height, treasures, scavengers, shooter, bulldozer);

                // Definicja pętli renderowania (AnimationTimer)
                this.timer = new AnimationTimer() {
                    @Override
                    public void handle(long now) {

                        // 1. Aktualizacja liczników statystyk
                        lblTreasure.setText("Skarby: " + board.treasureCount.get());
                        lblScavenger.setText("Szperacze: " + board.scavengerCount.get());
                        lblShooter.setText("Strzelcy: " + board.shooterCount.get());
                        lblBulldozer.setText("Spychacze: " + board.bulldozerCount.get());
                        lblShots.setText("Strzały: " + board.shotsFired.get());
                        lblKills.setText("Zabójstwa: " + board.kills.get());
                        lblTransformations.setText("Transformacje: " + board.transformations.get());

                        // 2. Aktualizacja wizualna siatki
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
                // Uruchomienie pętli i zablokowanie edycji ustawień
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
            //ETAP 2: Obsługa Pauzy
            if(board.isPaused()){
                // Wznowienie
                board.setPaused(false);
                timer.start();
                startButton.setText("Pauza");
            }else{
                // Zatrzymanie
                board.setPaused(true);
                timer.stop();
                startButton.setText("Start");
            }
        }
    }

    /**
     * Inicjalizuje obiekty logiczne i przygotowuje siatkę wizualną.
     * @param width Szerokość planszy.
     * @param height Wysokość planszy.
     * @param treasures Ilość skarbów.
     * @param scavengers Ilość Szperaczy.
     * @param shooter Ilość Strzelców.
     * @param bulldozer Ilość Spychaczy.
     */
    private void initializeSimulation(int width, int height, int treasures, int scavengers, int shooter, int bulldozer){
        // 1. Tworzenie modelu (Backend)
        board = new Board(width, height);
        creator = new Creator(board, treasures, scavengers, shooter, bulldozer);
        // 2. Uruchomienie wątku Kreatora
        new Thread(creator).start();

        // 3. Generowanie siatki w GUI
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

    /**
     * Wyświetla okno dialogowe z komunikatem błędu.
     * @param message Treść komunikatu do wyświetlenia użytkownikowi.
     */
    private void showError(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd danych");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
