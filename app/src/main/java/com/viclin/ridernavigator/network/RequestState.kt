package com.viclin.ridernavigator.network

//请求返回结果的状态统一处理
sealed class RequestState<out T> {
    object Loading : RequestState<Nothing>()
    data class Success<out T>(val data: T) : RequestState<T>()
    data class Error(val exception: Exception) : RequestState<Nothing>()
}
