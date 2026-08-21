# Third-party notices

SageSense source code is released under the MIT License. Dependencies retain their own licences.

## Original Product-thon blueprint

- **Sixth Sense**, created by Billy Hermawan.
- Reference: <https://devpost.com/software/sixth-sense-k2i7fq>
- Figma reference supplied by the competition: <https://www.figma.com/design/LPa1tulYRenigmUezdSJzK/Producton-Toolkit-v1?node-id=144-3627>
- The original `.fig`, PDF, screenshots, logos, mascot and presentation assets are not included in this repository. SageSense uses an original Compose-drawn eye-shield mascot and an independently implemented interface.

## Services

- DeepSeek API, accessed from the server only. Model: `deepseek-v4-flash`. Use is governed by DeepSeek's current API terms and pricing.
- Vercel Functions, optional deployment target for the FastAPI service.

## Typeface

- **Atkinson Hyperlegible**, regular, italic, bold and bold italic, copyright 2020 Braille Institute of America, Inc. The typeface was designed for improved character recognition and is distributed under the SIL Open Font License 1.1. The licence text is packaged at `android/app/src/main/res/raw/atkinson_hyperlegible_ofl.txt`.
- Project/source information: <https://www.brailleinstitute.org/freefont/>. The static OFL font files in this repository are the Braille Institute originals distributed through the Google Fonts repository: <https://github.com/google/fonts/tree/main/ofl/atkinsonhyperlegible>.
- Atkinson Hyperlegible is used on the FAQ & Safety page for supported Latin text. It does not include Simplified Chinese glyphs, so Android's system CJK font is used automatically for unsupported Chinese characters.

## Software libraries

- AndroidX, Jetpack Compose, Room, DataStore — Android Open Source Project / Apache License 2.0 components.
- Kotlin, Kotlin Coroutines, Kotlin Serialization — Apache License 2.0.
- Retrofit, OkHttp and the kotlinx.serialization Retrofit converter — Apache License 2.0.
- FastAPI, Pydantic, Uvicorn, OpenAI Python SDK, HTTPX and pytest — distributed under their respective open-source licences.

## Knowledge sources

`knowledge/cards.json` contains short original summaries and links, not copied articles. Source organisations retain all rights in their original publications. See `docs/sources.md` for provenance and access dates.
