package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var gamemode: Int = 0;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        initComponents()


    }

    private fun initComponents(){
        val startButton: Button = findViewById(R.id.startGameButton)
        startButton.setOnClickListener{
            handleStartButtonClick()
        }

        val spinner: Spinner = findViewById(R.id.chooseGameMode)
        ArrayAdapter.createFromResource(
                this,
            R.array.gamemodes_array,
            android.R.layout.simple_spinner_item
                ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = this
    }



    private fun handleStartButtonClick(){
        startGameActivity()
    }

    fun startGameActivity(){
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        gamemode = pos
        val startButton: Button = findViewById(R.id.startGameButton)
        startButton.text = resources.getStringArray(R.array.gamemodes_array)[pos]?.toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

}

