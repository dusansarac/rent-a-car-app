# Rent-A-Car (JavaFX)

JavaFX aplikacija za iznajmljivanje automobila, uradjena na osnovu ispitnog
zadatka iz predmeta **Objektno-orijentisano programiranje** (Racunarski
fakultet, Univerzitet Union, jul 2022/2023, grupa 1), sa nekoliko dodatih
funkcionalnosti u odnosu na originalni tekst zadatka.

## Osnovna funkcionalnost (iz originalnog zadatka)

- Ucitavanje podataka o vozilima iz `data/vozila.txt` (marka, model, tip,
  broj vozila, cena po danu).
- Prva forma: pregled svih tipova vozila i raspona cena; pretraga dostupnih
  vozila za izabrani tip i period; klik na red prikazuje ukupnu cenu; dugme
  **Rezervisi** pravi rezervaciju.
- Druga forma (**Sve rezervacije**): prikaz svih rezervacija sa filtriranjem
  po tipu (ili "Sve") i/ili periodu; dugme **Snimi** ispisuje prikazane
  rezervacije u `izlaz.txt`, grupisano po tipu, sortirano po datumu pocetka
  i marki, sa ukupnom zaradom po tipu (i ukupnom zaradom svih tipova kada je
  izabrano "Sve").

## Sta je dodato preko originalnog zadatka

1. **Provera stvarne dostupnosti vozila.** Originalni zadatak ne trazi
   proveru da li je odredjeni model vec iznajmljen u tom periodu - ovde se
   za svaki model racuna koliko je primeraka vec rezervisano u periodima
   koji se preklapaju sa trazenim, i model se prikazuje kao dostupan samo
   ako preostane bar jedan slobodan primerak (`PodaciServis.pretraziDostupna`,
   `Rezervacija.preklapaSe`).

2. **Popust od 10% za rezervacije od 7 ili vise dana**
   (`Rezervacija.ukupnaCena`).

3. **Perzistencija rezervacija** izmedju pokretanja aplikacije - rezervacije
   se cuvaju u `data/rezervacije.csv` i ucitavaju pri sledecem pokretanju,
   umesto da se gube kad se aplikacija ugasi.

4. **DatePicker** umesto teksta za unos datuma (manje mesta za pogresan
   format).

5. **Ispravka poznate zamke iz teksta ispita.** Tekst ispita predlaze
   `Period.between(date1, date2).getDays()` za racunanje broja dana izmedju
   dva datuma. To je cesta greska: `Period.between(...)` vraca kalendarsku
   razliku razbijenu na godine/mesece/dane, pa `getDays()` vraca *samo* "dan"
   komponentu (0-30), a ne ukupan broj dana. Npr. za period
   2023-01-01 -> 2023-03-10 taj poziv vraca 9, a ne 68 (stvarni broj dana).
   Ovde se koristi `ChronoUnit.DAYS.between(od, do)`, koje daje tacan broj
   dana (vidi komentar u `Rezervacija.brojDana()`).

## Struktura projekta

```
src/main/java/rs/unionfakultet/rentacar/
  Main.java                     - JavaFX Application, ulazna tacka
  model/Vozilo.java             - model vozila (marka, model, tip, broj, cena)
  model/Rezervacija.java        - model rezervacije (period, cena, popust)
  service/PodaciServis.java     - ucitavanje/cuvanje podataka, poslovna logika
  ui/PretragaView.java          - prva ekranska forma (pretraga i rezervisanje)
  ui/RezervacijeView.java       - druga ekranska forma (filtriranje i izvoz)
data/vozila.txt                 - ulazni podaci o vozilima
```

## Pokretanje

Potreban je JDK 17+ i Maven.

```bash
mvn javafx:run
```

ili napraviti izvrsni jar:

```bash
mvn package
java -jar target/rent-a-car-1.0.0.jar
```

Aplikaciju treba pokretati iz korena projekta (radi relativnih putanja
`data/vozila.txt`, `data/rezervacije.csv` i `izlaz.txt`).

## Testiranje

Logika (parsiranje `vozila.txt`, racunanje cena, generisanje `izlaz.txt`) je
rucno provera protiv primera datih uz ispit (`izlaz_primer.txt` i
`izlaz_primer_tipovi.txt`) - generisani izlaz se slaze sa ocekivanim
vrednostima (redosled grupa po tipu je alfabetski, sto tekst zadatka ne
propisuje eksplicitno).
