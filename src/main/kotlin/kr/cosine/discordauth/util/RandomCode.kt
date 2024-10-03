package kr.cosine.discordauth.util

import kotlin.random.Random

class RandomCode {
    private val random = Random(System.currentTimeMillis())

    private val numberRange = 0..9
    private val alphabetRange = 'A'..'Z'

    fun generate(): String {
        val builder = StringBuilder()
        repeat(5) {
            val n = random.nextInt(2)
            if (n == 0) {
                builder.append(numberRange.random())
            } else {
                builder.append(alphabetRange.random())
            }
        }
        return builder.toString()
    }
}