package com.example.cellplating

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CellPlatingRecipePage()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CellPlatingRecipePage() {
    val focusManager = LocalFocusManager.current

    var cellsHarvested by remember { mutableStateOf("") }
    var cellsVolume by remember { mutableStateOf("") }
    var selectedFlask by remember { mutableStateOf("T75") }
    var customArea by remember { mutableStateOf("") }
    var showCustomDialog by remember { mutableStateOf(false) }

    // Single source of truth: only per cm² values are editable
    var cellsPerCm2 by remember { mutableStateOf("0.028") }
    var mediaPerCm2 by remember { mutableStateOf("0.2") }

    val flaskAreas = mapOf(
        "T25" to 25,
        "T75" to 75,
        "T175" to 175,
        "T225" to 225
    )

    val flaskArea = when (selectedFlask) {
        "Custom" -> customArea.toIntOrNull() ?: 0
        else -> flaskAreas[selectedFlask] ?: 75
    }

    // Computed values (read-only)
    val cellsPerCm2Value = cellsPerCm2.toDoubleOrNull() ?: 0.0
    val mediaPerCm2Value = mediaPerCm2.toDoubleOrNull() ?: 0.0

    val cellsPerFlask = cellsPerCm2Value * flaskArea
    val mediaPerFlask = mediaPerCm2Value * flaskArea

    // Calculate recipe values
    val harvested = cellsHarvested.toDoubleOrNull() ?: 0.0
    val volume = cellsVolume.toDoubleOrNull() ?: 0.0

    val cellSuspensionVolume = if (harvested > 0 && cellsPerFlask > 0) {
        (cellsPerFlask / harvested) * volume
    } else {
        0.0
    }

    val mediaVolume = mediaPerFlask - cellSuspensionVolume

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cell Plating Recipe",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Cell Suspension Specification
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Cell Suspension Specification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = cellsHarvested,
                        onValueChange = { cellsHarvested = it },
                        label = { Text("Cells Harvested (millions)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cellsVolume,
                        onValueChange = { cellsVolume = it },
                        label = { Text("Cells Volume (mL)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Plating Specification
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Plating Specification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Flask Selection
                    Text(
                        text = "Flask Selection",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        flaskAreas.keys.forEach { flask ->
                            FilterChip(
                                selected = selectedFlask == flask,
                                onClick = { selectedFlask = flask },
                                label = { Text(flask) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFlask == "Custom",
                            onClick = {
                                selectedFlask = "Custom"
                                showCustomDialog = true
                            },
                            label = {
                                Text(if (selectedFlask == "Custom" && customArea.isNotEmpty()) {
                                    "Custom (${customArea} cm²)"
                                } else {
                                    "Custom"
                                })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Seeding Parameters
                    Text(
                        text = "Seeding Parameters",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    DensityTable(
                        cellsPerCm2 = cellsPerCm2,
                        onCellsPerCm2Change = { cellsPerCm2 = it },
                        mediaPerCm2 = mediaPerCm2,
                        onMediaPerCm2Change = { mediaPerCm2 = it },
                        cellsPerFlask = cellsPerFlask,
                        mediaPerFlask = mediaPerFlask,
                        focusManager = focusManager
                    )
                }
            }

            // Recipe
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Recipe",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    RecipeTable(
                        cellSuspensionVolume = cellSuspensionVolume,
                        mediaVolume = mediaVolume
                    )
                }
            }
        }

        // Custom Area Dialog
        if (showCustomDialog) {
            CustomFlaskDialog(
                currentValue = customArea,
                onDismiss = {
                    showCustomDialog = false
                    if (customArea.isEmpty()) {
                        selectedFlask = "T75"
                    }
                },
                onConfirm = { value ->
                    customArea = value
                    showCustomDialog = false
                }
            )
        }
    }
}

@Composable
fun CustomFlaskDialog(
    currentValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Flask Area") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter the surface area in cm²:")
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Surface Area (cm²)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value) },
                enabled = value.isNotEmpty() && value.toIntOrNull() != null && value.toInt() > 0
            ) {
                Text("OK")
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
fun DensityTable(
    cellsPerCm2: String,
    onCellsPerCm2Change: (String) -> Unit,
    mediaPerCm2: String,
    onMediaPerCm2Change: (String) -> Unit,
    cellsPerFlask: Double,
    mediaPerFlask: Double,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
    ) {
        // Header Row
        Row(modifier = Modifier.fillMaxWidth()) {
            TableCell(
                text = "",
                isHeader = true,
                modifier = Modifier.weight(1f)
            )
            TableCell(
                text = "per cm²",
                isHeader = true,
                modifier = Modifier.weight(1f)
            )
            TableCell(
                text = "per flask",
                isHeader = true,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        // M cells Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableCell(
                text = "M cells",
                isHeader = true,
                modifier = Modifier.weight(1f)
            )
            EditableTableCell(
                value = cellsPerCm2,
                onValueChange = onCellsPerCm2Change,
                modifier = Modifier.weight(1f),
                focusManager = focusManager,
                isLast = false
            )
            ReadOnlyTableCell(
                value = cellsPerFlask,
                decimalPlaces = 1,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        // ml media Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableCell(
                text = "ml media",
                isHeader = true,
                modifier = Modifier.weight(1f)
            )
            EditableTableCell(
                value = mediaPerCm2,
                onValueChange = onMediaPerCm2Change,
                modifier = Modifier.weight(1f),
                focusManager = focusManager,
                isLast = true
            )
            ReadOnlyTableCell(
                value = mediaPerFlask,
                decimalPlaces = 1,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RecipeTable(
    cellSuspensionVolume: Double,
    mediaVolume: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
    ) {
        // Header Row
        Row(modifier = Modifier.fillMaxWidth()) {
            TableCell(
                text = "Component",
                isHeader = true,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            TableCell(
                text = "Volume (mL)",
                isHeader = true,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        // Cell suspension Row
        Row(modifier = Modifier.fillMaxWidth()) {
            TableCell(
                text = "Cell Suspension",
                isHeader = false,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            TableCell(
                text = String.format("%.1f", cellSuspensionVolume),
                isHeader = false,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        // Media Row
        Row(modifier = Modifier.fillMaxWidth()) {
            TableCell(
                text = "Media",
                isHeader = false,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            TableCell(
                text = String.format("%.1f", maxOf(0.0, mediaVolume)),
                isHeader = false,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun TableCell(
    text: String,
    isHeader: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    Box(
        modifier = modifier.padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = color
        )
    }
}

@Composable
fun EditableTableCell(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusManager: androidx.compose.ui.focus.FocusManager,
    isLast: Boolean
) {
    Box(
        modifier = modifier.padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = if (isLast) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )
    }
}

@Composable
fun ReadOnlyTableCell(
    value: Double,
    decimalPlaces: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format("%.${decimalPlaces}f", value),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Normal
        )
    }
}