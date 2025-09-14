package com.example.ruto.data.security

interface SecureStore {
    fun putString(key: String, value: String?)
    fun getString(key: String): String?
    fun clear(key: String)
    fun clearAll()
}