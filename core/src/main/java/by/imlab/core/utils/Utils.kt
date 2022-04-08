package by.imlab.core.utils

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*
import kotlin.concurrent.timerTask

@kotlinx.coroutines.ExperimentalCoroutinesApi
fun tickerFlow(
    period: Long,
    initialDelay: Long = period
): Flow<Unit> = callbackFlow {
    require(period > 0)
    require(initialDelay > -1)

    val timer = Timer()
    timer.schedule(timerTask {
        offer(Unit)
    }, period)

    awaitClose { timer.cancel() }
}