package workdistribution.core

import java.util.concurrent.Executors

object ThreadManager {

    val threadCount = Runtime.getRuntime().availableProcessors()
    val executors = Array(threadCount) { Executors.newSingleThreadExecutor() }
}

class MyThread : Thread() {



    override fun run() {
        println("MyThread running")
    }
}
