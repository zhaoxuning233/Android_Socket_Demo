package com.qilinkeji.libsocket

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val list = listOf(1, 2, 3, 4, 5, 6, 7)
        for (index in 1 until list.size) {
            println(list[index])
        }
    }
}
