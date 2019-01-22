package org.toast.activegates.intel

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson
import org.toast.activegates.*


class IntroBarEvent : BaseBarEventWithPerson() {

    private fun destinationGate(): Gate? = Common.getGates(GateFilter.IntroFringe).firstOrNull()?.gate

    override fun shouldShowAtMarket(market: MarketAPI): Boolean =
        when {
            !super.shouldShowAtMarket(market) -> false
            destinationGate() == null -> false
            else -> !market.factionId.equalsAny("luddic_church", "luddic_path")
        }

    /**
     * Set up the text that appears when the player goes to the bar
     * and the option for them to start the conversation.
     */
    override fun addPromptAndOption(dialog: InteractionDialogAPI) {
        super.addPromptAndOption(dialog)
        regen(dialog.interactionTarget.market)
        val text = dialog.textPanel

        text.addPara("A $manOrWoman wearing an attention-grabbing cowboy hat is focused on $hisOrHer tripad in a corner of the bar; it looks like $heOrShe is staring at an image of a Gate.")

        dialog.optionPanel.addOption(
            "Move nonchalantly nearer to the \"cow$boyOrGirl\" for a closer look at $hisOrHer screen.", this
        )
    }

    /**
     * Called when the player chooses to start the conversation.
     */
    override fun init(dialog: InteractionDialogAPI) {
        super.init(dialog)
        this.done = false
        dialog.visualPanel.showPersonInfo(this.person, true)
        this.optionSelected(null as String?, OptionId.INIT)
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        if (optionData is OptionId) {
            val destinationSystem = destinationGate()?.starSystem
            dialog.optionPanel.clearOptions()

            when (optionData) {
                OptionId.INIT -> {
                    dialog.textPanel.addPara("You try to act naturally, but as soon as you get close, $heOrShe flips off $hisOrHer tripad and quickly rushes out, almost deliberately not looking at you.")
                    dialog.textPanel.addPara("However, just before $hisOrHer tripad goes dark, you catch one line: ${destinationSystem?.name}")
                    addIntel()
                    dialog.optionPanel.addOption(
                        "Watch the $manOrWoman hurry down the street and consider what could be at ${destinationSystem?.baseName}.",
                        OptionId.LEAVE
                    )
                }
                OptionId.LEAVE -> {
                    noContinue = true
                    done = true
                }
            }.exhaustiveWhen
        }
    }

    private fun addIntel() {
        var success = false
        val destinationGate = destinationGate()

        if (destinationGate != null) {
            val intel = IntroIntel(dialog.interactionTarget, destinationGate)

            if (!intel.isDone) {
                success = true
            }
        }

        if (!success) {
            dialog.textPanel.addPara("After a moment's consideration, you decide that there's nothing out there after all.")
        }
    }

    private val boyOrGirl
        get() = if (personGender == FullName.Gender.MALE) "boy" else "girl"

    override fun getPersonRank(): String {
        return Ranks.SPACE_SAILOR
    }

    enum class OptionId {
        INIT,
        LEAVE
    }
}