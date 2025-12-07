module pl.edu.pwr.lifyentsyev.gui {
    // 1. Backend
    requires pl.edu.pwr.lifyentsyev.logic;

    // 2. JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // 3. Otwieramy pakiet dla FXML (SceneBuilder)
    opens pl.edu.pwr.lifyentsyev.gui to javafx.fxml;

    // 4. Eksportujemy, żeby Java mogła uruchomić aplikację
    exports pl.edu.pwr.lifyentsyev.gui;
}