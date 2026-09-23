package rs.unionfakultet.rentacar.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Jedna napravljena rezervacija vozila za odredjeni period.
 */
public class Rezervacija {

    private final String tip;
    private final String marka;
    private final String model;
    private final double cenaPoDanu;
    private final LocalDate od;
    private final LocalDate doDatum;

    public Rezervacija(String tip, String marka, String model, double cenaPoDanu,
                        LocalDate od, LocalDate doDatum) {
        this.tip = tip;
        this.marka = marka;
        this.model = model;
        this.cenaPoDanu = cenaPoDanu;
        this.od = od;
        this.doDatum = doDatum;
    }

    public String getTip() {
        return tip;
    }

    public String getMarka() {
        return marka;
    }

    public String getModel() {
        return model;
    }

    public double getCenaPoDanu() {
        return cenaPoDanu;
    }

    public LocalDate getOd() {
        return od;
    }

    public LocalDate getDo() {
        return doDatum;
    }

    /**
     * Broj dana trajanja rezervacije.
     *
     * NAPOMENA (greska u tekstu originalnog ispita): tekst zadatka predlaze
     * "Period.between(date1, date2).getDays()" za racunanje broja dana. To je
     * cesta zamka - Period.between vraca kalendarsku razliku razbijenu na
     * godine/mesece/dane, pa getDays() vraca SAMO "dan" komponentu (0-30),
     * a ne ukupan broj dana izmedju dva datuma. Za period npr. od 2023-01-01
     * do 2023-03-10 to bi vratilo 9 (dana), a ne 68 (koliko dana stvarno ima
     * izmedju ta dva datuma). Ispravan nacin je ChronoUnit.DAYS.between(...).
     */
    public long brojDana() {
        return ChronoUnit.DAYS.between(od, doDatum);
    }

    /**
     * Ukupna cena za period, sa popustom od 10% ako je rezervacija na 7 ili
     * vise dana (dodatno pravilo u odnosu na originalni tekst zadatka).
     */
    public double ukupnaCena() {
        double osnovnaCena = cenaPoDanu * brojDana();
        if (brojDana() >= 7) {
            osnovnaCena *= 0.9;
        }
        return osnovnaCena;
    }

    public boolean imaPopust() {
        return brojDana() >= 7;
    }

    /**
     * Da li se period ove rezervacije preklapa sa zadatim periodom
     * [odDrugi, doDrugi]. Koristi se pri proveri dostupnosti vozila.
     */
    public boolean preklapaSe(LocalDate odDrugi, LocalDate doDrugi) {
        return !od.isAfter(doDrugi) && !odDrugi.isAfter(doDatum);
    }
}
