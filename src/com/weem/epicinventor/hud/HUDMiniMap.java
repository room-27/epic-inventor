package com.weem.epicinventor.hud;

import com.weem.epicinventor.*;
import com.weem.epicinventor.world.block.*;

import java.awt.*;
import java.awt.image.*;

public class HUDMiniMap extends HUD {

    private final static int BUTTON_X_START = 21;
    private BufferedImage imageMiniMapBorder;
    private String resourceName = "";

    public HUDMiniMap(HUDManager hm, Registry rg, int x, int y, int w, int h, String rn) {
        super(hm, rg, x, y, w, h);
        
        imageMiniMapBorder = registry.getImageLoader().getImage("Misc/MinimapFrame");
        resourceName = rn;
        HUDArea hudArea = null;
        shouldRender = false;
    }

    @Override
    public void HUDAreaClicked(HUDArea ha) {
//        HUDArea hudArea = null;
//
//        for (int i = 0; i < hudAreas.size(); i++) {
//            hudArea = hudAreas.get(i);
//        }
    }
    
    public void updateResourceName(String rn) {
        resourceName = rn;
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        if (shouldRender) {
            BlockManager bm = registry.getBlockManager();
            if (bm != null) {
                int sizeX = 160;
                int sizeY = 160;
                int x = registry.getGameController().getGamePanel().getWidth()-sizeX-10;
                int y = 10;
                if(bm.renderMiniMap(g, x, y, sizeX, sizeY, resourceName)) {
                    g.drawImage(imageMiniMapBorder, x-9, y-9, null);
                }
            }
        }
    }
}
