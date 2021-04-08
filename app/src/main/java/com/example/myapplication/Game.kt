package com.example.myapplication

import android.app.Activity
import android.content.Context
import java.lang.Exception

open class Game {

    private var context: Context? = null
    private var activity: Activity? = null
    private var board: Board? = null
    private var settings = IntArray(6){-1}


    constructor(context: Context, activity: Activity, board: Board, settings: IntArray){
        this.context = context
        this.activity = activity

        for (i in 0 until settings.size){
            try{
                this.settings[i] = settings[i]
            } catch (e: Exception){

            }
        }

        initializeGame()
    }

    open fun initializeGame(){

    }
}