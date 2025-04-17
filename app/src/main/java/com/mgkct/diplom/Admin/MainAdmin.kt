package com.mgkct.diplom.admin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mgkct.diplom.R
import com.mgkct.diplom.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "main_admin") {
                composable(
                    "main_admin/{full_name}/{center_name}",
                    arguments = listOf(
                        navArgument("full_name") { type = NavType.StringType },
                        navArgument("center_name") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val fullName = backStackEntry.arguments?.getString("full_name") ?: ""
                    val centerName = backStackEntry.arguments?.getString("center_name") ?: ""
                    MainAdminScreen(navController, fullName, centerName)
                }
            }
        }
    }
}

@Composable
fun MainAdminScreen(
    navController: NavController,
    fullName: String,
    centerName: String
) {
    // Состояние для хранения id центра
    var centerId by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Запускаем эффект для получения id центра
    LaunchedEffect(centerName) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("MainAdminScreen", "Trying to fetch polyclinics for center: $centerName")
                val polyclinics = RetrofitInstance.api.getPolyclinics()

                // Логируем полученные данные
                Log.d("MainAdminScreen", "Received polyclinics: ${polyclinics.joinToString { it.center_name }}")

                val center = polyclinics.find {
                    it.center_name.equals(centerName, ignoreCase = true)
                }

                if (center != null) {
                    centerId = center.id_center
                    Log.d("MainAdminScreen", "Success! Center '$centerName' found with id: $centerId")
                } else {
                    errorMessage = "Center '$centerName' not found in polyclinics list"
                    Log.w("MainAdminScreen", errorMessage!!)
                }
            } catch (e: Exception) {
                errorMessage = "Error fetching polyclinics: ${e.message}"
                Log.e("MainAdminScreen", errorMessage!!, e)
            }
        }
    }

    val medicalCenter = centerName
    val totalAppointments = 35
    val totalDoctors = 134
    val doctorsOnShift = 78
    val sickDoctors = 6

    val registeredPatients = 120
    val avgWaitingTime = "15 минут"

    val dailyIncome = "2500 BYN"
    val paidServices = 48
    val freeServices = 165

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
                            Text(fullName)
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
                                text = { Text("Управление стац лечением") },
                                onClick = { navController.navigate("") },
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
                        "Среднее время приема: $avgWaitingTime"
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Финансовая статистика
                InfoSection(
                    title = "💰 Финансовая статистика",
                    items = listOf(
                        "Доход за день: $dailyIncome",
                        "Оплаченные услуги: $paidServices",
                        "Бесплатные услуги: $freeServices"
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
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
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
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainAdminScreen() {
    MainAdminScreen(
        navController = rememberNavController(),
        fullName = "Иванов Сергей Васильевич",
        centerName = "ЛОДЭ"
    )
}