package com.example.ruto.data.security

interface SecureStore {
    fun putString(key: String, value: String?)
    fun getString(key: String): String?
    fun putBoolean(key: String, value: Boolean?)
    fun getBoolean(key: String, default: Boolean = false): Boolean
    fun clear(key: String)
    fun clearAll()
}