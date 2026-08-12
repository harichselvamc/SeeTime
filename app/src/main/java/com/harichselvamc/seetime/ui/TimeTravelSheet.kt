@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.harichselvamc.seetime.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Bottom sheet for the "Time Travel" feature — a friendlier replacement for the raw
 * planning slider. The user picks a direction (past/future), adjusts hours and minutes
 * with +/- stepper buttons, and taps Apply. Quick-chips offer common jumps.
 */
@Composable
fun TimeTravelSheet(
    currentOffsetMinutes: Int,
    onApply: (offsetMinutes: Int) -> Unit,
    onGoLive: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Local draft state — only committed when user taps Apply
    var draftHours   by remember { mutableStateOf(Math.abs(currentOffsetMinutes) / 60) }
    var draftMinutes by remember { mutableStateOf(Math.abs(currentOffsetMinutes) % 60) }
    var isFuture     by remember { mutableStateOf(currentOffsetMinutes >= 0) }

    val totalDraftMinutes = if (isFuture) (draftHours * 60 + draftMinutes)
                            else         -(draftHours * 60 + draftMinutes)

    val previewText = when {
        draftHours == 0 && draftMinutes == 0 -> "This will show live time — no offset."
        isFuture -> "Previewing ${draftHours}h ${draftMinutes}m ahead — what times will look like in the future."
        else     -> "Previewing ${draftHours}h ${draftMinutes}m behind — what times looked like in the past."
    }

    val accentColor by animateColorAsState(
        targetValue = when {
            draftHours == 0 && draftMinutes == 0 -> MaterialTheme.colorScheme.tertiary
            isFuture -> MaterialTheme.colorScheme.primary
            else     -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(300),
        label = "accent"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Time Travel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Preview any point in time — past or future",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Direction toggle: Past | Future
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = !isFuture,
                    onClick = { isFuture = false },
                    label = {
                        Text(
                            "← Past",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
                FilterChip(
                    selected = isFuture,
                    onClick = { isFuture = true },
                    label = {
                        Text(
                            "Future →",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hours stepper
            Text(
                "Hours",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StepperButton(icon = Icons.Outlined.Remove, onClick = {
                    if (draftHours > 0) draftHours--
                })
                Text(
                    text = "${draftHours}h",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                StepperButton(icon = Icons.Outlined.Add, onClick = {
                    if (draftHours < 23) draftHours++
                })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Minutes stepper
            Text(
                "Minutes",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StepperButton(icon = Icons.Outlined.Remove, onClick = {
                    draftMinutes = ((draftMinutes - 15 + 60) % 60)
                })
                Text(
                    text = "${draftMinutes}m",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                StepperButton(icon = Icons.Outlined.Add, onClick = {
                    draftMinutes = (draftMinutes + 15) % 60
                })
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick chips
            Text(
                "Quick jumps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "+1h" to Pair(true, 1 to 0),
                    "+3h" to Pair(true, 3 to 0),
                    "+6h" to Pair(true, 6 to 0),
                    "-3h" to Pair(false, 3 to 0)
                ).forEach { (label, pair) ->
                    val (future, hm) = pair
                    FilterChip(
                        selected = false,
                        onClick = {
                            isFuture = future
                            draftHours = hm.first
                            draftMinutes = hm.second
                        },
                        label = {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Preview text
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onGoLive()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.FiberManualRecord,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Go Live",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Button(
                    onClick = {
                        onApply(totalDraftMinutes)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(
                        "Apply",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
