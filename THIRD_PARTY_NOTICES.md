# Third-party notices

SageSense source code is released under the MIT License. Dependencies retain their own licences.

## Original Product-thon blueprint

- **Sixth Sense**, created by Billy Hermawan.
- Reference: <https://devpost.com/software/sixth-sense-k2i7fq>
- Figma reference supplied by the competition: <https://www.figma.com/design/LPa1tulYRenigmUezdSJzK/Producton-Toolkit-v1?node-id=144-3627>
- The original `.fig`, PDF, screenshots, logos, mascot and presentation assets are not included in this repository. SageSense uses an original Compose-drawn eye-shield mascot and an independently implemented interface.

## Services

- OpenCode Go, accessed from the server only through its OpenAI-compatible endpoint. It routes the selected `deepseek-v4-flash` model. Use is governed by OpenCode Go's subscription terms and current model/provider privacy disclosures.
- DeepSeek V4 Flash is the selected underlying model. SageSense does not embed either provider credential in the Android app.
- Vercel Functions, optional deployment target for the FastAPI service.

## Software libraries

- AndroidX, Jetpack Compose, Room, DataStore — Android Open Source Project / Apache License 2.0 components.
- Kotlin, Kotlin Coroutines, Kotlin Serialization — Apache License 2.0.
- Retrofit, OkHttp and the kotlinx.serialization Retrofit converter — Apache License 2.0.
- FastAPI, Pydantic, Uvicorn, OpenAI Python SDK, HTTPX and pytest — distributed under their respective open-source licences.

## Knowledge sources

`knowledge/cards.json` contains short original summaries and links, not copied articles. Source organisations retain all rights in their original publications. See `docs/sources.md` for provenance and access dates.
