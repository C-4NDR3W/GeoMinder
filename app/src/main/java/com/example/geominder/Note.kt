package com.example.geominder

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val place: String = "",
    val date: String = "",
    var time: String = "",
    var isPinned: Boolean = false,
    val groupId: String = ""
)
