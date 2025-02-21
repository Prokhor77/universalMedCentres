package com.mgkct.diplom.SudoAdmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.OffsetEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.mgkct.diplom.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

class AddAdmSudoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            AddAdmSudoScreen(navController)
        }
    }
}

data class Admin(
    val id: Int,
    val full_name: String,
    val email: String,
    val center_name: String,
    val password: String,
    val address: String,
    val med_center_id: Int
)

data class Polyclinics(
    val id_center: Int,
    val center_name: String
)

interface AdminApiService {
    @GET("admins")
    suspend fun getAdmins(): List<Admin>

    @POST("add-admin")
    suspend fun addAdmin(@Body admin: Admin): Response<ResponseBody>

    @PUT("update-admin/{id}")
    suspend fun updateAdmin(@Path("id") id: Int, @Body admin: Admin): Response<ResponseBody>

    @DELETE("delete-admin/{id}")
    suspend fun deleteAdmin(@Path("id") id: Int): Response<ResponseBody>

    @GET("polyclinics")
    suspend fun getPolyclinics(): List<Polyclinic>
}

object AdminRetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val api: AdminApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AdminApiService::class.java)
    }

    val medCenterApi: MedCenterApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MedCenterApiService::class.java)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAdmSudoScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Admin?>(null) }
    var adminToEdit by remember { mutableStateOf<Admin?>(null) }
    val adminsList = remember { mutableStateListOf<Admin>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val admins = AdminRetrofitInstance.api.getAdmins()
            adminsList.clear()
            adminsList.addAll(admins)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val filteredAdmins = adminsList.filter {
        it.full_name.orEmpty().contains(searchQuery, ignoreCase = true) ||
                it.email.orEmpty().contains(searchQuery, ignoreCase = true) ||
                it.center_name.orEmpty().contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Управление администраторами") },
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
                Icon(Icons.Default.Add, contentDescription = "Добавить администратора")
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
                    label = { Text("Поиск администратора") },
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

                if (filteredAdmins.isEmpty() && searchQuery.isNotEmpty()) {
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
                        items(filteredAdmins) { admin ->
                            AdminItem(
                                admin,
                                onDelete = { showDeleteDialog = admin },
                                onEdit = { adminToEdit = it }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    if (showDialog || adminToEdit != null) {
        AddOrEditAdminDialog(
            admin = adminToEdit,
            onDismiss = {
                showDialog = false
                adminToEdit = null
            },
            onSaveAdmin = { admin ->
                showDialog = false
                adminToEdit = null
                coroutineScope.launch {
                    try {
                        val admins = AdminRetrofitInstance.api.getAdmins()
                        adminsList.clear()
                        adminsList.addAll(admins)
                        snackbarHostState.showSnackbar(
                            if (admin.id == 0) "Администратор ${admin.full_name} успешно добавлен" else "Информация об администраторе ${admin.full_name} обновлена"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        snackbarHostState.showSnackbar("Ошибка при сохранении данных администратора")
                    }
                }
            }
        )
    }

    if (showDeleteDialog != null) {
        ConfirmDeleteAdminDialog(
            admin = showDeleteDialog!!,
            onDismiss = { showDeleteDialog = null },
            onConfirmDelete = {
                val deletedAdmin = showDeleteDialog
                adminsList.remove(showDeleteDialog)
                showDeleteDialog = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Администратор ${deletedAdmin?.full_name} был удален")
                }
            }
        )
    }
}

@Composable
fun ConfirmDeleteAdminDialog(admin: Admin, onDismiss: () -> Unit, onConfirmDelete: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val adminsList = remember { mutableStateListOf<Admin>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Подтверждение удаления") },
        text = { Text("Вы уверены, что хотите удалить администратора ${admin.full_name}?") },
        confirmButton = {
            Button(onClick = {
                coroutineScope.launch {
                    try {
                        AdminRetrofitInstance.api.deleteAdmin(admin.id)
                        try {
                            val admins = AdminRetrofitInstance.api.getAdmins()
                            adminsList.clear()
                            adminsList.addAll(admins)
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
fun AddOrEditAdminDialog(admin: Admin?, onDismiss: () -> Unit, onSaveAdmin: (Admin) -> Unit) {
    var name by remember { mutableStateOf(admin?.full_name ?: "") }
    var email by remember { mutableStateOf(admin?.email ?: "") }
    var password by remember { mutableStateOf(admin?.password ?: "") }
    var address by remember { mutableStateOf(admin?.address ?: "") }
    var selectedPolyclinic by remember { mutableStateOf(admin?.center_name ?: "") }
    var medCenterId by remember { mutableStateOf(admin?.med_center_id ?: 0) }
    var expanded by remember { mutableStateOf(false) }
    val polyclinics = remember { mutableStateListOf<Polyclinic>() }

    // Загрузка списка поликлиник при открытии диалога
    LaunchedEffect(Unit) {
        try {
            val response = AdminRetrofitInstance.api.getPolyclinics()
            polyclinics.clear()
            polyclinics.addAll(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (admin == null) "Добавить администратора" else "Редактировать администратора") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ФИО") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Адрес") })
                Spacer(modifier = Modifier.height(8.dp))
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
                    val newAdmin = Admin(
                        id = admin?.id ?: 0,
                        full_name = name,
                        email = email,
                        center_name = selectedPolyclinic,
                        password = password,
                        address = address,
                        med_center_id = medCenterId
                    )
                    if (admin == null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = AdminRetrofitInstance.api.addAdmin(newAdmin)
                                if (response.isSuccessful) {
                                    onSaveAdmin(newAdmin)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = AdminRetrofitInstance.api.updateAdmin(admin.id, newAdmin)
                                if (response.isSuccessful) {
                                    onSaveAdmin(newAdmin)
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
fun AdminItem(admin: Admin, onDelete: () -> Unit, onEdit: (Admin) -> Unit) {
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
                Text("ФИО: " + admin.full_name, style = MaterialTheme.typography.titleMedium)
                Text("Email: " + admin.email, style = MaterialTheme.typography.bodyMedium)
                Text("Поликлиника: " + admin.center_name, style = MaterialTheme.typography.bodyMedium)
                Text("Адрес: " + admin.address, style = MaterialTheme.typography.bodyMedium) // Добавляем адрес
                Text("Пароль: " + "*".repeat(admin.password.orEmpty().length), style = MaterialTheme.typography.bodyMedium)
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ){
                IconButton(onClick = { onEdit(admin) }) {
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
fun PreviewAddMainDoctorScreen() {
    AddMainDoctorScreen(rememberNavController())
}
