package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.os.Bundle
import java.lang.Exception

class StepSequenceGame{

    private var context: Context;
    private var activity: Activity;
    private lateinit var board: Board;
    private lateinit var settings: Bundle;

    constructor(context: Context, activity: Activity, board: Board, savedInstanceState: Bundle){
        this.context = context
        this.activity = activity
        this.settings = savedInstanceState


        initializeGame()
    }

    fun initializeGame() {
        val mode: Int = settings.getInt("gamemode")
        println("Mode: $mode");
    }
}