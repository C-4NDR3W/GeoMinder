package com.example.geominder

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val place: String = "",
    val date: String = "",
    var time: String = "",
    val groupId: String = "",
    var groupName: String = "",
    var isPinned: Boolean = false,
)
