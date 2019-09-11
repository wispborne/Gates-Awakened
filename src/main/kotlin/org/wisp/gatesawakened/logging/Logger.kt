package org.wisp.gatesawakened.logging

import org.apache.log4j.Logger
import org.wisp.gatesawakened.Common

typealias DebugLogger = Logger

internal fun DebugLogger.i(message: () -> String) {
    if (Common.isDebugModeEnabled) {
        this.info(message())
    }
}