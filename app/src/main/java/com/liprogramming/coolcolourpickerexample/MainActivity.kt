package com.liprogramming.coolcolourpickerexample

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.liprogramming.coolcolourpicker.CoolColourPicker
import com.liprogramming.coolcolourpicker.CoolColourResponse
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var col: Int = Color.GREEN

        var changeColour: ImageView = changeThisColour
        var selectColourAlpha: Button = selectBackgroundColourAlpha
        var selectColour: Button = selectBackgroundColour

        changeColour.setBackgroundColor(col)

        var dialog = CoolColourPicker(this, col, object: CoolColourResponse {
            override fun onOk(dialog: CoolColourPicker, selectedColour: Int) {
                // color is the color selected by the user.
                changeColour.setBackgroundColor(selectedColour)
                col = selectedColour
            }
            override fun onCancel(dialog: CoolColourPicker) {
                // cancel was selected by the user
                Toast.makeText(applicationContext, "NO GO", Toast.LENGTH_LONG).show()
                changeColour.setBackgroundColor(Color.BLACK)
            }
        })

        var alphaDialog = CoolColourPicker(this, col, true, object: CoolColourResponse {
            override fun onOk(dialog: CoolColourPicker, selectedColour: Int) {
                // color is the color selected by the user.
                changeColour.setBackgroundColor(selectedColour)
                col = selectedColour
            }
            override fun onCancel(dialog: CoolColourPicker) {
                // cancel was selected by the user
                Toast.makeText(applicationContext, "NO GO", Toast.LENGTH_LONG).show()
                changeColour.setBackgroundColor(Color.BLACK)
            }
        })

        selectColour.setOnClickListener{ dialog.show() }
        selectColourAlpha.setOnClickListener{ alphaDialog.show() }

    }
}