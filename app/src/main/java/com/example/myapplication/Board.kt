package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat


/**
 * TODO: document your custom view class.
 */
class Board : LinearLayout {
    val columns = 4
    val rows = 6

    private var activeCellLeft: MemoryCell2? = null
    private var activeCellRight: MemoryCell2? = null
    private var highlightedRow = 0

    var centerLineX: Int = 0
        get() {
            var temp = intArrayOf(0, 0)
            this.getLocationInWindow(temp)
            println("center measures: " + temp[0] + " " + this.measuredWidth)
            return temp[0] + this.measuredWidth/2
        }

    private var Moves = ArrayList<Int>()
    private var Points = 0
    private var StepPercentage: Float = 0f
    var Cells = ArrayList<MemoryCell2>()
    var leftFoot = -1
    var rightFoot = -1
    private var StepCompared = IntArray(88){0}
    private var correctSteps: Int = 0
    var game: Game;

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
        this.setPadding(20, 40, 40, 20)

        activityx = activity
        initBoard(context)


    }

    fun compareStepSimple(index: Int, stepseq: IntArray){
        if (index < stepseq.size){
            if (stepseq[index - 1] == Moves[index - 1]){
                StepCompared[index - 1] = 1
                correctSteps++
            }
        }
    }

    fun calculateStepPercentage(){
        var temp: Float = 0f
        temp = correctSteps/(Moves.size.toFloat())
        StepPercentage = temp*100
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

    fun removeOldFeet(){
        for (cell in Cells){
            if (cell.index != leftFoot && cell.index != rightFoot) cell.removeFoot()
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

    fun flashSequence(){

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

                cell.setOnTouchListener(OnTouchListener { v, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                       // Log.i("touch", cell.index.toString())
                     //   game.clickReceiver(cell)
                        game.touchDown(cell)
                    }

                    if (event.actionMasked == MotionEvent.ACTION_UP) {
                       // Log.i("touch", cell.index.toString())
                       // game.clickReceiver(cell)
                        game.touchUp(cell)
                    }

                    if (event.actionMasked == MotionEvent.ACTION_MOVE){
                        if (event.x > cell.width || event.x < 0 || event.y > cell.height || event.y < 0){
                            game.touchUp(cell)
                        }
                    }


                   // Log.i("touch", cell.width.toString())

                   // Log.i("action",event.toString())
                    true
                })




               // cell.setOnClickListener(){
                   // game.clickReceiver(cell)
               //}
                Cells.add(cell)
            }

            this.addView(row)
        }
        sortCells()
    }

    private fun sortCells(){
        var tempArr = ArrayList<MemoryCell2>()

        for (i in 0 until Cells.size){
            tempArr.add(Cells[i])
        }
        for (i in 0 until Cells.size){
            Cells[tempArr[i].index - 1] = tempArr[i]
        }
    }





}