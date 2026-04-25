# BYD Briefing

Android app para a multimídia DiLink do **BYD Song Premium**. Todo dia, no horário configurado, o app:

1. Chama a API do **Claude** (Anthropic) com a ferramenta **web_search** habilitada, usando o prompt em pt-BR sobre mercado financeiro brasileiro, notícias do Brasil, mercados globais, banco UBS e novidades de IA.
2. Converte o texto em áudio MP3 usando o **Google Cloud Text-to-Speech** (voz WaveNet pt-BR).
3. Salva localmente e notifica. Reproduz pelo player interno (botões grandes, landscape) ou pela barra de mídia nativa da BYD via **MediaSession**.

## Como construir o APK

**Mais fácil — GitHub Actions:** todo push para o branch dispara o workflow `.github/workflows/build.yml` que builda em runner Ubuntu com Android SDK pronto e publica o APK como artefato. Vá em **Actions → Build APK → último run → Artifacts** e baixe `byd-briefing-debug` (assinado com chave debug, suficiente para sideload de teste) ou `byd-briefing-release`.

**Localmente** — pré-requisitos: JDK 17, Android SDK (platform 34, build-tools 34.0.0).

```bash
# crie o keystore uma única vez
mkdir -p keystore
keytool -genkeypair -v -keystore keystore/release.jks \
  -keyalg RSA -keysize 4096 -validity 10950 \
  -alias bydnews -storepass CHANGE_ME -keypass CHANGE_ME \
  -dname "CN=BYD Briefing, O=Personal, C=BR"

cat > keystore/keystore.properties <<EOF
storeFile=keystore/release.jks
storePassword=CHANGE_ME
keyAlias=bydnews
keyPassword=CHANGE_ME
EOF

# build
./gradlew :app:assembleRelease
# saída: app/build/outputs/apk/release/app-release.apk
```

Sem `keystore.properties`, o build release usa a chave de debug (útil para teste).

## Como instalar no BYD Song Premium (firmware < 2310)

1. Formate um pendrive como **FAT32** e crie a pasta `third party apps` na raiz.
2. Copie `app-release.apk` para essa pasta.
3. Plugue no USB-A do multimídia.
4. Abra o instalador oculto e digite a senha **20211231**.
5. Toque no APK → Instalar.
6. Recomendado: também instale `PackageInstallerUnlocked.apk` (comunidade) para atualizações futuras sem senha.

> **Atenção**: a partir da versão 2310 de firmware a BYD bloqueou sideload. Não atualize se quiser continuar usando o app, ou procure as ferramentas de jailbreak do DiLink.

## Primeiro uso

1. Abra **BYD Briefing** → ícone de engrenagem → **Configurações**.
2. Cole a **chave Anthropic** (começa com `sk-ant-…`).
3. Cole a **chave Google Cloud TTS** (habilite a API Text-to-Speech no projeto GCP e restrinja a chave a esse serviço).
4. Toque em **Testar conexão**.
5. Escolha a voz (`pt-BR-Wavenet-B` por padrão) e o horário (06:00 por padrão).
6. **Salvar** → volte para a Home → **Gerar agora** para produzir o primeiro briefing.

## Debug via ADB

```bash
adb connect <ip-do-carro>:5555
adb logcat -s WM-WorkerWrapper:V DailyBriefingWorker:V AnthropicClient:V GoogleTtsClient:V PlaybackService:V
adb shell run-as com.bydnews.briefing ls files/briefings/audio
```

## Avisos

- **Sem Google Play Services**: o app não depende de GMS. WorkManager usa `AlarmManager` nativo.
- **Sleep do multimídia**: se o carro ficar desligado no horário agendado, o worker roda no próximo boot.
- **Custos**: Claude com `web_search` é cobrado por busca (~US$10/1k) + tokens. Google TTS tem cota grátis de 1M caracteres WaveNet/mês — suficiente para briefings diários.
