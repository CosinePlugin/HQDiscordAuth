package kr.cosine.discordauth

import org.junit.jupiter.api.Test
import kotlin.random.Random


class RandomCodeTest {

    @Test
    fun test() {
        val numberRange = 0..9
        val alphabetRange = 'A'..'Z'
        val random = Random(System.currentTimeMillis())
        repeat(20) {
            val builder = StringBuilder()
            repeat(5) {
                val n = random.nextInt(2)
                if (n == 0) {
                    builder.append(numberRange.random())
                } else {
                    builder.append(alphabetRange.random())
                }
            }
            println("랜덤: $builder")
        }
    }
}