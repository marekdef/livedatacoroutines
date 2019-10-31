package pl.mobilization.livedataarchitecture

import retrofit2.http.GET
import retrofit2.http.Query

interface HelloService {

    @GET("/hello")
    suspend fun hello(@Query("user") username: String) : String
}