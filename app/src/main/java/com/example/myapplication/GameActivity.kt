package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout

class GameActivity : AppCompatActivity() {

    private var settings = IntArray(6){-1}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val board: Board = createBoard()
        val game: Game = Game(this, this, board, settings)
    }

    private fun createBoard(): Board{
        val testLayout: LinearLayout = findViewById(R.id.testLayout)
        val board = Board(this, this)
        testLayout.addView(board)

        return board
    }
}