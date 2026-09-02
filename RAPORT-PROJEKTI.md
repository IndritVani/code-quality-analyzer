# Raport Projekti — Code Quality Analyzer

## Përshkrimi i projektit

**Code Quality Analyzer** është një vegël që analizon cilësinë e kodit të projekteve Java dhe
llogarit një **Indeks Mirëmbajtjeje (Maintainability Index)** për secilin projekt. Projektet mund
të merren nga një dosje lokale ose të klonohen drejtpërdrejt nga GitHub.

Për çdo projekt të analizuar, sistemi nxjerr metrika për skedar (LOC, kompleksitet ciklomatik,
lidhshmëri/coupling, dendësi komentesh, Halstead Volume), i përmbledh në nivel projekti dhe i
klasifikon projektet në tri nivele: **WELL_MAINTAINED**, **AVERAGE**, **NEGLECTED**.

## Arkitektura dhe teknologjitë

| Shtresa | Teknologjia |
|---|---|
| Backend | Java 21, Spring Boot 3.4, JavaParser, PostgreSQL (H2 për teste) |
| Frontend | React 18, Vite, Recharts |
| Analiza | Pipeline: `FileDiscovery → JavaParser → MetricExtractors → analizuesit e projektit → ResultAggregator` |

## Puna e kryer

### 1. Bashkimi i formulës klasike SEI në Indeksin e Mirëmbajtjes

Më parë Indeksi ishte një kompozit i ndërtuar posaçërisht (kompleksitet, dublim, coupling,
komente) dhe **nuk** përdorte formulën standarde. U ndërtua një model **me shtresa**:

- **Baza** = formula klasike Coleman–Oman / SEI (Halstead Volume, kompleksitet ciklomatik, LOC),
  e normalizuar në 0–100.
- **Rregullimet** = dublimi dhe coupling zbresin, dokumentimi shton — sinjalet që formula klasike
  nuk i mbulon.

Kjo zgjeron mbulimin e sinjaleve pa dyfishuar asnjë faktor (kompleksiteti qëndron vetëm te baza,
komentet vetëm te bonusi i dokumentimit).

### 2. Ekstraktimi i Halstead Volume

U shtua `HalsteadVolumeExtractor` që përshkon AST-në dhe llogarit `V = N · log₂(n)`. Fusha e re
`volume` u kalua nëpër të gjithë zinxhirin: `FileMetricResult → ProjectMetricSummary →
ProjectMetrics (DB) → ProjectMetricsResponse (API) → frontend`.

### 3. Rikalibrimi i pragjeve

Pas një skanimi mbi kod real, pragjet e niveleve dhe të ngjyrave u përshtatën me shpërndarjen e re
të pikëve (baza SEI ndëshkon madhësinë, ndaj pikët ulen).

### 4. Peshat e faktorëve u bënë të konfigurueshme

Të gjitha peshat dhe koeficientët u nxorën nga kodi në `application.yml` nën `analyzer.mi.*`,
të lidhura përmes `MaintainabilityProperties`. Tani mund të rregullohet **çdo faktor** — përfshirë
bazën SEI — pa ndryshuar kodin:

```yaml
analyzer:
  mi:
    base-weight: 1.0
    base:
      constant: 171.0
      volume-coefficient: 5.2
      complexity-coefficient: 0.23
      loc-coefficient: 16.2
    duplication-weight: 15.0
    coupling-weight: 10.0
    documentation-weight: 10.0
    coupling-cap: 20.0
    comment-cap: 0.30
```

Vlerat e paracaktuara riprodhojnë saktësisht rezultatin e mëparshëm. Një endpoint i ri
`GET /api/config/maintainability` i shërben peshat aktive te frontend-i, që grafiku i ndarjes së
pikëve (Score Breakdown) të mbetet i saktë.

> **Shënim praktik:** pas ndryshimit të `application.yml`, backend-i duhet **rinisur** që peshat e
> reja të zbatohen (JVM-ja nuk i lexon ndryshimet nga disku pa u rinisur).

## Testimi

- **Backend:** `mvnw test` — të gjitha testet kalojnë (përfshirë testet e reja për
  `HalsteadVolumeExtractor`, `ResultAggregator` dhe një test që dëshmon se ndryshimi i një peshe
  lëviz pikën).
- **Frontend:** `npm run build` — ndërtim i pastër.

## Hapat e ardhshëm (të mundshëm)

- Nxjerrja e pragjeve të `TierClassifier` (45/25) në konfigurim, njësoj si peshat.
- Rregullim i peshave live nga UI-ja (rillogaritje pa rianalizë).
- Përmirësim i vëzhgueshmërisë: log i `avgVolume`/`avgLoc` dhe një WARN kur nuk gjenden skedarë `.java`.
