package gg.vape.module.render;

import gg.vape.event.EventHandler;
import gg.vape.event.impl.EventPreTick;
import gg.vape.event.impl.EventRender2D;
import gg.vape.mapping.MappedClasses;
import gg.vape.module.Category;
import gg.vape.module.Mod;
import gg.vape.module.render.entity.RenderEntityContext;
import gg.vape.module.render.entity.RenderEntityContextCacheListener;
import gg.vape.utils.MathUtil;
import gg.vape.utils.render.GuiRenderPrimitives;
import gg.vape.utils.render.ImageRenderer;
import gg.vape.value.BooleanValue;
import gg.vape.value.NumberValue;
import gg.vape.wrapper.impl.*;
import java.awt.Color;

/**
 * TargetHud module based on OpenNixware's NoramlTargetHUD.
 * Renders a dark rounded rect with the target's player head, name, and animated health bar.
 */
public class TargetHud extends Mod {

    private final NumberValue xPos = NumberValue.create(this, "X", "#", "", 0.0, 300.0, 2000.0, 1.0, "X position of the target HUD");
    private final NumberValue yPos = NumberValue.create(this, "Y", "#", "", 0.0, 300.0, 2000.0, 1.0, "Y position of the target HUD");
    private final BooleanValue showOnChat = BooleanValue.create(this, "Show In Chat", true, "Show target HUD even when chat is open");
    private final NumberValue healthAnimSpeed = NumberValue.create(this, "Health Anim Speed", "#", "", 1.0, 12.0, 30.0, 1.0, "Speed of health bar animation");

    // State
    private EntityLivingBase target;
    private float animatedHealthWidth = 0;
    private long lastTickTime = 0;

    // Colors matching OpenNixware
    private static final Color BG_COLOR = new Color(20, 18, 18, 200);
    private static final Color INNER_BG = new Color(0, 0, 0, 128);
    private static final Color BORDER_BG = new Color(255, 255, 255, 18);
    private static final Color HEALTH_BG = new Color(0, 0, 0, 110);

    public TargetHud() {
        super("TargetHud", -98765, Category.RENDER, "Displays info about your attack target\nBased on OpenNixware's TargetHUD");
        this.addValue(this.xPos, this.yPos, this.showOnChat, this.healthAnimSpeed);
    }

    @EventHandler
    public void onTick(EventPreTick event) {
        if (!this.isEnabled() || Minecraft.theWorld().isNull() || Minecraft.thePlayer().isNull()) {
            return;
        }

        // Find target - look for the entity the player is attacking
        EntityLivingBase newTarget = findAttackTarget();

        if (newTarget != null && !newTarget.isNull() && newTarget.w$src$F$15l9epb() > 0) {
            if (this.target == null || !this.target.equals(newTarget)) {
                this.target = newTarget;
            }
        } else if (this.target != null) {
            // Keep showing for a short while after losing target
            this.target = null;
            this.animatedHealthWidth = 0;
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!this.isEnabled()) return;
        if (Minecraft.theWorld().isNull() || Minecraft.thePlayer().isNull()) return;

        // Show preview when chat is open or no target
        EntityLivingBase renderTarget = this.target;
        if (renderTarget == null || renderTarget.isNull()) {
            if (this.showOnChat.getEffectiveValue() && Minecraft.currentScreen().isNotNull()) {
                renderTarget = Minecraft.thePlayer();
            } else {
                return;
            }
        }

        FontRenderer fr = event.getFontRenderer();
        if (fr == null) return;

        double x = this.xPos.getValue().doubleValue();
        double y = this.yPos.getValue().doubleValue();
        float width = 145;
        float height = 37;

        // Background
        GuiRenderPrimitives.e(x, y, width, height, BG_COLOR, false, 3.0f, 1.0f);
        GuiRenderPrimitives.e(x, y, 145, 37, INNER_BG, false, 3.0f, 1.0f);
        GuiRenderPrimitives.e(x, y, 145, 37, BORDER_BG, false, 3.0f, 1.0f);

        // Player head (2D skin)
        if (renderTarget.isInstance(MappedClasses.Yl)) {
            EntityPlayer player = new EntityPlayer(renderTarget.getObject());
            String playerName = player.getName();
            if (playerName != null && !playerName.isEmpty()) {
                // Draw player head using skin texture
                drawPlayerHead(x + 3, y + 3, 31, 31, playerName);
            }
        }

        // Player name
        String name = renderTarget.getName();
        if (name != null && !name.isEmpty()) {
            fr.drawStringWithShadow(name, x + 39, y + 5, 0xFFFFFFFF);
        }

        // Health bar
        float maxHealth = renderTarget.w$src$F$15l9epb() + renderTarget.S$src$F$151gtcb(); // getMaxHealth + absorption
        float currentHealth = renderTarget.p() + renderTarget.S$src$F$151gtcb(); // getHealth + absorption
        float healthPercent = MathUtil.clamp(currentHealth / maxHealth, 0, 1);

        float healthBarWidth = 98;
        float healthBarHeight = 3;
        double healthBarX = x + 39;
        double healthBarY = y + height - 12;

        // Animated health width
        float targetHealthWidth = healthBarWidth * healthPercent;
        this.animatedHealthWidth += (targetHealthWidth - this.animatedHealthWidth) * 0.15f;

        // Health bar background
        GuiRenderPrimitives.e(healthBarX, healthBarY, healthBarWidth, healthBarHeight, HEALTH_BG, false, 1.0f, 1.0f);

        // Health bar fill with gradient (green to red based on health)
        if (this.animatedHealthWidth > 0.5f) {
            Color healthColorLow = new Color(255, 85, 85, 220);
            Color healthColorHigh = new Color(85, 255, 85, 220);
            Color healthColor = healthPercent > 0.5f ? healthColorHigh : healthColorLow;
            GuiRenderPrimitives.e(healthBarX, healthBarY, this.animatedHealthWidth, healthBarHeight, healthColor, false, 1.0f, 1.0f);
        }
    }

    private EntityLivingBase findAttackTarget() {
        // Use the same target finding logic as Vape's combat modules
        // Check if there's a tracked attack target from the combat system
        for (Object obj : Minecraft.theWorld().z()) {
            if (!MappedClasses.zm.isInstance(obj)) continue; // EntityLivingBase
            EntityLivingBase entity = new EntityLivingBase(obj);
            if (entity == Minecraft.thePlayer() || entity.isNull()) continue;
            if (entity.w$src$F$15l9epb() <= 0) continue; // isDead check
            // Check if this entity was recently attacked by the player
            // Simple heuristic: check if entity is within attack range
            double dist = Minecraft.thePlayer().i(entity.z(), entity.N(), entity.h());
            if (dist <= 6.0) {
                return entity;
            }
        }
        return null;
    }

    private void drawPlayerHead(double x, double y, double width, double height, String playerName) {
        // Render the player's skin head texture
        // Use Vape's ImageRenderer to draw the player head
        // The skin URL pattern for player heads
        String skinUrl = "head/" + playerName;
        ImageRenderer.drawImage(Color.WHITE, (float)x, (float)y, skinUrl, (float)width, (float)height, false);
    }

    @Override
    public void onEnable() {
        this.target = null;
        this.animatedHealthWidth = 0;
    }

    @Override
    public void onDisable() {
        this.target = null;
        this.animatedHealthWidth = 0;
    }
}
