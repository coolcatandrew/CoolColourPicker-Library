package com.liprogramming.coolcolourpicker

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.Shader.TileMode
import android.graphics.*


class CoolColourView: View {
    private var paint: Paint? = null
    private var shader: Shader? = null
    private val hsv = floatArrayOf(1f, 1f, 1f)

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)

    constructor(context: Context, attributes: AttributeSet, style: Int) : super(context, attributes, style)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (paint == null) {
            paint = Paint()
            shader = LinearGradient(0f, 0f, 0f, this.measuredHeight.toFloat(), Color.WHITE, Color.BLACK, TileMode.CLAMP) //set black and white gradient
        }
        val colour = Color.HSVToColor(hsv)
        val colourShader = LinearGradient(0f, 0f, this.measuredWidth.toFloat(), 0f, Color.WHITE, colour, TileMode.CLAMP) //set colour gradient
        val combinedShader = ComposeShader(shader, colourShader, PorterDuff.Mode.MULTIPLY) //combine gradients
        paint!!.shader = combinedShader
        canvas.drawRect(0f,0f,this.measuredWidth.toFloat(), this.measuredHeight.toFloat() , paint)
    }

    fun setHue(hue: Float) {
        hsv[0] = hue
        invalidate()
    }
}