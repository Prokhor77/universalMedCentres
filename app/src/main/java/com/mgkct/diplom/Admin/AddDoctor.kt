package com.mgkct.diplom.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.mgkct.diplom.Doctor
import com.mgkct.diplom.R
import com.mgkct.diplom.RetrofitInstance
import com.mgkct.diplom.WorkSection
import com.mgkct.diplom.WorkType
import kotlinx.coroutines.launch

class AddDoctorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            AddDoctorScreen(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDoctorScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Doctor?>(null) }
    var doctorToEdit by remember { mutableStateOf<Doctor?>(null) }
    val doctorsList = remember { mutableStateListOf<Doctor>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val doctors = RetrofitInstance.api.getDoctors()
                doctorsList.clear()
                doctorsList.addAll(doctors)
            } catch (e: Exception) {
                e.printStackTrace()
                snackbarHostState.showSnackbar("Ошибка при загрузке списка врачей")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Управление врачами") },
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
                    label = { Text("Поиск врача") },
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

                if (doctorsList.isEmpty() && searchQuery.isNotEmpty()) {
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
                            painter = painterResource(R.drawable.undefined),
                            contentDescription = "Animated GIF",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(300.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(doctorsList) { doctor ->
                            DoctorItem(doctor, onDelete = { showDeleteDialog = doctor }, onEdit = { doctorToEdit = it })
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
                        // Логика сохранения врача
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
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Доктор с ФИО: ${deletedDoctor?.full_name} был удален")
                }
            }
        )
    }
}

@Composable
fun ConfirmDeleteDialog(doctor: Doctor, onDismiss: () -> Unit, onConfirmDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Подтверждение удаления") },
        text = { Text("Вы уверены, что хотите удалить врача ${doctor.full_name}?") },
        confirmButton = {
            Button(onClick = {
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
fun AddOrEditDoctorDialog(
    doctor: Doctor?,
    onDismiss: () -> Unit,
    onSaveDoctor: (Doctor) -> Unit
) {
    var name by remember { mutableStateOf(doctor?.full_name ?: "") }
    var email by remember { mutableStateOf(doctor?.email ?: "") }
    var password by remember { mutableStateOf(doctor?.password ?: "") }
    var address by remember { mutableStateOf(doctor?.address ?: "") }
    var workType by remember { mutableStateOf(doctor?.work_type_description ?: "") }
    var workSection by remember { mutableStateOf(doctor?.work_section_description ?: "") }

    val coroutineScope = rememberCoroutineScope()
    val workTypes = remember { mutableStateListOf<WorkType>() }
    val workSections = remember { mutableStateListOf<WorkSection>() }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                workTypes.addAll(RetrofitInstance.api.getWorkTypes())
                workSections.addAll(RetrofitInstance.api.getWorkSections())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val categoryOptions = workTypes.map { it.type_description }
    val accessOptions = workSections.map { it.section_description }

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
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Адрес") })
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {}
                ) {
                    OutlinedTextField(
                        value = workType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Тип работы") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                        },
                        modifier = Modifier.menuAnchor()
                    )
                    DropdownMenu(
                        expanded = false,
                        onDismissRequest = {}
                    ) {
                        categoryOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    workType = option
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {}
                ) {
                    OutlinedTextField(
                        value = workSection,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Раздел работы") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                        },
                        modifier = Modifier.menuAnchor()
                    )
                    DropdownMenu(
                        expanded = false,
                        onDismissRequest = {}
                    ) {
                        accessOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    workSection = option
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
                    val newDoctor = Doctor(
                        id = doctor?.id ?: 0,
                        full_name = name,
                        email = email,
                        password = password,
                        center_name = doctor?.center_name ?: "",
                        med_center_id = doctor?.med_center_id ?: 0,
                        address = address,
                        work_type_description = workType,
                        work_section_description = workSection
                    )
                    onSaveDoctor(newDoctor)
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
fun DoctorItem(doctor: Doctor, onDelete: () -> Unit, onEdit: (Doctor) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f) // Занимает оставшееся пространство
            ) {
                Text("ФИО: ${doctor.full_name}", style = MaterialTheme.typography.titleMedium)
                Text("Email: ${doctor.email}", style = MaterialTheme.typography.bodyMedium)
                Text("Адрес: ${doctor.address}", style = MaterialTheme.typography.bodyMedium)
                Text("Тип работы: ${doctor.work_type_description}", style = MaterialTheme.typography.bodyMedium)
                Text("Раздел работы: ${doctor.work_section_description}", style = MaterialTheme.typography.bodyMedium)
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
    AddDoctorScreen(navController)
}