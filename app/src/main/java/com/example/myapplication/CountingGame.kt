package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.random.Random


class CountingGame @RequiresApi(Build.VERSION_CODES.O) constructor(
    context: Context,
    activity: Activity,
    savedInstanceState: Bundle
) : OrderSequenceGame(context, activity, savedInstanceState) {
    override fun giveClueFromCell(dif: Int, cell: MemoryCell2): String{
        val cellIndex = cell.index
        var firstNumber = Random.nextInt(-(cellIndex+5), cellIndex+5)

        var difficulty = 1.0

        if (firstNumber == cellIndex)
            while (firstNumber == cellIndex) firstNumber = Random.nextInt(cellIndex-10, cellIndex+11)

        if (firstNumber < 0){
            val acceptNegative = Random.nextFloat()
            Log.i("koira", (0.25*difficulty).toString())
            if ((1-0.25*difficulty) >= acceptNegative){
                while (firstNumber < 0) firstNumber = Random.nextInt(cellIndex-10, cellIndex+11)
            }
        }


        var secondNumber = 0
        var operator = ""

        if (firstNumber > cellIndex){
            operator = "-"
            secondNumber = firstNumber - cellIndex
        } else {
            operator = "+"
            secondNumber = cellIndex - firstNumber
        }

        return "$firstNumber $operator $secondNumber"
    }


}