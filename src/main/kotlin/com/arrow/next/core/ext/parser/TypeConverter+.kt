package com.arrow.next.core.ext.parser

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.koin.core.qualifier.named
import org.koin.core.context.GlobalContext.get
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow


typealias  PairLang = HashMap<String, LinkedTreeMap<String, String>>?
val gson = get().get(named("bgson")) as Gson
var moshi = get().get(named("bmoshi")) as Moshi
private var _pairLang : PairLang? = null

inline fun <reified T> toType(
    rawType: Class<*>,
    typeArgument: Class<*>? = null,
    json: String
): T {
    val moshi = moshi
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
    val moshi = moshi
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
    val moshi = moshi
    val adapter = moshi.adapter(T::class.java)
    return adapter.fromJson(obj)
}

inline fun <reified T> toJson(obj: T): String {
    val moshi = moshi
    val adapter = moshi.adapter(T::class.java)
    return adapter.toJson(obj)
}


inline fun <reified T > JsonTo(
    json: String
): T {
    return gson.fromJson(json, T::class.java)
}

inline fun <reified T > JsonTo(
    file: FileReader
): T {
    return gson.fromJson(file, T::class.java)
}

inline fun <reified T > JsonTo(
    reader: JsonReader
): T {
    return gson.fromJson(reader, T::class.java)
}

inline fun <reified T > JsonTo(
    element: JsonElement
): T {
    return gson.fromJson(element, T::class.java)
}

inline fun <reified T> JsonOf(
    clazz: T
): String {
    return gson.toJson(clazz)
}

inline fun <reified T> toListOfType(
    json: String
): List<T> {
    val typeToken = object : TypeToken<List<T>>() {}.type
    return gson.fromJson(json, typeToken)
}


@Deprecated("Will change stream reader")
private val file = get().get(named("file")) as File

val pairLang: PairLang
    get() {
        if(_pairLang == null) {
            lateinit var fileReader: FileReader
            try {
                fileReader = FileReader(file)
                _pairLang  = JsonTo(file = fileReader)
            } finally {
                fileReader.close()
            }
        }
        return _pairLang
    }


fun language(reload: Boolean = false): PairLang {
    return when (reload) {
        true -> {
            _pairLang = null
            pairLang
        }
        else -> {
            pairLang
        }
    }
}

fun translate (key: String, next: String): String {
    return pairLang!![key]?.get(next)!!
}


fun benchmark (f: () -> Unit) {
    System.gc()
    val startTime = System.currentTimeMillis()
    val startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

    f.invoke()

    System.gc()
    val finishTime = System.currentTimeMillis()
    val finishMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

    val elapsedMemory = finishMemory - startMemory
    val elapsedTime = finishTime - startTime

    println("Exe Process Time Consume: $elapsedTime")
    println("Exe Process Used memory: ${elapsedMemory.formatSize}")
}


val Long.formatSize : String
    get() {
        if (this <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(this / 1024.0.pow(digitGroups.toDouble())).toString() + " " + units[digitGroups]
}
