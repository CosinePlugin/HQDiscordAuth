package kr.cosine.discordauth.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TimeUtils {
    private val fullFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초")
    private val shortFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun getShortFormattedTime(time: LocalDateTime): String {
        return shortFormatter.format(time)
    }

    fun getFullFormattedTime(time: LocalDateTime): String {
        return fullFormatter.format(time)
    }
}