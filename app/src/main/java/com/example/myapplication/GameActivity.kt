package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        createBoardClass()
    }

    private fun createBoardClass(){
        val testLayout: LinearLayout = findViewById(R.id.testLayout)
        val board = Board(this, this)
        testLayout.addView(board)

    }
}