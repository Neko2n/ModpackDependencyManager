package dev.nekotune.mdm.client.gui.config.dependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import dev.nekotune.mdm.Config;
import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.client.gui.config.AbstractConfigScreen;
import dev.nekotune.mdm.client.gui.config.widgets.ScrollListContent;
import dev.nekotune.mdm.definition.DependencyInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public class DependenciesScreen extends AbstractConfigScreen {

    protected static final String KEY = AbstractConfigScreen.KEY + ".dependencies";
    private static final ResourceLocation EDIT_ICON = ResourceLocation.fromNamespaceAndPath(
            Constants.MOD_ID, "edit");
    private static final ResourceLocation DELETE_ICON = ResourceLocation.fromNamespaceAndPath(
            Constants.MOD_ID, "delete");
    private static final Map<PackType, Component> TITLES = new EnumMap<>(Map.of(
            PackType.CLIENT_RESOURCES, Component
                    .translatableWithFallback(KEY + "client-resources", "Resource Packs")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
            PackType.SERVER_DATA, Component
                    .translatableWithFallback(KEY + "server-data", "Data Packs")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));

    private final Collection<DependencyInfo> original;
    private final LinkedList<DependencyInfo> modifying;
    private final PackType packType;

    public DependenciesScreen(final Screen lastScreen, final PackType packType) {
        super(TITLES.get(packType), lastScreen);
        this.original = Config.INSTANCE.dependencies.stream()
                .filter(d -> d.type() == packType)
                .toList();
        this.modifying = new LinkedList<>(this.original);
        this.packType = packType;
    }

    protected void apply(final LinkedList<DependencyInfo> modified) {
        Config.INSTANCE.dependencies = new ArrayList<>(Config.INSTANCE.dependencies.stream()
                .filter(d -> !original.contains(d))
                .toList());
        Config.INSTANCE.dependencies.addAll(modified);
    }

    protected void editDependency(final DependencyInfo dependency) {
        final Consumer<DependencyInfo> injectModified = (final DependencyInfo modified) -> {
            this.modifying.replaceAll((final DependencyInfo original) -> {
                return original == dependency ? modified : original;
            });
            this.rebuildSettings();
        };
        this.minecraft.setScreen(new DependencyEditScreen(this, dependency, injectModified));
    }

    @Override
    protected void populateSettings(final ScrollListContent.Builder builder) {
        for (final DependencyInfo dependency : this.modifying) {
            final List<LayoutElement> infoWidgets = new LinkedList<>();

            // Display the dependency's title
            int titleInfoWidth = this.innerWidth() - 4;
            final var titleInfo = new StringWidget(Component.literal(dependency.title()), font);
            infoWidgets.add(titleInfo);

            // Display the dependency's active hosts as badges next to the title
            for (final DependencyInfo.Host host : dependency.hosts()) {
                infoWidgets.add(SpacerElement.width(4));
                final ToggleSprites toggleSprites = ToggleSprites.Hosts.get(host);
                infoWidgets.add(ImageWidget.sprite(
                        toggleSprites.width(), toggleSprites.height(), toggleSprites.onSprite()));
                titleInfoWidth -= toggleSprites.width() + 4;
            }

            // Button which modifies the dependency's information
            final Button editButton = SpriteIconButton.builder(Component.empty(),
                    (final Button button) -> editDependency(dependency),
                    true)
                    .size(Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT)
                    .sprite(EDIT_ICON, Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT)
                    .build();
            editButton.setTooltip(Tooltip.create(
                    Component.translatableWithFallback(KEY + ".edit.tooltip",
                            "Edit dependency")));
            titleInfoWidth -= editButton.getWidth() + 4;

            // Button which deletes the dependency from the list
            final Button deleteButton = SpriteIconButton.builder(
                    Component.empty(),
                    (final Button button) -> {

                        // On click, re-build the scroll list with this dependency removed.
                        this.modifying.remove(dependency);
                        this.rebuildSettings();
                    },
                    true)
                    .size(Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT)
                    .sprite(DELETE_ICON, Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT)
                    .build();
            deleteButton.setTooltip(Tooltip.create(
                    Component.translatableWithFallback(KEY + ".delete.tooltip",
                            "Delete dependency").withStyle(ChatFormatting.RED)));
            titleInfoWidth -= deleteButton.getWidth() + 4;

            // Arrange the edit & delete buttons
            final List<LayoutElement> buttons = new LinkedList<>();
            buttons.add(editButton);
            buttons.add(SpacerElement.width(4));
            buttons.add(deleteButton);

            // Adjust the title's width to fit
            titleInfo.setWidth(Math.min(titleInfo.getWidth(), titleInfoWidth));

            // Commit the line with information on the left and buttons on the right.
            builder.addLine(infoWidgets, buttons);
        }

        // Button to add a new dependency with default values to the list
        builder.addElement(Button.builder(
                Component.literal("+").withStyle(ChatFormatting.BOLD),
                (final Button button) -> {
                    final DependencyInfo dependency = DependencyInfo.createDefault(this.packType);
                    this.modifying.add(dependency);
                    this.rebuildSettings();
                    editDependency(dependency); // Automatically opens the editor for it
                }).size(Button.DEFAULT_HEIGHT * 2, Button.DEFAULT_HEIGHT)
                .build());
    }

    @Override
    public void onClose() {
        this.apply(this.modifying);
        super.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
