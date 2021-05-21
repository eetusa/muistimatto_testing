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
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random


class StepSequenceGame : Game{

    private var board: Board;
    var context: Context
    private var activity: Activity
    private var settings: Bundle;
    private var debugLayout: LinearLayout;
    private var debugTextView: TextView;

    private var gameDifficulty = 2
    private var gameSymbolAmount = 2

    private var indexOfLastClue = -1



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
    var playingSoundSequence: Job? = null

    var directionIsUp = true
    var stepCycleIndex: Int = 1
    var currentCycleRow = 1;
    var rowsOverInCycle = 0
    var stepsInCurrentCycle = ArrayList<Int>()

    var harmaaSound: AssetFileDescriptor? = null
    var punainenSound: AssetFileDescriptor? = null
    var sininenSound: AssetFileDescriptor? = null
    var keltainenSound: AssetFileDescriptor? = null
    var player: MediaPlayer? = null

    var elementSoundsArray = ArrayList<ArrayList<AssetFileDescriptor>>() // väri, kirjain, numero, symboli
    var orderSoundsArray = ArrayList<ArrayList<AssetFileDescriptor>>()

    var mediaPlayerQueue = ArrayList<MediaPlayer>()
    var mediaPlayerJobQueue = ArrayList<Job>()

    private var stepSymbols = ArrayList<String>()


    private var textcolor_nonselected: Int = 0
    private var textcolor_selected: Int = 0

    private var Moves = ArrayList<Int>()
    private var Points = 0

    var tts: TextToSpeech? = null

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

        initializeGame()
        initializeSoundsAndPlayer()

        tts = TextToSpeech(context, TextToSpeech.OnInitListener { i ->
            if (i == TextToSpeech.SUCCESS) {
                val result = tts!!.setLanguage(Locale.ENGLISH)
                if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    for (item in tts!!.availableLanguages){
                        println(item.language)
                    }
                    Log.e("TTS", "Language Not Supported")
                } else {
                   // tts!!.setSpeechRate(0.5f)
                    ConvertTextToSpeech("Game Start")
                    Log.e("TTS", "vittu")
                }
            } else {
                Log.e("TTS", "Initialization Failed " + i.toString())
            }
        })
    }

    fun handleStartButton(){
        if (gameOn){

        } else {
          //   val startbtn
        }
    }

    fun initializeSoundsAndPlayer(){

        player = MediaPlayer()

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




        playSound(elementSoundsArray[0],0)

    }

    fun playSound(soundArray: ArrayList<AssetFileDescriptor>, index: Int){
        try{

            if (soundArray != null){
                val playerx = MediaPlayer()
                playerx.setOnCompletionListener {
                    playerx.reset()
                    playerx.release()
                    mediaPlayerQueue.remove(playerx)
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

    fun playSoundsInSequence(soundArray: ArrayList<AssetFileDescriptor>, runningIndex: Int){
        //playingSoundSequence?.cancel()

        try{
            if (soundArray != null){
                if (soundArray.count() <= runningIndex) return

                var durations: ArrayList<Int> = getDurations(soundArray)
                Log.i("durations",  "Count: "+durations.count().toString())
                for (item in durations){
                    Log.i("durations",item.toString())
                }
                mediaPlayerJobQueue.add(viewModelScope.launch {
                   // Log.i("recursive sound play",runningIndex.toString()+  " " + soundArray.count().toString())
                 //   playSound(soundArray,runningIndex)

                  //  player?.setOnCompletionListener {
                  //      playSoundsInSequence(soundArray,runningIndex+1)
                        //player?.setOnCompletionListener {  }
                  //   }
                    /*
                    player?.setOnCompletionListener { fun onCompletion(player: MediaPlayer){
                        playSound(soundArray,1)
                        player?.setOnCompletionListener {  }
                    } }

                     */

                    for ((index, item) in soundArray.withIndex()){
                        playSound(soundArray, index)
                        delay((durations[index]*0.8).toLong())

                    }

                })

               // mediaPlayerJobQueue.add(this)


            }
        } catch (e: java.lang.Exception){
            e.message?.let { Log.i("sound", it) }
        }
    }

    private fun getDurations(soundArray: ArrayList<AssetFileDescriptor>): ArrayList<Int>{
        var durations = ArrayList<Int>()
        for (item in soundArray){
          //  if (player?.isPlaying == true) player?.stop()
          //  player?.reset()
          //  player?.setDataSource(item.fileDescriptor, item.startOffset, item.length)
           // player?.duration?.let { durations.add(it) }
             //  var file = getFileFromAssets(context,"harmaa.mp3")
               var dur = getDuration(item)
            if (dur != null) {
                durations.add(dur.toInt())
            }
           // durations.add(item.declaredLength.toInt())
        }
       // player?.reset()
        return durations
    }
    private fun getDuration(file: AssetFileDescriptor): String? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(file.fileDescriptor,file.startOffset,file.length);
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    }

    @Throws(IOException::class)
    fun getFileFromAssets(context: Context, fileName: String): File = File(context.cacheDir, fileName)
        .also {
            if (!it.exists()) {
                it.outputStream().use { cache ->
                    context.assets.open(fileName).use { inputStream ->
                        inputStream.copyTo(cache)
                    }
                }
            }
        }

    fun ConvertTextToSpeech(text2: String) {
        // TODO Auto-generated method stub
        Log.i("voice","testing")
        var text = text2

        if (text == null || "" == text) {
            text = "Content not available"
            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        } else tts!!.speak(text.toString(), TextToSpeech.QUEUE_FLUSH, null)
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

    private fun stopMediaPlayers(){
        for (player in mediaPlayerQueue){
            if (player!=null){
                player.stop()
           }
        }
        mediaPlayerQueue.clear()

        for (playerJob in mediaPlayerJobQueue){
            if (playerJob!=null){
                playerJob.cancel()
                Log.i("job","canceled")
            }
        }
        mediaPlayerJobQueue.clear()
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

        if (tts!==null){
            //ConvertTextToSpeech(clueArray[random])
        }
        return clueArray[random];
    }

    private fun playSoundFromCell(cell: MemoryCell2, type: Int){

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
                temporaryArray.add(orderSoundsArray[0][5-cell.row])
                temporaryArray.add(orderSoundsArray[2][0])
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

    fun newGame(){
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

    fun stopGame() {
        TODO("Not yet implemented")
    }

    fun startGame() {
        TODO("Not yet implemented")
    }

    override fun handleStartGameButton() {
        TODO("Not yet implemented")
    }

    override fun gameEnd() {
        TODO("Not yet implemented")
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
            showNextStepOnUI(wholeStepSequence, Moves.size,board.Cells);
        }
        printDebug("row",6-cell.row)
        printDebug("column",cell.column+1)
        printDebug("letter",cell.letter)
        printDebug("color",cell.color)
        printDebug("number",cell.index)


    }


}