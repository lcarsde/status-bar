package de.atennert.lcarsde.statusbar

import kotlinx.cinterop.*
import statusbar.*

const val CELL_SIZE = 40
const val GAP_SIZE = 8

const val LCARSDE_STATUS_BAR = "LCARSDE_STATUS_BAR"

const val STYLE_PATH = "/usr/share/lcarsde/status-bar/style.css"

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

/** Convert string to byte array as used in X properties */
fun String.toUByteArray(): UByteArray {
    return this.encodeToByteArray().asUByteArray()
}

/** convert this ubyte array pointer to a string */
fun CPointer<UByteVar>.toKString(): String {
    val byteString = mutableListOf<Byte>()
    var i = 0

    while (true) {
        val value = this[i]
        if (value.convert<Int>() == 0) {
            break
        }

        byteString.add(value.convert())
        i++
    }
    return byteString.toByteArray().toKString()
}

/** convert this ubyte array pointer to a string */
fun CPointer<UByteVar>?.toKString(): String = this?.toKString() ?: ""

/** print a float with a certain amount of places */
fun Float.print(places: Int): String {
    val flString = this.toString().split('.', limit = 2)
    return "${flString[0]}.${flString[1].take(places)}"
}

fun readFile(path: String): String? {
    platform.posix.fopen(path, "r")?. let { fp ->
        var s = ""
        val buf = ByteArray(1000)
        buf.usePinned {
            while (platform.posix.fgets(it.addressOf(0), 1000, fp) != null) {
                s += it.get().toKString()
            }
        }
        platform.posix.fclose(fp)
        return s.trim()
    }
    return null
}

fun executeCommand(command: String) {
    val commandParts = command.split(' ')

    val byteArgs = commandParts.map { it.encodeToByteArray().pin() }
    val convertedArgs = nativeHeap.allocArrayOfPointersTo(byteArgs.map { it.addressOf(0).pointed })
    when (fork()) {
        -1 -> return
        0 -> {
            if (setsid() == -1) {
                perror("setsid failed")
                exit(1)
            }

            if (execvp(commandParts[0], convertedArgs) == -1) {
                perror("execvp failed")
                exit(1)
            }

            exit(0)
        }
    }
    byteArgs.map { it.unpin() }
}
