package com.example.elixir.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream

fun saveImg(context: Context, imageBytes: ByteArray, fileName: String): String {
    val file = File(context.filesDir, fileName)
    FileOutputStream(file).use { it.write(imageBytes) }
    return file.absolutePath // 저장된 파일 경로 반환
}