# Cacciatori di Reliquie

Modalità Minecraft completa — 7 plugin integrati per Spigot/Paper 1.20+.

Il mondo è quasi vuoto di risorse normali. La vera ricchezza sono le reliquie leggendarie sparse per la mappa. Ogni reliquia esiste in una sola copia e ha abilità uniche.

## Plugin

| # | Plugin | Descrizione |
|---|--------|-------------|
| 1 | **CoreRelics** | Cuore del sistema: reliquie uniche, abilità, tracking possessore, storico proprietari |
| 2 | **RelicEvents** | Eventi automatici: dungeon temporanei, navi fantasma, meteore, boss leggendari |
| 3 | **RelicMap** | Bacheca pubblica, classifica possessori, avvistamenti, tag personalizzati |
| 4 | **RelicClaims** | Claim terreni, gilde, nazioni, alleanze, tassazione, protezione |
| 5 | **RelicCombat** | PvP bilanciato, drop reliquie, anti-combat log, sistema taglie |
| 6 | **RelicSeason** | Stagioni a tempo, classifiche finali, Hall of Fame, punteggi |
| 7 | **RelicLore** | Cronaca automatica del server, libro della storia, gesta giocatori |

## Struttura Progetto (Multi-modulo Maven)

```
CacciatoriDiReliquie/
├── pom.xml              (parent)
├── CoreRelics/
├── RelicEvents/
├── RelicMap/
├── RelicClaims/
├── RelicCombat/
├── RelicSeason/
└── RelicLore/
```

## Requisiti

- Java 17+
- Spigot/Paper 1.20+
- Maven per la compilazione

## Build

```bash
mvn clean package
```

Compila tutti i 7 plugin in una volta. I JAR si trovano in `<modulo>/target/`.

## Installazione

1. Compila tutti i plugin
2. Copia tutti i 7 JAR nella cartella `plugins/` del server
3. Riavvia il server
4. Configura ogni plugin nel rispettivo file `config.yml`

## Dipendenze tra Plugin

- **CoreRelics** → nessuna dipendenza (installare sempre per primo)
- **RelicEvents** → dipende da CoreRelics
- **RelicMap** → dipende da CoreRelics
- **RelicClaims** → soft-depend su CoreRelics
- **RelicCombat** → dipende da CoreRelics
- **RelicSeason** → dipende da CoreRelics
- **RelicLore** → dipende da CoreRelics

## Come Aprire in IntelliJ IDEA

1. File → Open → seleziona la cartella `CacciatoriDiReliquie`
2. IntelliJ riconoscerà automaticamente il progetto Maven multi-modulo
3. Tutti i 7 plugin saranno disponibili come moduli
4. Le dipendenze tra moduli (es. RelicEvents → CoreRelics) si risolvono automaticamente
