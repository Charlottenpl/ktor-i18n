package com.example.util

import java.io.File

enum class FileType{
    xml,
    strings,
    excel
}


interface FileHelper {
    fun write(file: File): ;
    fun read(file: File);
}

