package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.sql.Time
import java.time.Instant
import java.time.LocalTime
import java.time.Period
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random


class OrderSequenceGame : Game{

    private var board: Board;
    var context: Context
    private var activity: Activity
    private var settings: Bundle;
    private var debugLayout: LinearLayout;
    private var debugTextView: TextView;

    private var gameDifficulty = 2

    private var indexOfLastClue = -1

    var wholeStepSequence = ArrayList<Int>()
    var wholeStepFooting: String = ""

    var currentSequenceRow: Int = -1

    var lastFootLeft: Boolean = false

    var showingStepSequence: Job? = null
    var timerJob: Job? = null

    var stepCycleIndex: Int = 1
    var currentCycleRow = 1;
    var stepsInCurrentCycle = ArrayList<Int>()

    var elementSoundsArray = ArrayList<ArrayList<AssetFileDescriptor>>() // väri, kirjain, numero, symboli
    var orderSoundsArray = ArrayList<ArrayList<AssetFileDescriptor>>()

    var mediaPlayerQueue = ArrayList<MediaPlayer>()
    var mediaPlayerJobQueue = ArrayList<Job>()

    private var textcolor_nonselected: Int = 0
    private var textcolor_selected: Int = 0

    private var Moves = ArrayList<Int>()
    private var Points = 0

    private var gameOn: Boolean = false
    private var gamePlayed: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    private var startTime = Instant.now()




    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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

        initializeSounds()
        initializeGame()

    }

    private fun initializeSounds(){


        if (elementSoundsArray!=null)elementSoundsArray.clear()

        elementSoundsArray.add(ArrayList<AssetFileDescriptor>()) // väri (keltainen, harmaa, punainen, sininen)
        elementSoundsArray.add(ArrayList<AssetFileDescriptor>()) // kirjain (a, b,..)
        elementSoundsArray.add(ArrayList<AssetFileDescriptor>()) // numero (1, 2,...)
        elementSoundsArray.add(ArrayList<AssetFileDescriptor>()) // symboli (vesi, tuli, valo, kivi)

        orderSoundsArray.add(ArrayList<AssetFileDescriptor>()) // (ensimmäisen, toisen..)
        orderSoundsArray.add(ArrayList<AssetFileDescriptor>()) // (ensimmäinen, toinen..)
        orderSoundsArray.add(ArrayList<AssetFileDescriptor>()) // rivin
        orderSoundsArray.add(ArrayList<AssetFileDescriptor>()) // ruutu

        var letters = arrayListOf<String>("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","x","y")


        try{
            elementSoundsArray[0].add(context.assets.openFd("sounds/keltainen.mp3"))
            elementSoundsArray[0].add(context.assets.openFd("sounds/harmaa.mp3"))
            elementSoundsArray[0].add(context.assets.openFd("sounds/punainen.mp3"))
            elementSoundsArray[0].add(context.assets.openFd("sounds/sininen.mp3"))



            for (letter in letters){
                elementSoundsArray[1].add(context.assets.openFd("sounds/$letter.mp3"))
            }

            for (i in 1..10){
                elementSoundsArray[2].add(context.assets.openFd("sounds/$i.mp3"))
            }
            elementSoundsArray[2].add(context.assets.openFd("sounds/toista.mp3"))
            elementSoundsArray[2].add(context.assets.openFd("sounds/20.mp3"))


            elementSoundsArray[3].add(context.assets.openFd("sounds/valo.mp3"))
            elementSoundsArray[3].add(context.assets.openFd("sounds/kivi.mp3"))
            elementSoundsArray[3].add(context.assets.openFd("sounds/tuli.mp3"))
            elementSoundsArray[3].add(context.assets.openFd("sounds/vesi.mp3"))

            orderSoundsArray[0].add(context.assets.openFd("sounds/ensimmaisen.mp3"))
            orderSoundsArray[0].add(context.assets.openFd("sounds/toisen.mp3"))
            orderSoundsArray[0].add(context.assets.openFd("sounds/kolmannen.mp3"))
            orderSoundsArray[0].add(context.assets.openFd("sounds/neljannen.mp3"))
            orderSoundsArray[0].add(context.assets.openFd("sounds/viidennen.mp3"))
            orderSoundsArray[0].add(context.assets.openFd("sounds/kuudennen.mp3"))
            orderSoundsArray[1].add(context.assets.openFd("sounds/ensimmainen.mp3"))
            orderSoundsArray[1].add(context.assets.openFd("sounds/toinen.mp3"))
            orderSoundsArray[1].add(context.assets.openFd("sounds/kolmas.mp3"))
            orderSoundsArray[1].add(context.assets.openFd("sounds/neljas.mp3"))
            orderSoundsArray[1].add(context.assets.openFd("sounds/viides.mp3"))
            orderSoundsArray[1].add(context.assets.openFd("sounds/kuudes.mp3"))
            orderSoundsArray[2].add(context.assets.openFd("sounds/rivin.mp3"))
            orderSoundsArray[3].add(context.assets.openFd("sounds/ruutu.mp3"))

        } catch (e: java.lang.Exception){
            e.message?.let { Log.e("Sounds", it) }
        }


        //playSound(elementSoundsArray[0],0)

    }

    private fun playSound(soundArray: ArrayList<AssetFileDescriptor>, index: Int){
        try{
            if (soundArray != null){
                val playerx = MediaPlayer()
                playerx.setOnCompletionListener {
                    playerx.reset()
                    playerx.release()
                    try{
                        mediaPlayerQueue.remove(playerx)
                    } catch(e: java.lang.Exception){
                        e.message?.let { it1 -> Log.e("sound", it1) }
                    }

                }
                mediaPlayerQueue.add(playerx)
                if (index < soundArray.count()){

                    var sound = soundArray[index]

                    if (sound != null) {
                        //if (playerx.isPlaying == true) playerx.stop()

                        //  playerx.reset()
                        playerx.setDataSource(sound.fileDescriptor, sound.startOffset, sound.length)
                        playerx.prepare()
                        playerx.start()
                        // Log.i("sound duration", player?.duration.toString())
                    }
                }
            }
        } catch (e: java.lang.Exception){
            e.message?.let { Log.i("sound", it) }
        }


    }

    private fun playSoundsInSequence(soundArray: ArrayList<AssetFileDescriptor>, runningIndex: Int){

        try{
            if (soundArray != null){
                if (soundArray.count() <= runningIndex) return
                var durations: ArrayList<Int> = getDurations(soundArray)
                Log.i("durations",  "Count: "+durations.count().toString())
                for (item in durations){
                    Log.i("durations",item.toString())
                }
                mediaPlayerJobQueue.add(viewModelScope.launch {

                    for ((index, item) in soundArray.withIndex()){
                        playSound(soundArray, index)
                        delay((durations[index]*0.8).toLong())

                    }

                })

            }
        } catch (e: java.lang.Exception){
            e.message?.let { Log.i("sound", it) }
        }
    }

    private fun getDurations(soundArray: ArrayList<AssetFileDescriptor>): ArrayList<Int>{
        var durations = ArrayList<Int>()
        for (item in soundArray){
            var dur = getDuration(item)
            if (dur != null) {
                durations.add(dur.toInt())
            }
        }
        return durations
    }
    private fun getDuration(file: AssetFileDescriptor): String? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(file.fileDescriptor,file.startOffset,file.length);
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    }

    private fun initializeGame() {
        val mode: Int = settings.getInt("gamemode")
        val testLayout: LinearLayout = activity.findViewById(R.id.testLayout)

        testLayout.addView(board)

        // getAndShowRightFootOnUi(wholeStepFooting, 0)
       // generateStepSymbols(gameSymbolAmount,gameDifficulty)
        // setStepSymbolsOnUI(stepSymbols, 0)
        createRandomStepSequence()
        showNextStepOnUI(wholeStepSequence, Moves.size,board.Cells);
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
                // getAndShowRightFootOnUi(wholeStepFooting, Moves.size)
                // setStepSymbolsOnUI(stepSymbols,((Moves.size)%stepSymbols.size))
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






    private fun stopMediaPlayers(){

        for (playerJob in mediaPlayerJobQueue){
            if (playerJob!=null){
                playerJob.cancel()
                Log.i("job","canceled")
            }
        }
        mediaPlayerJobQueue.clear()

        for (player in mediaPlayerQueue){
            if (player!=null){
                player?.stop()
                player?.reset()
                player?.release()
                
            }
        }
        mediaPlayerQueue.clear()

    }

    private fun giveClueFromCell(dif: Int, cell: MemoryCell2): String{
        stopMediaPlayers()
        Log.i("Calling clue","calling")
        val clueArray = ArrayList<String>()
        val orderRowNumbers: Array<String> = arrayOf("ensimmäisen","toisen","kolmannen","neljännen","viidennen","kuudennen")
        val orderColumnNumbers: Array<String> = arrayOf("ensimmäinen","toinen","kolmas","neljäs")
        val columnAndRow: String = orderRowNumbers[5-cell.row] + " rivin " + orderColumnNumbers[cell.column] + " ruutu"
        val toistaIndex = 10

        clueArray.add(cell.color)
        clueArray.add(cell.letter)
        clueArray.add(cell.index.toString())
        clueArray.add(cell.symbol)

        clueArray.add(columnAndRow)
        var random = -2
        while(true){
            random = Random.nextInt(clueArray.size)
            if (random != indexOfLastClue){
                indexOfLastClue = random;
                break;
            }
        }

        playSoundFromCell(cell, random)
        return clueArray[random];
    }

    private fun playSoundFromCell(cell: MemoryCell2, type: Int){

        if (elementSoundsArray == null) return
        if (elementSoundsArray.count() == 0) return

        val toistaIndex = 10
        val kaksikymmentaIndex = toistaIndex+1


        if (type == 0 || type == 3){
            playSound(elementSoundsArray[type], cell.colorIndex)
        } else if (type == 1) {
            playSound(elementSoundsArray[type], cell.index - 1)
        }else if (type == 2){
            if (cell.index < 11){
                playSound(elementSoundsArray[type], cell.index-1)
            } else if (cell.index in 11..19){
                var temporaryArray = ArrayList<AssetFileDescriptor>()
                var firstIndex = cell.index-11
                try{
                    temporaryArray.add(elementSoundsArray[2][firstIndex])
                    temporaryArray.add(elementSoundsArray[2][toistaIndex])
                    playSoundsInSequence(temporaryArray,0)
                } catch (e: java.lang.Exception){
                    e.message?.let { Log.e("sound clue", it) }
                }

            } else if (cell.index == 20){
                playSound(elementSoundsArray[type], kaksikymmentaIndex)
            } else if (cell.index > 20){
                var temporaryArray = ArrayList<AssetFileDescriptor>()
                var secondIndex = cell.index-21
                try{
                    temporaryArray.add(elementSoundsArray[2][kaksikymmentaIndex])
                    temporaryArray.add(elementSoundsArray[2][secondIndex])
                    playSoundsInSequence(temporaryArray,0)
                } catch (e: java.lang.Exception){
                    e.message?.let { Log.e("sound clue", it) }
                }
            }
        } else if (type == 4){
            var temporaryArray = ArrayList<AssetFileDescriptor>()
            try{
              //  temporaryArray.add(orderSoundsArray[0][5-cell.row])
              //  temporaryArray.add(orderSoundsArray[2][0])
                temporaryArray.add(orderSoundsArray[1][cell.column])
                temporaryArray.add(orderSoundsArray[3][0])
                playSoundsInSequence(temporaryArray,0)
            } catch (e: java.lang.Exception){
                e.message?.let { Log.e("sound clue", it) }
            }
        }
    }

    private fun showNextStepOnUI(arrayList: ArrayList<Int>, index: Int, cells: ArrayList<MemoryCell2>){
        val footview: TextView = activity.findViewById(R.id.whichSymbolsTextView)
        //   Log.i("LOL",""+cells[arrayList[index]-1].index)
        if (index < arrayList.size){
            footview.text = giveClueFromCell(gameDifficulty,cells[arrayList[index]-1]);
        }
    }

    private fun clearNextStepOnUI(){
        val footview: TextView = activity.findViewById(R.id.whichSymbolsTextView)
        footview.text = ""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startTimer(){
        startTime = Instant.now()
        writeToMidTopLeft("0.00")
        timerJob = viewModelScope.launch {

            while(true){
                delay(100L)
                writeToMidTopLeft(getTimeFromStart())
            }

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTimeFromStart(): String{
        var timedif: Long = (Instant.now().toEpochMilli() - startTime.toEpochMilli())/1000L
        var td = (Instant.now().toEpochMilli() - startTime.toEpochMilli()).toString()
        if (td.length>2){
            var latter = td.takeLast(3)
            var pre = td.dropLast(3)
            return pre+"."+latter.substring(0,2)
        }

        return timedif.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopTimer(){
        timerJob?.cancel()
        writeToMidTopLeft(getTimeFromStart())
    }
    private fun clearTimer(){
        timerJob?.cancel()
        writeToMidTopLeft("0.00")
    }

    private fun writeToMidTopLeft(time: String){
        val footview: TextView = activity.findViewById(R.id.whichFootTextView)

        footview.text = time

    }


    private fun correctCellClicked(cell: MemoryCell2){
        cell.flashCorrect()
        Points++
        setFoot(cell)
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

    override fun newGame(){
        clearJobs()
        clearTimer()

        board.leftFoot = -1
        board.rightFoot = -1
        board.removeOldFeet()
        stepCycleIndex = 1
        currentCycleRow = 1
        Points = 0
        stepsInCurrentCycle.clear()
        currentSequenceRow = -1
        lastFootLeft = false
        gamePlayed = false
        gameOn = false



        Moves.clear()
        if (showStepsToggle(true)) {
            showStepsToggle()
        }
        clearPrintDebug()
        showNextStepOnUI(wholeStepSequence, Moves.size,board.Cells);
    }

    fun clearJobs(){
        timerJob?.cancel()
        if (mediaPlayerJobQueue != null){
            for (job in mediaPlayerJobQueue){
                job.cancel()
            }
        }
    }

    override fun showStepsToggle(query: Boolean): Boolean {
        return flashSequence(wholeStepSequence, query)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkGameState(){
        if (!gameOn && !gamePlayed) {
            startTimer()
            gameOn = true
        } else if (gameOn) {
            if (hasGameEnded()) gameEnd()
        }

    }

    private fun hasGameEnded(): Boolean{
        if (Moves.size >= wholeStepSequence.size-1){
            return true
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun gameEnd(){
        gameOn = false
        stopTimer()
        clearJobs()
        gamePlayed = true
        clearNextStepOnUI()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override  fun  clickReceiver(cell: MemoryCell2){

        if (showStepsToggle(true)) {
            showStepsToggle()
        } else {
            checkGameState()
            Moves.add(cell.index)
            compareStepThirdIteration(cell)
            showNextStepOnUI(wholeStepSequence, Moves.size,board.Cells);
        }
        printDebug("row",6-cell.row)
        printDebug("column",cell.column+1)
        printDebug("letter",cell.letter)
        printDebug("color",cell.color)
        printDebug("number",cell.index)


    }



}