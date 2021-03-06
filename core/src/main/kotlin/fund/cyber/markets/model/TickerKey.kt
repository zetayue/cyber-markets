package fund.cyber.markets.model

import fund.cyber.markets.dto.TokensPair
import java.sql.Timestamp

data class TickerKey(
        val pair: TokensPair,
        val windowDuration: Long,
        val timestamp: Timestamp
)