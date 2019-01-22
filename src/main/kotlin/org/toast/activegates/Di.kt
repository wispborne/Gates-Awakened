package org.toast.activegates

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.SettingsAPI
import com.fs.starfarer.api.campaign.SectorAPI
import org.apache.log4j.Logger

class Di(
    val sector: SectorAPI = Global.getSector(),
    val settings: SettingsAPI = Global.getSettings(),
    val logger: Logger = Global.getLogger(Di::class.java)
) {

    companion object {
        /**
         * Singleton instance of the service locator. Set a new one of these for unit tests.
         */
        lateinit var inst: Di
    }
}