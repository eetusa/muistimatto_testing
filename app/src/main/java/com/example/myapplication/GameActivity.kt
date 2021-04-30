package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout

class GameActivity : AppCompatActivity() {

    private var settings = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)


        if (intent.extras != null){
            startGame(intent.extras!!)
        }




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


}