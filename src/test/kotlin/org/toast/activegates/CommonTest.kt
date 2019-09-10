package org.toast.activegates

import com.fs.starfarer.api.SettingsAPI
import com.fs.starfarer.api.campaign.SectorAPI
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CommonTest {
    val sector: SectorAPI = mockk()
    val settings: SettingsAPI = mockk()

    @BeforeEach
    fun setUp() {
        di = Di(
            sector = sector,
            settings = settings
        )
    }

    @Test
    fun getInDebugMode() {
        every { settings.getBoolean("activeGates_Debug") } returns true

        assertThat(Common.isDebugModeEnabled).isTrue()
    }

    @Test
    fun canActivate() {
    }

    @Test
    fun payActivationCost() {
    }

    @Test
    fun jumpCostInFuel() {
    }

    @Test
    fun getSystems() {
    }

    @Test
    fun getGates() {
    }

    @Test
    fun getCommodityCostOf() {
    }
}