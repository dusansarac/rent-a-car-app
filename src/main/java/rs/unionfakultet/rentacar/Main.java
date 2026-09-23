package rs.unionfakultet.rentacar;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import rs.unionfakultet.rentacar.service.PodaciServis;
import rs.unionfakultet.rentacar.ui.PretragaView;

import java.nio.file.Path;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Path fajlVozila = Path.of("data", "vozila.txt");
        Path fajlRezervacija = Path.of("data", "rezervacije.csv");
        Path fajlIzlaz = Path.of("izlaz.txt");

        PodaciServis servis = new PodaciServis(fajlVozila, fajlRezervacija, fajlIzlaz);
        try {
            servis.ucitajSve();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Ne mogu da ucitam podatke iz " + fajlVozila.toAbsolutePath() + ": " + ex.getMessage());
            alert.showAndWait();
            return;
        }

        PretragaView pretragaView = new PretragaView(servis);
        pretragaView.prikazi();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
