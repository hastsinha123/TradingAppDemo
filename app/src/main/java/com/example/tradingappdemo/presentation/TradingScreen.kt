package com.example.tradingappdemo.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


private val UpColor = Color(0xFF16A34A)
private val DownColor = Color(0xFFDC2626)
private const val FlashAlpha = 0.35f
private const val FlashDurationMs = 600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingScreen(viewModel: TradingAppViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Stocks Watcher") }) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isConnecting) LinearProgressIndicator(Modifier.fillMaxWidth())
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.rows, key = { it.symbol }) { stock ->
                    StockRow(stock)
                }
            }
        }
    }
}

@Composable
fun StockRow(stock: StockUi, modifier: Modifier = Modifier) {
    val flash = remember { Animatable(0f) }

    LaunchedEffect(stock.flashId) {
        if (stock.direction != Direction.NONE) {
            flash.snapTo(1f)
            flash.animateTo(0f, tween(FlashDurationMs))
        }
    }

    val accent = when (stock.direction) {
        Direction.UP -> UpColor
        Direction.DOWN -> DownColor
        Direction.NONE -> MaterialTheme.colorScheme.onSurface
    }
    val arrow = when (stock.direction) {
        Direction.UP -> "▲"
        Direction.DOWN -> "▼"
        Direction.NONE -> ""
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .drawBehind { // draw-phase read of the Animatable
                drawRect(accent, alpha = flash.value * FlashAlpha)
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(stock.symbol, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                stock.name,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(arrow, color = accent, fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
            Text(
                stock.priceText,
                color = accent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
        }
    }
}
