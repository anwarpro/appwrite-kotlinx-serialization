package com.helloanwar.kmmapplication.ui.utils

sealed interface NetworkResponse<out T> {
    data class Success<out T>(val data: T) : NetworkResponse<T>
    data class Failure(val error: String) : NetworkResponse<Nothing>
    object Loading : NetworkResponse<Nothing>
    object Idle : NetworkResponse<Nothing>
}