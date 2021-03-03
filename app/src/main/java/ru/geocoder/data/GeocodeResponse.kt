package ru.geocoder.data

import com.google.gson.annotations.SerializedName

data class GeocodeResponse(
    @SerializedName("suggestions")
    val suggestions: List<Suggestion>
)

data class Suggestion(
    @SerializedName("value")
    val value: String
)