package pl.edu.pwr.lifyentsyev.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;

/**
 * Główna klasa startowa aplikacji.
 * Klasa dziedziczy po {@link Application} i odpowiada za uruchomienie
 * środowiska JavaFX, załadowanie widoku z pliku FXML oraz wyświetlenie
 * głównego okna (Stage) z symulacją.
 */
public class GuiApp extends Application {

    /**
     * Główna metoda wejściowa cyklu życia aplikacji JavaFX.
     * Wywoływana automatycznie po zainicjalizowaniu systemu graficznego przez metodę {@code launch()}.
     * @param stage Główne okno aplikacji, dostarczane przez platformę JavaFX.
     * @throws java.io.IOException Jeśli wystąpi błąd podczas wczytywania pliku FXML.
     */
    @Override
    public void start(Stage stage) throws java.io.IOException {
        // Tworzenie loadera FXML i wskazanie pliku widoku
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("simulation_view.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Symulacja Świata - JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Standardowy punkt wejścia dla aplikacji Java.
     * Uruchamia metodę {@code launch()}, która z kolei inicjalizuje JavaFX i wywołuje metodę {@code start()}.
     */
    public static void main(String[] args) {
        launch();
    }
}