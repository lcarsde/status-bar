package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.extensions.gSignalConnect
import de.atennert.lcarsde.statusbar.readFile
import kotlinx.cinterop.*
import statusbar.*

class MemoryWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>)
    : StatusWidget(widgetConfiguration, cssProvider, 1000) {

    private var ref: StableRef<MemoryWidget>? = null

    init {
        widget = gtk_drawing_area_new()!!
        gtk_widget_set_size_request(widget, widthPx, heightPx)
    }

    override fun start() {
        ref = StableRef.create(this)

        gSignalConnect(widget, "draw",
            staticCFunction { _: CPointer<GtkWidget>, c: CPointer<cairo_t>, p: COpaquePointer -> draw(c, p) },
            ref!!.asCPointer())

        super.start()
    }

    override fun stop() {
        super.stop()

        ref!!.dispose()
    }

    override fun update() {
        gtk_widget_queue_draw(widget)
    }

    class MemoryData(memoryInfo: String) {
        val data: Map<String, Long>

        init {
            data = memoryInfo.lines()
                .mapNotNull(this::readEntry)
                .associate { it }
        }

        private fun readEntry(entry: String): Pair<String, Long>? {
            val parts = entry
                .removeSuffix(" kB")
                .split(Regex(":\\s*"))

            return if (parts.size != 2)
                null
            else
                Pair(parts[0], parts[1].toLong())
        }
    }

    companion object {
        private fun draw(context: CPointer<cairo_t>, ref: COpaquePointer) {
            val widget = ref.asStableRef<MemoryWidget>().get()
            val memoryInfo = readData()

            if (memoryInfo == null) {
                drawMemory(widget, context, 0.8, 0.4, 0.4)
            } else {
                val memoryData = MemoryData(memoryInfo)

                drawMemory(widget, context, 1.0, 0.8, 0.6)
                drawMemoryStatus(widget, context, memoryData)
            }
        }

        private fun readData(): String? {
            return readFile("/proc/meminfo")
        }

        private fun drawMemory(widget: MemoryWidget, context: CPointer<cairo_t>, r: Double, g: Double, b: Double) {
            cairo_set_source_rgb(context, r, g, b)

            cairo_rectangle(context, 5.0, 0.0, (widget.widthPx - 10.0), widget.heightPx.toDouble())

            cairo_stroke(context)
        }

        private fun drawMemoryStatus(widget: MemoryWidget, context: CPointer<cairo_t>, memoryData: MemoryData) {
            val totalMemory = memoryData.data["MemTotal"] ?: return
            val freeMemory = memoryData.data["MemFree"] ?: return
            val availableMemory = memoryData.data["MemAvailable"] ?: return

            val usedMemory = totalMemory - availableMemory
            val reservedMemory = availableMemory - freeMemory

            val usedBottom = widget.heightPx - 1.0
            val usedHeight = usedMemory * (widget.heightPx - 2.0) / totalMemory
            val reservedBottom = usedBottom - usedHeight
            val reservedHeight = reservedMemory * (widget.heightPx - 2.0) / totalMemory

            cairo_set_source_rgba(context, 1.0, 0.6, 0.4, 0.6)
            cairo_rectangle(context, 6.0, usedBottom, (widget.widthPx - 12.0), -usedHeight)
            cairo_fill(context)

            cairo_set_source_rgba(context, 1.0, 0.8, 0.6, 0.6)
            cairo_rectangle(context, 6.0, reservedBottom, (widget.widthPx - 12.0), -reservedHeight)
            cairo_fill(context)
        }
    }
}