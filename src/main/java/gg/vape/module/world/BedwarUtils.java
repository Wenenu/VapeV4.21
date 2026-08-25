package gg.vape.module.world;

import gg.vape.event.EventHandler;
import gg.vape.event.impl.EventChat;
import gg.vape.event.impl.EventPreTick;
import gg.vape.event.impl.EventRender2D;
import gg.vape.mapping.MappedClasses;
import gg.vape.module.Category;
import gg.vape.module.Mod;
import gg.vape.utils.BlockUtil;
import gg.vape.value.BooleanValue;
import gg.vape.value.ModeValue;
import gg.vape.value.NumberValue;
import gg.vape.value.StringValue;
import gg.vape.wrapper.impl.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BedwarUtils extends Mod {

    // --- Diamond Upgrade Tracker ---
    private final BooleanValue diamondUpgrades = BooleanValue.create(this, "Diamond Upgrades", true, "Tracks enemy diamond upgrades from chat");
    private boolean trap;
    private String trapType = "";
    private boolean sharp;
    private int protLevel;

    // --- Item Tracker ---
    private final BooleanValue itemTracker = BooleanValue.create(this, "Item Tracker", true, "Tracks player item purchases from chat");
    private final Set<String> trackedItemMessages = new HashSet<>();
    private static final Pattern ITEM_TRACKER_PATTERN = Pattern.compile(
            "(.+?)\\s+has\\s+(?:an?\\s+)?(.+?)(?:[.!])?$", Pattern.CASE_INSENSITIVE);

    // --- Bed Tracker ---
    private final BooleanValue bedTracker = BooleanValue.create(this, "Bed Tracker", true, "Tracks your bed and alerts on enemies nearby");
    private BlockPos bedPos;
    private long bedScanAt = -1;
    private boolean scannedThisGame;
    private boolean waiting;
    private final LinkedHashMap<String, Long> alertCooldowns = new LinkedHashMap<>();
    private final LinkedHashSet<EntityEnderPearl> trackedPearls = new LinkedHashSet<>();
    private final LinkedHashSet<String> whitelistedPlayers = new LinkedHashSet<>();
    private final LinkedHashSet<String> autoIncPlayers = new LinkedHashSet<>();
    private long lastMacroTime = -1;

    // Bed Tracker - Alerts
    private final BooleanValue bedTrackerAlerts = BooleanValue.create(this, "Alerts", true, "Alert when enemies are near your bed");
    private final NumberValue bedTrackerAlertRange = NumberValue.create(this, "Alert Range", "#", "", 8.0, 48.0, 128.0, 1.0, "Range to alert for nearby enemies");
    private final BooleanValue bedTrackerAlertOnPearl = BooleanValue.create(this, "Alert On Pearl", true, "Alert when an ender pearl is detected");
    private final NumberValue bedTrackerAlertFrequency = NumberValue.create(this, "Alert Frequency", "#", "sec", 1.0, 5.0, 30.0, 1.0, "Minimum seconds between alerts for same player");

    // Bed Tracker - Auto Incoming
    private final BooleanValue bedTrackerAutoInc = BooleanValue.create(this, "Auto Inc", false, "Automatically sends 'inc' when enemies approach");

    // Bed Tracker - Macro
    private final BooleanValue bedTrackerMacro = BooleanValue.create(this, "Macro", false, "Automatically runs a command when enemies approach");
    private final NumberValue bedTrackerMacroRange = NumberValue.create(this, "Macro Range", "#", "", 8.0, 24.0, 128.0, 1.0, "Range to trigger macro");
    private final BooleanValue bedTrackerMacroOnPearl = BooleanValue.create(this, "Macro On Pearl", false, "Run macro when a pearl is detected");
    private final StringValue bedTrackerMacroText = StringValue.create(this, "Macro Text", "/lobby");
    private final NumberValue bedTrackerMacroDelay = NumberValue.create(this, "Macro Delay", "#", "sec", 1.0, 1.0, 10.0, 1.0, "Minimum seconds between macro executions");

    // --- Invisible Player Alert ---
    private final BooleanValue invisAlert = BooleanValue.create(this, "Invis Alert", true, "Alert when invisible/suspicious players are near your bed");
    private final LinkedHashMap<String, Long> invisAlertCooldowns = new LinkedHashMap<>();

    private static final long BED_SCAN_DELAY_MS = 3000L;
    private static final long BED_RESCAN_DELAY_MS = 5000L;

    public BedwarUtils() {
        super("BedwarUtils", -12345, Category.WORLD, "Tracks bedwars info: diamond upgrades, items, bed, and invisible players");
        this.addValue(this.diamondUpgrades, this.itemTracker, this.bedTracker, this.invisAlert);
        // Bed Tracker sub-values
        this.bedTrackerAlerts.addDependentValues(this.bedTrackerAlertRange, this.bedTrackerAlertOnPearl, this.bedTrackerAlertFrequency);
        this.bedTrackerAutoInc.addDependentValues();
        this.bedTrackerMacro.addDependentValues(this.bedTrackerMacroRange, this.bedTrackerMacroOnPearl, this.bedTrackerMacroText, this.bedTrackerMacroDelay);
        this.bedTracker.addDependentValues(this.bedTrackerAlerts, this.bedTrackerAlertRange, this.bedTrackerAlertOnPearl,
                this.bedTrackerAlertFrequency, this.bedTrackerAutoInc, this.bedTrackerMacro, this.bedTrackerMacroRange,
                this.bedTrackerMacroOnPearl, this.bedTrackerMacroText, this.bedTrackerMacroDelay);
    }

    // ==================== Event Handlers ====================

    @EventHandler
    public void onChat(EventChat event) {
        if (!this.isEnabled()) return;
        String text = event.getMessage().a(); // getUnformattedText
        String formattedText = event.getMessage().getFormattedText();
        scanMessage(text, formattedText);
    }

    @EventHandler
    public void onTick(EventPreTick event) {
        if (!this.isEnabled() || Minecraft.theWorld().isNull() || Minecraft.thePlayer().isNull()) return;

        // Item Tracker - scan player inventories
        if (this.itemTracker.getEffectiveValue()) {
            for (Object obj : Minecraft.theWorld().z()) {
                if (!MappedClasses.Yl.isInstance(obj)) continue;
                EntityPlayer player = new EntityPlayer(obj);
                if (player == Minecraft.thePlayer() || player.getName() == null || player.getName().isEmpty()) continue;
                scanPlayerItem(player, "held", player.getHeldItemHand());
                for (int slot = 0; slot < 4; slot++) {
                    // Armor slots are accessed differently
                }
            }
        }

        // Bed Tracker tick
        if (this.bedTracker.getEffectiveValue()) {
            bedTrackerTick();
        }

        // Invis Alert
        if (this.invisAlert.getEffectiveValue()) {
            scanInvisiblePlayers();
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!this.isEnabled()) return;
        FontRenderer fr = event.getFontRenderer();
        if (fr == null || Minecraft.theWorld().isNull() || Minecraft.thePlayer().isNull()) return;

        ScaledResolution sr = new ScaledResolution();
        double x = 4.0;
        double y = 66.0;

        if (this.diamondUpgrades.getEffectiveValue()) {
            // Trap line
            String trapPrefix = "- Trap: ";
            fr.drawStringWithShadow(trapPrefix, x, y, 0xFFFFFFFF);
            double valueX = x + fr.getStringWidth(trapPrefix);
            String trapValue = this.trap ? (this.trapType.isEmpty() ? "Unknown" : this.trapType) : "false";
            fr.drawStringWithShadow(trapValue, valueX, y, this.trap ? 0xFF55FF55 : 0xFFFF5555);
            y += 10.0;

            // Sharp line
            String sharpPrefix = "- Sharp: ";
            fr.drawStringWithShadow(sharpPrefix, x, y, 0xFFFFFFFF);
            valueX = x + fr.getStringWidth(sharpPrefix);
            fr.drawStringWithShadow(this.sharp ? "true" : "false", valueX, y, this.sharp ? 0xFF55FF55 : 0xFFFF5555);
            y += 10.0;

            // Prot line
            String protPrefix = "- Prot: ";
            fr.drawStringWithShadow(protPrefix, x, y, 0xFFFFFFFF);
            valueX = x + fr.getStringWidth(protPrefix);
            fr.drawStringWithShadow(this.protLevel > 0 ? "true" : "false", valueX, y, this.protLevel > 0 ? 0xFF55FF55 : 0xFFFF5555);
            if (this.protLevel > 0) {
                String suffix = " [" + toRoman(this.protLevel) + "]";
                fr.drawStringWithShadow(suffix, valueX + fr.getStringWidth("true"), y, 0xFFFFFFFF);
            }
            y += 10.0;
        }

        // Bed Tracker HUD
        if (this.bedTracker.getEffectiveValue()) {
            y += 10.0;
            boolean hasBed = isBed(this.bedPos);
            String bedPrefix = "Bed: ";
            fr.drawStringWithShadow(bedPrefix, x, y, 0xFFFFFFFF);
            double valueX = x + fr.getStringWidth(bedPrefix);
            fr.drawStringWithShadow(hasBed ? "true" : "false", valueX, y, hasBed ? 0xFF55FF55 : 0xFFFF5555);
        }
    }

    // ==================== Message Scanning ====================

    private void scanMessage(String text, String formattedText) {
        if (text == null) return;
        String lower = text.toLowerCase();

        // New game detection
        if (isNewGameMessage(lower)) {
            reset(true);
            return;
        }

        // Diamond Upgrades
        if (this.diamondUpgrades.getEffectiveValue()) {
            if (lower.contains("trap") || lower.contains("it's a trap") || lower.contains("alarm trap") || lower.contains("miner fatigue")) {
                this.trap = true;
                this.trapType = parseTrapType(lower);
            }
            if (lower.contains("sharpened swords") || lower.contains("sharpness") || lower.contains("sharp")) {
                this.sharp = true;
            }
            if (lower.contains("reinforced armor") || lower.contains("protection") || lower.contains("prot")) {
                int level = parseProtLevel(lower);
                this.protLevel = Math.max(this.protLevel, level <= 0 ? 1 : level);
            }
        }

        // Item Tracker - scan chat messages
        if (this.itemTracker.getEffectiveValue()) {
            scanItemTracker(text, formattedText);
        }
    }

    private boolean isNewGameMessage(String lower) {
        return lower.contains("protect your bed")
                || lower.contains("you are playing on")
                || lower.contains("the game starts in 1 second")
                || lower.contains("the game has started")
                || (lower.contains("bed wars") && lower.contains("protect your bed"));
    }

    private String parseTrapType(String lower) {
        if (lower.contains("alarm")) return "Alarm";
        if (lower.contains("miner fatigue") || lower.contains("miner")) return "Miner Fatigue";
        if (lower.contains("counter-offensive") || lower.contains("counter offensive") || lower.contains("counter")) return "Counter-Offensive";
        if (lower.contains("it's a trap") || lower.contains("its a trap")) return "It's a Trap";
        return "Unknown";
    }

    private int parseProtLevel(String text) {
        if (text.contains(" iv") || text.contains(" 4") || text.contains("level iv") || text.contains("level 4")) return 4;
        if (text.contains(" iii") || text.contains(" 3") || text.contains("level iii") || text.contains("level 3")) return 3;
        if (text.contains(" ii") || text.contains(" 2") || text.contains("level ii") || text.contains("level 2")) return 2;
        if (text.contains(" i") || text.contains(" 1") || text.contains("level i") || text.contains("level 1")) return 1;
        return 0;
    }

    // ==================== Item Tracker ====================

    private void scanPlayerItem(EntityPlayer player, String slot, ItemStack stack) {
        if (stack == null || stack.isNull()) return;
        String item = normalizeItemName(stack.x()); // x() = getDisplayName
        if (!isTrackedItem(item)) return;
        String key = player.getName().toLowerCase() + ":" + slot + ":" + item.toLowerCase();
        if (!trackedItemMessages.add(key)) return;
        sendItemTrackerMessage(player.getName(), item);
    }

    private void scanItemTracker(String text, String formattedText) {
        Matcher matcher = ITEM_TRACKER_PATTERN.matcher(text);
        if (!matcher.find()) return;
        String item = normalizeItemName(matcher.group(2).trim());
        if (!isTrackedItem(item)) return;
        String key = (matcher.group(1).trim() + " has " + item).toLowerCase();
        if (!trackedItemMessages.add(key)) return;
        String playerName = extractFormattedPlayer(formattedText, matcher.group(1).trim());
        sendItemTrackerMessage(playerName, item);
    }

    private boolean isTrackedItem(String item) {
        String lower = item.toLowerCase();
        boolean tieredGear = (lower.contains("stone") || lower.contains("iron") || lower.contains("diamond"))
                && (lower.contains("sword") || lower.contains("armor") || lower.contains("chestplate")
                || lower.contains("leggings") || lower.contains("boots") || lower.contains("helmet")
                || lower.contains("pickaxe") || lower.contains("axe"));
        boolean utilityItem = lower.contains("bow") || lower.contains("shears") || lower.contains("fireball")
                || lower.contains("ender pearl") || lower.contains("pearl") || lower.contains("invisibility")
                || lower.contains("invis") || lower.contains("jump") || lower.contains("speed");
        return tieredGear || utilityItem;
    }

    private String normalizeItemName(String item) {
        String normalized = item.replaceAll("(?i)^an?\\s+", "").trim();
        if (normalized.endsWith(".")) normalized = normalized.substring(0, normalized.length() - 1).trim();
        return normalized;
    }

    private String extractFormattedPlayer(String formattedText, String fallback) {
        if (formattedText == null) return fallback;
        String marker = " has ";
        String lowerFormatted = formattedText.toLowerCase();
        int index = lowerFormatted.indexOf(marker);
        if (index < 0) index = lowerFormatted.indexOf(" has an ");
        if (index < 0) index = lowerFormatted.indexOf(" has a ");
        return index > 0 ? formattedText.substring(0, index) : fallback;
    }

    private void sendItemTrackerMessage(String playerName, String item) {
        if (Minecraft.thePlayer().isNull()) return;
        Minecraft.thePlayer().sendChatMessage("");
        // Display as chat message in local client
        // We use addChatMessage equivalent via notification or just log
        // In Vape, we can send a chat message to display to the player
    }

    // ==================== Bed Tracker ====================

    private void bedTrackerTick() {
        long now = System.currentTimeMillis();

        // Automatic bed scan
        scheduleAutomaticBedScan();
        runPendingBedScan();
        pruneTrackedPearls();

        if (!isBed(this.bedPos)) return;

        boolean pearl = false;
        boolean macro = false;

        // Check for ender pearls
        for (Object entity : Minecraft.theWorld().z()) {
            if (MappedClasses.Zg.isInstance(entity)) { // EntityEnderPearl
                EntityEnderPearl enderPearl = new EntityEnderPearl(entity);
                if (!trackedPearls.contains(enderPearl)) {
                    trackedPearls.add(enderPearl);
                    if (this.bedTrackerAlertOnPearl.getEffectiveValue()) {
                        pearl = true;
                    }
                    if (this.bedTrackerMacroOnPearl.getEffectiveValue()
                            && lastMacroTime + (long) this.bedTrackerMacroDelay.getValue().doubleValue() * 1000L <= now) {
                        lastMacroTime = now;
                        macro = true;
                    }
                }
            }
        }

        // Check for enemy players near bed
        for (Object entity : Minecraft.theWorld().z()) {
            if (!MappedClasses.Yl.isInstance(entity)) continue;
            EntityPlayer player = new EntityPlayer(entity);
            if (player == Minecraft.thePlayer() || player.getName() == null || player.getName().isEmpty()) continue;
            if (whitelistedPlayers.contains(player.getName())) continue;

            // Skip bots/same team (simplified check)
            String name = player.getName();

            double distance = player.i(
                    this.bedPos.getX() + 0.5,
                    this.bedPos.getY() + 0.5,
                    this.bedPos.getZ() + 0.5
            );

            // Distance alert
            if (this.bedTrackerAlerts.getEffectiveValue() && distance < this.bedTrackerAlertRange.getValue().doubleValue()) {
                Long cooldown = alertCooldowns.get(name);
                if (cooldown == null || cooldown + (long) this.bedTrackerAlertFrequency.getValue().doubleValue() * 1000L <= now) {
                    alertCooldowns.put(name, now);
                    pearl = true;
                }
                if (this.bedTrackerAutoInc.getEffectiveValue() && autoIncPlayers.add(name.toLowerCase())) {
                    String team = getTeamName(player);
                    Minecraft.thePlayer().sendChatMessage(team.isEmpty() ? "inc" : team + " inc");
                }
            }

            // Pearl held alert
            if (this.bedTrackerAlertOnPearl.getEffectiveValue()) {
                ItemStack heldItem = player.getHeldItemHand();
                if (heldItem != null && !heldItem.isNull()) {
                    String itemName = heldItem.x().toLowerCase();
                    if (itemName.contains("pearl") || itemName.contains("ender pearl")) {
                        Long cooldown = alertCooldowns.get(name);
                        if (cooldown == null || cooldown + (long) this.bedTrackerAlertFrequency.getValue().doubleValue() * 1000L <= now) {
                            alertCooldowns.put(name, now);
                            pearl = true;
                        }
                    }
                }
            }

            // Macro trigger
            if ((this.bedTrackerMacro.getEffectiveValue() && distance < this.bedTrackerMacroRange.getValue().doubleValue())
                    || (this.bedTrackerMacroOnPearl.getEffectiveValue() && false /* pearl check handled above */)) {
                if (lastMacroTime + (long) this.bedTrackerMacroDelay.getValue().doubleValue() * 1000L <= now) {
                    lastMacroTime = now;
                    macro = true;
                }
            }
        }

        if (macro) {
            Minecraft.thePlayer().sendChatMessage(this.bedTrackerMacroText.getValue());
        }
    }

    private void scheduleAutomaticBedScan() {
        if (this.scannedThisGame || Minecraft.theWorld().isNull() || Minecraft.thePlayer().isNull() || isBed(this.bedPos)) return;
        if (this.bedScanAt == -1) {
            this.bedScanAt = System.currentTimeMillis() + BED_SCAN_DELAY_MS;
        }
    }

    private void runPendingBedScan() {
        if (this.bedScanAt == -1 || System.currentTimeMillis() < this.bedScanAt) return;
        this.bedScanAt = -1;
        if (Minecraft.theWorld().isNull() || Minecraft.thePlayer().isNull()) {
            this.bedScanAt = System.currentTimeMillis() + BED_RESCAN_DELAY_MS;
            return;
        }
        int x = (int) Math.floor(Minecraft.thePlayer().z());
        int y = (int) Math.floor(Minecraft.thePlayer().N() + Minecraft.thePlayer().X());
        int z = (int) Math.floor(Minecraft.thePlayer().h());

        for (int i = x - 25; i <= x + 25; i++) {
            for (int j = y - 25; j <= y + 25; j++) {
                for (int k = z - 25; k <= z + 25; k++) {
                    Block block = Minecraft.theWorld().getBlockByPos(i, j, k);
                    if (BlockUtil.f(block)) {
                        this.bedPos = BlockPos.create(i, j, k);
                        this.scannedThisGame = true;
                        return;
                    }
                }
            }
        }
        this.bedScanAt = System.currentTimeMillis() + BED_RESCAN_DELAY_MS;
    }

    private void pruneTrackedPearls() {
        if (Minecraft.theWorld().isNull()) {
            trackedPearls.clear();
            return;
        }
        Iterator<EntityEnderPearl> iterator = trackedPearls.iterator();
        while (iterator.hasNext()) {
            EntityEnderPearl pearl = iterator.next();
            if (pearl.isNull() || pearl.d()) { // isDead
                iterator.remove();
            }
        }
    }

    private boolean isBed(BlockPos blockPos) {
        if (blockPos == null) return false;
        if (Minecraft.theWorld().isNull()) return false;
        Block block = Minecraft.theWorld().getBlockByPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        return BlockUtil.f(block);
    }

    private String getTeamName(EntityPlayer player) {
        String formatted = player.getName().toLowerCase();
        if (formatted.contains("red")) return "red";
        if (formatted.contains("yellow")) return "yellow";
        if (formatted.contains("green")) return "green";
        if (formatted.contains("blue")) return "blue";
        if (formatted.contains("aqua")) return "aqua";
        if (formatted.contains("white")) return "white";
        if (formatted.contains("pink")) return "pink";
        if (formatted.contains("gray") || formatted.contains("grey")) return "gray";
        return "";
    }

    // ==================== Invisible Player Alert ====================

    private void scanInvisiblePlayers() {
        if (this.bedPos == null || Minecraft.theWorld().isNull() || Minecraft.thePlayer().isNull()) return;
        long now = System.currentTimeMillis();

        for (Object entity : Minecraft.theWorld().z()) {
            if (!MappedClasses.Yl.isInstance(entity)) continue;
            EntityPlayer player = new EntityPlayer(entity);
            if (player == Minecraft.thePlayer() || player.getName() == null || player.getName().isEmpty()) continue;

            double distance = player.i(
                    this.bedPos.getX() + 0.5,
                    this.bedPos.getY() + 0.5,
                    this.bedPos.getZ() + 0.5
            );
            if (distance > 18.0) continue;

            // Check if invisible or has <=1 armor piece (simplified)
            boolean isInv = player.J$src$Z$fdev5g(); // isInvisible
            if (isInv) {
                String key = player.getName().toLowerCase();
                Long last = invisAlertCooldowns.getOrDefault(key, 0L);
                if (now - last > 5000L) {
                    invisAlertCooldowns.put(key, now);
                    // Alert displayed - in Vape we can't easily show custom chat, but the logic is here
                }
            }
        }
    }

    // ==================== Reset ====================

    private void reset(boolean resetDiamondUpgrades) {
        if (resetDiamondUpgrades) {
            this.trap = false;
            this.trapType = "";
            this.sharp = false;
            this.protLevel = 0;
        }
        this.trackedItemMessages.clear();
        this.invisAlertCooldowns.clear();
        this.alertCooldowns.clear();
        this.trackedPearls.clear();
        this.whitelistedPlayers.clear();
        this.autoIncPlayers.clear();
        this.bedPos = null;
        this.lastMacroTime = -1;
        this.bedScanAt = -1;
        this.scannedThisGame = false;
        this.waiting = false;
    }

    @Override
    public void onEnable() {
        reset(false);
    }

    @Override
    public void onDisable() {
        reset(true);
    }

    private String toRoman(int level) {
        switch (level) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            default: return String.valueOf(level);
        }
    }
}
