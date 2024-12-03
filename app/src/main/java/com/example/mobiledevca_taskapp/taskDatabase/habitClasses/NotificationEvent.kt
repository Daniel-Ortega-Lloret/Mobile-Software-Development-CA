package com.example.mobiledevca_taskapp.taskDatabase.habitClasses

open class NotificationEvent<out T>(private val content: T) {
    private var notificationHandled = false

    fun getContentIfNotHandled(): T? {
        return if (notificationHandled) {
            null
        } else {
            notificationHandled = true
            content
        }
    }
}