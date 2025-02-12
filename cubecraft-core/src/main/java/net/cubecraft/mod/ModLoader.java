package net.cubecraft.mod;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import net.cubecraft.SharedContext;
import net.cubecraft.mod.object.Mod;

import java.io.File;
import java.util.jar.JarFile;

public interface ModLoader {
    Logger LOGGER = LogManager.getLogger("ModLoader");

    static void loadClientInternalMod() {
        ModManager modManager = SharedContext.MOD;

        modManager.registerMod(Mod.SHARED);
        modManager.registerMod(Mod.SERVER);
        modManager.registerMod(Mod.CLIENT);
    }

    static void loadServerInternalMod() {
        ModManager modManager = SharedContext.MOD;

        modManager.registerMod(Mod.SHARED);
        modManager.registerMod(Mod.SERVER);
    }

    static void loadStandaloneMods(File folder) {
        ModManager modManager = SharedContext.MOD;

        if (!folder.isDirectory()) {
            return;
        }

        File[] subFiles = folder.listFiles();

        if (subFiles == null) {
            return;
        }

        for (File f : subFiles) {
            if (f.isDirectory()) {
                continue;
            }
            try {
                modManager.registerMod(Mod.standalone(new JarFile(f)));
            } catch (Exception e) {
                LOGGER.throwing(e);
            }
        }
    }
}
