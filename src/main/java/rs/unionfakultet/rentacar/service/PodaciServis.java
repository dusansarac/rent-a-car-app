package rs.unionfakultet.rentacar.service;

import rs.unionfakultet.rentacar.model.Rezervacija;
import rs.unionfakultet.rentacar.model.Vozilo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Ucitava podatke o vozilima iz vozila.txt, cuva rezervacije (i pamti ih
 * izmedju pokretanja aplikacije u data/rezervacije.csv) i ume da ih izveze
 * u izlaz.txt po pravilima iz teksta ispita.
 */
public class PodaciServis {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Path fajlVozila;
    private final Path fajlRezervacija;
    private final Path fajlIzlaz;

    private final List<Vozilo> sviModeliVozila = new ArrayList<>();
    private final List<Rezervacija> sveRezervacije = new ArrayList<>();

    public PodaciServis(Path fajlVozila, Path fajlRezervacija, Path fajlIzlaz) {
        this.fajlVozila = fajlVozila;
        this.fajlRezervacija = fajlRezervacija;
        this.fajlIzlaz = fajlIzlaz;
    }

    // ------------------------------------------------------------------
    // Ucitavanje
    // ------------------------------------------------------------------

    public void ucitajSve() throws IOException {
        ucitajVozila();
        ucitajRezervacije();
    }

    private void ucitajVozila() throws IOException {
        sviModeliVozila.clear();
        List<String> linije = Files.readAllLines(fajlVozila, StandardCharsets.UTF_8);

        for (String linija : linije) {
            if (linija.isBlank()) continue;
            // preskoci header i separator liniju ("Marka  Model ..." i "----...")
            if (linija.startsWith("Marka") || linija.startsWith("---")) continue;

            String[] delovi = linija.trim().split("\\s{2,}");
            if (delovi.length < 5) continue;

            String marka = delovi[0].trim();
            String model = delovi[1].trim();
            String tip = delovi[2].trim();
            int brojVozila = Integer.parseInt(delovi[3].trim());
            double cena = Double.parseDouble(delovi[4].trim());

            sviModeliVozila.add(new Vozilo(marka, model, tip, brojVozila, cena));
        }
    }

    private void ucitajRezervacije() throws IOException {
        sveRezervacije.clear();
        if (!Files.exists(fajlRezervacija)) {
            return;
        }
        List<String> linije = Files.readAllLines(fajlRezervacija, StandardCharsets.UTF_8);
        for (String linija : linije) {
            if (linija.isBlank()) continue;
            String[] p = linija.split(";");
            if (p.length < 6) continue;
            String tip = p[0];
            String marka = p[1];
            String model = p[2];
            double cenaPoDanu = Double.parseDouble(p[3]);
            LocalDate od = LocalDate.parse(p[4], DATE_FMT);
            LocalDate doD = LocalDate.parse(p[5], DATE_FMT);
            sveRezervacije.add(new Rezervacija(tip, marka, model, cenaPoDanu, od, doD));
        }
    }

    // ------------------------------------------------------------------
    // Upiti nad tipovima vozila
    // ------------------------------------------------------------------

    /** Svi tipovi vozila i raspon cena (min-max) po danu za taj tip. */
    public Map<String, double[]> tipoviIRasponCena() {
        Map<String, double[]> rezultat = new TreeMap<>();
        for (Vozilo v : sviModeliVozila) {
            double[] raspon = rezultat.computeIfAbsent(v.getTip(), t -> new double[]{Double.MAX_VALUE, Double.MIN_VALUE});
            raspon[0] = Math.min(raspon[0], v.getCenaPoDanu());
            raspon[1] = Math.max(raspon[1], v.getCenaPoDanu());
        }
        return rezultat;
    }

    public List<String> sviTipovi() {
        return new ArrayList<>(tipoviIRasponCena().keySet());
    }

    // ------------------------------------------------------------------
    // Pretraga dostupnih vozila (prosirena logika u odnosu na originalni
    // ispit: uzima u obzir koliko je primeraka datog modela vec rezervisano
    // u preklapajucem periodu, ne prikazuje model ako nema slobodnih jedinica)
    // ------------------------------------------------------------------

    public List<Vozilo> pretraziDostupna(String tip, LocalDate od, LocalDate doDatum) {
        List<Vozilo> rezultat = new ArrayList<>();
        for (Vozilo v : sviModeliVozila) {
            if (!v.getTip().equalsIgnoreCase(tip)) continue;
            long zauzeto = brojZauzetihJedinica(v, od, doDatum);
            if (v.getBrojVozila() - zauzeto > 0) {
                rezultat.add(v);
            }
        }
        rezultat.sort(Comparator.comparing(Vozilo::getMarka).thenComparing(Vozilo::getModel));
        return rezultat;
    }

    private long brojZauzetihJedinica(Vozilo v, LocalDate od, LocalDate doDatum) {
        return sveRezervacije.stream()
                .filter(r -> r.getMarka().equals(v.getMarka()) && r.getModel().equals(v.getModel()))
                .filter(r -> r.preklapaSe(od, doDatum))
                .count();
    }

    // ------------------------------------------------------------------
    // Rezervacije
    // ------------------------------------------------------------------

    public Rezervacija napraviRezervaciju(Vozilo v, LocalDate od, LocalDate doDatum) throws IOException {
        Rezervacija r = new Rezervacija(v.getTip(), v.getMarka(), v.getModel(), v.getCenaPoDanu(), od, doDatum);
        sveRezervacije.add(r);
        sacuvajRezervacije();
        return r;
    }

    private void sacuvajRezervacije() throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(fajlRezervacija, StandardCharsets.UTF_8)) {
            for (Rezervacija r : sveRezervacije) {
                w.write(String.join(";",
                        r.getTip(), r.getMarka(), r.getModel(),
                        String.valueOf(r.getCenaPoDanu()),
                        r.getOd().format(DATE_FMT),
                        r.getDo().format(DATE_FMT)));
                w.newLine();
            }
        }
    }

    public List<Rezervacija> filtrirajRezervacije(String tip, LocalDate od, LocalDate doDatum) {
        return sveRezervacije.stream()
                .filter(r -> tip == null || tip.equalsIgnoreCase("Sve") || r.getTip().equalsIgnoreCase(tip))
                .filter(r -> od == null || !r.getOd().isBefore(od))
                .filter(r -> doDatum == null || !r.getDo().isAfter(doDatum))
                .sorted(Comparator.comparing(Rezervacija::getOd).thenComparing(Rezervacija::getMarka))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // Ispis u izlaz.txt (grupisano po tipu, po pravilima iz teksta ispita)
    // ------------------------------------------------------------------

    public void ispisiUFajl(List<Rezervacija> prikazane, boolean sveOpcija) throws IOException {
        Map<String, List<Rezervacija>> poTipu = new TreeMap<>();
        for (Rezervacija r : prikazane) {
            poTipu.computeIfAbsent(r.getTip(), t -> new ArrayList<>()).add(r);
        }

        try (BufferedWriter w = Files.newBufferedWriter(fajlIzlaz, StandardCharsets.UTF_8)) {
            boolean prviTip = true;
            double ukupnaZarada = 0.0;

            for (Map.Entry<String, List<Rezervacija>> ulaz : poTipu.entrySet()) {
                if (!prviTip) {
                    w.write("------------------------------");
                    w.newLine();
                }
                prviTip = false;

                List<Rezervacija> lista = ulaz.getValue();
                lista.sort(Comparator.comparing(Rezervacija::getOd).thenComparing(Rezervacija::getMarka));

                if (sveOpcija) {
                    w.write(ulaz.getKey());
                } else {
                    w.write("Kategorija: " + ulaz.getKey());
                }
                w.newLine();

                double ukupnoZaTip = 0.0;
                for (Rezervacija r : lista) {
                    w.write(String.format(Locale.US, "%s, %s:$%s-> od: %s do: %s-----$%s",
                            r.getMarka(), r.getModel(),
                            formatBroj(r.getCenaPoDanu()),
                            r.getOd().format(DATE_FMT),
                            r.getDo().format(DATE_FMT),
                            formatBroj(r.ukupnaCena())));
                    w.newLine();
                    ukupnoZaTip += r.ukupnaCena();
                }
                w.write("Ukupno: " + formatBroj(ukupnoZaTip));
                w.newLine();
                ukupnaZarada += ukupnoZaTip;
            }

            if (sveOpcija && !poTipu.isEmpty()) {
                w.write("------------------------------");
                w.newLine();
                w.write("Ukupna zarada: $" + formatBroj(ukupnaZarada));
                w.newLine();
            }
        }
    }

    private String formatBroj(double d) {
        // isti format kao u primerima izlaza: 85.0, 170.0 ...
        if (d == Math.rint(d)) {
            return String.format(Locale.US, "%.1f", d);
        }
        return String.format(Locale.US, "%.2f", d);
    }

    public Path getFajlIzlaz() {
        return fajlIzlaz;
    }
}
