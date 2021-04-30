package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import java.lang.Exception

abstract class Game : ViewModel() {
     abstract fun clickReceiver(cell: MemoryCell2)
     abstract fun newGame()
     abstract fun showStepsToggle(boolean: Boolean = false) : Boolean

    // var context: Context
   //  var activity: Activity
     //var settings: Bundle;

    // fun initializeGame(){}
}