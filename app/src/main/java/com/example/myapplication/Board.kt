package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import android.util.Log

/**
 * TODO: document your custom view class.
 */
class Board : LinearLayout {
    val columns = 4
    val rows = 6

    private var activeCellLeft: MemoryCell2? = null
    private var activeCellRight: MemoryCell2? = null
    private var highlightedRow = 0

    private var Moves = ArrayList<Int>()
    private var Points = 0
    private var StepPercentage: Float = 0f
    var Cells = ArrayList<MemoryCell2>()
    private var StepCompared = IntArray(88){0}
    private var correctSteps: Int = 0
    var game: Game;
    private var StepSequence: IntArray = intArrayOf(
        1,4,2,3,
        8,5,7,6,
        9,12,10,11,
        16,13,15,14,
        17,20,18,19,
        24,21,23,22,
        24,21,23,22,
        17,20,18,19,
        16,13,15,14,
        9,12,10,11,
        8,5,7,6,
        1,4,2,3
    )
    var bg: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.all_borders, null)

    var activityx: Activity? = null

    constructor(context: Context, activity: Activity?, game: Game) : super(context) {
       this.layoutParams = LinearLayout.LayoutParams(
           LinearLayout.LayoutParams.MATCH_PARENT,
           LinearLayout.LayoutParams.MATCH_PARENT
       )
        this.orientation = VERTICAL
        this.Points = Points
        this.background = bg
        this.game = game;
        this.setPadding(20,40,40,20)
        activityx = activity
        initBoard(context)
    }

    fun compareStepSimple(index: Int, stepseq: IntArray){
        if (index < stepseq.size){
            if (stepseq[index-1] == Moves[index-1]){
                StepCompared[index-1] = 1
                correctSteps++
            }
        }
    }

    fun calculateStepPercentage(){
        var temp: Float = 0f
        temp = correctSteps/(Moves.size.toFloat())
        StepPercentage = temp*100
    }

    fun clickReceiver(cell: MemoryCell2){
        if (cell.column > 1){
            this.setActiveRight(cell)
        } else {
            this.setActiveLeft(cell)
        }
        addMove(cell.index)
    }

    fun setActiveCell(cell: MemoryCell2){
        if (cell.column > 1){
            this.setActiveRight(cell)
        } else {
            this.setActiveLeft(cell)
        }
    }

    fun setActiveLeft(cell: MemoryCell2){
        activeCellLeft?.setDefaultBg()
        cell.setActiveBg()
        activeCellLeft = cell
    }

    fun setActiveRight(cell: MemoryCell2){
        activeCellRight?.setDefaultBg()
        cell.setActiveBg()
        activeCellRight = cell
    }

    fun addMove(value: Int){
        Moves.add(value)
        Points++
        compareStepSimple(Moves.size, StepSequence)
        calculateStepPercentage()
        updateAll()
    }

    fun updateAll(){
        var tv: TextView? = this.activityx?.findViewById(R.id.scoreCounter)
        //tv?.setText(Points.toString())
        tv?.setText(StepPercentage.toString())
    }

    fun highlightRow(row: Int){
        var starting: Int = columns * row
        if (Cells.size - rows >= starting-2 && row > -1){
            for (i in starting until starting + columns){
                if (!isCellSelected((Cells[i])))Cells[i].setHighlightBg()
            }
        }
    }

    fun setDefaultBGforNonactive(){
        for (cell in Cells){
            if (!isCellSelected(cell)) cell.setDefaultBg()
        }
    }

     fun isCellSelected(cell: MemoryCell2): Boolean{
        if (cell == activeCellRight || cell == activeCellLeft) return true
        return false
    }


    private fun initBoard(context: Context){
        for (i in 0 until rows){
            val row = LinearLayout(context)

            row.orientation = LinearLayout.HORIZONTAL

            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0)
            lp.weight = 1f
            row.layoutParams = lp

            for (j in 0 until columns){
                val cell = MemoryCell2(context, i, j)
                row.addView(cell)
                cell.setOnClickListener(){
                    game.clickReceiver(cell)
                }
                Cells.add(cell)
            }

            this.addView(row)
        }
    }


}