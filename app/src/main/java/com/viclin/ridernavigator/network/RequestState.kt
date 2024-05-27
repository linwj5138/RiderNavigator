package com.viclin.ridernavigator.network

sealed class RequestState<out T> {
    object Loading : RequestState<Nothing>()
    data class Success<out T>(val data: T) : RequestState<T>()
    data class Error(val exception: Exception) : RequestState<Nothing>()
}
