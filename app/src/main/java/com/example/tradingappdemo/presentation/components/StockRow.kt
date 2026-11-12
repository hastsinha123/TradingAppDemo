package com.example.tradingappdemo.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tradingappdemo.R
import com.example.tradingappdemo.domain.model.Direction
import com.example.tradingappdemo.presentation.StockUi
import com.example.tradingappdemo.ui.theme.LocalTradingColors

private const val FLASH_ALPHA = 0.30f
private const val FLASH_DURATION_MS = 600

private val AvatarPalette = listOf(
    Color(0xFF4F46E5),
    Color(0xFFF59E0B),
    Color(0xFF0EA5E9),
    Color(0xFFEC4899),
    Color(0xFF10B981),
    Color(0xFF8B5CF6),
)

@Composable
fun StockRow(stock: StockUi, modifier: Modifier = Modifier) {
    val colors = LocalTradingColors.current

    val flash = remember { Animatable(0f) }
    val firstFlashId = remember { stock.flashId }
    LaunchedEffect(stock.flashId) {
        if (stock.flashId != firstFlashId && stock.tickDirection != Direction.NONE) {
            flash.snapTo(1f)
            flash.animateTo(0f, tween(FLASH_DURATION_MS))
        }
    }
    val flashColor = when (stock.tickDirection) {
        Direction.UP -> colors.gain
        Direction.DOWN -> colors.loss
        Direction.NONE -> Color.Transparent
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (stock.isStale) 0.5f else 1f,
        label = "staleAlpha",
    )

    val description = stringResource(
        R.string.row_description,
        stock.name,
        stock.priceText,
        stock.changeText ?: stringResource(R.string.change_unavailable_spoken),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = description }
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .drawBehind { drawRect(flashColor, alpha = flash.value * FLASH_ALPHA) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinAvatar(ticker = stock.ticker)
        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = stock.ticker,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
            Text(
                text = stock.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.graphicsLayer { alpha = contentAlpha },
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stock.priceText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFeatureSettings = "tnum",
                    fontWeight = FontWeight.Bold,
                ),
            )
            ChangePill(text = stock.changeText, direction = stock.changeDirection)
        }
    }
}

@Composable
private fun ChangePill(text: String?, direction: Direction) {
    val colors = LocalTradingColors.current
    val scheme = MaterialTheme.colorScheme

    if (text == null) {
        Text(
            text = StockUi.PRICE_PLACEHOLDER,
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant,
        )
        return
    }

    val foreground: Color
    val background: Color
    val arrow: String
    when (direction) {
        Direction.UP -> {
            foreground = colors.gain
            background = colors.gainContainer
            arrow = "▲ "
        }

        Direction.DOWN -> {
            foreground = colors.loss
            background = colors.lossContainer
            arrow = "▼ "
        }

        Direction.NONE -> {
            foreground = scheme.onSurfaceVariant
            background = scheme.outlineVariant
            arrow = ""
        }
    }

    Text(
        text = arrow + text,
        color = foreground,
        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun CoinAvatar(ticker: String, modifier: Modifier = Modifier) {
    val tint = AvatarPalette[ticker.hashCode().mod(AvatarPalette.size)]
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = ticker.take(4),
            color = tint,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
    }
}
