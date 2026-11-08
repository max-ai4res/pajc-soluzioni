# sol-dxtr-01-c05-plugin

I tre comandi (`data`, `conta`, `inverti`) che l'applicazione dell'Esercizio 5.8 di
`sol-dxtr-01-c05` carica dalla cartella `plugin/`. Il progetto dipende da `sol-dxtr-01-c05` per
l'interfaccia `Comando` (il contratto sta nel programma, non qui) e dichiara le sue
classi in `src/META-INF/services/it.unibs.pajc.redolfi.e08.Comando`.

Per rigenerare il `.jar` dopo una modifica (Eclipse copia `META-INF` in `bin/`):

```
jar --create --file ../sol-dxtr-01-c05/plugin/comandi.jar -C bin .
```
