package net.perfectdreams.dreamsocial.gui.profile.helper

import java.time.Instant
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime

fun getZonedDate(): ZonedDateTime = Instant.now().atZone(ZoneId.of("America/Sao_Paulo"))

val ZonedDateTime.startOfTheMonthInMillis get() = this
    .withDayOfMonth(1)
    .withHour(0)
    .withMinute(0)
    .withSecond(0)
    .toEpochSecond() * 1000

val ZonedDateTime.localizedMonth get() = when (this.month!!) {
    Month.JANUARY -> "janeiro"
    Month.FEBRUARY -> "fevereiro"
    Month.MARCH -> "março"
    Month.APRIL -> "abril"
    Month.MAY -> "maio"
    Month.JUNE -> "junho"
    Month.JULY -> "julho"
    Month.AUGUST -> "agosto"
    Month.SEPTEMBER -> "setembro"
    Month.OCTOBER -> "outubro"
    Month.NOVEMBER -> "novembro"
    Month.DECEMBER -> "dezembro"
}