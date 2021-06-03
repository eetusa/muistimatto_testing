package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class StepSequenceGame : Game{

    private var debugLayout: LinearLayout
    private var debugTextView: TextView

    private var gameDifficulty = 2
    private var gameSymbolAmount = 2

    // var nextCorrectStep = -1

    //var wholeStepSequenceInRows = ArrayList<ArrayList<Int>>()
   // var wholeStepFootingInRows = ArrayList<String>()
    // var currentSequenceRow: Int = -1

    private var lastFootLeft: Boolean = false
    private var showingStepSequence: Job? = null

    private var stepSymbols = ArrayList<String>()


    private var textcolorNonSelected: Int = 0
    private var textcolorSelected: Int = 0


    constructor(context: Context, activity: Activity, savedInstanceState: Bundle){
        this.context = context
        this.activity = activity
        this.settings = savedInstanceState
        this.board = Board(context, activity, this)

        this.debugLayout = activity.findViewById(R.id.rightLayout)
        this.debugTextView = TextView(context)
       // this.debugTextView.text = "0"
        this.debugLayout.addView(debugTextView)

        textcolorNonSelected = ContextCompat.getColor(context, R.color.text_default_color)
        textcolorSelected = ContextCompat.getColor(context, R.color.material_on_background_emphasis_high_type)

        initializeGame()
    }


     private fun initializeGame() {
        // val mode: Int = settings.getInt("gamemode")
         val testLayout: LinearLayout = activity.findViewById(R.id.testLayout)

         testLayout.addView(board)

         getStepSequenceWhole()
         getAndShowRightFootOnUi(wholeStepFooting, 0)
         generateStepSymbols(gameSymbolAmount,gameDifficulty)
         setStepSymbolsOnUI(stepSymbols, 0)
         initializeSpinner()


    }

    private fun initializeSpinner(){
        val spinner: Spinner = activity.findViewById(R.id.chooseStepSequence)
        ArrayAdapter.createFromResource(
            context,
            R.array.stepsequence_names_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                Log.i("position", position.toString())
                when (position) {
                    0 -> { getStepSequenceWhole(0) }
                    1 -> { getStepSequenceWhole(1)}
                    2 -> { getStepSequenceWhole(2)}
                    3 -> {getStepSequenceWhole(3)}
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        }
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

    private fun getStepSequenceWhole(seq: Int = 0){
        var steps = ""
        when (seq){
            0 -> {steps = activity.resources.getString(R.string.seq_a)
                Log.i("juu","a")}
            1 -> {steps = activity.resources.getString(R.string.seq_b)
                Log.i("juu","b")}
            2 -> {steps = activity.resources.getString(R.string.seq_c)
                Log.i("juu","c")}
            3 -> {steps = activity.resources.getString(R.string.seq_d)
                Log.i("juu","d")}
        }
       // val steps = activity.resources.getString(R.string.seq_b)
        var value = ""
        setExtraGameData("","Askelsarja nro")
        if (wholeStepSequence != null) wholeStepSequence.clear()
        if (wholeStepFooting != null) wholeStepFooting = ""

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
                if (gameOn) stopGame()
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
            return false
        } else {
            if (!query) {
                setSequenceButtonTextToActive(false)
                showingStepSequence!!.cancel()
                showingStepSequence = null
            }
            return true
        }


    }


    private fun compareStepThirdIteration(cell: MemoryCell2){
        clearPrintDebug()

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
     //   printDebug("Points: $Points / ${wholeStepSequence.size}")
     //   printDebug("Steps",wholeStepSequence)
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

/*
    private fun showNextStepOnUI(arrayList: ArrayList<Int>, index: Int, cells: ArrayList<MemoryCell2>){
        val footview: TextView = activity.findViewById(R.id.whichSymbolsTextView)
        if (index < arrayList.size){
          //  footview.text = giveClueFromCell(gameDifficulty,cells[arrayList[index]-1]);
        }

    }

 */

    private fun setStepSymbolsOnUI(arrayList: ArrayList<String>, activeIndex: Int){
        val footview: TextView = activity.findViewById(R.id.whichSymbolsTextView)
        var color: Int

        if (arrayList.size < 1) return

        footview.text = ""

        for (i in 0 until arrayList.size){
            val indexText = SpannableString(arrayList[i] + " ")
            if (i == activeIndex) color = textcolorSelected
            else color = textcolorNonSelected

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
        correctMoveLogic()
        setFoot(wholeStepFooting[Moves.size-1], cell)
    }


    private fun incorrectCellClicked(cell: MemoryCell2){
        //val convertedStep = wholeStepSequenceInRows[currentSequenceRow][stepsInCurrentCycle.size-1]
        incorrectMoveLogic()
        val correctIndex = wholeStepSequence[Moves.size-1]
        //printDebug(correctIndex)
       //printDebug(board.Cells[correctIndex-1].index)
        board.Cells[correctIndex-1].flashCorrect()
        cell.flashMistake()
        setFoot(cell)
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


    fun newGame(){
        board.leftFoot = -1
        board.rightFoot = -1
        board.removeOldFeet()
        Points = 0
        lastFootLeft = false
        gamePlayed = false
        gameOn = false
        gamePaused = false
        getAndShowRightFootOnUi(wholeStepFooting, 0)
        generateStepSymbols(gameSymbolAmount, gameDifficulty)
        clearTimer()

        Moves.clear()
        if (showStepsToggle(true)) {
            showStepsToggle()
        }
        clearPrintDebug()
       // resetStreak()
        updatePointsAndStreaks()
    }


    fun stopGame() {
        clearJobs()
        stopTimer()
        gamePaused = true
        changeNewGameButton()
    }


    fun startGame() {
        startTimer()
        gameOn = true
        gamePaused = false
        changeNewGameButton()
    }


    override fun handleStartGameButton() {
        Log.i("start game button","gameOn: "+gameOn+" gamePaused: "+gamePaused)
        if (!gameOn){
            //newGame()
            startGame()
        } else if (gameOn && gamePaused){
            gameEnd()
            newGame()
            resetStreak()
        } else if (gameOn && !gamePaused){
            stopGame()
            resetStreak()
        }
    }

    private fun clearJobs(){
        timerJob?.cancel()
    }


    override fun gameEnd() {
        gameFinished()
        Log.i("finished","game finished" + Moves.count())
        gameOn = false
        //stopTimer()
        clearJobs()
        gamePaused = false
        gamePlayed = true
        changeNewGameButton()
        Moves.clear()

    }


    private fun checkGameState(): String{
        //  changeNewGameButton()
        if (!gameOn && !gamePlayed) {
            return "preGame"
            // changeNewGameButton()
        } else if (gameOn && !gamePaused) {
            if (hasGameEnded()) {
                gameEnd()
                return "end"
            }
            return "game on"
        } else if (gameOn && gamePaused){
            // gameEnd()
            return "user ended"
        }

        return "preGame"
    }

    private fun hasGameEnded(): Boolean{
        if (Moves.size >= wholeStepSequence.size){
            return true
        }
        return false
    }


    override fun showStepsToggle(query: Boolean): Boolean {
        return flashSequence(wholeStepSequence, query)
    }

    override  fun  clickReceiver(cell: MemoryCell2){


        if (showStepsToggle(true)) {
            showStepsToggle()
        } else {
            /*
            Moves.add(cell.index)
            compareStepThirdIteration(cell)
            showNextStepOnUI(wholeStepSequence, Moves.size,board.Cells);

             */
            val gamestate = checkGameState()
            Log.i("Status","gameOn: "+gameOn+ " gamePaused: "+gamePaused+ " gamePlayed: "+gamePlayed +" gamestate: "+gamestate)
            if (gamestate == "end"){
                Moves.add(cell.index)
                compareStepThirdIteration(cell)
            } else if (gamestate == "game on"){
                Moves.add(cell.index)
                compareStepThirdIteration(cell)
                if (hasGameEnded()) gameEnd()
            } else if (gamestate == "user ended" || gamestate == "preGame"){
                cell.flashBGColor(cell.highlightBgColor)
            }
        }
        Log.i("Streak",Streak.toString() + " " +BestStreak.toString())
        /*
        printDebug("row",6-cell.row)
        printDebug("column",cell.column+1)
        printDebug("letter",cell.letter)
        printDebug("color",cell.color)
        printDebug("number",cell.index)

         */


    }


}