package com.mgkct.diplom.SudoAdmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.mgkct.diplom.Clinic
import com.mgkct.diplom.R
import com.mgkct.diplom.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddMedCenterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            AddMedCenterScreen(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedCenterScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Clinic?>(null) }
    var medCenterToEdit by remember { mutableStateOf<Clinic?>(null) }
    val medCentersList = remember { mutableStateListOf<Clinic>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val medCenters = RetrofitInstance.api.getMedCenters()
            medCentersList.clear()
            medCentersList.addAll(medCenters)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val filteredMedCenters = medCentersList.filter {
        it.center_name.contains(searchQuery, ignoreCase = true) ||
                it.center_address.contains(searchQuery, ignoreCase = true) ||
                it.center_description.contains(searchQuery, ignoreCase = true)||
                it.center_number.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Управление мед. центрами") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить мед. центр")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.background_image),
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Column(modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск мед. центра") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Очистить")
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (filteredMedCenters.isEmpty() && searchQuery.isNotEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            "Ничего не найдено",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Image(
                            painter = rememberImagePainter(R.drawable.undefined),
                            contentDescription = "Animated GIF",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(300.dp)
                        )
                    }
                } else {
                    LazyColumn {
                        items(filteredMedCenters) { clinic ->
                            MedCenterItem(
                                clinic,
                                onDelete = { showDeleteDialog = clinic },
                                onEdit = { medCenterToEdit = clinic }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    if (showDialog || medCenterToEdit != null) {
        AddOrEditMedCenterDialog(
            medCenter = medCenterToEdit,
            onDismiss = {
                showDialog = false
                medCenterToEdit = null
            },
            onSaveMedCenter = { clinic ->
                showDialog = false
                medCenterToEdit = null
                coroutineScope.launch {
                    try {
                        val medCenters = RetrofitInstance.api.getMedCenters()
                        medCentersList.clear()
                        medCentersList.addAll(medCenters)
                        snackbarHostState.showSnackbar(
                            if (clinic.id_center == 0)
                                "Мед. центр ${clinic.center_name} успешно добавлен"
                            else
                                "Информация о мед. центре ${clinic.center_name} обновлена"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        snackbarHostState.showSnackbar("Ошибка при сохранении данных мед. центра")
                    }
                }
            }
        )
    }

    if (showDeleteDialog != null) {
        ConfirmDeleteMedCenterDialog(
            medCenter = showDeleteDialog!!,
            onDismiss = { showDeleteDialog = null },
            onConfirmDelete = {
                val deletedMedCenter = showDeleteDialog
                medCentersList.remove(showDeleteDialog)
                showDeleteDialog = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Мед. центр ${deletedMedCenter?.center_name} был удален")
                }
            }
        )
    }
}

@Composable
fun ConfirmDeleteMedCenterDialog(
    medCenter: Clinic,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val medCentersList = remember { mutableStateListOf<Clinic>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Подтверждение удаления") },
        text = { Text("Вы уверены, что хотите удалить мед. центр ${medCenter.center_name}?") },
        confirmButton = {
            Button(onClick = {
                coroutineScope.launch {
                    try {
                        RetrofitInstance.api.deleteMedCenter(medCenter.id_center)
                        try {
                            val centers = RetrofitInstance.api.getMedCenters()
                            medCentersList.clear()
                            medCentersList.addAll(medCentersList)
                        }catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                onConfirmDelete()
                onDismiss()
            }) {
                Text("Удалить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditMedCenterDialog(
    medCenter: Clinic?,
    onDismiss: () -> Unit,
    onSaveMedCenter: (Clinic) -> Unit
) {
    var name by remember { mutableStateOf(medCenter?.center_name ?: "") }
    var description by remember { mutableStateOf(medCenter?.center_description ?: "") }
    var address by remember { mutableStateOf(medCenter?.center_address ?: "") }
    var number by remember { mutableStateOf(medCenter?.center_number ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (medCenter == null) "Добавить мед. центр" else "Редактировать мед. центр") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название центра") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание центра") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Адрес центра") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Контактный номер") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newMedCenter = Clinic(
                        id_center = medCenter?.id_center ?: 0,
                        center_name = name,
                        center_description = description,
                        center_address = address,
                        center_number = number
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            if (medCenter == null) {
                                RetrofitInstance.api.addMedCenter(newMedCenter)
                            } else {
                                RetrofitInstance.api.updateMedCenter(medCenter.id_center, newMedCenter)
                            }
                            onSaveMedCenter(newMedCenter)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    onDismiss()
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun MedCenterItem(clinic: Clinic, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(text = "Название: ${clinic.center_name}", style = MaterialTheme.typography.titleMedium)
                Text(text = "Описание: ${clinic.center_description}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Адрес: ${clinic.center_address}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Телефон: ${clinic.center_number}", style = MaterialTheme.typography.bodyMedium)
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewAddMedCenterScreen() {
    AddMedCenterScreen(rememberNavController())
}