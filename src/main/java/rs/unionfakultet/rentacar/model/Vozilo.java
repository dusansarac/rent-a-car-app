package rs.unionfakultet.rentacar.model;

/**
 * Predstavlja jedan red iz vozila.txt: konkretan model vozila koji auto-kuca
 * poseduje u odredjenom broju primeraka.
 */
public class Vozilo {

    private final String marka;
    private final String model;
    private final String tip;
    private final int brojVozila;
    private final double cenaPoDanu;

    public Vozilo(String marka, String model, String tip, int brojVozila, double cenaPoDanu) {
        this.marka = marka;
        this.model = model;
        this.tip = tip;
        this.brojVozila = brojVozila;
        this.cenaPoDanu = cenaPoDanu;
    }

    public String getMarka() {
        return marka;
    }

    public String getModel() {
        return model;
    }

    public String getTip() {
        return tip;
    }

    public int getBrojVozila() {
        return brojVozila;
    }

    public double getCenaPoDanu() {
        return cenaPoDanu;
    }

    /**
     * Dva vozila se smatraju "istim modelom" ako imaju istu marku i model -
     * to je jedinica za koju pratimo koliko je primeraka trenutno zauzeto
     * u datom periodu.
     */
    public String kljucModela() {
        return marka + "|" + model;
    }

    @Override
    public String toString() {
        return marka + " " + model + " (" + tip + ")";
    }
}
