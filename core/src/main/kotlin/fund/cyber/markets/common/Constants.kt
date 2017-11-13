package fund.cyber.markets.common

import java.util.regex.Pattern

object Constants {
    val KAFKA_CONNECTION: String = "KAFKA_CONNECTION"
    val WINDOW_DURATIONS_MIN: String = "WINDOW_DURATIONS_MIN"
    val WINDOW_HOP_SEC: String = "WINDOW_HOP_SEC"
    val TRADES_TOPIC_NAME_PATTERN = Pattern.compile("TRADES-.*")
    val WINDOW_TATS_TOPIC_NAME = "WINDOW-STATS-TOPIC"
}