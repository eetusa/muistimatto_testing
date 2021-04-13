package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.os.Bundle

class GameHandler {
    private var context: Context
    private var activity: Activity
    private lateinit var settings: Bundle;


    constructor(context: Context, activity: Activity, savedInstanceState: Bundle){
        this.context = context
        this.activity = activity
        settings = savedInstanceState;
        startGame()
    }

    open fun startGame(){
        val mode: Int = settings.getInt("gamemode")
        when (mode) {
            0 -> { val gameRunner = StepSequenceGame(context, activity, settings) }
        }
    }
}