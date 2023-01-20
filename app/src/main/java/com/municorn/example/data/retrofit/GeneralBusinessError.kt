package com.municorn.example.data.retrofit

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Extended http error codes for business logic errors
 * It is returned with code 500 and may be processed by app as usual http error
 */
@JsonClass(generateAdapter = true)
internal class GeneralBusinessError(
    @Json(name = "error")
    val error: GeneralBusinessErrorDescription,
)

@JsonClass(generateAdapter = true)
internal class GeneralBusinessErrorDescription(
    @Json(name = "code")
    val code: Int,
    @Json(name = "description")
    val description: String,
)
