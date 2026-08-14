package dev.nekotune.mdm.client.gui.config.dependencies;

import java.util.EnumMap;
import java.util.Set;
import java.util.function.Consumer;

import dev.nekotune.mdm.client.gui.config.AbstractConfigScreen;
import dev.nekotune.mdm.client.gui.config.widgets.ScrollListContent.Builder;
import dev.nekotune.mdm.client.gui.config.widgets.input.LinkedListInput;
import dev.nekotune.mdm.client.gui.config.widgets.input.SelectionInput;
import dev.nekotune.mdm.client.gui.config.widgets.input.ToggleInput;
import dev.nekotune.mdm.definition.DependencyInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Pop-up screen to edit a dependency.
 */
public class DependencyEditScreen extends AbstractConfigScreen {

    protected static final String KEY = DependenciesScreen.KEY + ".edit";
    private static final Component TITLE = Component.translatableWithFallback(KEY + ".title", "Edit Dependency")
            .withStyle(ChatFormatting.BOLD);
    private static final int BG_COLOR = 0xBB000000;

    private final Consumer<DependencyInfo> apply;
    private final DependencyInfo original;
    private final Component subtitle;
    private SettingsWidgets settingsWidgets;
    private Button applyButton = Button.builder(Component.empty(), $ -> {
    }).build();

    protected DependencyEditScreen(final DependenciesScreen below,
            final DependencyInfo dependency, final Consumer<DependencyInfo> apply) {
        super(TITLE, below);
        final MutableComponent subtitle = Component.empty();
        switch (dependency.type()) {
            case CLIENT_RESOURCES:
                subtitle.append(Component
                        .translatableWithFallback(KEY + ".subtitle.resource-pack", "Resource Pack")
                        .withStyle(ChatFormatting.GREEN));
                break;
            case SERVER_DATA:
                subtitle.append(
                        Component.translatableWithFallback(KEY + ".subtitle.data-pack", "Data Pack")
                                .withStyle(ChatFormatting.GOLD));
                break;
        }
        this.subtitle = subtitle;
        this.apply = apply;
        this.original = dependency;
    }

    @Override
    protected void populateSettings(final Builder builder) {
        final String hostsKey = KEY + ".hosts";
        final LinearLayout hostsLayout = LinearLayout.horizontal().spacing(4);
        this.settingsWidgets.hosts.values().forEach(hostsLayout::addChild);

        builder.addSetting(KEY + ".slug", this.settingsWidgets.slug());
        builder.addElement(this.settingsWidgets.mirrors());
        builder.addSetting(hostsKey, hostsLayout);
        builder.addSetting(KEY + ".mode", this.settingsWidgets.mode());
        builder.addSetting(KEY + ".load-priority", this.settingsWidgets.loadPriority());
    }

    @Override
    protected void init() {
        this.settingsWidgets = SettingsWidgets.create(this);
        super.init(); // Calls populateSettings

        // Button to apply changes
        final Button.OnPress onApplyPressed = $ -> {
            this.applyButton.active = false;
            int loadPriority = 0;
            try {
                loadPriority = Integer.valueOf(this.settingsWidgets.loadPriority().getValue());
            } catch (final NumberFormatException e) {
            }
            final Set<DependencyInfo.Host> hosts = Set.copyOf(this.settingsWidgets.hosts().keySet().stream()
                    .filter(host -> this.settingsWidgets.hosts().get(host).getValue())
                    .toList());
            if (hosts.isEmpty()) {
                hosts.add(DependencyInfo.Host.MODRINTH);
            }
            this.apply.accept(new DependencyInfo(
                    this.original.type(),
                    this.settingsWidgets.slug().getValue(),
                    this.settingsWidgets.mirrors().getValues(),
                    hosts,
                    this.settingsWidgets.mode().getValue(),
                    loadPriority));
        };
        this.applyButton = Button.builder(
                Component.translatableWithFallback(KEY + ".button.apply", "Apply"), onApplyPressed)
                .size(Button.SMALL_WIDTH, Button.DEFAULT_HEIGHT)
                .pos(this.width / 2 - (this.applyButton.getWidth() / 2),
                        this.height - this.barHeight() / 2 - (this.applyButton.getHeight() / 2))
                .build();
        this.addRenderableWidget(this.applyButton);
    }

    // Render the subtitle along with the main title
    @Override
    public void renderTitle(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        guiGraphics.drawCenteredString(this.font, this.title,
                this.width / 2, this.barHeight() / 2 - 5, 0xFFFFFFFF);
        guiGraphics.drawCenteredString(this.font, this.subtitle,
                this.width / 2, this.barHeight() / 2 + 5, 0xFFFFFFFF);
    }

    // Remove the default close button in favor of the "apply changes" button
    @Override
    public void renderBottomButton(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
    }

    // Render a blurred version of the screen below this one
    @Override
    public void renderBackground(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.lastScreen.render(guiGraphics, -1, -1, partialTick);
        guiGraphics.flush();
        this.minecraft.gameRenderer.processBlurEffect(partialTick);
        this.minecraft.getMainRenderTarget().bindWrite(true);
        guiGraphics.fill(0, 0, this.width, this.height, BG_COLOR);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    protected static record SettingsWidgets(
            EditBox slug,
            LinkedListInput mirrors,
            EnumMap<DependencyInfo.Host, ToggleInput.IconToggle> hosts,
            SelectionInput<DependencyInfo.Mode> mode,
            EditBox loadPriority) {

        public static SettingsWidgets create(final DependencyEditScreen screen) {

            // Primary slug setting
            final var slugEditBox = new EditBox(screen.font, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT,
                    Component.literal(screen.original.slug()));
            slugEditBox.setFilter(DependencyInfo.SLUG_VALIDATOR);
            slugEditBox.setResponder(text -> screen.applyButton.active = true);

            // Mirror slugs setting
            final var mirrorsList = new LinkedListInput(0, 0, screen.innerWidth(), screen.height, screen.font,
                    Component.translatable(KEY + ".mirrors"));
            mirrorsList.setResponder(values -> screen.applyButton.active = true);
            mirrorsList.setFilter(DependencyInfo.SLUG_VALIDATOR);
            mirrorsList.onCollapsedChanged = $ -> screen.rebuildSettings();

            // Hosts setting
            final EnumMap<DependencyInfo.Host, ToggleInput.IconToggle> hostToggles = new EnumMap<>(
                    DependencyInfo.Host.class);
            final String hostsKey = KEY + ".hosts";
            final LinearLayout hostsLayout = LinearLayout.horizontal();
            hostsLayout.spacing(4);
            final String tooltipKey = hostsKey + ".%s.tooltip";
            for (final DependencyInfo.Host host : DependencyInfo.Host.values()) {
                final boolean enabled = screen.original.hosts().contains(host);
                final var toggleInput = new ToggleInput.IconToggle(
                        ToggleSprites.Hosts.get(host), enabled,
                        newValue -> screen.applyButton.active = true);
                hostToggles.put(host, toggleInput);
                final String hostTooltipKey = tooltipKey.formatted(host.name()
                        .toLowerCase().replace('_', '-'));
                toggleInput.setTooltip(Tooltip.create(Component.translatable(hostTooltipKey)));
                hostsLayout.addChild(toggleInput);
            }

            // Mode setting
            final var modeInput = new SelectionInput<DependencyInfo.Mode>(0, 0,
                    Button.SMALL_WIDTH, Button.DEFAULT_HEIGHT,
                    Set.of(DependencyInfo.Mode.values()), screen.original.mode(),
                    $ -> screen.applyButton.active = true);

            // Load priority setting
            final var loadPriorityEditBox = new EditBox(screen.font, Button.SMALL_WIDTH, Button.DEFAULT_HEIGHT,
                    Component.literal(String.valueOf(screen.original.loadPriority())));
            loadPriorityEditBox.setFilter(text -> text.isEmpty()
                    || (text.matches("^\\d+$") && text.length() <= 3));
            loadPriorityEditBox.setResponder(text -> screen.applyButton.active = true);

            return new SettingsWidgets(slugEditBox, mirrorsList, hostToggles, modeInput, loadPriorityEditBox);
        }
    }
}
