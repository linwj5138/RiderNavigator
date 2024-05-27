package com.viclin.ridernavigator.network

import com.viclin.ridernavigator.network.service.DirectionsApiService
import com.viclin.ridernavigator.network.service.DirectionsApiServiceV2
import com.viclin.ridernavigator.network.service.DirectionsApiServiceV3
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val TIME_OUT = 10

    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"
    private const val BASE_URL_V2 = "https://routes.googleapis.com/"

    private val logging = HttpLoggingInterceptor()

    private val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
        builder.addInterceptor(logging.setLevel(HttpLoggingInterceptor.Level.BODY))
        builder.build()
    }

//    val retrofitInstance: Retrofit by lazy {
//        Retrofit.Builder()
//            .client(client)
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }

//    val directionsApiService: DirectionsApiService by lazy {
//        retrofitInstance.create(DirectionsApiService::class.java)
//    }

    val directionsApiService by lazy { getService(DirectionsApiService::class.java, BASE_URL) }

    val directionsDataSourceV2 by lazy { getService(DirectionsApiServiceV2::class.java, BASE_URL_V2) }

    val directionsDataSourceV3 by lazy { getService(DirectionsApiServiceV3::class.java, BASE_URL) }

    private fun <S> getService(
        serviceClass: Class<S>,
        baseUrl: String,
        client: OkHttpClient = this.client
    ): S {
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build().create(serviceClass)
    }
}
