package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Formatters {

    private val turkishSymbols = DecimalFormatSymbols(Locale("tr", "TR")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private val currencyFormat = DecimalFormat("#,##0", turkishSymbols)
    private val decimalFormat = DecimalFormat("#,##0.00", turkishSymbols)
    private val compactDecimalFormat = DecimalFormat("#,##0.0", turkishSymbols)

    fun formatLira(amount: Double): String {
        return "₺" + currencyFormat.format(amount.toLong())
    }

    fun formatLiraDetailed(amount: Double): String {
        return "₺" + decimalFormat.format(amount)
    }

    fun formatLiraCompact(amount: Double): String {
        val abs = Math.abs(amount)
        val sign = if (amount < 0) "-" else ""
        return when {
            abs >= 1_000_000_000.0 -> "$sign₺${compactDecimalFormat.format(abs / 1_000_000_000.0)} Mr"
            abs >= 1_000_000.0 -> "$sign₺${compactDecimalFormat.format(abs / 1_000_000.0)} Mn"
            abs >= 1_000.0 -> "$sign₺${compactDecimalFormat.format(abs / 1_000.0)} B"
            else -> "$sign₺${currencyFormat.format(abs.toLong())}"
        }
    }

    fun formatPercent(value: Double, includeSign: Boolean = true): String {
        val sign = if (includeSign && value > 0) "+" else ""
        return "$sign${compactDecimalFormat.format(value)}%"
    }

    fun formatRate(rate: Double): String {
        return decimalFormat.format(rate)
    }
}
