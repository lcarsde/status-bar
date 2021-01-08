package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.readFile
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.closedir
import platform.posix.opendir
import platform.posix.readdir
import statusbar.GtkCssProvider
import statusbar.cairo_t
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.Map
import kotlin.collections.map
import kotlin.collections.maxOrNull
import kotlin.collections.set
import kotlin.collections.sortedBy

class TemperatureGraphWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>)
    : RadarGraphWidget(widgetConfiguration, cssProvider, 5000, 125) {

    private val attentionTemp = 60
    private val warningTemp = 80

    override fun drawData(context: CPointer<cairo_t>) {
        val temperatures = getTemperatures().entries
                .sortedBy { it.key }
                .map { it.value }

        val maxTemp = temperatures.maxOrNull() ?: return
        var angle = 0.0
        val points = ArrayList<Pair<Double, Double>>(temperatures.size)
        for (temp in temperatures) {
            points.add(polarToCartesian(this, temp * scale, angle))
            angle += 360 / temperatures.size
        }

        when {
            maxTemp > warningTemp -> setDoubleRgb(context, 0.8, 0.4, 0.4)
            maxTemp > attentionTemp -> setDoubleRgb(context, 1.0, 0.6, 0.0)
            else -> setDoubleRgb(context, 1.0, 0.8, 0.6)
        }

        drawPoints(context, points)
    }

    /**
     * Get every /sys/class/thermal/thermal_zone* directory
     * and read type and temp
     * and set the data.
     */
    private fun getTemperatures(): Map<String, Int> {
        val basePath = "/sys/class/thermal"
        val temperatures = HashMap<String, Int>()

        opendir(basePath)?.let { dir ->
            while (true) {
                val subDir = readdir(dir)?.pointed?.d_name?.toKString() ?: break

                if (subDir.startsWith("thermal_zone")) {
                    val type = readFile("$basePath/$subDir/type") ?: continue
                    val temp = readFile("$basePath/$subDir/temp")?.toInt() ?: continue

                    temperatures[type] = temp / 1000
                }
            }
            closedir(dir)
        }

        return temperatures
    }
}