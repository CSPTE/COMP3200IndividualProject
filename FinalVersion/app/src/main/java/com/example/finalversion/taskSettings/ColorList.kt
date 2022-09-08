package com.example.finalversion.taskSettings

class ColorList
{
    private val blackHex = "000000"
    private val whiteHex = "FFFFFF"

    val defaultColor: ColorObject = basicColors()[0]

    fun colorPosition(colorObject: ColorObject): Int
    {
        for (i in basicColors().indices)
        {
            if(colorObject == basicColors()[i])
                return i
        }
        return 0
    }

    fun basicColors(): List<ColorObject>
    {
        return listOf(
            //ColorObject("Black", blackHex, whiteHex),
            ColorObject("Silver", "C0C0C0", blackHex),
            ColorObject("Blush Pink", "FE828C", blackHex),
            ColorObject("Pastel Red", "FF6961", blackHex),
            ColorObject("Fuchsia", "FF00FF", blackHex),
            ColorObject("Mauve", "E0B0FF", blackHex),
            ColorObject("Cornflower Blue", "6495ED", blackHex),
            ColorObject("Azure", "007fff", blackHex),
            ColorObject("Aqua", "00FFFF", blackHex),
            ColorObject("Aquamarine", "7FFFD4", blackHex),
            ColorObject("Lime", "00FF00", blackHex),
            ColorObject("Emerald Green", "50C878", blackHex),
            ColorObject("Olive Green", "BAB86C", blackHex),
            ColorObject("Wheat", "F5DEB3", blackHex),
            ColorObject("Yellow", "FFFF00", blackHex),
            ColorObject("Orange", "FFA500", blackHex)
        )
    }
}