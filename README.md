# CoolColourPicker-Library
Simple colour picker library for android

** Not yet released **

### Example Usage

```
var alphaDialog = CoolColourPicker(context, colour, alphaEnabled, object: CoolColourResponse {
            override fun onOk(dialog: CoolColourPicker, selectedColour: Int) {
                // color is the color selected by the user.
                // do some cool stuff with selectedColour
            }
            override fun onCancel(dialog: CoolColourPicker) {
                // cancel was selected by the user
            }
        })

selectColourAlpha.setOnClickListener{ alphaDialog.show() }

```
