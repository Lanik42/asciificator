
fun <T> measureTimeMillis(label: String? = null, function: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = function()
    val end = System.currentTimeMillis()

    println("${label.orEmpty()} elapsed time: ${end - start}")
    return result to end - start
}

fun <T> measureTimeNanos(label: String? = null, function: () -> T): Pair<T, Long> {
    val start = System.nanoTime()
    val result = function()
    val end = System.nanoTime()

    val elapsed = end - start
  //  println("${label.orEmpty()} elapsed time: elapsed")
    return result to elapsed
}

suspend fun <T> measureTimeMillisSus(label: String? = null, function: suspend () -> T): T {
    val start = System.currentTimeMillis()
    val result = function()
    val end = System.currentTimeMillis()

    println("${label.orEmpty()} elapsed time: ${end - start}")
    return result
}
