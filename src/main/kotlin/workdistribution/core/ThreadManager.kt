package workdistribution.core

import java.util.concurrent.Executors

object ThreadManager {

    val threadCount = Runtime.getRuntime().availableProcessors()
    val executors = Array(threadCount) { Executors.newSingleThreadExecutor() }
}