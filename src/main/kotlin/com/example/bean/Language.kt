package com.example.bean

data class Language(var language: String = "", var kv: Map<String, String> = mapOf()){

    /**
     * 合并kv
     */
    fun plus(lang: Language){
        kv = kv + lang.kv
    }
}