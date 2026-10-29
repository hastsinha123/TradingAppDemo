# TradingAppDemo

A small Android app that shows live prices for 8 coins. When a price goes up the row flashes
green, and when it goes down it flashes red. I focused on keeping the list smooth even when
prices update very fast.

Built with Kotlin, Jetpack Compose, Coroutines/Flow, Hilt and OkHttp.

## Where the prices come from

The app listens to Binance's public WebSocket, so no API key is needed:

```
wss://stream.binance.com:9443/stream?streams=btcusdt@trade/ethusdt@trade/...
```

I used crypto pairs instead of stocks because they trade 24/7, so you can see the live
updates at any time. Stock feeds usually need an API key and only run during market hours.

## How to run

1. Clone the repo and open it in Android Studio.
2. Let Gradle sync, then run the `app` on an emulator or phone with internet.

## How it's built

I used MVVM with a repository, and Hilt for dependency injection.

```
Binance WebSocket
  -> data source      parses the JSON into a price
  -> repository       reconnects if the connection drops
  -> ViewModel        limits updates and works out up/down
  -> Compose screen   shows the rows and the flash
```

- **data:** the WebSocket and the JSON parsing
- **domain:** the models and the repository interface
- **presentation:** the ViewModel and the Compose screen
- **di:** the Hilt modules

## How I kept it smooth

- **Fewer updates.** The socket can send hundreds of prices a second. The ViewModel only passes
  the latest prices to the screen about 5 times a second.
- **Only changed rows redraw.** Rows use stable keys, so Compose skips any row whose price
  didn't change.
- **The flash doesn't cause recomposition.** The colour is drawn in the draw phase, so the
  animation repaints the row without rebuilding it.
- **Saves battery.** The socket closes shortly after the app goes to the background.

## Trade-offs

- The watchlist is hardcoded. A real app would let the user choose their own.
- Sampling every 200 ms adds a tiny delay in exchange for smoothness.
- Prices are `Double`, which is fine for showing them. A real trading engine would use
  `BigDecimal`.
