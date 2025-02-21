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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.mgkct.diplom.MainDoctor
import com.mgkct.diplom.Polyclinic
import com.mgkct.diplom.R
import com.mgkct.diplom.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AddMainDoctorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            AddMainDoctorScreen(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMainDoctorScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<MainDoctor?>(null) }
    var doctorToEdit by remember { mutableStateOf<MainDoctor?>(null) }
    val doctorsList = remember { mutableStateListOf<MainDoctor>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope() // Add the coroutine scope here



    LaunchedEffect(Unit) {
        try {
            val doctors = RetrofitInstance.api.getMainDoctors()
            doctorsList.clear()
            doctorsList.addAll(doctors)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val filteredDoctors = doctorsList.filter {
        (it.full_name ?: "").contains(searchQuery, ignoreCase = true) ||
                (it.email ?: "").contains(searchQuery, ignoreCase = true) ||
                (it.center_name ?: "").contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text("Управление главными врачами") },
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
                Icon(Icons.Default.Add, contentDescription = "Добавить врача")
            }
        }

    ) { paddingValues ->

        // Background Image
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.background_image),
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск главного врача") },
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

                if (filteredDoctors.isEmpty() && searchQuery.isNotEmpty()) {
                    // Show message and GIF if no results found
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            "Ничего не найдено",
                            style = MaterialTheme.typography.headlineSmall, // Увеличиваем шрифт
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center // Выравнивание по центру
                        )
                        Image(
                            painter = rememberImagePainter(R.drawable.undefined), // Используйте имя вашего GIF-файла
                            contentDescription = "Animated GIF",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(300.dp) // Увеличиваем размер GIF
                        )
                    }
                } else {
                    LazyColumn {
                        items(filteredDoctors) { doctor ->
                            MainDoctorItem(
                                doctor,
                                onDelete = { showDeleteDialog = doctor },
                                onEdit = { doctorToEdit = it }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

            }
        }
    }

    if (showDialog || doctorToEdit != null) {
        AddOrEditDoctorDialog(
            doctor = doctorToEdit,
            onDismiss = {
                showDialog = false
                doctorToEdit = null
            },
            onSaveDoctor = { doctor ->
                showDialog = false
                doctorToEdit = null
                coroutineScope.launch {
                    try {
                        val doctors = RetrofitInstance.api.getMainDoctors()
                        doctorsList.clear()
                        doctorsList.addAll(doctors)
                        snackbarHostState.showSnackbar(
                            if (doctor.id == 0) "Врач ${doctor.full_name} успешно добавлен" else "Информация о враче ${doctor.full_name} обновлена"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        snackbarHostState.showSnackbar("Ошибка при сохранении данных врача")
                    }
                }
            }

        )
    }

    if (showDeleteDialog != null) {
        ConfirmDeleteDialog(
            doctor = showDeleteDialog!!,
            onDismiss = { showDeleteDialog = null },
            onConfirmDelete = {
                val deletedDoctor = showDeleteDialog
                doctorsList.remove(showDeleteDialog)
                showDeleteDialog = null
                // Показать Snackbar после удаления
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Доктор с ФИО: ${deletedDoctor?.full_name} был удален")
                }
            }
        )
    }

}

@Composable
fun ConfirmDeleteDialog(doctor: MainDoctor, onDismiss: () -> Unit, onConfirmDelete: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val doctorsList = remember { mutableStateListOf<MainDoctor>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Подтверждение удаления") },
        text = { Text("Вы уверены, что хотите удалить врача ${doctor.full_name}?") },
        confirmButton = {
            Button(onClick = {
                coroutineScope.launch {
                    try {
                        // Deleting doctor
                        RetrofitInstance.api.deleteDoctor(doctor.id)
                        // Update doctors list after deletion
                        try {
                            val doctors = RetrofitInstance.api.getMainDoctors()
                            // Update doctors list
                            doctorsList.clear()
                            doctorsList.addAll(doctors)
                        } catch (e: Exception) {
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
fun AddOrEditDoctorDialog(doctor: MainDoctor?, onDismiss: () -> Unit, onSaveDoctor: (MainDoctor) -> Unit) {
    var name by remember { mutableStateOf(doctor?.full_name ?: "") }
    var email by remember { mutableStateOf(doctor?.email ?: "") }
    var password by remember { mutableStateOf(doctor?.password ?: "") }
    var address by remember { mutableStateOf(doctor?.address ?: "") } // Добавляем поле для адреса
    var selectedPolyclinic by remember { mutableStateOf(doctor?.center_name ?: "") }
    var medCenterId by remember { mutableStateOf(doctor?.med_center_id ?: 0) }

    val polyclinics = remember { mutableStateListOf<Polyclinic>() }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.api.getPolyclinics()
            polyclinics.clear()
            polyclinics.addAll(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (doctor == null) "Добавить врача" else "Редактировать врача") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("ФИО") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Пароль") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Адрес") }) // Поле для адреса
                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown для поликлиники
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedPolyclinic,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Поликлиника") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        polyclinics.forEach { polyclinic ->
                            DropdownMenuItem(
                                text = { Text(polyclinic.center_name) },
                                onClick = {
                                    selectedPolyclinic = polyclinic.center_name
                                    // Теперь айди берется из поликлиники
                                    medCenterId = polyclinic.id_center
                                    expanded = false
                                }
                            )
                        }
                    }

                }
            }
        },
        confirmButton = {
            Button(
                onClick = {

                    val newDoctor = MainDoctor(
                        id = doctor?.id ?: 0, // Если нового врача, то id = 0
                        full_name = name,
                        email = email,
                        center_name = selectedPolyclinic,
                        password = password,
                        address = address, // Добавляем адрес
                        med_center_id = medCenterId // Добавляем med_center_id
                    )
                    if (doctor == null) {
                        // Добавление нового врача
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = RetrofitInstance.api.addDoctor(newDoctor)
                                if (response.isSuccessful) {
                                    onSaveDoctor(newDoctor)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        // Обновление данных врача
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = RetrofitInstance.api.updateDoctor(doctor.id, newDoctor)
                                if (response.isSuccessful) {
                                    onSaveDoctor(newDoctor)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
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
fun MainDoctorItem(doctor: MainDoctor, onDelete: () -> Unit, onEdit: (MainDoctor) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ФИО: "+ doctor.full_name, style = MaterialTheme.typography.titleMedium)
                Text("Email: "+doctor.email, style = MaterialTheme.typography.bodyMedium)
                Text("Поликлиника: " + doctor.center_name, style = MaterialTheme.typography.bodyMedium)
                Text("Адрес: " + doctor.address, style = MaterialTheme.typography.bodyMedium) // Добавляем адрес
                Text("Пароль: " + "*".repeat(doctor.password.orEmpty().length), style = MaterialTheme.typography.bodyMedium)
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ){
                IconButton(onClick = { onEdit(doctor) }) {
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
fun PreviewScreen() {
    val navController = rememberNavController()
    AddMainDoctorScreen(navController)
}
