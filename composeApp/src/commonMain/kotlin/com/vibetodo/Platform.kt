package com.vibetodo

enum class Platform {
    Desktop, Android, IOS, Web
}

expect fun getPlatform(): Platform
