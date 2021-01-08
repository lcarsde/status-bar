package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.readFile
import kotlinx.cinterop.CPointer
import statusbar.GtkCssProvider
import statusbar.cairo_t

class CpuUsageWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>)
    : RadarGraphWidget(widgetConfiguration, cssProvider, 500, 100) {

    private val attentionFreq = 60
    private val warningFreq = 80

    private var lastIdlesTotals: List<Pair<Float, Float>> = emptyList()

    override fun drawData(context: CPointer<cairo_t>) {
        val frequencies = getCpuUtilization()

        val maxFreq = frequencies.maxOrNull() ?: return
        val points = ArrayList<Pair<Double, Double>>(frequencies.size)
        var angle = 0.0
        for (freq in frequencies) {
            points.add(polarToCartesian(this, freq * scale, angle))
            angle += 360 / frequencies.size
        }

        when {
            maxFreq > warningFreq -> setDoubleRgb(context, 0.8, 0.4, 0.4)
            maxFreq > attentionFreq -> setDoubleRgb(context, 1.0, 0.6, 0.0)
            else -> setDoubleRgb(context, 1.0, 0.8, 0.6)
        }

        drawPoints(context, points)
    }

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

        return deltas.map { (idle, total) -> try {
                100 * (1.0 - idle / total)
            } catch (e: ArithmeticException) {
                100 * (1.0 - idle / 0.001)
            }
        }
    }
}