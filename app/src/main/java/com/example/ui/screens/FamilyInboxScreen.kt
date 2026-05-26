package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.FamilyInboxItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HomePilotCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyInboxScreen(
    inboxItems: List<FamilyInboxItem>,
    isExtracting: Boolean,
    onAddInboxMessage: (String, String) -> Unit,
    onApplyAction: (Int) -> Unit,
    onDismissItem: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var rawText by remember { mutableStateOf("") }
    var senderName by remember { mutableStateOf("Manual Clip") }

    val activeInbox = inboxItems.filter { !it.isProcessed }

    val presets = listOf(
        Pair("Airtel SMS Alert", "Dear Rajesh, fiber broadband bill generated. Total Amount Due: Rs. 1499.00 due by June 12, 2026. Avoid late fee charges."),
        Pair("Dadi Chemist Receipt", "Prescription receipt No #2819. Please replenish Telmisartan daily dose. Cost: INR 450 total. Doctor advice: morning after breakfast."),
        Pair("Water Board Bill", "Municipal water authority consumer: 02881928, charges for May outstanding: INR 850. Due: 10/06/2026.")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Header Info ---
        item {
            Text(
                text = "Family Inbox",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Paste raw text schedules, message screens, or SMS. HomePilot AI structures actionable entries instantly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // --- 2. Live Entry & Parsing Card ---
        item {
            HomePilotCard(backgroundColor = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Import Unstructured Snippet:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = rawText,
                        onValueChange = { rawText = it },
                        placeholder = { Text("Paste messy WhatsApp, clipboard notifications, doctor prescriptions, or SMS logs here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = senderName,
                            onValueChange = { senderName = it },
                            label = { Text("Source") },
                            modifier = Modifier.width(130.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (rawText.isNotEmpty()) {
                                    onAddInboxMessage(senderName, rawText)
                                    rawText = ""
                                }
                            },
                            enabled = rawText.isNotEmpty() && !isExtracting,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isExtracting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Extract", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset prompts
                    Text(
                         text = "Or choose mock presets to test AI flow:",
                         fontSize = 10.sp,
                         color = Color.Gray,
                         fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        senderName = preset.first
                                        rawText = preset.second
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(preset.first, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Structured Pending Feeds ---
        item {
            Text(
                text = "Extracted Action Items Queue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (isExtracting && activeInbox.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analyzing unstructured message contents...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        if (activeInbox.isEmpty() && !isExtracting) {
            item {
                EmptyStateView(
                    title = "Inbox Clean",
                    message = "All text data logs have been analyzed. Great job!",
                    icon = Icons.Default.MarkChatRead
                )
            }
        } else {
            items(activeInbox) { item ->
                InboxActionCard(
                    item = item,
                    onAccept = { onApplyAction(item.id) },
                    onDecline = { onDismissItem(item.id) }
                )
            }
        }
    }
}

@Composable
fun InboxActionCard(
    item: FamilyInboxItem,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    HomePilotCard(backgroundColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "From: ${item.senderName}",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Smart Proposed Card",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Text copy
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Text(
                    text = item.content,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Extracted AI Action items
            Box(
                 modifier = Modifier
                     .fillMaxWidth()
                     .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                     .padding(10.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                        Text("Action Proposed by HomePilot:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.extractedAction,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Accept / Decline button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDecline) {
                    Text("Dismiss", color = Color.Gray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAccept,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add details & Settle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


