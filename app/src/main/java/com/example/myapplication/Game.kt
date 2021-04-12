package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.os.Bundle
import java.lang.Exception

open class Game {

    private var context: Context
    private var activity: Activity
    private var board: Board;
    private lateinit var settings: Bundle;


    constructor(context: Context, activity: Activity, board: Board, savedInstanceState: Bundle){
        this.context = context
        this.activity = activity
        this.board = board;

        settings = savedInstanceState;

        initializeGame()
    }

    open fun initializeGame(){
        val mode: Int = settings.getInt("gamemode")
        when (mode) {
            0 -> { val gameRunner = StepSequenceGame(context, activity, board, settings) }
        }
    }
}