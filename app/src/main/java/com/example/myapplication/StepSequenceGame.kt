package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import java.lang.Exception
import kotlin.reflect.KProperty

class StepSequenceGame : Game{

    private var board: Board;
    var context: Context
    var activity: Activity
    var settings: Bundle;
    var debugLayout: LinearLayout;
    var debugTextView: TextView;

    var stepSequenceUp = ArrayList<Int>()
    var stepSequenceDown = ArrayList<Int>()
    var stepFootingUp = ArrayList<String>()
    var stepFootingDown = ArrayList<String>()

    var direction = "up"
    var stepCycleIndex: Int = 1
    var currentCycleRow = 1;
    var rowsOverInCycle = 0
    var stepsInCurrentCycle = ArrayList<Int>()

    private var Moves = ArrayList<Int>()
    private var Points = 0

    constructor(context: Context, activity: Activity, savedInstanceState: Bundle){
        this.context = context
        this.activity = activity
        this.settings = savedInstanceState
        this.board = Board(context, activity, this)

        this.debugLayout = activity.findViewById(R.id.rightLayout)
        this.debugTextView = TextView(context)
        this.debugTextView.text = "0"
        this.debugLayout.addView(debugTextView)

        initializeGame()
    }

     private fun initializeGame() {
         val mode: Int = settings.getInt("gamemode")
         val testLayout: LinearLayout = activity.findViewById(R.id.testLayout)
         testLayout.addView(board)

         getStepSequence()

         board.Cells[0].setLeftFoot()
    }

    private fun clearPrintDebug(){
        debugTextView.text = ""
    }

    private fun <T> printDebug(str: T){
        debugTextView.append(str.toString() + "\n")
    }
    private fun <T> printDebug(list: ArrayList<T> ){
        for (item in list){
            debugTextView.append(item.toString() + " ")
        }
        debugTextView.append("\n")
    }


    private fun getStepSequence(){
        val steps = activity.resources.getStringArray(R.array.sequence_a)
        var up: Boolean = true
        for (i in steps.indices){
            try {
                val value = steps[i].toInt()
                if (up) {
                    stepSequenceUp.add(value)
                    println("up: " + steps[i])
                    when {
                        value > 4 -> rowsOverInCycle = 1
                        value > 8 -> rowsOverInCycle = 2
                        value > 12 -> rowsOverInCycle = 3
                    }
                }
                else {
                    stepSequenceDown.add(value)
                    println("down: " + steps[i])
                }
            } catch (e: Exception){
                if (up) {
                    println("up: " + steps[i])
                    stepFootingUp.add(steps[i])
                } else {
                    println("down: " + steps[i])
                    stepFootingDown.add(steps[i])
                }
                up = false;
            }
        }
    }

    private fun compareStep(cell: MemoryCell2){
        var row = board.rows - cell.row;

        if (row > rowsOverInCycle + currentCycleRow){
            stepsInCurrentCycle.clear()
            currentCycleRow = row
        }

        stepsInCurrentCycle.add(cell.index)


        clearPrintDebug()
        compareCurrentCycle(cell)
        printDebug(stepsInCurrentCycle)
        printDebug("Points: " + Points.toString())

    }

    private fun compareCurrentCycle(cell: MemoryCell2){
        val index = stepsInCurrentCycle.size-1



        if (index < stepSequenceUp.size){
            val convertedStep = stepSequenceUp[index] + (currentCycleRow-1) * stepSequenceUp.size
            //printDebug(stepSequenceUp[index].toString() + " . " + convertedStep.toString());
            if (stepsInCurrentCycle[index] == convertedStep){
                Points++
            } else {
                val convIndexForCells = board.Cells.size-1-currentCycleRow*board.columns+stepSequenceUp[index]
                board.Cells[convIndexForCells].flashCorrect()
            }
        }
    }

    override  fun  clickReceiver(cell: MemoryCell2){

        board.setActiveCell(cell)
        board.setDefaultBGforNonactive()
        //board.highlightRow(cell.row)

        Moves.add(cell.index)
        compareStep(cell)
    }


}