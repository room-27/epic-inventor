package com.weem.epicinventor.particle;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.Rectangle.*;
import java.awt.geom.Point2D.*;
import java.awt.geom.AffineTransform;

public class Bandage extends Particle {

    protected int updates = 0;
    
    public Bandage(ParticleEmitter pm, Registry rg, Actor as, String im, int x, int y, float vx, float vy, float a, float av, float s, int t, boolean f, boolean p, boolean dt, int d, boolean vbr) {
        super(pm, rg, as, im, x, y, vx, vy, a, av, s, t, f, p, dt, d, vbr);
    }

    @Override
    public void update() {
        ttl--;
        if (!isNew) {
            mapX += velocityX;
            mapY += velocityY;
            angle += angularVelocity;
        } else {
            isNew = false;
        }

        if (registry.getGameController().multiplayerMode != registry.getGameController().multiplayerMode.CLIENT) {
            Point ePoint = new Point((int) mapX, (int) mapY);

            //check for hitting a block
            if (!disregardTerrain) {
                if (particleEmitter.checkForBlock(getCenterPoint()) != 0) {
                    isDirty = true;
                }
            }
        }
        
        updates++;
        if(updates > 20) {
            isDirty = true;
        }
    }

    @Override
    public void render(Graphics g) {
        if (image != null) {
            int xPos = particleEmitter.mapToPanelX((int) mapX);
            int yPos = particleEmitter.mapToPanelY((int) mapY);

            //flip the yPos since drawing happens top down versus bottom up
            yPos = particleEmitter.getPHeight() - yPos;

            //subtract the block height since points are bottom left and drawing starts from top left
            yPos -= height;

            if (updates > 10) {
                BufferedImage bandageImage = registry.getImageLoader().changeTransperancy(image, 1f - (((float)updates - 10f) / 20f));
                g.drawImage(bandageImage, xPos, yPos, null);
            } else {
                g.drawImage(image, xPos, yPos, null);
            }
        }
    }
}
