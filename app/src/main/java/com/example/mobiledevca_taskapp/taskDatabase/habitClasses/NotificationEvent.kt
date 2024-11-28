package com.example.mobiledevca_taskapp.taskDatabase.habitClasses

/**
 * Event wrapper pattern to ensure step notifications are sent only once
 * Prevents user from getting annoyed at too many notifications being sent
 */
open class NotificationEvent<out T>(private val content: T) {
    private var notificationHandled = false

    /**
     * Returns content and prevents it from being used again
     */
    fun getContentIfNotHandled(): T? {
        return if (notificationHandled) {
            null
        } else {
            notificationHandled = true
            content
        }
    }

    /**
     * Returns content, even if its been used already
     */
    fun peekContent(): T = content
}