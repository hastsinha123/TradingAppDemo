package com.example.tradingappdemo.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tradingappdemo.R
import com.example.tradingappdemo.domain.model.ConnectionStatus
import com.example.tradingappdemo.domain.model.Direction
import com.example.tradingappdemo.domain.model.StockFilter
import com.example.tradingappdemo.domain.model.StockSort
import com.example.tradingappdemo.presentation.components.ConnectionBanner
import com.example.tradingappdemo.presentation.components.EmptyState
import com.example.tradingappdemo.presentation.components.ErrorState
import com.example.tradingappdemo.presentation.components.FilterBar
import com.example.tradingappdemo.presentation.components.LiveBadge
import com.example.tradingappdemo.presentation.components.MarketPulse
import com.example.tradingappdemo.presentation.components.SearchField
import com.example.tradingappdemo.presentation.components.StockRow
import com.example.tradingappdemo.presentation.components.StockRowSkeleton
import com.example.tradingappdemo.ui.theme.TradingAppDemoTheme

@Composable
fun TradingScreen(viewModel: TradingAppViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    TradingContent(
        state = state,
        query = query,
        onQueryChange = viewModel::onQueryChange,
        onFilterChange = viewModel::onFilterChange,
        onSortChange = viewModel::onSortChange,
        onRetry = viewModel::onRetry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingContent(
    state: TradingUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterChange: (StockFilter) -> Unit,
    onSortChange: (StockSort) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.screen_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(R.string.screen_subtitle),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    LiveBadge(status = state.connection)
                    Spacer(Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val screen = state.screen) {
                ScreenState.Loading -> LoadingList()
                is ScreenState.Failed -> ErrorState(kind = screen.kind, onRetry = onRetry)
                ScreenState.Content -> MarketContent(
                    state = state,
                    query = query,
                    onQueryChange = onQueryChange,
                    onFilterChange = onFilterChange,
                    onSortChange = onSortChange,
                )
            }
        }
    }
}

@Composable
private fun LoadingList() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        userScrollEnabled = false,
    ) {
        items(count = SKELETON_ROWS) { StockRowSkeleton() }
    }
}

@Composable
private fun MarketContent(
    state: TradingUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterChange: (StockFilter) -> Unit,
    onSortChange: (StockSort) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = state.connection != ConnectionStatus.Connected) {
            ConnectionBanner(
                status = state.connection,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        SearchField(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        FilterBar(
            filter = state.filter,
            sort = state.sort,
            gainers = state.gainers,
            losers = state.losers,
            onFilterChange = onFilterChange,
            onSortChange = onSortChange,
        )

        if (state.rows.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (state.gainers + state.losers > 0) {
                    item(key = "pulse") {
                        MarketPulse(gainers = state.gainers, losers = state.losers)
                    }
                }
                items(state.rows, key = { it.symbol }) { row ->
                    StockRow(stock = row, modifier = Modifier.animateItem())
                }
            }
        }
    }
}

private const val SKELETON_ROWS = 8

private val PreviewRows = listOf(
    StockUi("BTCUSDT", "BTC", "Bitcoin", "\$67,123.45", "+2.35%", Direction.UP, Direction.UP, 4),
    StockUi("ETHUSDT", "ETH", "Ethereum", "\$3,412.10", "-1.12%", Direction.DOWN, Direction.DOWN, 2),
    StockUi("DOGEUSDT", "DOGE", "Dogecoin", "\$0.18420", "0.00%", Direction.NONE),
    StockUi("SHIBUSDT", "SHIB", "Shiba Inu", "\$0.00001850", "+5.80%", Direction.UP),
)

private val PreviewState = TradingUiState(
    screen = ScreenState.Content,
    rows = PreviewRows,
    connection = ConnectionStatus.Connected,
    gainers = 9,
    losers = 6,
)

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun TradingContentPreview() {
    TradingAppDemoTheme(darkTheme = false) {
        TradingContent(PreviewState, "", {}, {}, {}, {})
    }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun TradingContentDarkPreview() {
    TradingAppDemoTheme(darkTheme = true) {
        TradingContent(PreviewState, "", {}, {}, {}, {})
    }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun TradingContentOfflinePreview() {
    TradingAppDemoTheme(darkTheme = false) {
        TradingContent(
            state = PreviewState.copy(
                connection = ConnectionStatus.Offline,
                rows = PreviewRows.map { it.copy(isStale = true) },
            ),
            query = "",
            onQueryChange = {},
            onFilterChange = {},
            onSortChange = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun TradingContentErrorPreview() {
    TradingAppDemoTheme(darkTheme = false) {
        TradingContent(
            state = TradingUiState(screen = ScreenState.Failed(ErrorKind.CONNECTION_FAILED)),
            query = "",
            onQueryChange = {},
            onFilterChange = {},
            onSortChange = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun TradingContentLoadingPreview() {
    TradingAppDemoTheme(darkTheme = false) {
        TradingContent(TradingUiState(), "", {}, {}, {}, {})
    }
}

