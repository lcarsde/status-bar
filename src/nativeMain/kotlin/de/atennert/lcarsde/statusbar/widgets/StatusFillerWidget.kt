package de.atennert.lcarsde.statusbar.widgets

import de.atennert.lcarsde.statusbar.configuration.WidgetConfiguration
import de.atennert.lcarsde.statusbar.extensions.setStyling
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.reinterpret
import statusbar.*
import kotlin.random.Random

class StatusFillerWidget(widgetConfiguration: WidgetConfiguration, cssProvider: CPointer<GtkCssProvider>)
    : StatusWidget(widgetConfiguration, cssProvider, null) {

    init {
        val text = "${Random.nextInt(10000)}".padStart(4, '0')
        widget = gtk_label_new(text)!!
        gtk_widget_set_size_request(widget, widthPx, heightPx)
        gtk_label_set_xalign(widget.reinterpret(), 1f)
        gtk_label_set_yalign(widget.reinterpret(), 1f)

        val colorIdx = Random.nextInt(colors.size)
        val color = colors[colorIdx]

        widget.setStyling(cssProvider, "button--$color", "button--long")
    }

    override fun update() {
        // Nothing to do
    }

    companion object {
        private val colors = arrayOf("c9c", "99c", "f96", "000")
    }
}