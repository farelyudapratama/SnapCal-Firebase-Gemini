package com.yuch.snapcalfirebasegemini.ui.components // Sesuaikan jika path package-mu berbeda

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yuch.snapcalfirebasegemini.R
import java.util.Locale

@Composable
fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
}

@Composable
fun TagSelectionSection(
    title: String,
    options: List<String>,
    selectedItems: List<String>,
    customItems: List<String> = emptyList(),
    onToggle: (String) -> Unit,
    onAddCustom: (String) -> Unit,
    getLocalizedText: (String) -> Int,
    description: String? = null
) {
    var customInput by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title)
        description?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
        _root_ide_package_.com.yuch.snapcalfirebasegemini.view.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(
                8.dp
            ), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tampilkan predefined options
            options.forEach { item ->
                TagChip(
                    stringResource(getLocalizedText(item)),
                    selectedItems.contains(item)
                ) { onToggle(item) }
            }
            // Tampilkan custom items
            customItems.forEach { item ->
                TagChip(
                    item.replaceFirstChar { it.titlecase(Locale.getDefault()) },
                    selectedItems.contains(item)
                ) { onToggle(item) }
            }
        }
        OutlinedTextField(
            value = customInput,
            onValueChange = { customInput = it },
            label = { Text(stringResource(R.string.add_other)) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    if (customInput.isNotBlank()) {
                        onAddCustom(customInput.trim())
                        customInput = ""
                    }
                }) {
                    Icon(Icons.Default.Add, stringResource(R.string.add))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(FilterChipDefaults.IconSize)) } } else null
    )
}

@Composable
fun SelectableCard(title: String, subtitle: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, "Selected", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}