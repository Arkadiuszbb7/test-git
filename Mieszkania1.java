import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Wyszukiwarka ofert nieruchomości (dane losowe) dla województw
 * zachodniopomorskiego i pomorskiego.
 *
 * W dowolnym pytaniu wpisz "f1" (lub naciśnij klawisz F1 i Enter),
 * aby wrócić na początek. Wpisz "q", aby zakończyć program.
 */
public class Mieszkania1 {

    // ======================= MODEL DANYCH =======================

    enum Typ {
        BLOK("Mieszkanie w bloku", 28, 85, 1.00),
        KAMIENICA("Mieszkanie w kamienicy", 35, 120, 0.90),
        MODULOWY("Dom modułowy", 60, 160, 0.80),
        SZKIELETOWY("Dom szkieletowy", 80, 200, 0.85),
        MUROWANY("Dom murowany (ściany betonowe)", 90, 260, 1.00);

        final String nazwa;
        final int minM2, maxM2;
        final double mnoznikCeny;

        Typ(String nazwa, int minM2, int maxM2, double mnoznikCeny) {
            this.nazwa = nazwa;
            this.minM2 = minM2;
            this.maxM2 = maxM2;
            this.mnoznikCeny = mnoznikCeny;
        }
    }

    enum Stan {
        DEWELOPERSKI("Stan deweloperski", 1.00),
        DO_REMONTU("Do remontu", 0.75),
        WYKONCZONE("Wykończone", 1.15);

        final String nazwa;
        final double mnoznikCeny;

        Stan(String nazwa, double mnoznikCeny) {
            this.nazwa = nazwa;
            this.mnoznikCeny = mnoznikCeny;
        }
    }

    enum Strona {
        PN("Północna część"),
        PD("Południowa część"),
        WS("Wschodnia część"),
        ZA("Zachodnia część");

        final String nazwa;

        Strona(String nazwa) {
            this.nazwa = nazwa;
        }
    }

    record Miasto(String nazwa, int cenaM2) {}

    static class Wojewodztwo {
        final String nazwa;
        final Map<Strona, List<Miasto>> miasta = new EnumMap<>(Strona.class);

        Wojewodztwo(String nazwa) {
            this.nazwa = nazwa;
        }
    }

    record Oferta(int id, Wojewodztwo woj, Miasto miasto, Strona strona, Typ typ,
                  int cena, int metraz, Stan stan, int rok, boolean odnawiany) {
        int cenaM2() {
            return Math.round((float) cena / metraz);
        }

        /** Im niższa wartość, tym korzystniejsza oferta. */
        double ocena() {
            double o = cenaM2();
            if (odnawiany) o *= 0.97;                 // bonus za remont
            o *= 1.0 - (rok - 1880) / 1500.0;         // bonus za nowszy budynek
            return o;
        }
    }

    /** Kryteria wybrane przez użytkownika (null = bez ograniczenia). */
    static class Kryteria {
        Typ typ;
        Integer cenaMin, cenaMax, metrazMin, metrazMax, rokMin, rokMax;
        Stan stan;            // null = dowolny
        Boolean odnawiany;    // null = obojętnie
        Strona strona;
        Miasto miasto;

        boolean pasuje(Oferta o) {
            return o.typ() == typ
                    && o.miasto() == miasto
                    && (cenaMin == null || o.cena() >= cenaMin)
                    && (cenaMax == null || o.cena() <= cenaMax)
                    && (metrazMin == null || o.metraz() >= metrazMin)
                    && (metrazMax == null || o.metraz() <= metrazMax)
                    && (rokMin == null || o.rok() >= rokMin)
                    && (rokMax == null || o.rok() <= rokMax)
                    && (stan == null || o.stan() == stan)
                    && (odnawiany == null || o.odnawiany() == odnawiany);
        }
    }

    // ======================= DANE (LOSOWE) =======================

    static final Random RND = new Random();
    static final List<Wojewodztwo> WOJEWODZTWA = new ArrayList<>();
    static final List<Oferta> OFERTY = new ArrayList<>();

    static void zbudujMiasta() {
        Wojewodztwo zp = new Wojewodztwo("zachodniopomorskie");
        zp.miasta.put(Strona.PN, List.of(new Miasto("Kołobrzeg", 13500), new Miasto("Świnoujście", 14000),
                new Miasto("Koszalin", 9500), new Miasto("Darłowo", 10500)));
        zp.miasta.put(Strona.PD, List.of(new Miasto("Choszczno", 6500), new Miasto("Myślibórz", 6000),
                new Miasto("Dębno", 6200), new Miasto("Pyrzyce", 6300)));
        zp.miasta.put(Strona.WS, List.of(new Miasto("Białogard", 7000), new Miasto("Szczecinek", 7500),
                new Miasto("Wałcz", 6800), new Miasto("Połczyn-Zdrój", 6500)));
        zp.miasta.put(Strona.ZA, List.of(new Miasto("Szczecin", 10500), new Miasto("Police", 8500),
                new Miasto("Goleniów", 8000), new Miasto("Gryfino", 8200)));

        Wojewodztwo pm = new Wojewodztwo("pomorskie");
        pm.miasta.put(Strona.PN, List.of(new Miasto("Władysławowo", 15000), new Miasto("Hel", 20000),
                new Miasto("Puck", 11500), new Miasto("Jastarnia", 19000)));
        pm.miasta.put(Strona.PD, List.of(new Miasto("Tczew", 8500), new Miasto("Starogard Gdański", 7800),
                new Miasto("Chojnice", 7500), new Miasto("Czersk", 6500)));
        pm.miasta.put(Strona.WS, List.of(new Miasto("Malbork", 7200), new Miasto("Kwidzyn", 7000),
                new Miasto("Sztum", 6300), new Miasto("Nowy Dwór Gdański", 6800)));
        pm.miasta.put(Strona.ZA, List.of(new Miasto("Gdynia", 16000), new Miasto("Wejherowo", 10000),
                new Miasto("Lębork", 7800), new Miasto("Słupsk", 8300)));

        WOJEWODZTWA.add(zp);
        WOJEWODZTWA.add(pm);
    }

    /** Względna wielkość rynku w mieście (więcej ogłoszeń w dużych miastach). */
    static final Map<String, Double> WIELKOSC = Map.ofEntries(
            Map.entry("Szczecin", 6.0), Map.entry("Gdynia", 6.0), Map.entry("Koszalin", 3.0),
            Map.entry("Słupsk", 3.0), Map.entry("Kołobrzeg", 3.0), Map.entry("Świnoujście", 2.0),
            Map.entry("Wejherowo", 2.5), Map.entry("Tczew", 2.0), Map.entry("Starogard Gdański", 2.0),
            Map.entry("Chojnice", 2.0), Map.entry("Szczecinek", 1.8), Map.entry("Malbork", 1.8),
            Map.entry("Police", 1.8), Map.entry("Kwidzyn", 1.5), Map.entry("Lębork", 1.5),
            Map.entry("Goleniów", 1.2), Map.entry("Gryfino", 1.2), Map.entry("Władysławowo", 1.2),
            Map.entry("Hel", 0.5), Map.entry("Jastarnia", 0.5), Map.entry("Czersk", 0.6),
            Map.entry("Sztum", 0.6), Map.entry("Nowy Dwór Gdański", 0.5), Map.entry("Połczyn-Zdrój", 0.5),
            Map.entry("Myślibórz", 0.7), Map.entry("Pyrzyce", 0.7), Map.entry("Choszczno", 0.7));

    /** Typowa liczba ogłoszeń danego rodzaju w mieście o wielkości 1.0. */
    static int bazaOfert(Typ t) {
        return switch (t) {
            case BLOK -> 40;
            case KAMIENICA -> 12;
            case MODULOWY -> 5;
            case SZKIELETOWY -> 7;
            case MUROWANY -> 22;
        };
    }

    static double ogranicz(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    static void wygenerujOferty() {
        int id = 1;
        for (Wojewodztwo w : WOJEWODZTWA) {
            for (Map.Entry<Strona, List<Miasto>> e : w.miasta.entrySet()) {
                for (Miasto m : e.getValue()) {
                    double wielkosc = WIELKOSC.getOrDefault(m.nazwa(), 1.0);
                    for (Typ typ : Typ.values()) {
                        int ile = (int) Math.round(bazaOfert(typ) * wielkosc * (0.7 + RND.nextDouble() * 0.6));
                        for (int i = 0; i < ile; i++) {
                            // stan: najwięcej wykończonych, najmniej do remontu
                            int los = RND.nextInt(100);
                            Stan stan = los < 20 ? Stan.DEWELOPERSKI : los < 35 ? Stan.DO_REMONTU : Stan.WYKONCZONE;

                            // metraż z rozkładem zbliżonym do normalnego (najczęściej średnie wartości)
                            double srednia = (typ.minM2 + typ.maxM2) / 2.0;
                            int metraz = (int) Math.round(ogranicz(
                                    srednia + RND.nextGaussian() * (typ.maxM2 - typ.minM2) / 5.0,
                                    typ.minM2, typ.maxM2));

                            int rok = losujRok(typ, stan);
                            boolean odnawiany = stan != Stan.DEWELOPERSKI
                                    && (stan == Stan.WYKONCZONE ? RND.nextInt(100) < 45 : RND.nextInt(100) < 5);

                            // cena za m²: miasto x rodzaj x stan x wiek x wielkość lokalu x remont x szum rynkowy
                            double czynnikWieku = ogranicz(0.90 + 0.20 * (rok - 1960) / 66.0, 0.88, 1.10);
                            double czynnikMetrazu = 1.0 + (srednia - metraz) / srednia * 0.15;
                            double szum = ogranicz(1.0 + RND.nextGaussian() * 0.10, 0.75, 1.30);
                            double cenaM2 = m.cenaM2() * typ.mnoznikCeny * stan.mnoznikCeny
                                    * czynnikWieku * czynnikMetrazu * (odnawiany ? 1.06 : 1.0) * szum;
                            double cena = cenaM2 * metraz;

                            // domy: do ceny dochodzi wartość działki (500-1100 m²)
                            if (typ == Typ.MODULOWY || typ == Typ.SZKIELETOWY || typ == Typ.MUROWANY) {
                                int dzialka = 500 + RND.nextInt(601);
                                cena += dzialka * m.cenaM2() * 0.03 * szum;
                            }
                            int cenaZaokraglona = (int) (Math.round(cena / 1000.0) * 1000);
                            OFERTY.add(new Oferta(id++, w, m, e.getKey(), typ,
                                    cenaZaokraglona, metraz, stan, rok, odnawiany));
                        }
                    }
                }
            }
        }
    }

    static int losujRok(Typ typ, Stan stan) {
        if (stan == Stan.DEWELOPERSKI) return 2024 + RND.nextInt(3);
        return switch (typ) {
            case KAMIENICA -> 1880 + RND.nextInt(60);
            case MODULOWY -> 2005 + RND.nextInt(21);
            case SZKIELETOWY -> 1995 + RND.nextInt(31);
            default -> 1960 + RND.nextInt(64);
        };
    }

    // ======================= WEJŚCIE / WYJŚCIE =======================

    static final BufferedReader IN =
            new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

    /** Rzucany, gdy użytkownik wciśnie F1 – powrót na początek. */
    static class PowrotNaPoczatek extends RuntimeException {
        PowrotNaPoczatek() {
            super(null, null, false, false);
        }
    }

    static String czytaj(String zachęta) {
        System.out.print(zachęta);
        String linia;
        try {
            linia = IN.readLine();
        } catch (IOException e) {
            linia = null;
        }
        if (linia == null) {
            System.out.println("\nKoniec.");
            System.exit(0);
        }
        linia = linia.trim();
        // "f1" wpisane ręcznie lub sekwencje klawisza F1 z terminala (ESC O P / ESC [ 11 ~)
        if (linia.equalsIgnoreCase("f1") || linia.contains("\u001bOP") || linia.contains("\u001b[11~")) {
            throw new PowrotNaPoczatek();
        }
        if (linia.equalsIgnoreCase("q")) {
            System.out.println("Do widzenia!");
            System.exit(0);
        }
        return linia;
    }

    /** Wybór numeru od 1 do max. */
    static int wybierz(String zachęta, int max) {
        while (true) {
            String s = czytaj(zachęta);
            try {
                int n = Integer.parseInt(s);
                if (n >= 1 && n <= max) return n;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("  Podaj liczbę od 1 do " + max + ".");
        }
    }

    /** Liczba całkowita albo pusty Enter (= bez ograniczenia). */
    static Integer liczbaLubPusty(String zachęta) {
        while (true) {
            String s = czytaj(zachęta + " (Enter = bez ograniczenia): ");
            if (s.isEmpty()) return null;
            try {
                int n = Integer.parseInt(s.replace(" ", ""));
                if (n >= 0) return n;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("  Podaj nieujemną liczbę całkowitą albo naciśnij Enter.");
        }
    }

    static String fmt(int n) {
        return String.format(Locale.US, "%,d", n).replace(',', ' ');
    }

    static String zakres(Integer min, Integer max) {
        return (min == null ? "bez limitu" : fmt(min)) + " - " + (max == null ? "bez limitu" : fmt(max));
    }

    // ======================= PRZEBIEG PROGRAMU =======================

    public static void main(String[] args) {
        zbudujMiasta();
        wygenerujOferty();

        System.out.println("=== WYSZUKIWARKA NIERUCHOMOŚCI (dane losowe, demo) ===");
        System.out.println("F1 + Enter (lub wpisz f1) = powrót na początek, q = wyjście.");

        while (true) {
            try {
                sesja();
            } catch (PowrotNaPoczatek e) {
                System.out.println("\n↺ Powrót na początek – wpisz parametry od nowa.");
            }
        }
    }

    /** Województwo wybierane numerem (1, 2) albo nazwą. */
    static Wojewodztwo wybierzWojewodztwo() {
        while (true) {
            String s = czytaj("Wybierz województwo (numer lub nazwa): ").toLowerCase();
            for (int i = 0; i < WOJEWODZTWA.size(); i++) {
                Wojewodztwo w = WOJEWODZTWA.get(i);
                if (s.equals(String.valueOf(i + 1)) || s.equals(w.nazwa)) return w;
            }
            System.out.println("  Wpisz 1, 2 albo nazwę: zachodniopomorskie / pomorskie.");
        }
    }

    static void sesja() {
        Kryteria k = new Kryteria();

        // 1. Województwo
        System.out.println("\n--- Województwo ---");
        for (int i = 0; i < WOJEWODZTWA.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + WOJEWODZTWA.get(i).nazwa);
        }
        Wojewodztwo woj = wybierzWojewodztwo();

        // 2. Rodzaj nieruchomości (od razu po wyborze województwa)
        System.out.println("\n--- Rodzaj nieruchomości w woj. " + woj.nazwa + " ---");
        Typ[] typy = Typ.values();
        for (int i = 0; i < typy.length; i++) {
            System.out.println("  " + (i + 1) + ". " + typy[i].nazwa);
        }
        k.typ = typy[wybierz("Wybierz rodzaj nieruchomości: ", typy.length) - 1];

        // 3. Parametry
        System.out.println("\n--- Parametry: " + k.typ.nazwa + " ---");
        k.cenaMin = liczbaLubPusty("Cena minimalna [zł]");
        k.cenaMax = liczbaLubPusty("Cena maksymalna [zł]");
        if (k.cenaMin != null && k.cenaMax != null && k.cenaMin > k.cenaMax) {
            Integer tmp = k.cenaMin; k.cenaMin = k.cenaMax; k.cenaMax = tmp;
        }
        k.metrazMin = liczbaLubPusty("Metraż minimalny [m²]");
        k.metrazMax = liczbaLubPusty("Metraż maksymalny [m²]");
        if (k.metrazMin != null && k.metrazMax != null && k.metrazMin > k.metrazMax) {
            Integer tmp = k.metrazMin; k.metrazMin = k.metrazMax; k.metrazMax = tmp;
        }

        System.out.println("Stan nieruchomości:");
        System.out.println("  1. Stan deweloperski\n  2. Do remontu\n  3. Wykończone\n  4. Dowolny");
        int s = wybierz("Wybierz stan: ", 4);
        k.stan = s == 4 ? null : Stan.values()[s - 1];

        k.rokMin = liczbaLubPusty("Rok budowy budynku – od");
        k.rokMax = liczbaLubPusty("Rok budowy budynku – do");
        if (k.rokMin != null && k.rokMax != null && k.rokMin > k.rokMax) {
            Integer tmp = k.rokMin; k.rokMin = k.rokMax; k.rokMax = tmp;
        }

        System.out.println("Czy budynek/lokal był odnawiany lub remontowany?");
        System.out.println("  1. Tak\n  2. Nie\n  3. Obojętnie");
        int r = wybierz("Wybierz: ", 3);
        k.odnawiany = r == 3 ? null : (r == 1);

        // 4. Lokalizacja
        System.out.println("\n--- Lokalizacja w woj. " + woj.nazwa + " ---");
        Strona[] strony = Strona.values();
        for (int i = 0; i < strony.length; i++) {
            System.out.println("  " + (i + 1) + ". " + strony[i].nazwa);
        }
        k.strona = strony[wybierz("Wybierz część województwa: ", strony.length) - 1];

        List<Miasto> miasta = woj.miasta.get(k.strona);
        System.out.println("\nMiasta – " + k.strona.nazwa.toLowerCase() + ":");
        for (int i = 0; i < miasta.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + miasta.get(i).nazwa());
        }
        k.miasto = miasta.get(wybierz("Wybierz miasto: ", miasta.size()) - 1);

        // 5. Wyniki
        pokazWyniki(woj, k);

        czytaj("\nNaciśnij Enter (albo F1), aby zacząć od nowa, q = wyjście: ");
    }

    static void pokazWyniki(Wojewodztwo woj, Kryteria k) {
        List<Oferta> pasujace = OFERTY.stream()
                .filter(o -> o.woj() == woj)
                .filter(k::pasuje)
                .sorted(Comparator.comparingDouble(Oferta::ocena))
                .collect(Collectors.toList());

        System.out.println("\n==================== WYNIKI ====================");
        System.out.println("Województwo:  " + woj.nazwa);
        System.out.println("Miasto:       " + k.miasto.nazwa() + " (" + k.strona.nazwa.toLowerCase() + ")");
        System.out.println("Rodzaj:       " + k.typ.nazwa);
        System.out.println("Cena [zł]:    " + zakres(k.cenaMin, k.cenaMax));
        System.out.println("Metraż [m²]:  " + zakres(k.metrazMin, k.metrazMax));
        System.out.println("Stan:         " + (k.stan == null ? "dowolny" : k.stan.nazwa));
        System.out.println("Rok budowy:   " + (k.rokMin == null ? "bez limitu" : k.rokMin)
                + " - " + (k.rokMax == null ? "bez limitu" : k.rokMax));
        System.out.println("Odnawiany:    " + (k.odnawiany == null ? "obojętnie" : k.odnawiany ? "tak" : "nie"));
        System.out.println("------------------------------------------------");
        System.out.println("Liczba pasujących ogłoszeń: " + pasujace.size());

        if (pasujace.isEmpty()) {
            pokazPrzyblizone(woj, k);
            return;
        }

        IntSummaryStatistics st = pasujace.stream().mapToInt(Oferta::cena).summaryStatistics();
        System.out.println("Ceny: od " + fmt(st.getMin()) + " do " + fmt(st.getMax())
                + " zł, średnio " + fmt((int) st.getAverage()) + " zł");

        int ile = Math.min(4, pasujace.size());
        System.out.println("\nNajkorzystniejsze oferty (najniższa cena za m², z premią za remont i nowszy budynek):");
        for (int i = 0; i < ile; i++) {
            wypiszOferte(i + 1, pasujace.get(i), false, null);
        }
    }

    /** Gdy brak dokładnych wyników: 4 oferty najbliższe podanym parametrom. */
    static void pokazPrzyblizone(Wojewodztwo woj, Kryteria k) {
        // najpierw to samo miasto; jeśli w ogóle nie ma ofert tego rodzaju – cała część województwa
        List<Oferta> kandydaci = OFERTY.stream()
                .filter(o -> o.woj() == woj && o.typ() == k.typ && o.miasto() == k.miasto)
                .collect(Collectors.toList());
        boolean innaMiasta = false;
        if (kandydaci.isEmpty()) {
            kandydaci = OFERTY.stream()
                    .filter(o -> o.woj() == woj && o.typ() == k.typ && o.strona() == k.strona)
                    .collect(Collectors.toList());
            innaMiasta = true;
        }

        if (kandydaci.isEmpty()) {
            System.out.println("Brak ofert tego rodzaju w tej okolicy. Wpisz parametry od nowa (F1).");
            return;
        }

        List<Oferta> najblizsze = kandydaci.stream()
                .sorted(Comparator.comparingDouble((Oferta o) -> odleglosc(o, k)).thenComparingDouble(Oferta::ocena))
                .limit(4)
                .collect(Collectors.toList());

        System.out.println("Brak ofert spełniających wszystkie parametry.");
        System.out.println("Najbardziej zbliżone oferty"
                + (innaMiasta ? " (z innych miast tej części województwa)" : " w mieście " + k.miasto.nazwa()) + ":");
        for (int i = 0; i < najblizsze.size(); i++) {
            wypiszOferte(i + 1, najblizsze.get(i), innaMiasta, k);
        }
    }

    /** Miara niedopasowania oferty do kryteriów (0 = pasuje idealnie). */
    static double odleglosc(Oferta o, Kryteria k) {
        double d = odchylenie(o.cena(), k.cenaMin, k.cenaMax)
                + odchylenie(o.metraz(), k.metrazMin, k.metrazMax)
                + odchylenieRok(o.rok(), k.rokMin, k.rokMax);
        if (k.stan != null && o.stan() != k.stan) d += 0.5;
        if (k.odnawiany != null && o.odnawiany() != k.odnawiany) d += 0.25;
        return d;
    }

    /** Względna odległość wartości od przedziału [min, max]. */
    static double odchylenie(int w, Integer min, Integer max) {
        if (min != null && w < min) return (double) (min - w) / Math.max(min, 1);
        if (max != null && w > max) return (double) (w - max) / Math.max(max, 1);
        return 0;
    }

    /** Dla roku: każde 30 lat różnicy to 1.0. */
    static double odchylenieRok(int rok, Integer min, Integer max) {
        if (min != null && rok < min) return (min - rok) / 30.0;
        if (max != null && rok > max) return (rok - max) / 30.0;
        return 0;
    }

    static String opisRoznic(Oferta o, Kryteria k) {
        List<String> r = new ArrayList<>();
        if (k.cenaMin != null && o.cena() < k.cenaMin) r.add("cena niższa o " + fmt(k.cenaMin - o.cena()) + " zł od minimum");
        if (k.cenaMax != null && o.cena() > k.cenaMax) r.add("cena wyższa o " + fmt(o.cena() - k.cenaMax) + " zł od maksimum");
        if (k.metrazMin != null && o.metraz() < k.metrazMin) r.add("metraż mniejszy o " + (k.metrazMin - o.metraz()) + " m²");
        if (k.metrazMax != null && o.metraz() > k.metrazMax) r.add("metraż większy o " + (o.metraz() - k.metrazMax) + " m²");
        if (k.rokMin != null && o.rok() < k.rokMin) r.add("budynek starszy o " + (k.rokMin - o.rok()) + " lat");
        if (k.rokMax != null && o.rok() > k.rokMax) r.add("budynek nowszy o " + (o.rok() - k.rokMax) + " lat");
        if (k.stan != null && o.stan() != k.stan) r.add("inny stan");
        if (k.odnawiany != null && o.odnawiany() != k.odnawiany) r.add(k.odnawiany ? "nie był odnawiany" : "był odnawiany");
        return r.isEmpty() ? "brak" : String.join("; ", r);
    }

    static void wypiszOferte(int nr, Oferta o, boolean pokazMiasto, Kryteria rozniceDla) {
        System.out.println("\n  #" + nr + "  (ID ogłoszenia: " + o.id() + ")");
        if (pokazMiasto) System.out.println("      Miasto:      " + o.miasto().nazwa());
        System.out.println("      Cena:        " + fmt(o.cena()) + " zł (" + fmt(o.cenaM2()) + " zł/m²)");
        System.out.println("      Metraż:      " + o.metraz() + " m²");
        System.out.println("      Stan:        " + o.stan().nazwa);
        System.out.println("      Rok budowy:  " + o.rok());
        System.out.println("      Odnawiany:   " + (o.odnawiany() ? "tak" : "nie"));
        if (rozniceDla != null) System.out.println("      Różnice:     " + opisRoznic(o, rozniceDla));
    }
}
