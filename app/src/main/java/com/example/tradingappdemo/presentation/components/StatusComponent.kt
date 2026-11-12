package com.example.tradingappdemo.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tradingappdemo.R
import com.example.tradingappdemo.domain.model.ConnectionStatus
import com.example.tradingappdemo.ui.theme.LocalTradingColors

@Composable
fun LiveBadge(status: ConnectionStatus, modifier: Modifier = Modifier) {
    val colors = LocalTradingColors.current
    val label: String
    val color: Color
    when (status) {
        ConnectionStatus.Connected -> {
            label = stringResource(R.string.status_live)
            color = colors.gain
        }

        ConnectionStatus.Connecting -> {
            label = stringResource(R.string.status_connecting)
            color = colors.warning
        }

        is ConnectionStatus.Reconnecting -> {
            label = stringResource(R.string.status_reconnecting)
            color = colors.warning
        }

        ConnectionStatus.Offline -> {
            label = stringResource(R.string.status_offline)
            color = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    val pulse = rememberInfiniteTransition(label = "livePulse")
    val pulseAlpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "livePulseAlpha",
    )
    val isLive = status == ConnectionStatus.Connected

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(8.dp)
                .graphicsLayer { alpha = if (isLive) pulseAlpha else 1f }
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(text = label, color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ConnectionBanner(status: ConnectionStatus, modifier: Modifier = Modifier) {
    val colors = LocalTradingColors.current
    val scheme = MaterialTheme.colorScheme

    val message: String
    val container: Color
    val content: Color
    val showSpinner: Boolean
    when (status) {
        ConnectionStatus.Connecting, ConnectionStatus.Connected -> {
            message = stringResource(R.string.banner_connecting)
            container = scheme.secondaryContainer
            content = scheme.onSecondaryContainer
            showSpinner = true
        }

        is ConnectionStatus.Reconnecting -> {
            message = stringResource(R.string.banner_reconnecting, status.attempt)
            container = scheme.errorContainer
            content = scheme.onErrorContainer
            showSpinner = false
        }

        ConnectionStatus.Offline -> {
            message = stringResource(R.string.banner_offline)
            container = scheme.errorContainer
            content = scheme.onErrorContainer
            showSpinner = false
        }
    }

    Surface(modifier = modifier.fillMaxWidth(), color = container, shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showSpinner) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = content,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = colors.warning,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(text = message, color = content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
