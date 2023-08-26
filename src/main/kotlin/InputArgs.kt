data class InputArgs(
    val path: String,
    val symbolToPixelAreaRatio: Int,
    val fontSize: Int,
    val colored: Boolean,
    val outPath: String,
    val scale: Boolean,
)
// Добавить выходной формат (jpg / png), возможно цвет фона
// Добавить строку, которая используется для grayScale

fun Array<String>.parse(): InputArgs {
    val nameToValueMap = getArgNameToValueMap()
    return nameToValueMap.parseMapToInputArgs()
}

fun Array<String>.getArgNameToValueMap(): Map<String, String> {
    val argNameToValueMap = mutableMapOf<String, String>()

    for (i in 0..lastIndex step 2) {
        if (i + 1 >= size || get(i + 1).startsWith("-")) {
            error("Wrong input format! Each argument should be followed by value. Missing value for argument ${get(i)}.")
        }

        argNameToValueMap[get(i)] = get(i + 1)
    }

    return argNameToValueMap
}

fun Map<String, String>.parseMapToInputArgs(): InputArgs {
    val path = get(ABSOLUTE_FILE_PATH) ?: error("Path (-path) argument not specified!")
    val symbolToPixelAreaRatio = get(SYMBOL_TO_PIXEL_AREA_RATIO) ?: error("Ratio (-ratio) argument not specified!")
    val colored = get(COLORED) ?: error("Colored (-colored) argument not specified!")
    val outPath = get(OUT_PATH) ?: error("Out path (-output) argument not specified!")
    val fontSize = get(FONT_SIZE) ?: error("Font size (-fontSize) argument not specified!")
    val scale = get(SCALE_SYMBOLS_FIT) ?: error("Scale (-scale) argument not specified!")


    return InputArgs(
        path = path,
        symbolToPixelAreaRatio = symbolToPixelAreaRatio.toInt(),
        fontSize = fontSize.toInt(),
        colored = colored.toBoolean(),
        outPath = outPath,
        scale = scale.toBoolean(),
    )
}