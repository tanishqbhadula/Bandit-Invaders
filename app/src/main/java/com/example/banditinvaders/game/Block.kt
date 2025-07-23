package com.example.banditinvaders.game

data class Block(
    var x : Int,
    var y : Int,
    var width : Int,
    var height : Int,
    val imgResID : Int? = null,  // ID for drawing image
    var alive : Boolean = true,  // for enemies
    var used : Boolean = false  // for bullets
)

