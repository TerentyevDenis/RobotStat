package ru.terentev.Controllers

import java.util.regex.PatternSyntaxException

fun String.isRegexp():String?{
    try {
        if (this != null) {
            this.toRegex()
        }
    }
    catch (e: PatternSyntaxException){
        return e.message
    }
    return null
}