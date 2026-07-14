# RelicEvents

Plugin Minecraft (Spigot/Paper 1.20+) per la generazione di eventi con reliquie — parte della modalità **Cacciatori di Reliquie**.

## Descrizione

Genera reliquie nel mondo attraverso eventi automatici: dungeon temporanei, navi fantasma, meteore e boss leggendari.

## Requisiti

- **CoreRelics** (dipendenza obbligatoria)
- Java 17+
- Spigot/Paper 1.20+

## Eventi

### 🏰 Dungeon Temporaneo
Un dungeon in deepslate appare nel mondo con mob guardiani e un boss finale. La reliquia è custodita in un baule al centro.

### ⛵ Nave Fantasma
Una nave fantasma appare (preferibilmente in acqua) con un equipaggio di scheletri e un Capitano Fantasma. La reliquia è nella stiva del capitano.

### ☄ Meteora
Una meteora precipita dal cielo con effetti particellari. Crea un cratere con blocchi di magma e ossidiana piangente. La reliquia è sepolta nel cratere.

### ☠ Boss Leggendario
Un boss potentissimo appare con armatura di netherite e servitori. Uccidendolo, droppa una reliquia. Ha vita e danno moltiplicati.

## Comandi

| Comando | Descrizione | Permesso |
|---------|-------------|----------|
| `/relicevent start` | Avvia lo scheduler eventi | `relicevents.admin` |
| `/relicevent stop` | Ferma gli eventi e pulisci | `relicevents.admin` |
| `/relicevent status` | Stato dell'evento attivo | `relicevents.admin` |
| `/relicevent force <tipo>` | Forza un evento specifico | `relicevents.admin` |
| `/relicevent reload` | Ricarica configurazione | `relicevents.admin` |

Tipi evento: `dungeon`, `ghost_ship`, `meteor`, `boss`

## Configurazione

- Intervallo tra eventi (default: 60 minuti)
- Raggio min/max dal spawn per la generazione
- Durata eventi temporanei
- Peso (probabilità) di ogni tipo di evento
- Numero di mob per evento
- Moltiplicatore vita/danno boss
- Messaggi personalizzabili

## Build

```bash
mvn clean package
```

Il JAR compilato sarà in `target/RelicEvents-1.0.0.jar`.

## Installazione

1. Installa prima **CoreRelics**
2. Copia `RelicEvents-1.0.0.jar` in `plugins/`
3. Riavvia il server
4. Configura in `plugins/RelicEvents/config.yml`
