package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi

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
        startGame()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    open fun startGame(){
        val mode: Int = settings.getInt("gamemode")
        if (gameRunner == null){
            when (mode) {
                0 -> { gameRunner = StepSequenceGame(context, activity, settings) }
                1 -> { gameRunner = OrderSequenceGame(context, activity, settings)}
            }

        } else {
            gameRunner!!.newGame()
        }

    }
    fun showSteps(){
        if (gameRunner != null){
            gameRunner!!.showStepsToggle()
        }
    }
}