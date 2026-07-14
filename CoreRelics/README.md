# CoreRelics

Plugin Minecraft (Spigot/Paper 1.20+) per la modalità **Cacciatori di Reliquie**.

## Descrizione

Il mondo è quasi vuoto di risorse normali. La vera ricchezza sono le reliquie leggendarie sparse per la mappa. Ogni reliquia esiste in una sola copia e ha abilità uniche.

## Funzionalità

- **Reliquie Uniche**: Ogni reliquia esiste in una sola copia nel server
- **Abilità Speciali**: Ogni reliquia ha un'abilità attivabile con click destro
- **Tracking Possessore**: Il plugin tiene traccia di chi possiede ogni reliquia
- **Storico Proprietari**: Cronologia completa di tutti i proprietari passati
- **Sistema di Fama**: Classifica pubblica dei possessori
- **Drop alla Morte**: Le reliquie cadono quando un giocatore muore
- **Comandi Admin**: Gestione completa via comandi

## Reliquie Incluse

| Reliquia | Abilità | Descrizione |
|----------|---------|-------------|
| Spada del Re Perduto | Colpo Devastante | Colpisce tutti i nemici nel raggio |
| Anello del Viaggiatore | Teletrasporto | Si teletrasporta dove guarda |
| Martello del Titano | Frantumazione | Distrugge un'area di blocchi |

## Comandi

| Comando | Descrizione | Permesso |
|---------|-------------|----------|
| `/relic list` | Lista tutte le reliquie | `corerelics.admin` |
| `/relic give <player> <relic>` | Dai una reliquia a un giocatore | `corerelics.admin` |
| `/relic remove <player> <relic>` | Rimuovi una reliquia | `corerelics.admin` |
| `/relic spawn <relic>` | Spawna una reliquia a terra | `corerelics.admin` |
| `/relic info <relic>` | Informazioni su una reliquia | `corerelics.admin` |
| `/relic history <relic>` | Storico dei proprietari | `corerelics.admin` |
| `/relic fame` | Classifica reliquie | `corerelics.fame` |
| `/relic reload` | Ricarica configurazione | `corerelics.admin` |

## Permessi

| Permesso | Descrizione | Default |
|----------|-------------|---------|
| `corerelics.admin` | Accesso ai comandi admin | OP |
| `corerelics.fame` | Vedere la classifica | Tutti |

## Installazione

1. Compila il plugin con `mvn clean package`
2. Copia il JAR da `target/` nella cartella `plugins/` del server
3. Riavvia il server
4. Configura le reliquie in `plugins/CoreRelics/config.yml`

## Configurazione

Il file `config.yml` permette di:
- Aggiungere nuove reliquie personalizzate
- Configurare cooldown delle abilità
- Personalizzare messaggi
- Attivare/disattivare il drop alla morte
- Attivare/disattivare i broadcast

## Aggiungere Nuove Reliquie

```yaml
relics:
  mia_reliquia:
    display-name: "&6La Mia Reliquia"
    lore:
      - "&7Descrizione della reliquia"
    material: DIAMOND_SWORD
    ability: SWEEPING_STRIKE  # SWEEPING_STRIKE, TELEPORT, AREA_BREAK, NONE
    ability-cooldown: 30
    ability-radius: 5.0
    ability-damage: 8.0
```

## Requisiti

- Java 17+
- Spigot/Paper 1.20+
- Maven per la compilazione

## Build

```bash
mvn clean package
```

Il JAR compilato sarà in `target/CoreRelics-1.0.0.jar`.
