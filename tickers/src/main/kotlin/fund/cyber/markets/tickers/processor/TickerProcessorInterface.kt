package fund.cyber.markets.tickers.processor

import fund.cyber.markets.dto.TokensPair
import fund.cyber.markets.model.Ticker
import java.util.*

interface TickerProcessorInterface {

    fun update(hopTickersProcessor: HopTickerProcessor, currentMillis: Long)

    fun get(pair: TokensPair, exchange: String, windowDuration: Long, currentMillis: Long): Ticker

    fun getWindow(pair: TokensPair, exchange: String, windowDuration: Long): Queue<Ticker>

    fun updateTimestamps(currentMillisHop: Long)


    fun cleanupWindows()

    fun calculatePrice(volumeProcessor: VolumeProcessor, currentMillisHop: Long)

    fun saveAndProduceToKafka(currentMillisHop: Long)

}