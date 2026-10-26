package com.miroshnikov.messenger.ui

interface ChatActions {
    fun retryChannels()
    fun selectChannel(name: String)
    fun closeChat()
    fun loadNextPage()
    fun retryChat()
    fun openImage(link: String)
    fun closeImage()
}
