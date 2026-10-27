# Release

Este documento descreve como gerar uma nova versão do **Pikuach Nefesh** e publicá-la como release no GitHub.

O processo é automatizado pelo workflow [`.github/workflows/release.yml`](../.github/workflows/release.yml).

## Como publicar uma versão

1. Garanta que a `master` está com tudo o que deve entrar na versão.
2. Crie e envie uma tag com prefixo `v`:

   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

3. Acompanhe a execução na aba **Actions** do repositório.
4. Ao terminar, a release aparece em **Releases** com os arquivos para download.

### Disparo manual

Também é possível gerar a release sem criar a tag localmente:

**Actions** → **Release** → **Run workflow** → informe a tag (ex.: `v1.0.0`).

## O que é publicado

| Arquivo | Descrição |
|---|---|
| `PikuachNefesh-<versão>-release.apk` | APK de release assinado (requer as secrets de assinatura) |
| `PikuachNefesh-<versão>-release-unsigned.apk` | APK de release sem assinatura (gerado quando as secrets não estão configuradas) |
| `PikuachNefesh-<versão>-debug.apk` | APK de debug, instalável diretamente no aparelho |
| `PikuachNefesh-<versão>-release.aab` | Android App Bundle, para envio à Play Store |
| `SHA256SUMS.txt` | Hashes SHA-256 de todos os arquivos acima |
| `Source code (zip / tar.gz)` | Código-fonte, adicionado automaticamente pelo GitHub |

As notas da release são geradas automaticamente a partir dos commits e pull requests desde a tag anterior.

## Versionamento

- **`versionName`** vem da tag, sem o `v` (ex.: `v1.2.0` → `1.2.0`).
- **`versionCode`** usa o número da execução do workflow (`github.run_number`), que sempre aumenta.

Em builds locais, os valores padrão definidos em `app/build.gradle.kts` são usados (`1` / `"1.0"`). Para sobrescrever:

```bash
gradle assembleRelease -PversionName=1.2.0 -PversionCode=12
```

Recomenda-se seguir [Versionamento Semântico](https://semver.org/lang/pt-BR/): `vMAJOR.MINOR.PATCH`.

## Assinatura do APK

Sem assinatura, o APK de release **não pode ser instalado** no Android. Para que o workflow gere um APK assinado, configure as secrets abaixo em **Settings** → **Secrets and variables** → **Actions**:

| Secret | Valor |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Conteúdo do keystore em Base64 |
| `ANDROID_KEYSTORE_PASSWORD` | Senha do keystore |
| `ANDROID_KEY_ALIAS` | Alias da chave |
| `ANDROID_KEY_PASSWORD` | Senha da chave |

### Criando um keystore

```bash
keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias pikuach
```

### Convertendo para Base64

Linux / Git Bash:

```bash
base64 -w0 release.jks
```

PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.jks"))
```

Cole a saída na secret `ANDROID_KEYSTORE_BASE64`.

> **Importante:** guarde o arquivo `release.jks` e as senhas em local seguro (ex.: gerenciador de senhas). Se o keystore for perdido, não será possível publicar atualizações do app com a mesma assinatura. Nunca faça commit do keystore — o `.gitignore` já ignora `*.jks` e `*.keystore`.

## Ambiente do build

O workflow roda em `ubuntu-latest` com:

- JDK 21 (Temurin)
- Android SDK (`android-actions/setup-android`)
- Gradle 9.3.1 (`gradle/actions/setup-gradle`)

## Solução de problemas

| Problema | Causa provável |
|---|---|
| Workflow não dispara | A tag não começa com `v`, ou só foi criada localmente (falta `git push origin <tag>`) |
| Só aparece APK `unsigned` | Secrets de assinatura ausentes ou com nome errado |
| Erro `Keystore was tampered with, or password was incorrect` | `ANDROID_KEYSTORE_PASSWORD` incorreta |
| Erro de alias / chave não encontrada | `ANDROID_KEY_ALIAS` ou `ANDROID_KEY_PASSWORD` incorretos |
| Erro `403` ao publicar a release | Em **Settings** → **Actions** → **General**, habilite *Read and write permissions* em *Workflow permissions* |
| Tag já existe | Apague a tag (`git tag -d vX` e `git push origin :refs/tags/vX`) ou use uma nova versão |
