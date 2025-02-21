package com.mgkct.diplom

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mgkct.diplom.Admin.AddDoctorScreen
import com.mgkct.diplom.Admin.MainAdminScreen
import com.mgkct.diplom.SudoAdmin.AddAdmSudoScreen
import com.mgkct.diplom.SudoAdmin.AddMainDoctorScreen
import com.mgkct.diplom.SudoAdmin.AddMedCenterScreen
import com.mgkct.diplom.SudoAdmin.MainSudoAdminScreen
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.IOException

// Определение API
interface ApiService {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("/user/{id}")
    suspend fun getUserInfo(@Path("id") userId: String): UserInfo

    @GET("/med_center/{id}")
    suspend fun getMedCenterInfo(@Path("id") centerId: String): MedCenterInfo
}


data class UserInfo(
    val full_name: String,
    val med_center_id: String
)

data class MedCenterInfo(
    val center_name: String
)

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(
    val role: String,
    val db_id: String,
    val full_name: String,
    val center_name: String
)

val retrofit = Retrofit.Builder()
    .baseUrl("http://10.0.2.2:8000/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService: ApiService = retrofit.create(ApiService::class.java)

// Активность для экрана входа
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "login_screen") {
                composable("login_screen") { LoginScreen(navController) }
                composable("main_sudo_admin") { MainSudoAdminScreen(navController) }
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
                composable("add_adm_sudo") { AddAdmSudoScreen(navController) }
                composable("add_main_doctor") { AddMainDoctorScreen(navController) }
                composable("add_doctor") { AddDoctorScreen(navController) }
                composable("add_med_center") { AddMedCenterScreen(navController) }
            }
        }
    }
}

// Компонент экрана входа
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") } // Состояние для email
    var password by remember { mutableStateOf("") } // Состояние для пароля
    var passwordVisible by remember { mutableStateOf(false) } // Видимость пароля
    var errorMessage by remember { mutableStateOf<String?>(null) } // Сообщение об ошибке
    var isLoading by remember { mutableStateOf(false) } // Состояние загрузки
    var emailError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun validateEmail(email: String): Boolean {
        return if (email.isBlank()) {
            emailError = "Поле Email не может быть пустым"
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Некорректный формат Email"
            false
        } else {
            emailError = null
            true
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Фоновое изображение
        Image(
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = "Фон",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Основной контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.mipmap.medical),
                contentDescription = "Логотип медицинской платформы",
                modifier = Modifier
                    .size(70.dp) // Размер иконки
                    .padding(bottom = 8.dp),
                contentScale = ContentScale.Fit
            )

            // Описание платформы
            Text(
                text = "Единая платформа для всех медицинских учреждений, обеспечивающая удобное управление, безопасность данных и эффективное взаимодействие между сотрудниками и пациентами.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )

            // Карточка для формы входа
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Заголовок
                    Text(
                        text = "Вход",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Поле для ввода email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            validateEmail(it)
                        },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        isError = emailError != null
                    )
                    emailError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Поле для ввода пароля
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Отображение сообщения об ошибке
                    errorMessage?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Кнопка входа
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = "" // Очистка ошибки перед новой попыткой входа
                                try {
                                    val response = apiService.login(LoginRequest(email, password))
                                    when (response.role) {
                                        "sudo-admin" -> navController.navigate("main_sudo_admin")
                                        "admin" -> navController.navigate("main_admin/${response.full_name}/${response.center_name}")
                                        else -> errorMessage = "Неизвестная роль: ${response.role}"
                                    }
                                    Log.e("MyApp", "db_name: ${response.db_id}")
                                } catch (e: HttpException) {
                                    errorMessage = "Ошибка сервера: ${e.code()} - ${e.message()}"
                                    Log.e("LoginError", "HttpException: ${e.response()?.errorBody()?.string()}")
                                } catch (e: IOException) {
                                    errorMessage = "Ошибка сети: проверьте соединение с интернетом"
                                    Log.e("LoginError", "IOException: ${e.message}")
                                } catch (e: Exception) {
                                    errorMessage = "Ошибка входа: ${e.localizedMessage}"
                                    Log.e("LoginError", "Exception: ${e.stackTraceToString()}")
                                }
                                finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Text("Войти")
                        }
                    }


                }
            }
        }
    }
}

// Предварительный просмотр экрана входа
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    LoginScreen(navController)
}