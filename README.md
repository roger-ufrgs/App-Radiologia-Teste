# Como executar o aplicativo

Este projeto contém tudo o que é necessário para executar o aplicativo localmente.

## Requisitos

- Android Studio instalado.

## Executando o projeto

1. Abra o **Android Studio**.
2. Selecione **Open** e escolha a pasta deste projeto.
3. Aguarde a importação do projeto e permita que o Android Studio corrija automaticamente eventuais incompatibilidades.
4. Crie um arquivo chamado `.env` na raiz do projeto e defina a variável `GEMINI_API_KEY` com sua chave da API do Gemini (consulte o arquivo `.env.example` como referência).
5. Remova a seguinte linha do arquivo `app/build.gradle.kts`:

```kotlin
signingConfig = signingConfigs.getByName("debugConfig")
