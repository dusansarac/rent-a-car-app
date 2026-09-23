package rs.unionfakultet.rentacar.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import rs.unionfakultet.rentacar.model.Rezervacija;
import rs.unionfakultet.rentacar.model.Vozilo;
import rs.unionfakultet.rentacar.service.PodaciServis;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

/**
 * Prva ekranska forma: pregled tipova vozila + raspona cena, pretraga
 * dostupnih vozila za izabrani tip i period, i pravljenje rezervacije.
 */
public class PretragaView {

    private final PodaciServis servis;
    private final Stage stage;

    private final ListView<String> listaTipova = new ListView<>();
    private final DatePicker odDatuma = new DatePicker(LocalDate.now());
    private final DatePicker doDatuma = new DatePicker(LocalDate.now().plusDays(2));
    private final TableView<Vozilo> tabelaVozila = new TableView<>();
    private final Label labelaCena = new Label("Cena za odabrani auto i period: -");
    private final Button dugmeRezervisi = new Button("Rezervisi");

    public PretragaView(PodaciServis servis) {
        this.servis = servis;
        this.stage = new Stage();
        napraviUI();
        popuniTipove();
    }

    public void prikazi() {
        stage.show();
    }

    private void napraviUI() {
        stage.setTitle("Ispit JUL - RENT-A-CAR");

        Label naslov = new Label("RENT-A-CAR");
        naslov.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        HBox naslovBox = new HBox(naslov);
        naslovBox.setAlignment(Pos.CENTER);

        listaTipova.setPrefHeight(220);

        Label labelaPeriod = new Label("Odaberite period rentiranja:");
        Button dugmePretrazi = new Button("Pretrazi");
        dugmePretrazi.setOnAction(e -> pretraziDostupnaVozila());

        HBox periodBox = new HBox(8, odDatuma, new Label("-"), doDatuma, dugmePretrazi);
        periodBox.setAlignment(Pos.CENTER_LEFT);

        VBox levoGore = new VBox(8, new Label("Tipovi:"), listaTipova);
        VBox desnoGore = new VBox(8, labelaPeriod, periodBox);
        desnoGore.setPadding(new Insets(0, 0, 0, 20));

        HBox goreBox = new HBox(levoGore, desnoGore);

        napraviTabelu();
        tabelaVozila.getSelectionModel().selectedItemProperty().addListener((obs, stari, novi) -> osveziCenu());

        dugmeRezervisi.setDisable(true);
        dugmeRezervisi.setOnAction(e -> napraviRezervaciju());

        Button dugmeSveRezervacije = new Button("Sve rezervacije");
        dugmeSveRezervacije.setOnAction(e -> new RezervacijeView(servis).prikazi());

        HBox donjiBox = new HBox(10, dugmeRezervisi);
        donjiBox.setAlignment(Pos.CENTER_LEFT);

        HBox sveRezervacijeBox = new HBox(dugmeSveRezervacije);
        sveRezervacijeBox.setAlignment(Pos.CENTER_RIGHT);

        VBox donjiDeo = new VBox(6,
                new Label("Dostupno za odabrani period:"),
                tabelaVozila,
                labelaCena,
                donjiBox,
                sveRezervacijeBox);

        VBox root = new VBox(10, naslovBox, goreBox, donjiDeo);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 620, 560);
        stage.setScene(scene);
    }

    @SuppressWarnings("unchecked")
    private void napraviTabelu() {
        TableColumn<Vozilo, String> kolMarka = new TableColumn<>("Marka");
        kolMarka.setCellValueFactory(new PropertyValueFactory<>("marka"));

        TableColumn<Vozilo, String> kolModel = new TableColumn<>("Model");
        kolModel.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<Vozilo, Double> kolCena = new TableColumn<>("Cena po danu");
        kolCena.setCellValueFactory(new PropertyValueFactory<>("cenaPoDanu"));

        tabelaVozila.getColumns().addAll(kolMarka, kolModel, kolCena);
        tabelaVozila.setPrefHeight(220);
        tabelaVozila.setPlaceholder(new Label("Izaberite tip i period, pa kliknite Pretrazi"));
    }

    private void popuniTipove() {
        ObservableList<String> stavke = FXCollections.observableArrayList();
        for (Map.Entry<String, double[]> ulaz : servis.tipoviIRasponCena().entrySet()) {
            double[] raspon = ulaz.getValue();
            stavke.add(String.format(Locale.US, "%s cena po danu: $%.1f-$%.1f",
                    ulaz.getKey(), raspon[0], raspon[1]));
        }
        listaTipova.setItems(stavke);
    }

    private String izabraniTip() {
        String stavka = listaTipova.getSelectionModel().getSelectedItem();
        if (stavka == null) return null;
        return stavka.split(" cena po danu")[0];
    }

    private void pretraziDostupnaVozila() {
        String tip = izabraniTip();
        if (tip == null) {
            prikaziUpozorenje("Izaberite tip vozila iz liste.");
            return;
        }
        LocalDate od = odDatuma.getValue();
        LocalDate doD = doDatuma.getValue();
        if (od == null || doD == null) {
            prikaziUpozorenje("Unesite oba datuma perioda rentiranja.");
            return;
        }
        if (od.isAfter(doD)) {
            prikaziUpozorenje("Datum pocetka mora biti pre datuma kraja.");
            return;
        }

        tabelaVozila.setItems(FXCollections.observableArrayList(servis.pretraziDostupna(tip, od, doD)));
        labelaCena.setText("Cena za odabrani auto i period: -");
        dugmeRezervisi.setDisable(true);
    }

    private void osveziCenu() {
        Vozilo v = tabelaVozila.getSelectionModel().getSelectedItem();
        if (v == null) {
            labelaCena.setText("Cena za odabrani auto i period: -");
            dugmeRezervisi.setDisable(true);
            return;
        }
        Rezervacija privremena = new Rezervacija(v.getTip(), v.getMarka(), v.getModel(),
                v.getCenaPoDanu(), odDatuma.getValue(), doDatuma.getValue());
        String popust = privremena.imaPopust() ? "  (ukljucen popust 10% za 7+ dana)" : "";
        labelaCena.setText(String.format(Locale.US, "Cena za odabrani auto i period: $%.1f%s",
                privremena.ukupnaCena(), popust));
        dugmeRezervisi.setDisable(false);
    }

    private void napraviRezervaciju() {
        Vozilo v = tabelaVozila.getSelectionModel().getSelectedItem();
        if (v == null) return;
        try {
            servis.napraviRezervaciju(v, odDatuma.getValue(), doDatuma.getValue());
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Rezervacija je uspesno napravljena.");
            alert.showAndWait();
            pretraziDostupnaVozila();
        } catch (Exception ex) {
            prikaziUpozorenje("Greska pri cuvanju rezervacije: " + ex.getMessage());
        }
    }

    private void prikaziUpozorenje(String poruka) {
        Alert alert = new Alert(Alert.AlertType.WARNING, poruka);
        alert.showAndWait();
    }
}
