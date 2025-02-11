package workdistribution.core

import java.util.concurrent.Executors

object ThreadManager {

    val threadCount = Runtime.getRuntime().availableProcessors()

    val executors = Executors.newFixedThreadPool(threadCount)
    val nexecutors = Executors.newFixedThreadPool(threadCount)
}