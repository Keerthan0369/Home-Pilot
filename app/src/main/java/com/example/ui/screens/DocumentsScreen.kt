package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DocumentItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HomePilotCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    documents: List<DocumentItem>,
    isBiometricUnlocked: Boolean,
    isAiExtracting: Boolean,
    onAddDocument: (title: String, category: String, rawText: String, isProtected: Boolean) -> Unit,
    onDeleteDocument: (Int) -> Unit,
    onUnlockPIN: (String) -> Boolean,
    onLockVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showScanSheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") } // Aadhaar, PAN, Passport, Insurance, Certificates, Medical, Utility...
    var activeProtectedDocId by remember { mutableStateOf<Int?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }
    var viewingDocDetail by remember { mutableStateOf<DocumentItem?>(null) }

    val categories = listOf("All", "Aadhaar", "PAN", "Passport", "Insurance", "Vehicle", "Certificates", "Medical", "Utility")

    val filteredDocs = if (selectedCategory == "All") {
        documents
    } else {
        documents.filter { it.category == selectedCategory }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showScanSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Document")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // --- 1. Header Vault status ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Encrypted Document Vault",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "AI extracted metadata & biometric safeguards.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Lock/Unlock indicator
                IconButton(
                    onClick = {
                        if (isBiometricUnlocked) {
                            onLockVault()
                        } else {
                            showPinDialog = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isBiometricUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = "Safe state",
                        tint = if (isBiometricUnlocked) Color(0xFF1B6A4E) else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Category Scroll Pills ---
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.take(5).forEach { cat ->
                    val isChecked = selectedCategory == cat
                    FilterChip(
                        selected = isChecked,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. AI Extraction Loader ---
            AnimatedVisibility(visible = isAiExtracting) {
                Card(
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                     modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "HomePilot AI: Extracting parameters & generating summary...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // --- 4. Document Items List ---
            if (filteredDocs.isEmpty()) {
                EmptyStateView(
                    title = "Vault Secure & Empty",
                    message = "No documents archived. Tap the Scanner floating button below to import & scan identity records.",
                    icon = Icons.Default.LibraryBooks
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDocs) { doc ->
                        DocumentItemCard(
                            doc = doc,
                            isUnlocked = isBiometricUnlocked,
                            onClick = {
                                if (doc.isBiometricProtected && !isBiometricUnlocked) {
                                    activeProtectedDocId = doc.id
                                    showPinDialog = true
                                } else {
                                    viewingDocDetail = doc
                                }
                            },
                            onDelete = { onDeleteDocument(doc.id) }
                        )
                    }
                }
            }
        }

        // --- Simulated Capture sheet ---
        if (showScanSheet) {
            SimulatedScanDialog(
                onDismiss = { showScanSheet = false },
                onScanCompleted = { title, cat, rawText, isProtected ->
                    onAddDocument(title, cat, rawText, isProtected)
                    showScanSheet = false
                }
            )
        }

        // --- PIN input dialog ---
        if (showPinDialog) {
            PinInputDialog(
                onDismiss = { 
                    showPinDialog = false
                    activeProtectedDocId = null
                },
                onSubmitPin = { pin ->
                    val success = onUnlockPIN(pin)
                    if (success) {
                        showPinDialog = false
                        if (activeProtectedDocId != null) {
                            val target = documents.find { it.id == activeProtectedDocId }
                            if (target != null) {
                                viewingDocDetail = target
                            }
                        }
                    }
                    activeProtectedDocId = null
                    success
                }
            )
        }

        // --- Document Detailed Drawer ---
        if (viewingDocDetail != null) {
            DocumentDetailDialog(
                doc = viewingDocDetail!!,
                onDismiss = { viewingDocDetail = null }
            )
        }
    }
}

@Composable
fun DocumentItemCard(
    doc: DocumentItem,
    isUnlocked: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val needsAuth = doc.isBiometricProtected && !isUnlocked

    HomePilotCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (doc.category) {
                        "Passport" -> Icons.Default.AirplanemodeActive
                        "Insurance" -> Icons.Default.Shield
                        "Medical" -> Icons.Default.MedicalServices
                        else -> Icons.Default.Fingerprint
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (doc.isBiometricProtected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = if (isUnlocked) Color.Gray else Color(0xFFC62828),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = "Category: ${doc.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                if (!needsAuth && doc.summary.isNotEmpty()) {
                    Text(
                        text = doc.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (needsAuth) {
                    Text(
                        text = "🔒 Biometric PIN code required to review document summary.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun SimulatedScanDialog(
    onDismiss: () -> Unit,
    onScanCompleted: (title: String, category: String, rawText: String, isProtected: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Aadhaar") }
    var isProtected by remember { mutableStateOf(false) }
    var textSnippet by remember { mutableStateOf("") }

    val mockSnippets = mapOf(
        "Aadhaar" to "GOVERNMENT OF INDIA. UID NUMBER: 4281 9283 1928.\nHOLDER: Rajesh Sharma. Address: 501, Wing C, Orchid Heights, Mumbai. Date of Birth: 05/11/1982. This document is authenticated by UIDAI.",
        "PAN" to "INCOME TAX DEPARTMENT, GOVT OF INDIA.\nPermanent Account Number: BPRPS1928L.\nHOLDER: Savitri Devi Sharma. Father's Name: Madhukar Sharma. DOB: 12/04/1954.",
        "Passport" to "REPUBLIC OF INDIA PASSPORT. Document No: Z9182736.\nSurname: SHARMA. Given Names: SAVITRI DEVI. DOB: 12/04/1954. Place of Issue: MUMBAI. Expiry: 15/09/2029.",
        "Insurance" to "HDFC ERGO HEALTH POLICY. Policy No: 1029281-2291.\nPlan: Optima Super Secure. Sum Insured: INR 10,00,000. Covered Policy Holders: Rajesh, Anjali, Aarav, Dadi.",
        "Vehicle" to "REGISTRATION CERTIFICATE (R.C.)\nState of Maharashtra. Vehicle No: MH 02 CD 9912. Model: Honda City 1.5 i-VTEC. Registered owner: Rajesh Sharma."
    )

    // Update snippet automatically when type changes to make testing effortless
    LaunchedEffect(selectedCategory) {
        textSnippet = mockSnippets[selectedCategory] ?: ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Simulate High-Fidelity OCR Scanner", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Configure scan credentials. Hit Complete to trigger Gemini AI summary extraction.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title *") },
                    placeholder = { Text("e.g. Papa's PAN Card copy") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Document Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Aadhaar", "PAN", "Passport", "Insurance", "Vehicle").forEach { cat ->
                        val active = selectedCategory == cat
                        Button(
                            onClick = { selectedCategory = cat },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(cat, fontSize = 10.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = textSnippet,
                    onValueChange = { textSnippet = it },
                    label = { Text("Raw TEXT (Extracted via OCR Mock)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                Row(
                     verticalAlignment = Alignment.CenterVertically,
                     horizontalArrangement = Arrangement.SpaceBetween,
                     modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Biometric Safelock", fontSize = 13.sp)
                    Switch(checked = isProtected, onCheckedChange = { isProtected = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty()) {
                        onScanCompleted(title, selectedCategory, textSnippet, isProtected)
                    }
                },
                enabled = title.isNotEmpty()
            ) {
                Text("Process Scan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PinInputDialog(
    onDismiss: () -> Unit,
    onSubmitPin: (String) -> Boolean
) {
    var pin by remember { mutableStateOf("") }
    var err by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Biometric Shield PIN", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter your 4-digit security PIN to unlock sensitive document archives. For simulation, enter '1234'.", fontSize = 12.sp)
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            pin = it
                            err = false
                        }
                    },
                    label = { Text("4-digit PIN") },
                    singleLine = true,
                    isError = err,
                    modifier = Modifier.fillMaxWidth()
                )
                if (err) {
                    Text("Incorrect security code. Please try again.", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pass = onSubmitPin(pin)
                    if (!pass) {
                        err = true
                    }
                }
            ) {
                Text("Verify passcode")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DocumentDetailDialog(
    doc: DocumentItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(doc.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Document Category: ${doc.category}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Divider()

                Text("HomePilot AI Extracted Summary:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(
                    text = doc.summary.ifEmpty { "Summary and structure analyzing in background." },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Divider()

                Text("Raw OCR Content Text:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Card(
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = doc.scannedText.ifEmpty { "No physical text recognized." },
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close details")
            }
        }
    )
}
