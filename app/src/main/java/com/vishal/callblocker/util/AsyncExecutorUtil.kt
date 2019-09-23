package com.vishal.callblocker.util

import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AsyncExecutorUtil private constructor() {

    val executor: Executor

    init {
        executor = Executors.newFixedThreadPool(3)
    }

    companion object {
        val instance = AsyncExecutorUtil()
    }
}
