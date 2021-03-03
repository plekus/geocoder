package ru.geocoder.di

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.geocoder.data.GeocodeAPI
import java.util.concurrent.TimeUnit

private const val TIMEOUT = 60
private const val WRITE_TIMEOUT = 120
private const val CONNECT_TIMEOUT = 10

fun networkModule(
    baseUrl: String,
) = module {
    single<GeocodeAPI> { get<Retrofit>().create(GeocodeAPI::class.java) }
    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }
    single<OkHttpClient> {
        val httpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)

        httpClientBuilder.build()
    }
    single<Gson> {
        GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
    }
}