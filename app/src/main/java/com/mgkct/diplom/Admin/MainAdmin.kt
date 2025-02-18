package com.mgkct.diplom.Admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mgkct.diplom.R

class MainAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainAdminScreen(rememberNavController())
        }
    }
}

@Composable
fun MainAdminScreen(navController: NavController) {
    val medicalCenter = "ЛОДЭ"
    val totalAppointments = 35
    val totalDoctors = 134
    val doctorsOnShift = 78
    val sickDoctors = 6

    val registeredPatients = 120
    val avgWaitingTime = "15 минут"

    val dailyIncome = "2500 BYN"
    val paidServices = 48

    val newRequests = 5
    val complaints = 2

    var expandedMenu by remember { mutableStateOf(false) }

    val data = listOf(
        "Приемов за сегодня: $totalAppointments",
        "Общее кол-во врачей: $totalDoctors",
        "Врачей на смене: $doctorsOnShift",
        "Врачей на больничном: $sickDoctors"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = "Фон",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.mipmap.medical),
                                contentDescription = "Иконка",
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Иванов Сергей Васильевич")
                        }
                    },
                    actions = {
                        IconButton(onClick = { expandedMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                        }
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Главная") },
                                onClick = { navController.navigate("main_admin") },
                                leadingIcon = {
                                    Icon(Icons.Default.Home, contentDescription = "Главная")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Добавить врача") },
                                onClick = { navController.navigate("add_doctor") },
                                leadingIcon = {
                                    Icon(Icons.Default.AccountCircle, contentDescription = "Добавить врача")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Выйти") },
                                onClick = { navController.navigate("login_screen") },
                                leadingIcon = {
                                    Icon(Icons.Default.ExitToApp, contentDescription = "Выход")
                                }
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = medicalCenter,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(1.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(data) { text ->
                        InfoCard(text)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Статистика пациентов
                InfoSection(
                    title = "📊 Статистика пациентов",
                    items = listOf(
                        "Количество записанных пациентов: $registeredPatients",
                        "Среднее время ожидания: $avgWaitingTime"
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Финансовая статистика
                InfoSection(
                    title = "💰 Финансовая статистика",
                    items = listOf(
                        "Доход за день: $dailyIncome",
                        "Оплаченные услуги: $paidServices"
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Заявки и жалобы
                InfoSection(
                    title = "📩 Заявки и жалобы",
                    items = listOf(
                        "Новые заявки на рассмотрение: $newRequests",
                        "Жалобы от пациентов за неделю: $complaints"
                    )
                )
            }
        }
    }
}

@Composable
fun InfoSection(title: String, items: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        items.forEach { item ->
            Text(
                text = item,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp) // Увеличиваем размер шрифта
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun InfoCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(1.5f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp) // Увеличиваем размер шрифта
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainAdminScreen() {
    MainAdminScreen(navController = rememberNavController())
}
