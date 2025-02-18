package com.mgkct.diplom.Admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mgkct.diplom.R

class AddDoctorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "add_doctor") {
                composable("add_doctor") { AddDoctorScreen(navController) }
            }
        }
    }
}

@Composable
fun AddDoctorScreen(navController: NavController) {
    var doctorName by remember { mutableStateOf("") }
    var doctorPhone by remember { mutableStateOf("") }
    var doctorEmail by remember { mutableStateOf("") }
    var doctorAddress by remember { mutableStateOf("") }
    var doctorExperience by remember { mutableStateOf("") }
    var doctorCategory by remember { mutableStateOf("") }
    var accessRights by remember { mutableStateOf(listOf<String>()) }
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedAccess by remember { mutableStateOf(false) }

    val categoryOptions = listOf("Терапевт", "Хирург", "Педиатр", "Кардиолог")
    val accessOptions = listOf("Регистрация пациентов", "Модерация записей", "Назначение лечения/анализов", "Передача пациентов", "Управление расписанием")

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = "Фоновое изображение",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        @OptIn(ExperimentalMaterial3Api::class)
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Добавить врача") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("ФИО врача") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = doctorPhone,
                    onValueChange = { doctorPhone = it },
                    label = { Text("Телефон врача") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = doctorEmail,
                    onValueChange = { doctorEmail = it },
                    label = { Text("Email врача") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = doctorAddress,
                    onValueChange = { doctorAddress = it },
                    label = { Text("Адрес врача") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = doctorExperience,
                    onValueChange = { doctorExperience = it },
                    label = { Text("Опыт работы") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Выпадающее меню для выбора категории врача
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = doctorCategory,
                        onValueChange = { doctorCategory = it },
                        label = { Text("Категория врача") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedCategory = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Выбрать категорию")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categoryOptions.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    doctorCategory = category
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Выпадающее меню для выбора прав доступа
                // Выпадающее меню для выбора прав доступа
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = accessRights.joinToString(", "),
                        onValueChange = {},
                        label = { Text("Права доступа") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            Row {
                                // Кнопка очистки поля ввода прав
                                IconButton(onClick = { accessRights = listOf() }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Очистить права доступа")
                                }
                                // Иконка для открытия меню выбора прав
                                IconButton(onClick = { expandedAccess = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Выбрать права доступа")
                                }
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandedAccess,
                        onDismissRequest = { expandedAccess = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        accessOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    // Toggle access right selection
                                    if (accessRights.contains(option)) {
                                        accessRights = accessRights.filterNot { it == option }
                                    } else {
                                        accessRights = accessRights + option
                                    }
                                    expandedAccess = false
                                }
                            )
                        }
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (doctorName.isNotEmpty() && doctorPhone.isNotEmpty() && doctorEmail.isNotEmpty() && doctorAddress.isNotEmpty() && doctorExperience.isNotEmpty() && doctorCategory.isNotEmpty() && accessRights.isNotEmpty()) {
                            // Сюда добавить логику добавления врача
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Добавить врача")
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Кнопка для очистки полей
                Button(
                    onClick = {
                        doctorName = ""
                        doctorPhone = ""
                        doctorEmail = ""
                        doctorAddress = ""
                        doctorExperience = ""
                        doctorCategory = ""
                        accessRights = listOf()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Очистить все поля")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AddDoctorScreen(navController = rememberNavController())
}
