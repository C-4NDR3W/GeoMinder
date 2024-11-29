package com.example.geominder

data class Group(
    val name: String,
    val admin : String,
    val id : String,
    val desc : String,
    val members: List<User>
)

