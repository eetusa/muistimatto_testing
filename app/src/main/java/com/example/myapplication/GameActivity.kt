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
    var game: GameHandler? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)

        if (intent.extras != null){
            startGame(intent.extras!!)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        game!!.shutDownGame()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun startGame(savedInstanceState: Bundle){
        //val board: Board = createBoard()
        game = GameHandler(this, this, savedInstanceState)
        initiateListeners(game!!)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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




}