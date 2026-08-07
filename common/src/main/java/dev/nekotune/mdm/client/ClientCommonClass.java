package dev.nekotune.mdm.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.nekotune.mdm.CommonClass;
import dev.nekotune.mdm.Config;
import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.DownloadManager;
import dev.nekotune.mdm.DownloadManager.DownloadResult;
import dev.nekotune.mdm.client.gui.DownloadErrorScreen;
import dev.nekotune.mdm.client.gui.ReloadPromptScreen;
import dev.nekotune.mdm.client.gui.loading.DownloadWaitOverlay;
import dev.nekotune.mdm.platform.PlatformEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.packs.PackType;

public class ClientCommonClass {

    public static void init() {
        PlatformEvents.CLIENT_LOADED.hook.connect(ClientCommonClass::clientLoaded);

        // Enable OPTIONAL_ENABLED client packs by default
        DownloadManager.onDownloadsFinished.connect(() -> {
            final Minecraft mc = Minecraft.getInstance();
            CommonClass.enableDownloadedOptionals(mc.getResourcePackRepository(), PackType.CLIENT_RESOURCES);
            final IntegratedServer server = mc.getSingleplayerServer();
            if (server != null) {
                CommonClass.enableDownloadedOptionals(server.getPackRepository(), PackType.SERVER_DATA);
            }
        });
    }

    /**
     * Called the first time the game reaches the title screen after loading.
     * Call site is loader-dependent.
     */
    public static void clientLoaded() {
        Constants.LOG.debug("[Client] clientLoaded called");

        // Thread safety for race condition between event connection and switch case
        final var canRun = new AtomicBoolean(true);
        final Runnable showFinishedScreen = () -> {
            if (canRun.getAcquire()) {
                setScreenAtomic(ClientCommonClass::downloadFinishedScreen);
                if (!Config.INSTANCE.promptEnabled) {
                    Minecraft.getInstance().reloadResourcePacks();
                }
            }
            canRun.setRelease(false);
        };
        DownloadManager.onDownloadsFinished.connect(showFinishedScreen);

        switch (DownloadManager.getState()) {
            case FINISHED: {
                showFinishedScreen.run();
                break;
            }
            // If dependencies haven't finished downloading,
            // show the dependency download progress overlay.
            case NOT_STARTED:
            case STARTED: {
                final Minecraft mc = Minecraft.getInstance();
                mc.setOverlay(new DownloadWaitOverlay(mc, false));
                break;
            }
            // If the download thread was interrupted,
            // show the internal error screen.
            case INTERRUPTED: {
                setScreenAtomic(mc -> {
                    final Screen lastScreen = mc.screen;
                    return new DownloadErrorScreen(DownloadResult.EXCEPTION, List.of(),
                            button -> setScreenAtomic(() -> lastScreen));
                });
                break;
            }
        }
    }

    /**
     * Shows any download errors that occured, then a prompt to reload resources if
     * any dependencies were downloaded.
     */
    private static Screen downloadFinishedScreen(final Minecraft mc) {

        final Stack<Screen> screenStack = new Stack<>();

        // If anything was downloaded, prompt the user to reload resources
        final Screen lastScreen = mc.screen;
        if (DownloadManager.getDownloaded().size() > 0) {
            screenStack.push(new ReloadPromptScreen(() -> setScreenAtomic(() -> lastScreen)));
        } else {
            screenStack.push(lastScreen);
        }

        // Show error screens for any invalid dependencies and/or download failures
        final Map<DownloadResult, List<String>> errors = new EnumMap<>(DownloadResult.class);
        for (final String cause : DownloadManager.DOWNLOAD_ERRORS.keySet()) {
            final DownloadResult error = DownloadManager.DOWNLOAD_ERRORS.get(cause);
            final List<String> causes = errors
                    .computeIfAbsent(error, $ -> new ArrayList<>());
            causes.add(cause);
        }
        for (final DownloadResult error : errors.keySet()) {
            final List<String> causes = errors.get(error);
            Constants.LOG.debug("[Client] Pushing error screen for error " + error.toString() + " with instances "
                    + Arrays.toString(causes.toArray()));
            screenStack.push(new DownloadErrorScreen(error, causes,
                    button -> setScreenAtomic(() -> {
                        screenStack.pop();
                        return screenStack.peek();
                    })));
        }

        return screenStack.peek();
    }

    /**
     * Helper method which ensures thread safety when checking and setting screens.
     */
    private static void setScreenAtomic(final Function<Minecraft, Screen> factory) {
        final Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            setScreenAtomic$lock.acquireUninterruptibly();
            final Screen screen = factory.apply(mc);
            if (screen instanceof ReloadPromptScreen && !Config.INSTANCE.promptEnabled) {
                setScreenAtomic$lock.release();
                return;
            }
            Constants.LOG.debug("[Client] Setting screen to " + screen.getClass().getName());
            mc.setScreen(screen);
            setScreenAtomic$lock.release();
        });
    }

    private static void setScreenAtomic(final Supplier<Screen> supplier) {
        setScreenAtomic(mc -> supplier.get());
    }

    private static final Semaphore setScreenAtomic$lock = new Semaphore(1);
}
