# TradingAppDemo

An Android app that shows live crypto prices. It connects to Binance's public WebSocket,
shows the 24h percentage change for each coin, and keeps working when the connection drops.

Built with Kotlin, Jetpack Compose, Coroutines/Flow, Hilt and OkHttp.

## What it does

- Live prices that flash green or red on every tick
- 24h change shown as a percentage pill (for example `▲ +2.35%`)
- Search by name or ticker
- Filter by All, Gainers or Losers, and sort by name, price or 24h change
- Summary bar showing how many coins are up vs. down
- Handles bad connections: loading skeletons, offline banner, automatic reconnect,
  and an error screen with a retry button
- Light and dark theme

## How it's built

The code is split into three layers:

- `domain`: models, repository interfaces and use cases. Plain Kotlin, no Android code.
- `data`: the WebSocket, message parsing, reconnect logic and network monitoring.
- `presentation`: the ViewModel, UI state and Compose screens.

Data flows one way: WebSocket -> repository -> use case -> ViewModel -> UI.
Hilt connects the pieces together.

## Connection handling

- The socket closes a few seconds after the app goes to the background and reopens on return.
- If the socket fails, the app retries after 1s, 2s, 4s and so on, up to 30s.
- If the internet drops, the socket is closed and the app shows an offline banner.
  It reconnects as soon as the network is back.
- The last known prices stay on screen (dimmed) while reconnecting.
- The UI updates about 5 times a second, even though the socket can send hundreds of
  messages per second.

## Tests

Unit tests cover the domain logic, message parser, backoff timing, reconnect behaviour
and the ViewModel. Run them with:

```
./gradlew testDebugUnitTest
```

## Data source

`wss://stream.binance.com:9443/stream` (public, no API key). Each coin uses two streams:
`@trade` for the live price and `@ticker` for the 24h open price used in the percentage change.

## Running it

Open the project in Android Studio, sync Gradle, and run the `app` module on a device or
emulator with internet access.

## Possible improvements

- Let users edit their own watchlist (the list is currently fixed)
- A detail screen with a price chart
- Favourites saved locally
- UI tests for the Compose screens