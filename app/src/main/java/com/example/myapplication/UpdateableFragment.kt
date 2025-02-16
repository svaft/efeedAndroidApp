package com.example.myapplication

interface UpdateableFragment {
    fun update()
    fun fabText():String
    fun buildCmd():String
}