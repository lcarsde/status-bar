package de.atennert.lcarsde.statusbar.extensions

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.reinterpret
import statusbar.*

fun <F : CFunction<*>> gSignalConnect(obj: CPointer<*>, actionName: String, action: CPointer<F>,
                                      data: gpointer? = null, connect_flags: GConnectFlags = 0u) {
    g_signal_connect_data(obj.reinterpret(), actionName, action.reinterpret(), data,
        destroy_data = null, connect_flags = connect_flags)
}

fun CPointer<GtkWidget>.setStyling(cssProvider: CPointer<GtkCssProvider>, vararg classes: String) {
    val styleContext = gtk_widget_get_style_context(this)
    for (cls in classes) {
        gtk_style_context_add_class(styleContext, cls)
    }
    gtk_style_context_add_provider(styleContext, cssProvider.reinterpret(), GTK_STYLE_PROVIDER_PRIORITY_USER)
}
