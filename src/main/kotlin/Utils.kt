
fun <T> measureTimeMillis(label: String? = null, function: () -> T): T {
    val start = System.currentTimeMillis()
    val result = function()
    val end = System.currentTimeMillis()

    println("${label.orEmpty()} elapsed time: ${end - start}")
    return result
}
