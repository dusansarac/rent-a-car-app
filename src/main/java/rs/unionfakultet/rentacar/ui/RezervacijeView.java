package rs.unionfakultet.rentacar.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import rs.unionfakultet.rentacar.model.Rezervacija;
import rs.unionfakultet.rentacar.service.PodaciServis;

import java.time.LocalDate;
import java.util.List;

/**
 * Druga ekranska forma: sve rezervacije, sa filtriranjem po tipu i/ili
 * periodu, i ispis u izlaz.txt.
 */
public class RezervacijeView {

    private final PodaciServis servis;
    private final Stage stage;

    private final ComboBox<String> comboTip = new ComboBox<>();
    private final DatePicker odDatuma = new DatePicker();
    private final DatePicker doDatuma = new DatePicker();
    private final TableView<Rezervacija> tabela = new TableView<>();

    private List<Rezervacija> trenutnoPrikazane = List.of();

    public RezervacijeView(PodaciServis servis) {
        this.servis = servis;
        this.stage = new Stage();
        napraviUI();
        primeniFilter();
    }

    public void prikazi() {
        stage.show();
    }

    private void napraviUI() {
        stage.setTitle("Rezervacije");

        comboTip.getItems().add("Sve");
        comboTip.getItems().addAll(servis.sviTipovi());
        comboTip.getSelectionModel().select("Sve");

        Button dugmePretrazi = new Button("Pretrazi");
        dugmePretrazi.setOnAction(e -> primeniFilter());

        HBox filterBox = new HBox(8,
                comboTip,
                new Label("Od:"), odDatuma,
                new Label("Do:"), doDatuma,
                dugmePretrazi);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        napraviTabelu();

        Button dugmeSnimi = new Button("Snimi");
        dugmeSnimi.setOnAction(e -> snimiUFajl());

        HBox donjiBox = new HBox(dugmeSnimi);
        donjiBox.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10, filterBox, tabela, donjiBox);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 780, 480);
        stage.setScene(scene);
    }

    @SuppressWarnings("unchecked")
    private void napraviTabelu() {
        TableColumn<Rezervacija, String> kolTip = new TableColumn<>("Tip");
        kolTip.setCellValueFactory(new PropertyValueFactory<>("tip"));

        TableColumn<Rezervacija, String> kolMarka = new TableColumn<>("Marka");
        kolMarka.setCellValueFactory(new PropertyValueFactory<>("marka"));

        TableColumn<Rezervacija, String> kolModel = new TableColumn<>("Model");
        kolModel.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<Rezervacija, Double> kolCena = new TableColumn<>("Cena po danu");
        kolCena.setCellValueFactory(new PropertyValueFactory<>("cenaPoDanu"));

        TableColumn<Rezervacija, LocalDate> kolOd = new TableColumn<>("Od");
        kolOd.setCellValueFactory(new PropertyValueFactory<>("od"));

        TableColumn<Rezervacija, LocalDate> kolDo = new TableColumn<>("Do");
        kolDo.setCellValueFactory(new PropertyValueFactory<>("do"));

        TableColumn<Rezervacija, Double> kolUkupno = new TableColumn<>("Ukupna cena");
        kolUkupno.setCellValueFactory(param ->
                new javafx.beans.property.SimpleDoubleProperty(param.getValue().ukupnaCena()).asObject());

        tabela.getColumns().addAll(kolTip, kolMarka, kolModel, kolCena, kolOd, kolDo, kolUkupno);
        tabela.setPrefHeight(320);
        tabela.setPlaceholder(new Label("Nema rezervacija za zadati filter"));
    }

    private void primeniFilter() {
        String tip = comboTip.getSelectionModel().getSelectedItem();
        LocalDate od = odDatuma.getValue();
        LocalDate doD = doDatuma.getValue();
        trenutnoPrikazane = servis.filtrirajRezervacije(tip, od, doD);
        tabela.setItems(FXCollections.observableArrayList(trenutnoPrikazane));
    }

    private void snimiUFajl() {
        try {
            String tip = comboTip.getSelectionModel().getSelectedItem();
            boolean sveOpcija = tip == null || tip.equalsIgnoreCase("Sve");
            servis.ispisiUFajl(trenutnoPrikazane, sveOpcija);
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Rezervacije su sacuvane u: " + servis.getFajlIzlaz().toAbsolutePath());
            alert.showAndWait();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Greska pri snimanju: " + ex.getMessage());
            alert.showAndWait();
        }
    }
}
