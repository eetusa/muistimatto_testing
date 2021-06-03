package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

abstract class Game : ViewModel() {

     @SuppressLint("StaticFieldLeak")
     lateinit var board: Board;
     @SuppressLint("StaticFieldLeak")
     lateinit var context: Context
     @SuppressLint("StaticFieldLeak")
     lateinit var activity: Activity
     lateinit var settings: Bundle;

     var gameOn: Boolean = false
     var gamePaused: Boolean = false
     var gamePlayed: Boolean = false

     var wholeStepSequence = ArrayList<Int>()
     var wholeStepFooting: String = ""

     var indexOfLastClue = -1


     var timerJob: Job? = null

    var startTimeLong: Long = 0

     var Moves = ArrayList<Int>()
     var Points = 0
     var Streak = 0
     var BestStreak = 0
   //  var BestTime = ArrayList<Long>()
     var BestTime: Long = 0

     var chosenSubGame = 0

     var pointView: TextView? = null
     var streakView: TextView? = null
     var bestStreakView: TextView? = null
     var bestTimeView: TextView? = null
     var extraGameDataLabel: TextView? = null
     var extraGameDataValue: TextView? = null

     abstract fun clickReceiver(cell: MemoryCell2)
     abstract fun handleStartGameButton()
     abstract fun showStepsToggle(query: Boolean = false) : Boolean
     abstract fun gameEnd()

     fun checkBestStreak(){
          if (Streak > BestStreak) BestStreak = Streak
     }


     fun correctMoveLogic(){
          Points++
          Streak++
          checkBestStreak()
          updatePointsAndStreaks()
     }


     fun incorrectMoveLogic(){
          resetStreak()
          updatePointsAndStreaks()
     }

     fun resetStreak(){
          Streak = 0
     }


     fun gameFinished(){
          var time = stopTimer()
          if (Moves.count() >= wholeStepSequence.count()) {
               updateBestTime(time)
               updatePointsAndStreaks()
          }
     }

    fun touchDown(cell: MemoryCell2){

        cell.isBeingPressed = true
        clickReceiver(cell)
    }

    fun touchUp(cell: MemoryCell2){

        if (cell.isBeingPressed){
            
            cell.isBeingPressed = false
            cell.removeFoot()
        }
    }

     private fun updateBestTime(time: Long){
          if (time < BestTime || BestTime == 0L){
               BestTime = time

          }
     }


     fun startTimer(){
         startTimeLong = System.currentTimeMillis()

         //startTime = Instant.now()
         writeToMidTopLeft("0.00")
         timerJob = viewModelScope.launch {

              while(true){
                   delay(10L)
                   writeToMidTopLeft(millisecondsReFormat(getTimeFromStart()))
              }

         }

    }

     private fun getTimeFromStart(): Long{
         // return Instant.now().toEpochMilli() - startTime.toEpochMilli()
         return (System.currentTimeMillis() - startTimeLong)
     }


     fun millisecondsReFormat(timedif: Long): String{
          var td = timedif.toString()
          if (td.length>2){
               var latter = td.takeLast(3)
               var pre = td.dropLast(3)
               return pre+"."+latter.substring(0,2)
          }

          return timedif.toString()
     }

     fun hideShowStepSequenceButton(){
          val btn = activity.findViewById<Button>(R.id.show_step_sequence)
          if (btn != null) btn.visibility = GONE

          val spinner = activity.findViewById<Spinner>(R.id.chooseStepSequence)
          if (spinner != null) spinner.visibility = GONE
     }

     private fun writeToMidTopLeft(time: String){
          val footview: TextView = activity.findViewById(R.id.timerTextView)

          footview.text = time

     }


     fun stopTimer(): Long{
          timerJob?.cancel()
          var time = getTimeFromStart()
          writeToMidTopLeft(millisecondsReFormat(time))
          return time
     }
     fun clearTimer(){
          timerJob?.cancel()
          writeToMidTopLeft("0.00")
     }
     fun indicateRow(){
          val row = getOrderRow()
          Log.i("row ind",row.toString())
          when (row) {
               1 -> {
                    val indicator: View = activity.findViewById(R.id.testIndicator0)
                    setIndicator(indicator)
               }
               2 -> {
                    val indicator: View = activity.findViewById(R.id.testIndicator1)
                    setIndicator(indicator)
               }
               3 -> {
                    val indicator: View = activity.findViewById(R.id.testIndicator2)
                    setIndicator(indicator)
               }
               4 -> {
                    val indicator: View = activity.findViewById(R.id.testIndicator3)
                    setIndicator(indicator)
               }
               5 -> {
                    val indicator: View = activity.findViewById(R.id.testIndicator4)
                    setIndicator(indicator)
               }
               6 -> {
                    val indicator: View = activity.findViewById(R.id.testIndicator5)
                    setIndicator(indicator)
               }
          }
          clearIndicatorRowsApartFrom(row)
     }
     private fun getOrderRow(): Int{
          if (Moves.size < wholeStepSequence.size){
               return (wholeStepSequence[Moves.size]-1)/board.columns+1
          }

          return -1
     }
     fun clearIndicatorRowsApartFrom(row: Int){
          if (row != 1){
               activity.findViewById<View>(R.id.testIndicator0).background = null
          }

          if (row != 2) {
               activity.findViewById<View>(R.id.testIndicator1).background = null
          }

          if (row != 3) {
               activity.findViewById<View>(R.id.testIndicator2).background = null
          }
          if (row != 4) {
               activity.findViewById<View>(R.id.testIndicator3).background = null
          }
          if (row != 5) {
               activity.findViewById<View>(R.id.testIndicator4).background = null
          }
          if (row != 6) {
               activity.findViewById<View>(R.id.testIndicator5).background = null
          }
     }
     private fun setIndicator(ind: View){
          ind.background = ResourcesCompat.getDrawable(context.resources, R.drawable.row_indicator, null)
     }
     fun changeNewGameButton(){
          val btn: Button = activity.findViewById(R.id.start_new_game)
          if (btn!=null){
               if (!gameOn){
                    btn.text = activity.resources.getText(R.string.start_new_game)
               } else if (gameOn && gamePaused){
                    btn.text = activity.resources.getText(R.string.startGameButton)
               } else if (gameOn && !gamePaused){
                    btn.text = activity.resources.getText(R.string.end_game)
               }
          }
     }

     fun updatePointsAndStreaks(){
          if (pointView == null)pointView = activity.findViewById(R.id.pointsAmount)
          if (streakView == null)streakView = activity.findViewById(R.id.streakAmount)
          if (bestStreakView == null)bestStreakView = activity.findViewById(R.id.bestStreakAmount)
          if (bestTimeView == null)bestTimeView = activity.findViewById(R.id.bestTime)


          pointView?.text = Points.toString()
          streakView?.text = Streak.toString()
          bestStreakView?.text = BestStreak.toString()
          bestTimeView?.text = millisecondsReFormat(BestTime)

     }

     fun setExtraGameData(label: String, value: String){
          if (extraGameDataLabel == null)extraGameDataLabel = activity.findViewById(R.id.extraGameDataLabel)
          if (extraGameDataValue == null)extraGameDataValue = activity.findViewById(R.id.extraGameDataValue)
          extraGameDataLabel?.text = label;
          extraGameDataValue?.text = value;


     }
}