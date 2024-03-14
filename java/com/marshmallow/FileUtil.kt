package com.marshmallow

import android.content.Context
import java.io.File
import java.io.IOException

class FileUtil(val context: Context) {

    fun createDirectory(directoryName: String): File {
        val directory = File(context.filesDir, directoryName)

        if (!directory.exists()) {
            try {
                if (directory.mkdir()) {
                    println("Directory created successfully: $directoryName")
                } else {
                    println("Failed to create directory: $directoryName")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return directory
    }

    fun createFile(directory: File, fileName: String): File {
        val file = File(directory, fileName)

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    println("File created successfully: $fileName")
                } else {
                    println("Failed to create file: $fileName")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return file
    }
}