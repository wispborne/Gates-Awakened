package org.wisp.gatesawakened;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.thoughtworks.xstream.XStream;
import org.wisp.gatesawakened.constants.MiscKt;

public class ModVerificationPlugin extends BaseModPlugin implements ILifecyclePlugin {
    private ILifecyclePlugin mainPlugin = null;

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();

        if (Global.getSettings().getModManager().isModEnabled(MiscKt.LazyLibId)) {
            mainPlugin = new LifecyclePlugin();
        } else {
            throw new RuntimeException("LazyLib is required to run Gates Awakened.");
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);
        mainPlugin.onGameLoad(newGame);
    }

    @Override
    public void onNewGameAfterTimePass() {
        super.onNewGameAfterTimePass();
        mainPlugin.onNewGameAfterTimePass();
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
