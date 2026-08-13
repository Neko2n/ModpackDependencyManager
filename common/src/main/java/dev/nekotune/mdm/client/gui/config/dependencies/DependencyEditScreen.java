package dev.nekotune.mdm.client.gui.config.dependencies;

import java.util.EnumMap;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import dev.nekotune.mdm.Constants;
import dev.nekotune.mdm.client.gui.config.widgets.ScrollListContent;
import dev.nekotune.mdm.client.gui.config.widgets.SettingsListWidget;
import dev.nekotune.mdm.client.gui.config.widgets.input.LinkedListInput;
import dev.nekotune.mdm.client.gui.config.widgets.input.SelectionInput;
import dev.nekotune.mdm.client.gui.config.widgets.input.ToggleInput;
import dev.nekotune.mdm.definition.DependencyInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Pop-up screen to edit a dependency.
 */
public class DependencyEditScreen extends Screen {

    protected static final String KEY = AbstractDependenciesScreen.KEY + ".edit";
    private static final Component TITLE = Component.translatableWithFallback(KEY + ".title", "Edit Dependency")
            .withStyle(ChatFormatting.BOLD);
    private static final int BG_COLOR = 0x75000000;
    private static final int PADDING = 40;
    private static final int SPACING = 20;

    public static record ButtonSprites(ResourceLocation onSprite, ResourceLocation offSprite) {
        private static final String KEY = DependencyEditScreen.KEY + ".hosts";
        public static final ButtonSprites MODRINTH = new ButtonSprites(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, KEY + ".modrinth.on"),
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, KEY + ".modrinth.off"));
        public static final ButtonSprites CURSEFORGE = new ButtonSprites(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, KEY + ".curseforge.on"),
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, KEY + ".curseforge.off"));
    }

    protected Supplier<Integer> innerWidth = () -> width - PADDING * 2;
    protected Supplier<Integer> innerHeight = () -> height - PADDING * 2;
    private final EnumMap<DependencyInfo.Host, Boolean> hostToggles = new EnumMap<>(DependencyInfo.Host.class);
    private final Component subtitle;
    private final AbstractDependenciesScreen below;
    private final Consumer<DependencyInfo> apply;
    private final Button applyButton;
    private final SettingsListWidget settings;
    private final Supplier<DependencyInfo> modifiedDependency;

    protected DependencyEditScreen(final AbstractDependenciesScreen below,
            final DependencyInfo dependency, final Consumer<DependencyInfo> apply) {
        super(TITLE);
        this.below = below;
        this.apply = apply;

        final MutableComponent subtitle = Component.empty();
        switch (dependency.type()) {
            case CLIENT_RESOURCES:
                subtitle.append(Component.translatableWithFallback(KEY + ".subtitle.resource-pack", "Resource Pack")
                        .withStyle(ChatFormatting.GREEN));
                break;
            case SERVER_DATA:
                subtitle.append(Component.translatableWithFallback(KEY + ".subtitle.data-pack", "Data Pack")
                        .withStyle(ChatFormatting.GOLD));
                break;
        }
        this.subtitle = subtitle;

        final var listBuilder = new ScrollListContent.Builder(innerWidth.get(), this.font);

        // Primary slug setting
        final var slugEditBox = new EditBox(font, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT,
                Component.literal(dependency.slug()));
        slugEditBox.setFilter(DependencyInfo.SLUG_VALIDATOR);
        listBuilder.addSetting(KEY + ".settings.slug", slugEditBox);

        // Mirror slugs setting
        final var mirrorsLabel = new StringWidget(Component.translatable(KEY + ".settings.mirrors"), font);
        final var mirrorsList = new LinkedListInput(0, 0, innerWidth.get(), height, font,
                Component.empty());
        mirrorsList.setFilter(DependencyInfo.SLUG_VALIDATOR);
        final LinearLayout mirrorsLayout = LinearLayout.vertical();
        mirrorsLayout.spacing(4);
        mirrorsLayout.addChild(mirrorsLabel);
        mirrorsLayout.addChild(mirrorsList);
        listBuilder.addElement(mirrorsLayout);

        // Hosts setting
        final String hostsKey = KEY + ".settings.hosts";
        final LinearLayout hostsLayout = LinearLayout.horizontal();
        hostsLayout.spacing(4);
        final String tooltipKey = hostsKey + ".%s.tooltip";
        for (final DependencyInfo.Host host : DependencyInfo.Host.values()) {
            final boolean enabled = dependency.hosts().contains(host);
            this.hostToggles.put(host, enabled);
            final var toggleInput = new ToggleInput.IconToggle(
                    ButtonSprites.MODRINTH.onSprite(), ButtonSprites.MODRINTH.offSprite(),
                    enabled, newValue -> this.hostToggles.put(host, newValue));
            final String hostTooltipKey = tooltipKey.formatted(host.name()
                    .toLowerCase().replace('_', '-'));
            toggleInput.setTooltip(Tooltip.create(Component.translatable(hostTooltipKey)));
            hostsLayout.addChild(toggleInput);
        }
        listBuilder.addSetting(hostsKey, hostsLayout);

        // Mode setting
        final String modeKey = KEY + ".settings.mode";
        final var modeInput = new SelectionInput<DependencyInfo.Mode>(0, 0,
                Button.SMALL_WIDTH, Button.DEFAULT_HEIGHT,
                Set.of(DependencyInfo.Mode.values()), dependency.mode());
        listBuilder.addSetting(modeKey, modeInput);

        // Load priority setting
        final var loadPriorityEditBox = new EditBox(font, Button.SMALL_WIDTH, Button.DEFAULT_HEIGHT,
                Component.literal(String.valueOf(dependency.loadPriority())));
        loadPriorityEditBox.setFilter(text -> text.isEmpty()
                || (text.matches("^\\d+$") && text.length() <= 3));
        listBuilder.addSetting(KEY + ".settings.load-priority", loadPriorityEditBox);

        // Build settings list
        this.settings = new SettingsListWidget(0, 0, innerWidth.get(), innerHeight.get(),
                listBuilder.build());

        this.modifiedDependency = () -> {
            int loadPriority = 0;
            try {
                loadPriority = Integer.valueOf(loadPriorityEditBox.getValue());
            } catch (final NumberFormatException e) {
            }
            return new DependencyInfo(
                    dependency.type(),
                    slugEditBox.getValue(),
                    mirrorsList.getValues(),
                    Set.copyOf(hostToggles.keySet().stream().filter(hostToggles::get).toList()),
                    modeInput.getValue(),
                    loadPriority);
        };

        this.applyButton = Button.builder(
                Component.translatableWithFallback(KEY + ".button.apply", "Apply"),
                (final Button button) -> {
                    button.active = false;
                    this.apply.accept(this.modifiedDependency.get());
                }).size(Button.SMALL_WIDTH, Button.DEFAULT_HEIGHT)
                .build();
    }

    @Override
    protected void init() {
        super.init();
        final LinearLayout layout = LinearLayout.vertical();
        layout.addChild(SpacerElement.width(width));
        layout.addChild(new StringWidget(this.title, font));
        layout.addChild(SpacerElement.height(4));
        layout.addChild(new StringWidget(this.subtitle, font));
        layout.addChild(SpacerElement.height(SPACING));
        layout.addChild(this.settings, settings -> settings.alignHorizontallyCenter());
        layout.addChild(SpacerElement.height(SPACING));
        layout.addChild(this.applyButton, settings -> settings.alignHorizontallyCenter());
        layout.addChild(SpacerElement.height(SPACING));
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void renderBackground(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.below.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.fill(0, 0, this.width, this.height, BG_COLOR);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(below);
    }
}
