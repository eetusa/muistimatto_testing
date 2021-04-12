package com.example.myapplication

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.textservice.TextInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import org.w3c.dom.Text


/**
 * Custom textview-class for board cells
 */
class MemoryCell2 : ConstraintLayout {

    var row = 0
    var column = 0
    private val cellAlphabet = "ABCDEFGHIJKLMNOPQRSTUVXY"
    private var letter = ""
    var index = 0


    private var board: Board? = null
    private var defaultColor: Int = Color.parseColor("#808080")
    private var defaultAlphabetColor: Int = defaultColor
    var bg: Drawable? = null
    var bg_active: Drawable? = null
    private var contentView: TextView = TextView(context)
    private var Colors: IntArray = intArrayOf(
        Color.parseColor("#eeab04"), Color.parseColor("#76685f"), Color.parseColor(
            "#d80731"
        ), Color.parseColor("#03a1e0")
    )
    var selectedColor = defaultColor;
    var defaultBgColor = ContextCompat.getColor(context, R.color.cell_background_default)



    constructor(context: Context, row: Int, column: Int, board: Board) : super(context) {
        // init(null, 0)

        this.row = row
        this.column = column
        this.index = 21 - (row * 4) + column
        this.board = board



        try {
            this.letter = cellAlphabet.substring(index - 1, index);
        } catch (e: Exception) {
            this.letter = "?"
        }
        initSettings()

    }

    constructor(context: Context, row: Int, column: Int) : super(context) {
        // init(null, 0)

        this.row = row
        this.column = column
        this.index = 21 - (row * 4) + column
        try {
            this.letter = cellAlphabet.substring(index - 1, index);
        } catch (e: Exception) {
            this.letter = "?"
        }
        initSettings()

    }


    private fun initSettings(){
        val tvparam = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
        tvparam.weight = 1f
        this.layoutParams = tvparam
      //  this.setPadding(10,10,10,10)

        val cwparam = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        cwparam.setMargins(20,20,20,20)
        contentView.layoutParams = cwparam

        contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);
        contentView.gravity = Gravity.RIGHT or Gravity.TOP
        contentView.setPadding(0, 10, 20, 0)

        initBg()
       //setDefaultBg()
        setAlphabetColor()
        setCellText()
        this.addView(contentView)


        this.setOnClickListener(){
            board?.clickReceiver(this)
        }

    }

    private fun setCellText(){
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
    }

    private fun initBg(){
        if (column < 2){
            selectedColor = ContextCompat.getColor(context, R.color.cell_background_active_left)
        } else {
            selectedColor = ContextCompat.getColor(context, R.color.cell_background_active_right)
        }
        if (bg == null){
            if (this.column == 0){
                if (this.row == 0){
                    bg = ResourcesCompat.getDrawable(resources, R.drawable.top_left_border, null)
                    bg_active = ResourcesCompat.getDrawable(resources, R.drawable.top_left_border, null)
                } else if (this.row == 5){
                    bg = ResourcesCompat.getDrawable(resources, R.drawable.bottom_left_border, null)
                    bg_active = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.bottom_left_active_border,
                        null
                    )
                } else {
                    bg = ResourcesCompat.getDrawable(resources, R.drawable.left_mid_border, null)
                    bg_active = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.left_mid_active_border,
                        null
                    )
                }
            } else if ( this.column == 1 ){
                if (this.row == 0){
                    bg = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.top_mid_left_border,
                        null
                    )
                    bg_active = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.top_mid_left_active_border,
                        null
                    )
                } else if (this.row == 5){
                    bg = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.bottom_mid_left_border,
                        null
                    )
                    bg_active = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.bottom_mid_left_active_border,
                        null
                    )
                } else {
                    bg = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.mid_mid_left_border,
                        null
                    )
                    bg_active = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.mid_mid_left_active_border,
                        null
                    )
                }
            } else if ( this.column == 2 ){
                if (this.row == 0){
                    bg = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.top_mid_right_border,
                        null
                    )
                    bg_active = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.top_mid_right_active_border,
                        null
                    )
                } else if (this.row == 5){
                    bg = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.bottom_mid_right_border,
                        null
                    )
                    bg_active = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.bottom_mid_right_active_border,
                        null
                    )
                } else {
                    bg = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.mid_mid_right_border,
                        null
                    )
                    bg_active = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.mid_mid_right_active_border,
                        null
                    )
                }
            } else if ( this.column == 3 ){
                if (this.row == 0){
                    bg = ResourcesCompat.getDrawable(resources, R.drawable.top_right_border, null)
                    bg_active = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.top_right_active_corner,
                        null
                    )
                } else if (this.row == 5){
                    bg = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.bottom_right_border,
                        null
                    )
                    bg_active = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.bottom_right_active_border,
                        null
                    )
                } else {
                    bg = ResourcesCompat.getDrawable(resources, R.drawable.right_mid_border, null)
                    bg_active = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.right_mid_active_border,
                        null
                    )
                }
            }

        }
        this.background = bg;
    }

    fun setDefaultBg(){
        val valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        valueAnimator.duration = 325
        valueAnimator.addUpdateListener { valueAnimator ->
            val fractionAnim = valueAnimator.animatedValue as Float
            contentView.setBackgroundColor(
                ColorUtils.blendARGB(
                    selectedColor,
                    defaultBgColor,
                    fractionAnim
                )
            )
        }
        valueAnimator.start()
        //this.bg = bg
    }

    fun setActiveBg(){
        // var fggw: LayerDrawable = resources.getDrawable(R.drawable.top_left_border) as LayerDrawable
        // var d1: Drawable = fggw.findDrawableByLayerId(R.id.ggwp)
        //d1.


        val valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        valueAnimator.duration = 200
        valueAnimator.addUpdateListener { valueAnimator ->
            val fractionAnim = valueAnimator.animatedValue as Float
            contentView.setBackgroundColor(
                ColorUtils.blendARGB(
                    defaultBgColor,
                    selectedColor,
                    fractionAnim
                )
            )
        }
        valueAnimator.start()

        //this.background = bg_active


    }


    fun setAlphabetColor(){
        if ( (this.column + this.row * 1) % 4 == 0){
            defaultAlphabetColor = Colors[3]
        } else if ((this.column + this.row * 1)% 4 == 1){
            defaultAlphabetColor = Colors[0]
        }else if ((this.column + this.row * 1)% 4 == 2){
            defaultAlphabetColor = Colors[1]
        }else if ((this.column + this.row * 1)% 4 == 3){
            defaultAlphabetColor = Colors[2]
        }
    }
    public fun resetTextColor(){
        contentView.setTextColor(defaultColor)
    }



}