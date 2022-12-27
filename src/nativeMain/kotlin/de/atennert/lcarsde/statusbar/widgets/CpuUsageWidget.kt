package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.readFile
import kotlinx.cinterop.CPointer
import statusbar.GtkCssProvider

class CpuUsageWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>) :
    RadarGraphWidget(widgetConfiguration, cssProvider, 500, 100) {

    override val attentionValue = 60.0
    override val warningValue = 80.0

    private var lastIdlesTotals: List<Pair<Float, Float>> = emptyList()

    override fun getData(): List<Double> = getCpuUtilization()

    private fun getCpuUtilization(): List<Double> {
        val statData = readFile("/proc/stat")?.lines() ?: return emptyList()
        val cpuData = statData
            .filter { it.startsWith("cpu") }
            .drop(1)
            .map {
                it.trim()
                    .split(' ')
                    .drop(1)
                    .map(String::toFloat)
            }
        val idlesTotals = cpuData.map { Pair(it[3] + it[4], it.sum()) }

        val deltas = if (lastIdlesTotals.isEmpty()) {
            Array(idlesTotals.size) { Pair(1f, 1f) }.toList()
        } else {
            idlesTotals.zip(lastIdlesTotals) { (c1, c2), (l1, l2) -> Pair(c1 - l1, c2 - l2) }
        }
        lastIdlesTotals = idlesTotals

        return deltas.map { (idle, total) ->
            try {
                100 * (1.0 - idle / total)
            } catch (e: ArithmeticException) {
                100 * (1.0 - idle / 0.001)
            }
        }
    }
}