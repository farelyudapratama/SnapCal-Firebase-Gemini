package com.yuch.snapcalfirebasegemini.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditFoodScreen(
    foodId: String,
    onSave: (String, Int, Int, Int) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf(0) }
    var protein by remember { mutableStateOf(0) }
    var fat by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Food Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = carbs.toString(),
            onValueChange = { carbs = it.toIntOrNull() ?: 0 },
            label = { Text("Carbs (g)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = protein.toString(),
            onValueChange = { protein = it.toIntOrNull() ?: 0 },
            label = { Text("Protein (g)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = fat.toString(),
            onValueChange = { fat = it.toIntOrNull() ?: 0 },
            label = { Text("Fat (g)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { onSave(name, carbs, protein, fat) }) {
                Text("Save")
            }
            Button(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}