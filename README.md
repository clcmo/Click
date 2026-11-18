# Click

[![CI](https://github.com/clcmo/Click/actions/workflows/ci.yml/badge.svg)](https://github.com/clcmo/Click/actions/workflows/ci.yml)

App Android de check-in: encontra sua localização, abre a câmera e deixa um marcador no mapa a cada foto.
Landing page: [`docs/`](docs/index.html) (GitHub Pages, pasta `/docs`).

## Arquitetura (MVVM)

```
model/   Coordinates, Checkin
data/    LocationRepository (Fused Location Provider, exposto como Flow)
ui/      MapsActivity (View) <-> MapsViewModel (estado + eventos)
```

A `MapsViewModel` não conhece `Context` nem o Maps SDK, por isso é testável em JVM puro (`MapsViewModelTest`).

## Rodando

1. Crie uma chave do Maps SDK for Android no Google Cloud e restrinja-a ao pacote `clcmo.com.br.click`.
2. No `local.properties` (que não é versionado), adicione: `MAPS_API_KEY=sua_chave`
3. Abra no Android Studio e rode. Testes: `./gradlew test`

## CI/CD

| Workflow | Quando roda | O que faz |
|---|---|---|
| `ci.yml` | push na `main` e PRs | testes unitários, lint e APK de debug (artefato) |
| `release.yml` | push na `main` | o release-please mantém um PR de versão; ao mergear, gera o APK e o AAB assinados e anexa ao GitHub Release |
| `pages.yml` | mudanças em `docs/` | publica a landing page no GitHub Pages |

**Versão:** o arquivo `version.txt` é a fonte única (`versionName`; `versionCode = MAJOR*10000 + MINOR*100 + PATCH`).
Use commits no padrão [Conventional Commits](https://www.conventionalcommits.org/pt-br/): `fix:` sobe o patch, `feat:` sobe o minor, `feat!:` sobe o major.

**Segredos do repositório** (Settings → Secrets and variables → Actions): `MAPS_API_KEY`, `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
