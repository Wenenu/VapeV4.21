package gg.vape.module.render.hud;

import gg.vape.ui.click.frame.impl.hud.ArrayListHudFrame;
import gg.vape.unmap.ModeOption;
import gg.vape.value.BooleanValue;
import gg.vape.value.ModeValue;
import gg.vape.value.NumberValue;

public class ArrayListHudModule
extends HudModule {
    public final BooleanValue importantModules = BooleanValue.create(this, "Important", false, "Hides render modules from the list");
    public final ModeOption blackShadow = new ModeOption("Black");
    public final ModeOption coloredShadow = new ModeOption("Colored");
    public final ModeOption noShadow = new ModeOption("None");
    public final ModeValue textShadow = ModeValue.create(this, "Text Shadow", this.blackShadow, this.blackShadow, this.coloredShadow, this.noShadow);
    public final ModeOption noneRectangle = new ModeOption("None");
    public final ModeOption topRectangle = new ModeOption("Top");
    public final ModeOption sideRectangle = new ModeOption("Side");
    public final ModeOption outlineRectangle = new ModeOption("Outline");
    public final ModeValue rectangle = ModeValue.create(this, "Rectangle", this.topRectangle, this.noneRectangle, this.topRectangle, this.sideRectangle, this.outlineRectangle);
    public final ModeOption moveInAnimation = new ModeOption("Move in");
    public final ModeOption scaleInAnimation = new ModeOption("Scale in");
    public final ModeValue animation = ModeValue.create(this, "Animation", this.scaleInAnimation, this.moveInAnimation, this.scaleInAnimation);
    public final NumberValue height = NumberValue.create(this, "Height", "#.#", "", 9.0, 11.0, 20.0, 0.5);
    public final NumberValue colorIndex = NumberValue.create(this, "Color Separation", "#.#", "", 5.0, 20.0, 100.0, 1.0);
    public final NumberValue colorSpeed = NumberValue.create(this, "Color Speed", "#.#", "", 2.0, 15.0, 30.0, 1.0);
    public final BooleanValue background = BooleanValue.create(this, "Background", false);
    public final BooleanValue backgroundColor = BooleanValue.create(this, "Background Color", false, "Tints the background with the row color");
    public final NumberValue backgroundAlpha = NumberValue.create(this, "Background Alpha", "#.##", "", 0.0, 0.35, 1.0, 0.01);
    public final ModeOption basicSuffix = new ModeOption("Basic");
    public final ModeOption extendedSuffix = new ModeOption("Extended");
    public final ModeOption noSuffix = new ModeOption("None");
    public final ModeValue suffixMode = ModeValue.create(this, "Suffix Mode", this.basicSuffix, this.basicSuffix, this.extendedSuffix, this.noSuffix);

    public int getSuffixModeIndex() {
        if (this.suffixMode.getValue() == this.basicSuffix) {
            return 0;
        }
        return this.suffixMode.getValue() == this.extendedSuffix ? 1 : 2;
    }

    public ArrayListHudModule() {
        super("ArrayList", HudModuleGroup.HUD, "arraylist", ArrayListHudFrame.class);
        this.setSuffix("Displays your active modules");
        this.addValue(this.importantModules, this.textShadow, this.rectangle, this.animation, this.height, this.colorIndex, this.colorSpeed, this.background, this.backgroundColor, this.backgroundAlpha, this.suffixMode);
        this.background.addDependentValues(this.backgroundColor, this.backgroundAlpha);
    }
}
