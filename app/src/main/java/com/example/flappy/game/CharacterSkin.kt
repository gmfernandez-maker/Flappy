package com.example.flappy.game

import android.graphics.Color

enum class CharacterSkin(
    val title: String,
    val color: Int,
    val price: Int
) {
    Ember("Ember", Color.rgb(255, 124, 92), 100),
    Ocean("Ocean", Color.rgb(86, 186, 255), 200),
    Moss("Moss", Color.rgb(102, 204, 122), 300),
    Violet("Violet", Color.rgb(192, 118, 255), 400),
    Sun("Sun", Color.rgb(255, 205, 66), 500),
    Crimson("Crimson", Color.rgb(255, 92, 116), 600);

    companion object {
        val count: Int = values().size
    }
}