package com.fs.starfarer.api.impl.campaign.rulecmd;

// Must use this package because this plugin is called by rules.csv


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;
import org.wisp.gatesawakened.constants.MiscKt;

import java.util.List;
import java.util.Map;

public class DearPlayer_PleaseAdd_LazyLib_ToUseGatesAwakened extends BaseCommandPlugin {
    public DearPlayer_PleaseAdd_LazyLib_ToUseGatesAwakened() {
        super();

        if (!Global.getSettings().getModManager().isModEnabled(MiscKt.LazyLibId)) {
            throw new NullPointerException("LazyLib is required to run Gates Awakened.");
        }
    }

    @Override
    public boolean execute(String s, InteractionDialogAPI interactionDialogAPI, List<Misc.Token> list, Map<String, MemoryAPI> map) {
        return true;
    }
}