# Filkonvertering til PDF

For å gi større frihet for brukere, ønsker man å åpne for opplasting av flere filformater i søknaden. Da fagsystemene 
kun håndterer noen bildefiler og PDF, må eventuelt andre filtyper konverteres til PDF "on the fly".

Dette biblioteket støtter følgende filtyper:
* Excel (.xlsx, IKKE .xls)

Merk at dette ikke er en universell konverterer, og det er viktig å sjekke at aktuelle eksempelfiler konverteres som 
ventet før dette brukes i produksjon. 

## Bruk

Eksisterende filkonverterere implementerer ```interface FilTilPdfConverter```. 

### Excel -> PDF
```kotlin
object ExcelToPdfConverter: FilTilPdfConverter {
    override fun konverterTilPdf(source: ByteArray) = konverterTilPdfWithOptions(source, PdfPageOptions())
    
    fun konverterTilPdfWithOptions(source: ByteArray, options: PdfPageOptions): ByteArray
}
```
Konverterer data fra excel-fil (source) og returnerer ByteArray = pdf. 2-stegs prosess hvor man henter ut data fra source,
og prøver bygge det opp så likt som mulig i pdf. Prøver å ivareta tabellstruktur, men det er viktig
å bekrefte dette før bruk i produksjon. `konverterTilPdfWithOptions` kan brukes hvis man ønsker å endre strukturell 
utseende på PDF-siden(e). Kun .xslx støttes, IKKE .xsl. (Merk: `TilpassKolonner`-flagget har ingen innvirkning på konvertering av excel.)
Hvis innholdet er lenger enn 1 pdf-side, vil den forsøke skrive videre innhold på ny(e) side(r). 
Er innholdet for bredt, vil det kastes exception.

```kotlin
class WritePdfPageOptions(
    var fontByteArray: ByteArray = getDefaultFontBytes(),
    var fontSize: Short = 11,
    val lineStartFromEdge: Float = 1f,
    val columnMargin: Float = 3f,
    val rowMargin: Float = 3f,
    val tilpassKolonner: Boolean = false,
)
```
Påvirker utseendet på den genererte PDF-filen. Kun for excel- og csv-konvertering. 
### fontByteArray og fontSize
Default = ```calibri```. Støtte for valgfrie fonter er ikke testet ordentlig.
### lineStartFromEdge
Utgangspunkt for starten av en linje på x-aksen. Oppgis som offset i punkter hvor A4-ark defineres som 595.27563 punkter av 
biblioteket.
### columnMargin
Hvor mange punkter som skiller 2 celler/kolonner (hvis teksten er stor nok eller høyrejustert). 
### rowMargin
Hvor mange punkter mellom rader.
