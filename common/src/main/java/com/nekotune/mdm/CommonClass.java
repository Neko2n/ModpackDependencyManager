package com.nekotune.mdm;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.nekotune.mdm.definition.DependencyInfo;
import com.nekotune.mdm.platform.Services;
import com.nekotune.mdm.platform.services.IPlatformHelper;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;

public final class CommonClass {

    public static void init() {
        // Load configuration settings from file
        Config.INSTANCE.load();

        // Enable OPTIONAL_ENABLED server packs by default
        DownloadManager.onDownloadFinished.connect(() -> {
            Constants.LOG.debug("[CommonClass] onDownloadFinished called");
            final IPlatformHelper platform = Services.PLATFORM.get();
            final PackRepository repo = platform.getServer().getPackRepository();
            repo.reload();
            final List<DependencyInfo> optionalEnabled = Config.INSTANCE.dependencies.stream()
                    .filter(dependency -> dependency.mode() == DependencyInfo.Mode.OPTIONAL_ENABLED
                            && dependency.type() == PackType.SERVER_DATA)
                    .toList();
            final Set<String> selectedPacks = new LinkedHashSet<>(repo.getSelectedIds());
            selectedPacks.addAll(optionalEnabled.stream()
                    .sorted(Comparator.comparingInt(v -> v.loadPriority()))
                    .map(DependencyInfo::packId)
                    .toList());
            repo.setSelected(selectedPacks);
        });

        // Download dependencies
        DownloadTaskThread.start();
    }
}