package com.liprogramming.coolcolourpicker

interface CoolColourResponse{
    fun onCancel(dialog: CoolColourPicker)

    fun onOk(dialog: CoolColourPicker, selectedColour: Int)
}
