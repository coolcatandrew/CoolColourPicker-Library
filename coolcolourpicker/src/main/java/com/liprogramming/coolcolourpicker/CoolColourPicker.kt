package com.liprogramming.coolcolourpicker

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color.colorToHSV
import android.graphics.drawable.GradientDrawable
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.ImageView
import kotlinx.android.synthetic.main.picker_dialog.view.*
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import liprogramming.coolcolourpicker.R
import android.graphics.*

class CoolColourPicker {
    private var container: View
    private var coolColourView: CoolColourView
    private var hueBar: ImageView
    private var alphaBar: ImageView
    private var alphaBarBackground: ImageView
    private var oldColourBox: ImageView
    private var newColourBox: ImageView
    private var newColourTarget: ImageView
    private var hueTarget: ImageView
    private var alphaTarget: ImageView

    private var dialog: AlertDialog
    private var alphaEnabled: Boolean = false
    private var response: CoolColourResponse? = null
    var currentHue: Float = 1f //Hue is 0-360degrees; (0-60=red, 60-120=yellow, 120-180=green, 180-240=cyan, 240-300=blue, 300-360=magenta)
    var currentSaturation: Float = 1f //Saturation is the amount of grey in the colour (0-100%)
    var currentValue: Float = 1f //Value/Brightness is intensity of colour (0-100%)
    private var alpha: Int = 0

    /**
     * Add a CoolColourPicker.
     *
     * @param context activity context
     * @param colour default colour
     * @param response a CoolColourResponse used to determine action of 'Ok' or 'Cancel' click
     */
    constructor(context: Context, colour: Int, response: CoolColourResponse) : this(context, colour, false, response)

    /**
     * Add a CoolColourPicker.
     *
     * @param context activity context
     * @param colour default colour
     * @param response a CoolColourResponse used to determine action of 'Ok' or 'Cancel' click
     */
    constructor(context: Context, colour: Int, alphaEnabled: Boolean, response: CoolColourResponse) {
        var newColor = colour
        this.alphaEnabled = alphaEnabled
        this.response = response

        if (!alphaEnabled) { // remove alpha if not supported
            newColor = newColor or -0x1000000
        }

        colorToHSV(newColor, getCurrentHSV())
        alpha = Color.alpha(newColor)

        //set up elements
        val view: View = LayoutInflater.from(context).inflate(R.layout.picker_dialog, null, false)
        hueBar = view.colour_bar
        coolColourView = view.colour_view
        hueTarget = view.colour_pointer
        oldColourBox = view.oldColor
        newColourBox = view.newColor
        newColourTarget = view.target
        container = view.container
        alphaBar = view.alpha_bar
        alphaTarget = view.alpha_pointer
        alphaBarBackground = view.alpha_bar_background

        // hide or show alpha accordingly
        run {
            alphaBar.visibility = if (alphaEnabled) View.VISIBLE else View.GONE
            alphaTarget.visibility = if (alphaEnabled) View.VISIBLE else View.GONE
            alphaBarBackground.visibility = if (alphaEnabled) View.VISIBLE else View.GONE
        }

        //set colours
        setUpHueView()
        coolColourView.setHue(currentHue)
        oldColourBox.setBackgroundColor(newColor)
        newColourBox.setBackgroundColor(newColor)

        //set up onclick events
        coolColourView.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                var x = event.x
                var y = event.y

                if (x < 0f) { x = 0f } else if (x > coolColourView.measuredWidth) { x = coolColourView.measuredWidth.toFloat() }
                if (y < 0f) { y = 0f } else if (y > coolColourView.measuredHeight) { y = coolColourView.measuredHeight.toFloat() }

                currentSaturation = (1f / coolColourView.measuredWidth * x)
                currentValue = (1f - (1f / coolColourView.measuredHeight * y))

                // update view
                moveTarget()
                updateAlphaView()
                newColourBox.setBackgroundColor(getColour())
                return@OnTouchListener true
            }
            false
        })
        hueBar.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                var x = event.x
                //avoid the target moving out of the colour bar
                if (x < 0f) { x = 0f } else if (x > hueBar.measuredWidth) { x = hueBar.measuredWidth - 0.001f }
                var hue = 360f - 360f / hueBar.measuredWidth * x
                if (hue == 360f) { hue = 0f }
                currentHue = hue

                // update view
                coolColourView.setHue(currentHue)
                moveColourCursor()
                newColourBox.setBackgroundColor(getColour())
                updateAlphaView()
                return@OnTouchListener true
            }
            false
        })
        if (alphaEnabled) {
            alphaBar.setOnTouchListener(View.OnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                    var x = event.x
                    //avoid the target moving out of the alpha bar
                    if (x < 0f) { x = 0f } else if (x > alphaBarBackground.measuredWidth) { x = alphaBarBackground.measuredWidth - 0.001f }
                    val a = Math.round(255f - 255f / alphaBarBackground.measuredWidth * x)
                    alpha = a

                    // update view
                    moveAlphaCursor()
                    val col = getColour()
                    val c = a shl 24 or (col and 0x00ffffff)
                    newColourBox.setBackgroundColor(c)
                    return@OnTouchListener true
                }
                false
            })
        }

        //set responses
        dialog = AlertDialog.Builder(context)
            .setPositiveButton(R.string.ok) { dialog, which ->
                if (this@CoolColourPicker.response != null) {
                    oldColourBox.setBackgroundColor(getColour());
                    this@CoolColourPicker.response!!.onOk(this@CoolColourPicker, getColour())
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, which ->
                if (this@CoolColourPicker.response != null) {
                    this@CoolColourPicker.response!!.onCancel(this@CoolColourPicker)
                }
            }
            .setOnCancelListener(object : DialogInterface.OnCancelListener {
                // if back button is used, call back our listener.
                override fun onCancel(paramDialogInterface: DialogInterface) {
                    if (this@CoolColourPicker.response != null) {
                        this@CoolColourPicker.response!!.onCancel(this@CoolColourPicker)
                    }
                }
            })
            .create()

        // kill all padding from the dialog window
        dialog.setView(view, 0, 0, 0, 0)

        // move cursor & target on first draw
        val observer = view.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                moveColourCursor()
                moveTarget()
                if (alphaEnabled) moveAlphaCursor()
                if (alphaEnabled) updateAlphaView()
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun getCurrentHSV(): FloatArray{
        return floatArrayOf(currentHue, currentSaturation, currentValue)
    }

    private fun getColour(): Int {
        val argb = Color.HSVToColor(getCurrentHSV())
        // shl is << (bit shift)
        return alpha shl 24 or (argb and 0x00ffffff)
    }

    private fun setColour(colour: Int){

    }

    private fun moveColourCursor() {
        var hueX = ((currentHue * hueBar.measuredWidth)/360f)
        if (hueX == 0f) { hueX = hueBar.measuredWidth.toFloat() }
        var x = hueBar.measuredWidth - hueX - hueTarget.measuredWidth/2
        val layoutParams = hueTarget.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.leftMargin = (hueBar.left + x - container.paddingLeft).toInt()
        hueTarget.layoutParams = layoutParams
    }

    private fun moveAlphaCursor() {
        val x = alphaBarBackground.measuredWidth - ((alpha * alphaBarBackground.measuredWidth)/255f) - alphaTarget.measuredWidth/2
        val layoutParams = alphaTarget.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.leftMargin = (alphaBarBackground.left + x - container.paddingLeft.toDouble()).toInt()
        alphaTarget.layoutParams = layoutParams
    }

    private fun moveTarget() {
        val x = currentSaturation * coolColourView.measuredWidth - (newColourTarget.measuredWidth / 2).toFloat()
        val y = (1f - currentValue) * coolColourView.measuredHeight - (newColourTarget.measuredHeight / 2).toFloat()
        val layoutParams = newColourTarget.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.leftMargin = (coolColourView.left + x - container.paddingLeft).toInt()
        layoutParams.topMargin = (coolColourView.top + y - container.paddingTop).toInt()
        newColourTarget.layoutParams = layoutParams
    }

    fun show() {
        newColourBox.setBackgroundColor(getColour())
        dialog.show()
    }

    private fun updateAlphaView() {
        val gd = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(Color.HSVToColor(getCurrentHSV()), 0x0)
        )
        alphaBar.background = gd
    }

    private fun setUpHueView(){
        val gd = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                Color.RED,
                Color.MAGENTA,
                Color.BLUE,
                Color.CYAN,
                Color.GREEN,
                Color.YELLOW,
                Color.RED
            )
        )
        hueBar.background = gd
    }
}