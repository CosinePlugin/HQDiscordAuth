package kr.hqservice.auth.util

class RandomCode {

    private val range = (100000..999999)

    fun generate(): Int {
        return range.random()
    }
}