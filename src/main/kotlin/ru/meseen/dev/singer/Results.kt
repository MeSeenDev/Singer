package ru.meseen.dev.singer

sealed class Results<out R> {
    data class Success<out Type> (val data: Type) : Results<Type>()
    data class Error(val error: Throwable) : Results<Nothing>()
    object Fail : Results<Nothing>()
}
