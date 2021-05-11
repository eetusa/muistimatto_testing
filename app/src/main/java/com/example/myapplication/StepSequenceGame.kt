package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.random.Random
import kotlin.reflect.KProperty

class StepSequenceGame : Game{

    private var board: Board;
    var context: Context
    private var activity: Activity
    private var settings: Bundle;
    private var debugLayout: LinearLayout;
    private var debugTextView: TextView;

    private var gameDifficulty = 2
    private var gameSymbolAmount = 2

    var stepSequenceUp = ArrayList<Int>()
    var stepSequenceDown = ArrayList<Int>()
    var stepFootingUp: String = ""
    var stepFootingDown: String = ""

    var wholeStepSequence = ArrayList<Int>()
    var wholeStepFooting: String = ""
    var nextCorrectStep = -1

    var wholeStepSequenceInRows = ArrayList<ArrayList<Int>>()
    var wholeStepFootingInRows = ArrayList<String>()
    var currentSequenceRow: Int = -1

    var lastFootLeft: Boolean = false
    var showingStepSequence: Job? = null

    var directionIsUp = true
    var stepCycleIndex: Int = 1
    var currentCycleRow = 1;
    var rowsOverInCycle = 0
    var stepsInCurrentCycle = ArrayList<Int>()

    private var stepSymbols = ArrayList<String>()


    private var textcolor_nonselected: Int = 0
    private var textcolor_selected: Int = 0

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

        textcolor_nonselected = ContextCompat.getColor(context, R.color.text_default_color)
        textcolor_selected = ContextCompat.getColor(context, R.color.material_on_background_emphasis_high_type)

        initializeGame()
    }

     private fun initializeGame() {
         val mode: Int = settings.getInt("gamemode")
         val testLayout: LinearLayout = activity.findViewById(R.id.testLayout)

         testLayout.addView(board)

        // getStepSequence()
         getStepSequenceWhole()
         getAndShowRightFootOnUi(wholeStepFooting, 0)
         generateStepSymbols(gameSymbolAmount,gameDifficulty)
        // setStepSymbolsOnUI(stepSymbols, 0)
         createRandomStepSequence()

    }

    private fun createRandomStepSequence(){
        val stepsPerRow = 4

        var row = 0
        var rowsteps = ArrayList<Int>()
        var randomSteps = ArrayList<Int>()

        while (randomSteps.size < stepsPerRow*6){
            while(randomSteps.size < stepsPerRow*(row+1)){
                val random = row*stepsPerRow+Random.nextInt(4)+1
                if (randomSteps.indexOf(random) == -1){
                    randomSteps.add(random)
                }
            }
            row++;
        }
        row--
        while (randomSteps.size < stepsPerRow*11){

            row--

            while(rowsteps.size < stepsPerRow){
                val random = row*stepsPerRow+Random.nextInt(4)+1
                if (rowsteps.indexOf(random) == -1){
                    rowsteps.add(random)
                    randomSteps.add(random)
                }
            }

            rowsteps.clear()
        }

        printDebug("rowsteps",randomSteps)

        if (wholeStepSequence.size>0)wholeStepSequence.clear()
        wholeStepSequence.apply {  addAll(randomSteps) }
    }

    private fun clearPrintDebug(){
        debugTextView.text = ""
    }

    private fun <T> printDebug(str: T){
        debugTextView.append(str.toString() + "\n")
    }

    private fun <T> printDebug(tag: String, str: T){
        debugTextView.append("\n'$tag':\n")
        debugTextView.append(str.toString() + "\n")
    }

    private fun <T> printDebug(list: ArrayList<T> ){
        for (item in list){
            debugTextView.append(item.toString() + " ")
        }
        debugTextView.append("\n")
    }
    private fun <T> printDebug(tag: String, list: ArrayList<T> ){
        debugTextView.append("\n'$tag':\n")
        for (item in list){
            debugTextView.append(item.toString() + " ")
        }
        debugTextView.append("\n")
    }

    private fun getStepSequenceWhole(){
        val steps = activity.resources.getString(R.string.seq_b)
        var value = ""
        var index = 0
        for (i in steps){
            if (i==','){

                try {
                    wholeStepSequence.add(value.toInt())
                    //println("num: "+value)
                } catch (e: Exception){
                    wholeStepFooting = value
                   // println("footing: "+value)
                }
                value=""
            }
            else value += i
        }
        index = 0
        var temp = ArrayList<Int>()
        for (i in wholeStepSequence){
            if (index % board.columns == 0){
                if (temp.size>0){
                    var tempTemp = ArrayList<Int>()
                    for (i in temp){
                        tempTemp.add(i)
                    }
                    wholeStepSequenceInRows.add(tempTemp)
                    temp.clear()
                }
            }
            temp.add(i)
            index++;
        }
        var tempTemp = ArrayList<Int>()
        for (i in temp){
            tempTemp.add(i)
        }
        wholeStepSequenceInRows.add(tempTemp)

        index = 0
        var temp2 = ""
        for (i in wholeStepFooting){
            if (index % board.columns == 0){
                if (temp2.isNotEmpty()){
                    var tempTemp = ""
                    for (i in temp2){
                        tempTemp += i
                    }
                    wholeStepFootingInRows.add(tempTemp)
                    temp2 = ""
                }
            }
            temp2 += i
            index++;
        }
        var tempTemp2 = ""
        for (i in temp2){
            tempTemp2 += i
        }
        wholeStepFootingInRows.add(tempTemp2)
        for (i in wholeStepSequenceInRows){
            println(i)
        }
        for (i in wholeStepFootingInRows){
            println(i)
        }

        nextCorrectStep = wholeStepSequence[0]
        //println("lengths: " + wholeStepFootingInRows.size + " " + wholeStepSequenceInRows.size)
    }

    private fun getStepSequence(){
        val steps = activity.resources.getStringArray(R.array.sequence_b)
        var up = true
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
                    stepFootingUp = steps[i]
                } else {
                    println("down: " + steps[i])
                    stepFootingDown = steps[i]
                }
                up = false
            }
        }
        for (i in 0..board.columns*board.rows-1){ // 0..23

        }
    }

    private fun setSequenceButtonTextToActive(value: Boolean){
        try {
            val stepbutton: Button = activity.findViewById(R.id.show_step_sequence)
            if (!value){
                stepbutton.text = activity.resources.getText(R.string.show_step_sequence)
            } else {
                stepbutton.text = activity.resources.getText(R.string.show_step_sequence_active)
            }

        } catch (e: Exception) {

        }
    }

    private fun flashSequence(sequence: ArrayList<Int>, query: Boolean): Boolean{

        if (showingStepSequence == null){
            if (!query) {
                setSequenceButtonTextToActive(true)

                showingStepSequence = viewModelScope.launch {

                    for ((index, i) in sequence.withIndex()) {
                        board.Cells[i - 1].flashCorrect()

                        if (wholeStepFooting[index]=='L'){
                            board.Cells[i -1 ].setLeftFoot(true)
                        } else{
                            board.Cells[i -1 ].setRightFoot(true)
                        }

                        delay(1000L)
                        board.Cells[i -1 ].removeFoot()
                    }
                    showingStepSequence = null
                    setSequenceButtonTextToActive(false)
                }
            }
            return false;
        } else {
            if (!query) {
                setSequenceButtonTextToActive(false)
                showingStepSequence!!.cancel()
                showingStepSequence = null
            }
            return true;
        }


    }

    private fun compareStepThirdIteration(cell: MemoryCell2){
        clearPrintDebug()
       // printDebug(Moves.size.toString() + " " +wholeStepSequence.size.toString())
        if (Moves.size < wholeStepSequence.size+1){
            if (Moves.size < wholeStepSequence.size){
                getAndShowRightFootOnUi(wholeStepFooting, Moves.size)
                setStepSymbolsOnUI(stepSymbols,((Moves.size)%stepSymbols.size))
            }
            if (Moves[Moves.size-1] == wholeStepSequence[Moves.size-1]){
                correctCellClicked(cell)
            } else {
                incorrectCellClicked(cell)
            }
        } else {
            cell.flashBGColor(cell.highlightBgColor)
            setFoot(cell)
        }
        printDebug("Points: $Points / ${wholeStepSequence.size}")
        printDebug("Steps",wholeStepSequence)
    }

    private fun compareStepRevised(cell: MemoryCell2){
        clearPrintDebug()
        if (5-cell.row != currentSequenceRow){
            if (5-cell.row > currentSequenceRow){
                if (directionIsUp){
                    currentSequenceRow = 5-cell.row
                    stepsInCurrentCycle.clear()
                }
            } else {
                if (!directionIsUp){
                    currentSequenceRow = 5-cell.row
                    stepsInCurrentCycle.clear()
                }
            }
        }

        stepsInCurrentCycle.add(cell.index)
        printDebug(currentSequenceRow)
        printDebug(stepsInCurrentCycle)
        printDebug(wholeStepSequenceInRows[currentSequenceRow])
        if (stepsInCurrentCycle.size<5){
            if(cell.index == wholeStepSequenceInRows[currentSequenceRow][stepsInCurrentCycle.size-1]){
                correctCellClicked(cell)

            } else {
                incorrectCellClicked(cell)
            }
        }


    }

    private fun getAndShowRightFootOnUi(array: String, index: Int){
        if (array[index] == 'L') indicateLeftFootOnUI()
        else indicateLeftFootOnUI(false)
    }

    private fun indicateLeftFootOnUI(leftfoot: Boolean = true){
        val footview: TextView = activity.findViewById(R.id.whichFootTextView)
        if (leftfoot) footview.text ="L"
        else footview.text="R"
    }

    private fun generateStepSymbols(n: Int, difficulty: Int){
        val symbol_set: Array<String> = arrayOf("Numero","Kirjain","Väri","Symboli")
        var diff_index = symbol_set.size - (2 - difficulty)
        if (diff_index > symbol_set.size) diff_index = symbol_set.size

        if (stepSymbols.size > 0){
            stepSymbols.clear()
        }

        println(diff_index)
        for (i in 0..n-1){
            val random = Random.nextInt(diff_index)
            stepSymbols.add(symbol_set[random])
        }

        var allSame=true

        for (i in 0 until stepSymbols.size-1){
            if (stepSymbols[i] != stepSymbols[i+1]) allSame=false
        }
        if (allSame) generateStepSymbols(n, difficulty)
        else setStepSymbolsOnUI(stepSymbols, 0)
    }

    private fun setStepSymbolsOnUI(arrayList: ArrayList<String>, activeIndex: Int){
        val footview: TextView = activity.findViewById(R.id.whichSymbolsTextView)
        var color: Int

        if (arrayList.size < 1) return

        footview.text = ""

        for (i in 0..arrayList.size-1){
            val indexText = SpannableString(arrayList[i] + " ")
            if (i == activeIndex) color = textcolor_selected
            else color = textcolor_nonselected

            indexText.setSpan(
                    ForegroundColorSpan(color),
                    0,
                    indexText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (i == activeIndex){
                indexText.setSpan(
                        StyleSpan(Typeface.BOLD),
                        0,
                        indexText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            footview.append(indexText)
        }


    }

    /*
    private fun indicateActiveSymbolOnUI(index: Int){
        val footview: Button = activity.findViewById(R.id.whichFootTextView)
            val indexText = SpannableString("$index ")
            indexText.setSpan(
                    ForegroundColorSpan(defaultColor),
                    0,
                    indexText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            contentView.text = indexText

            val alphabetText = SpannableString("$letter")

            alphabetText.setSpan(
                    ForegroundColorSpan(defaultAlphabetColor),
                    0,
                    alphabetText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            alphabetText.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    alphabetText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            contentView.append(alphabetText)

    }*/

    private fun correctCellClicked(cell: MemoryCell2){
        cell.flashCorrect()
        Points++
        setFoot(wholeStepFooting[Moves.size-1], cell)
    }

    private fun incorrectCellClicked(cell: MemoryCell2){
        //val convertedStep = wholeStepSequenceInRows[currentSequenceRow][stepsInCurrentCycle.size-1]

        val correctIndex = wholeStepSequence[Moves.size-1]
        //printDebug(correctIndex)
       //printDebug(board.Cells[correctIndex-1].index)
        board.Cells[correctIndex-1].flashCorrect()
        cell.flashMistake()
        setFoot(cell)
    }

    private fun compareStep(cell: MemoryCell2){

        clearPrintDebug()
        compareStepRevised(cell)
        val row = board.rows - cell.row
        printDebug("row: " + row)
        printDebug("currentCycleRow: " + currentCycleRow)
        printDebug("rowsoverincycle: " + rowsOverInCycle)
        if (row > rowsOverInCycle + currentCycleRow){
            stepsInCurrentCycle.clear()
            currentCycleRow = row
            if (currentCycleRow % (stepSequenceUp.size/board.columns) == 0) currentCycleRow--
        }

        stepsInCurrentCycle.add(cell.index)

        compareCurrentCycle(cell)

        printDebug("curCycle row: $currentCycleRow")
        printDebug(stepsInCurrentCycle)
        printDebug("Points: $Points")

    }

    private fun compareCurrentCycle(cell: MemoryCell2){
        val index = stepsInCurrentCycle.size-1

        if (index < stepSequenceUp.size){

            val convertedStep = stepSequenceUp[index] + (currentCycleRow*board.columns / stepSequenceUp.size) * stepSequenceUp.size
            if (stepsInCurrentCycle[index] == convertedStep){

                Points++
                cell.flashCorrect()
                setFoot(stepFootingUp[index], cell)
            } else {

                board.Cells[convertedStep-1].flashCorrect()
                cell.flashMistake()
                setFoot(cell)
            }

            println(stepFootingUp[index])



        } else {
            if (currentCycleRow+rowsOverInCycle==board.rows){
                directionIsUp=false
            }
            cell.flashBGColor(cell.highlightBgColor)
            setFoot(cell)
        }
    }

    private fun setFoot(cell: MemoryCell2){
        if (lastFootLeft){
            lastFootLeft = false
            cell.setRightFoot()
            board.rightFoot = cell.index
        } else {
            lastFootLeft = true
            cell.setLeftFoot()
            board.leftFoot = cell.index
        }
        board.removeOldFeet()
    }

    private fun setFoot(side: Char, cell: MemoryCell2, whiten: Boolean = false){
        if (side == 'L'){
            cell.setLeftFoot(whiten)
            if (!whiten){
                lastFootLeft = true
                board.leftFoot = cell.index
            }

        } else {
            cell.setRightFoot(whiten)
            if (!whiten){
                lastFootLeft = false
                board.rightFoot = cell.index
            }

        }
        if (!whiten){
            board.removeOldFeet()
        }

    }

    public override fun newGame(){
        board.leftFoot = -1
        board.rightFoot = -1
        board.removeOldFeet()
        directionIsUp = true
        stepCycleIndex = 1
        currentCycleRow = 1
        Points = 0
        stepsInCurrentCycle.clear()
        currentSequenceRow = -1
        lastFootLeft = false
        getAndShowRightFootOnUi(wholeStepFooting, 0)
        generateStepSymbols(gameSymbolAmount, gameDifficulty)

        Moves.clear()
        if (showStepsToggle(true)) {
            showStepsToggle()
        }
        clearPrintDebug()
    }

    override fun showStepsToggle(query: Boolean): Boolean {
        return flashSequence(wholeStepSequence, query)
    }
    override  fun  clickReceiver(cell: MemoryCell2){

        if (showStepsToggle(true)) {
            showStepsToggle()
        } else {
            Moves.add(cell.index)
            compareStepThirdIteration(cell)
           // printDebug(board.centerLineX)
        }
        printDebug("row",cell.row)
        printDebug("column",cell.column)
        printDebug("letter",cell.letter)
        printDebug("color",cell.defaultAlphabetColor)


    }


}