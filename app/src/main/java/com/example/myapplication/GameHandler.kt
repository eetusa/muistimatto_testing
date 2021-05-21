package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class GameHandler {
    private var context: Context
    private var activity: Activity
    private lateinit var settings: Bundle;
    private var gameRunner: Game? = null;


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, activity: Activity, savedInstanceState: Bundle){
        this.context = context
        this.activity = activity
        settings = savedInstanceState;
        initializeGame()
       // startGame()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initializeGame(){
        val mode: Int = settings.getInt("gamemode")
        if (gameRunner == null){
            when (mode) {
                0 -> { gameRunner = StepSequenceGame(context, activity, settings) }
                1 -> { gameRunner = OrderSequenceGame(context, activity, settings)}
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    open fun startGame(){
        gameRunner!!.handleStartGameButton()
    }


    fun showSteps(){
            gameRunner!!.showStepsToggle()
    }

    fun shutDownGame(){
        gameRunner!!.gameEnd()
    }
}