package org.toast.activegates

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.SettingsAPI
import com.fs.starfarer.api.campaign.SectorAPI
import org.toast.activegates.logging.DebugLogger

class Di(
    val sector: SectorAPI = Global.getSector(),
    val settings: SettingsAPI = Global.getSettings(),
    val logger: DebugLogger = Global.getLogger(Di::class.java)
) {

    companion object {
        /**
         * Singleton instance of the service locator. Set a new one of these for unit tests.
         */
        var inst: Di = Di()
    }
}