package org.wisp.gatesawakened;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.thoughtworks.xstream.XStream;
import org.wisp.gatesawakened.constants.MiscKt;

public class ModVerificationPlugin extends BaseModPlugin {
    private LifecyclePlugin mainPlugin = null;

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();

        if (Global.getSettings().getModManager().isModEnabled(MiscKt.LazyLibId)) {
            mainPlugin = new LifecyclePlugin();
            mainPlugin.onApplicationLoad();
        } else {
            throw new RuntimeException("LazyLib is required to run Gates Awakened.");
        }
    }

    @Override
    public void onNewGameAfterTimePass() {
        super.onNewGameAfterTimePass();
        mainPlugin.onNewGameAfterTimePass();
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
        mainPlugin.onNewGame();
    }

    @Override
    public void beforeGameSave() {
        super.beforeGameSave();
        mainPlugin.beforeGameSave();
    }

    @Override
    public void afterGameSave() {
        super.afterGameSave();
        mainPlugin.afterGameSave();
    }

    @Override
    public void configureXStream(XStream x) {
        super.configureXStream(x);
        mainPlugin.configureXStream(x);
    }
}
