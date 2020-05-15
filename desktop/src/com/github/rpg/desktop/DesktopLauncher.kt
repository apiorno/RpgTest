package com.github.rpg.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.mygdx.game.MyGdxGame

fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setWindowSizeLimits(1280, 720, -1, -1)
        setTitle("RPG PRUEBA")
    }
    Lwjgl3Application(MyGdxGame(), config)
}