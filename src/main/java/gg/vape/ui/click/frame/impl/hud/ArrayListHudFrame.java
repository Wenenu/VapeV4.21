package gg.vape.ui.click.frame.impl.hud;

import gg.vape.Vape;
import gg.vape.module.Category;
import gg.vape.module.Mod;
import gg.vape.module.render.hud.ArrayListHudModule;
import gg.vape.ui.font.SmoothFontRenderer;
import gg.vape.unmap.ColorUtil;
import gg.vape.utils.render.GuiRenderPrimitives;
import gg.vape.wrapper.impl.Minecraft;
import gg.vape.wrapper.impl.ScaledResolution;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayListHudFrame
extends HudModuleConfigFrameBase<ArrayListHudModule> {
    private static final double PADDING = 5.0;
    private static final double ROW_MARGIN = 1.0;
    private final ArrayListHudModule arrayListModule = (ArrayListHudModule)this.getModule();
    private final Map<Mod, Float> animationProgress = new HashMap<Mod, Float>();
    private final List<Mod> rowModules = new ArrayList<Mod>();
    private double contentWidth = 80.0;
    private double contentHeight = 20.0;

    @Override
    public String getName() {
        return "ArrayListFrame";
    }

    public ArrayListHudFrame() {
        super(ArrayListHudModule.class);
    }

    @Override
    public double A() {
        return this.contentWidth;
    }

    @Override
    public double L() {
        return this.contentHeight;
    }

    private boolean isRightAligned() {
        ScaledResolution scaledResolution = Minecraft.G();
        return this.G$src$D$1b2f02a() + this.A() / 2.0 >= (double)scaledResolution.getScaledWidth() / 2.0;
    }

    private List<Mod> collectEnabledModules() {
        ArrayList<Mod> modules = new ArrayList<Mod>();
        for (Mod mod : Vape.INSTANCE.getModManager().collectMods()) {
            if (mod == this.arrayListModule || !mod.isEnabled() || mod.getGuiColor() == 0 || !mod.q$src$Z$12h8h4c()) continue;
            if (this.arrayListModule.importantModules.getEffectiveValue().booleanValue() && mod.getCategory() == Category.RENDER) continue;
            modules.add(mod);
        }
        return modules;
    }

    private void updateAnimations(List<Mod> enabledModules) {
        for (Mod mod : enabledModules) {
            Float progress = this.animationProgress.get(mod);
            this.animationProgress.put(mod, Float.valueOf(progress == null ? 0.0f : Math.min(1.0f, progress.floatValue() + 0.08f)));
        }
        ArrayList<Mod> toRemove = new ArrayList<Mod>();
        for (Map.Entry<Mod, Float> entry : this.animationProgress.entrySet()) {
            Mod mod = entry.getKey();
            if (enabledModules.contains(mod)) continue;
            float progress = entry.getValue().floatValue() - 0.08f;
            if (progress <= 0.0f) {
                toRemove.add(mod);
                continue;
            }
            entry.setValue(Float.valueOf(progress));
        }
        for (Mod mod : toRemove) {
            this.animationProgress.remove(mod);
        }
    }

    @Override
    public void renderHudContent() {
        SmoothFontRenderer font = Vape.INSTANCE.getFontManager().W(1.0, false);
        List<Mod> enabledModules = this.collectEnabledModules();
        this.updateAnimations(enabledModules);
        this.rowModules.clear();
        for (Map.Entry<Mod, Float> entry : this.animationProgress.entrySet()) {
            if (entry.getValue().floatValue() <= 0.0f) continue;
            this.rowModules.add(entry.getKey());
        }
        this.rowModules.sort(Comparator.comparingDouble((Mod mod) -> this.getTextWidth(font, mod)).reversed());
        double frameX = this.G$src$D$1b2f02a();
        double frameY = this.n();
        boolean rightAligned = this.isRightAligned();
        double rowHeight = this.arrayListModule.height.getValue() + ROW_MARGIN;
        double maxRowWidth = 0.0;
        double yOffset = 0.0;
        int count = 0;
        int lastIndex = this.rowModules.size() - 1;
        for (Mod mod : this.rowModules) {
            String text = this.getDisplayText(mod);
            float progress = this.animationProgress.get(mod).floatValue();
            double textWidth = font.N(text);
            maxRowWidth = Math.max(maxRowWidth, textWidth);
            double x = rightAligned ? frameX + this.A() - textWidth - PADDING : frameX + PADDING;
            double y = frameY + yOffset;
            Color textColor = this.getRowColor(count);
            textColor = this.applyDefaultEditorAlpha(textColor);
            boolean scaleIn = this.arrayListModule.animation.getValue() == this.arrayListModule.scaleInAnimation;
            boolean moveIn = this.arrayListModule.animation.getValue() == this.arrayListModule.moveInAnimation;
            if (moveIn) {
                if (rightAligned) {
                    x += (1.0 - (double)progress) * (textWidth + this.A());
                } else {
                    x -= (1.0 - (double)progress) * (textWidth + this.A());
                }
            } else if (scaleIn) {
                if (rightAligned) {
                    x += (1.0 - (double)progress) * textWidth / 2.0;
                } else {
                    x -= (1.0 - (double)progress) * textWidth / 2.0;
                }
            }
            if (this.arrayListModule.background.getEffectiveValue().booleanValue()) {
                Color background = this.arrayListModule.backgroundColor.getEffectiveValue().booleanValue() ? textColor : new Color(10, 10, 10);
                int alpha = (int)(255.0f * this.arrayListModule.backgroundAlpha.getValue().floatValue() * progress);
                GuiRenderPrimitives.d(x - 2.0, y, textWidth + 6.0, rowHeight, ColorUtil.withAlpha(background, alpha));
            }
            double textY = y + (rowHeight - font.d(text)) / 2.0;
            this.renderText(font, text, x, textY, textColor, progress);
            this.renderRectangle(textWidth, x, y, rowHeight, textColor, count, lastIndex, rightAligned);
            yOffset += (double)progress * rowHeight;
            ++count;
        }
        double width = maxRowWidth + PADDING * 2.0;
        double height = yOffset + (this.rowModules.isEmpty() ? 14.0 : 0.0);
        this.contentWidth = Math.max(width, 30.0);
        this.contentHeight = Math.max(height, 14.0);
    }

    private String getDisplayText(Mod mod) {
        String suffix = mod.getSuffixForMode(this.arrayListModule.getSuffixModeIndex());
        if (suffix == null || suffix.isEmpty()) {
            return mod.getName();
        }
        return mod.getName() + " \u00a77" + suffix;
    }

    private double getTextWidth(SmoothFontRenderer font, Mod mod) {
        return font.N(this.getDisplayText(mod));
    }

    private void renderText(SmoothFontRenderer font, String text, double x, double y, Color color, float progress) {
        switch (this.arrayListModule.textShadow.getValue().getName()) {
            case "None": {
                font.d(text, x, y, color);
                break;
            }
            case "Colored": {
                Color darker = new Color(color.getRed() / 2, color.getGreen() / 2, color.getBlue() / 2, color.getAlpha());
                font.d(text, x + 1.0, y + 1.0, darker);
                font.d(text, x, y, color);
                break;
            }
            default: {
                font.T(text, x, y, color, ColorUtil.withAlpha(Color.BLACK, (int)(255.0f * progress)));
            }
        }
    }

    private void renderRectangle(double textWidth, double x, double y, double rowHeight, Color color, int count, int lastIndex, boolean rightAligned) {
        switch (this.arrayListModule.rectangle.getValue().getName()) {
            case "Top": {
                if (count == 0) {
                    GuiRenderPrimitives.a(x - 2.0, y - 1.0, textWidth + 6.0, 1.0f, color);
                }
                break;
            }
            case "Side": {
                if (rightAligned) {
                    GuiRenderPrimitives.d(x + textWidth + 2.0, y, rowHeight, 1.0f, color);
                } else {
                    GuiRenderPrimitives.d(x - 3.0, y, rowHeight, 1.0f, color);
                }
                break;
            }
            case "Outline": {
                if (count == 0) {
                    GuiRenderPrimitives.a(x - 3.0, y - 1.0, textWidth + 6.0, 1.0f, color);
                }
                if (count == lastIndex) {
                    GuiRenderPrimitives.a(x - 3.0, y + rowHeight, textWidth + 6.0, 1.0f, color);
                }
                if (rightAligned) {
                    GuiRenderPrimitives.d(x + textWidth + 2.0, y, rowHeight, 1.0f, color);
                } else {
                    GuiRenderPrimitives.d(x - 3.0, y, rowHeight, 1.0f, color);
                }
                break;
            }
        }
    }

    private Color getRowColor(int index) {
        int rowIndex = (int)((double)index * this.arrayListModule.colorIndex.getValue());
        Color guiColor = Vape.INSTANCE.getClientSettings().guiColor.getMutableColor();
        if (Vape.INSTANCE.getClientSettings().guiColor.isRainbowEnabled()) {
            int angle = (int)((System.currentTimeMillis() / this.arrayListModule.colorSpeed.getValue() + (double)rowIndex) % 360.0);
            float hue = (float)angle / 360.0f;
            return ColorUtil.createReadableHsbColor(hue, 0.9f, 1.0f, 4);
        }
        return this.interpolateColorsBackAndForth(this.arrayListModule.colorSpeed.getValue().intValue(), rowIndex, guiColor, Color.WHITE);
    }

    private Color interpolateColorsBackAndForth(int speed, int index, Color color1, Color color2) {
        int angle = (int)((System.currentTimeMillis() / (double)speed + (double)index) % 360.0);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        float ratio = (float)angle / 360.0f;
        return new Color((int)((float)color1.getRed() + (float)(color2.getRed() - color1.getRed()) * ratio), (int)((float)color1.getGreen() + (float)(color2.getGreen() - color1.getGreen()) * ratio), (int)((float)color1.getBlue() + (float)(color2.getBlue() - color1.getBlue()) * ratio), color1.getAlpha());
    }
}
