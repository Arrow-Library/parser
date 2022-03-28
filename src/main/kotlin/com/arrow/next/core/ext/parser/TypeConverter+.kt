package com.arrow.next.core.ext.parser

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types


inline fun <reified T> toType(
    rawType: Class<*>,
    typeArgument: Class<*>? = null,
    json: String
): T {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val adapter =
        if (typeArgument != null) {
            val type = Types.newParameterizedType(rawType, typeArgument)
            moshi.adapter<T>(type)
        } else {
            moshi.adapter<T>(rawType)
        }
    return adapter.fromJson(json)!!
}

inline fun <reified T> toTypeOf(
    rawType: Class<*>,
    typeArgument: Class<*>? = null,
    json: String? = null
): JsonAdapter<T> {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val adapter =
        if (typeArgument != null) {
            val type = Types.newParameterizedType(rawType, typeArgument)
            moshi.adapter<T>(type)
        } else {
            moshi.adapter<T>(rawType)
        }
    return adapter
}

/**
 * For some reason that API response key with UpperCase Moshi could not
 * Convert so we have to format key to lowercase
 */
inline fun <reified T> toTypeOfForceMoshi(json: String): T? {
    val reg = ",\"[A-Z]|\\{\"[A-Z]".toRegex()
    val obj = reg.replace(json) { value ->
        value.value.lowercase()
    }
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(T::class.java)
    return adapter.fromJson(obj)
}

inline fun <reified T> toJson(obj: T): String {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(T::class.java)
    return adapter.toJson(obj)
}


inline fun <reified T > JsonTo(
    json: String
): T {
    return Gson().fromJson(json, T::class.java)
}
inline fun <reified T> JsonOf(
    clazz: T
): String {
    return Gson().toJson(clazz)
}

inline fun <reified T> toListOfType(
    json: String
): List<T> {
    val typeToken = object : TypeToken<List<T>>() {}.type
    return Gson().fromJson(json, typeToken)
}