package ru.meseen.dev.singer

sealed class Results {
    data class Success(val data: String) : Results()
    data class Error(val error: String) : Results()
    object Fail : Results()
}