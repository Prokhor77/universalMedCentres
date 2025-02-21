package com.mgkct.diplom

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

interface ApiService {

    // Логин
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // Получение информации о пользователе
    @GET("/user/{id}")
    suspend fun getUserInfo(@Path("id") userId: String): UserInfo

    // Получение информации о медицинском центре
    @GET("/med_center/{id}")
    suspend fun getMedCenterInfo(@Path("id") centerId: String): MedCenterInfo

    // Работа с медицинскими центрами
    @GET("med-centers")
    suspend fun getMedCenters(): List<Clinic>

    @POST("add-med-center")
    suspend fun addMedCenter(@Body clinic: Clinic): Response<ResponseBody>

    @PUT("update-med-center/{id}")
    suspend fun updateMedCenter(@Path("id") id: Int, @Body clinic: Clinic): Response<ResponseBody>

    @DELETE("delete-med-center/{id}")
    suspend fun deleteMedCenter(@Path("id") id: Int): Response<ResponseBody>

    // Работа с главными врачами
    @GET("main-doctors")
    suspend fun getMainDoctors(): List<MainDoctor>

    @POST("add-doctor")
    suspend fun addDoctor(@Body doctor: MainDoctor): Response<ResponseBody>

    @PUT("update-doctor/{id}")
    suspend fun updateDoctor(@Path("id") id: Int, @Body doctor: MainDoctor): Response<ResponseBody>

    @DELETE("delete-doctor/{id}")
    suspend fun deleteDoctor(@Path("id") id: Int): Response<ResponseBody>

    // Работа с администраторами
    @GET("admins")
    suspend fun getAdmins(): List<Admin>

    @POST("add-admin")
    suspend fun addAdmin(@Body admin: Admin): Response<ResponseBody>

    @PUT("update-admin/{id}")
    suspend fun updateAdmin(@Path("id") id: Int, @Body admin: Admin): Response<ResponseBody>

    @DELETE("delete-admin/{id}")
    suspend fun deleteAdmin(@Path("id") id: Int): Response<ResponseBody>

    // Получение списка поликлиник
    @GET("polyclinics")
    suspend fun getPolyclinics(): List<Polyclinic>
}

object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
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

data class Clinic(
    val id_center: Int,
    val center_name: String,
    val center_description: String,
    val center_address: String,
    val center_number: String
)

data class MainDoctor(
    val id: Int,
    val full_name: String?,
    val email: String?,
    val center_name: String,
    val password: String?,
    val address: String,
    val med_center_id: Int
)

data class Admin(
    val id: Int,
    val full_name: String,
    val email: String,
    val center_name: String,
    val password: String,
    val address: String,
    val med_center_id: Int
)

data class Polyclinic(
    val id_center: Int,
    val center_name: String
)