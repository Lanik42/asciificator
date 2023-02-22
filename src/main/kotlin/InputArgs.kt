data class InputArgs (
    val path: String,
    val symbolToPixelAreaRatio: Int,
    val fontSize: Int,
    val colored: Boolean,
    val outFormat: String,
    val outPath: String,
    val scale: Boolean,
)
// Добавить выходной формат (jpg / png), возможно цвет фона
// Добавить строку, которая используется для grayScale