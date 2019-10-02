package org.wisp.gatesawakened

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.SettingsAPI
import com.fs.starfarer.api.campaign.SectorAPI
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI
import org.wisp.gatesawakened.constants.Memory
import org.wisp.gatesawakened.logging.DebugLogger

class Di(
    val sector: SectorAPI = Global.getSector(),
    val memory: Memory = Memory(sector.memoryWithoutUpdate),
    val intelManager: IntelManagerAPI = sector.intelManager,
    val settings: SettingsAPI = Global.getSettings(),
    val logger: DebugLogger = Global.getLogger(Di::class.java)
)

/**
 * Singleton instance of the service locator. Set a new one of these for unit tests.
 */
var di: Di = Di()