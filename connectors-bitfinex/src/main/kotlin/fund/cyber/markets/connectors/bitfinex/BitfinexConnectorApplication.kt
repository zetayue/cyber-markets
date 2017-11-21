package fund.cyber.markets.connectors.bitfinex

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.connectors.bitfinex.connector.BitfinexOrdersEndpoint
import fund.cyber.markets.connectors.bitfinex.connector.BitfinexTradesEndpoint
import fund.cyber.markets.model.OrdersBatch
import fund.cyber.markets.model.Trade
import io.undertow.protocols.ssl.UndertowXnioSsl
import io.undertow.server.DefaultByteBufferPool
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.newSingleThreadContext
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.xnio.OptionMap
import org.xnio.Options
import org.xnio.Xnio
import org.xnio.XnioWorker

/*----------------------------- Undertow ------------------------------------------*/
val NO_OPTIONS: OptionMap = OptionMap.EMPTY
private val xnioSettings = OptionMap.builder()
        .set(Options.TCP_NODELAY, true)
        .set(Options.WORKER_IO_THREADS, 2)
        .set(Options.WORKER_TASK_CORE_THREADS, 5)
        .set(Options.WORKER_TASK_MAX_THREADS, 5)
        .set(Options.READ_TIMEOUT, 10 * 1000)
        .set(Options.WRITE_TIMEOUT, 10 * 1000)
        .getMap()
val xnioWorker: XnioWorker = Xnio.getInstance().createWorker(xnioSettings)
val xnioSsl = UndertowXnioSsl(Xnio.getInstance(), OptionMap.EMPTY)
val byteBuffersPool = DefaultByteBufferPool(true, 2048)
/*----------------------------- Others ---------------------------------------------*/
val httpDispatcher = Dispatcher(xnioWorker)
val httpClient = OkHttpClient.Builder().dispatcher(httpDispatcher).build()!!
val coroutinesThreads = 2
val applicationContext = newFixedThreadPoolContext(coroutinesThreads, "Coroutines Concurrent Pool")
val applicationSingleThreadContext = newSingleThreadContext("Coroutines Single Thread Pool")
val jsonParser = ObjectMapper()
val supportedTradesEndpoints = listOf(
        BitfinexTradesEndpoint()
)
val supportedOrdersEndpoints = listOf(
        BitfinexOrdersEndpoint()
)
val tradeKafkaProducer = fund.cyber.markets.connectors.common.kafka.ConnectorKafkaProducer<Trade>()
val orderKafkaProducer = fund.cyber.markets.connectors.common.kafka.ConnectorKafkaProducer<OrdersBatch>()
fun main(args: Array<String>) {
    val debugMode = System.getProperty("debug") != null

    supportedTradesEndpoints.forEach { exchange ->
        fund.cyber.markets.connectors.helpers.concurrent {
            val dataChannel = exchange.subscribe()
            fund.cyber.markets.connectors.helpers.concurrent {
                while (true) {
                    val message = dataChannel.receive()
                    message.trades.forEach { trade ->
                        if (debugMode) println(trade) else tradeKafkaProducer.send(fund.cyber.markets.connectors.common.kafka.TradeProducerRecord(trade))
                    }
                }
            }
        }
    }

    supportedOrdersEndpoints.forEach { exchange ->
        fund.cyber.markets.connectors.helpers.concurrent {
            val dataChannel = exchange.subscribe()
            fund.cyber.markets.connectors.helpers.concurrent {
                while (true) {
                    val message = dataChannel.receive()
                    if (debugMode) println(message) else orderKafkaProducer.send(fund.cyber.markets.connectors.common.kafka.OrdersUpdateProducerRecord(message.ordersBatch))
                }
            }
        }
    }
}