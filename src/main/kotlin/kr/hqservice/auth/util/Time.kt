package kr.hqservice.auth.util

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object Time {

    private const val location = "Asia/Seoul"
    private val zoneId = ZoneId.of(location)
    private const val pattern = "yyyy-MM-dd HH:mm:ss"
    private val formatter = DateTimeFormatter.ofPattern(pattern)

    fun getNowTime(): ZonedDateTime {
        return ZonedDateTime.now(zoneId)
    }

    fun String.toZonedDateTime(): ZonedDateTime {
        return ZonedDateTime.of(LocalDateTime.parse(this, formatter), zoneId)
    }

    fun ZonedDateTime.toText(): String {
        return this.format(formatter)
    }

    fun ZonedDateTime.toMinute(): Long {
        val get = ChronoUnit.SECONDS.between(getNowTime(), this)
        return get / 60
    }

    fun ZonedDateTime.toSecond(): Long {
        val get = ChronoUnit.SECONDS.between(getNowTime(), this)
        return get % 60
    }

    @JvmStatic
    fun getBetween(time: ZonedDateTime): Long {
        return Duration.between(getNowTime(), time).seconds
    }

    fun isTimeAfter(after: String): Boolean {
        val future = ZonedDateTime.of(LocalDateTime.parse(after, formatter), zoneId)
        return getNowTime().isAfter(future)
    }

    fun isTimeAfter(after: ZonedDateTime): Boolean {
        return getNowTime().isAfter(after)
    }
}