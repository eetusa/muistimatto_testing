package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import java.lang.Exception

abstract class Game : ViewModel() {
     var gameOn: Boolean = false
     var gamePaused: Boolean = false
     var gamePlayed: Boolean = false

     abstract fun clickReceiver(cell: MemoryCell2)
     abstract fun handleStartGameButton()
     abstract fun showStepsToggle(boolean: Boolean = false) : Boolean
     abstract fun gameEnd()

    // var context: Context
   //  var activity: Activity
     //var settings: Bundle;

    // fun initializeGame(){}
}