package com.weem.epicinventor.hud;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.utility.*;
import com.weem.epicinventor.weapon.*;

import java.awt.*;
import java.text.*;

public class HUDOobaboo extends HUD {

    private final static int BUTTON_X_START = 21;
    private final static int BUTTON_X_SPACING = 120;
    private final static int BUTTON_Y = 347;
    private final static int BUTTON_WIDTH = 68;
    private final static int BUTTON_HEIGHT = 28;
    private final static int BUTTON_CLOSE_WIDTH = 42;
    private final static int BUTTON_CLOSE_HEIGHT = 42;
    private final static int BUTTON_CLOSE_X = 310;
    private final static int BUTTON_CLOSE_Y = 0;

    public HUDOobaboo(HUDManager hm, Registry rg, int x, int y, int w, int h) {
        super(hm, rg, x, y, w, h);

        setImage("HUD/Oobaboo/BG");

        HUDArea hudArea = null;

        //gatherer
        hudArea = addArea(BUTTON_X_START + (BUTTON_X_SPACING * 0), BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, "gatherer");
        hudArea.setImage("HUD/Oobaboo/ButtonTrade");

        //warrior
        hudArea = addArea(BUTTON_X_START + (BUTTON_X_SPACING * 1), BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, "warrior");
        hudArea.setImage("HUD/Oobaboo/ButtonTrade");

        //healer
        hudArea = addArea(BUTTON_X_START + (BUTTON_X_SPACING * 2), BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, "healer");
        hudArea.setImage("HUD/Oobaboo/ButtonTrade");

        //healer
        hudArea = addArea(BUTTON_CLOSE_X, BUTTON_CLOSE_Y, BUTTON_CLOSE_WIDTH, BUTTON_CLOSE_HEIGHT, "close");
        hudArea.setImage("HUD/LevelUp/ButtonClose");

        shouldRender = false;
    }

    @Override
    public void HUDAreaClicked(HUDArea ha) {
        HUDArea hudArea = null;

        if (shouldRender) {
            for (int i = 0; i < hudAreas.size(); i++) {
                hudArea = hudAreas.get(i);
                if (hudArea == ha) {
                    if (hudArea.getType().equals("gatherer")) {
                        SoundClip cl = new SoundClip("Oobaboo/Leaves");
                        registry.getPlayerManager().getCurrentPlayer().spawnOobaboo("Gatherer");
                        shouldRender = false;
                    } else if (hudArea.getType().equals("warrior")) {
                        SoundClip cl = new SoundClip("Oobaboo/Leaves");
                        registry.getPlayerManager().getCurrentPlayer().spawnOobaboo("Warrior");
                        shouldRender = false;
                    } else if (hudArea.getType().equals("healer")) {
                        SoundClip cl = new SoundClip("Oobaboo/Leaves");
                        registry.getPlayerManager().getCurrentPlayer().spawnOobaboo("Healer");
                        shouldRender = false;
                    } else if (hudArea.getType().equals("close")) {
                        registry.getPlayerManager().playerAddItem("Idol", 1);
                        shouldRender = false;
                    }
                }
            }
        }
    }

    @Override
    public void showOobabooHUD() {
        shouldRender = !shouldRender;
    }
}