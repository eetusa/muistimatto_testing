package com.example.myapplication

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


class GameActivity : AppCompatActivity() {

    private var settings = ArrayList<Int>()
    var tts: TextToSpeech? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)


        if (intent.extras != null){
            startGame(intent.extras!!)
        }
/*
        tts = TextToSpeech(this) { status -> // TODO Auto-generated method stub
            if (status == TextToSpeech.SUCCESS) {
                val result = tts!!.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Log.e("error", "This Language is not supported")
                } else {
                    ConvertTextToSpeech("hello everyone")
                }
            } else Log.e("error", "Initilization Failed!")
        }



        tts = TextToSpeech(this, TextToSpeech.OnInitListener { i ->
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
                   ConvertTextToSpeech("neljä toista")
                    Log.e("TTS", "vittu")
                }
            } else {
                Log.e("TTS", "Initialization Failed " + i.toString())
            }
        })
 */



    }
/*
    private fun createBoard(): Board{
        val testLayout: LinearLayout = findViewById(R.id.testLayout)
        val board = Board(this, this)
        testLayout.addView(board)

        return board
    }

*/

    private fun startGame(savedInstanceState: Bundle){
        //val board: Board = createBoard()
        val game: GameHandler = GameHandler(this, this, savedInstanceState)
        initiateListeners(game)
    }

    private fun initiateListeners(game: GameHandler) {
        val newGame: Button = findViewById(R.id.start_new_game)
        newGame.setOnClickListener {
            game.startGame()
        }

        val showSequence: Button = findViewById(R.id.show_step_sequence)
        showSequence.setOnClickListener {
            game.showSteps()
        }
    }

    override fun onPause() {
        // TODO Auto-generated method stub
        //if (tts != null) {
       //     tts!!.stop()
       //     tts!!.shutdown()
      //  }
        super.onPause()
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


}