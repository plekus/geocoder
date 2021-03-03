package ru.geocoder.data

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface GeocodeAPI {

    @Headers("Authorization: Token 0b6c11c11f5f6f49d695f4e0b4e7ed97e732026e")
    @GET("/suggestions/api/4_1/rs/geolocate/address")
    fun getAddress(@Query("lat") lat: Double, @Query("lon") lon: Double): Single<GeocodeResponse>
}