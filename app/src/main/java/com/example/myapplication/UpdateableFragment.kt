package com.example.myapplication

interface UpdateableFragment {
    fun update(a: MainActivity)
    fun fabText():String
    fun buildCmd(a: MainActivity):String
}