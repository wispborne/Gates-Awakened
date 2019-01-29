package org.toast.activegates.logging

import org.apache.log4j.Logger
import org.toast.activegates.Common

typealias DebugLogger = Logger

internal fun DebugLogger.i(message: () -> String) {
    if (Common.isDebugModeEnabled) {
        this.info(message())
    }
}