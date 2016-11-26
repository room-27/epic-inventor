package com.weem.epicinventor.particle;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.Rectangle.*;
import java.awt.geom.Point2D.*;
import java.awt.geom.AffineTransform;

public class Leaf extends Particle {

    protected boolean bigAssLeaf = false;
    protected float stretchFactor;

    public Leaf(ParticleEmitter pm, Registry rg, Actor as, String im, int x, int y, float vx, float vy, float a, float av, float s, int t, boolean f, boolean p, boolean dt, int d, boolean vbr, boolean bal) {
        super(pm, rg, as, im, x, y, vx, vy, a, av, s, t, f, p, dt, d, vbr);

        bigAssLeaf = bal;
        if (bigAssLeaf) {
            stretchFactor = 1;
            if (velocityX > 5) {
                velocityX = 5;
            } else if (velocityX < 0) {
                velocityX = -5;
            }
            if (velocityY > 5) {
                velocityY = 5;
            } else if (velocityY < 0) {
                velocityY = -5;
            }
            disregardTerrain = true;
        } else {
            disregardTerrain = false;
        }
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

        Point ePoint = new Point((int) mapX, (int) mapY);

        //check for hitting a block
        if (!disregardTerrain) {
            if (particleEmitter.checkForBlock(getCenterPoint()) != 0) {
                isDirty = true;
            }
        }
        if (bigAssLeaf) {
            if (velocityY > 0) {
                velocityY -= 0.10;
            } else {
                velocityY -= 0.02;
            }
            if (velocityY < -10) {
                velocityY = -10;
            }
            stretchFactor += 0.25f;
            if (stretchFactor > 20) {
                stretchFactor = 20;
                isDirty = true;
            }
        } else {
            if (velocityY > 0) {
                velocityY -= 0.40;
            } else {
                velocityY -= 0.08;
            }
            if (velocityY < -10) {
                velocityY = -10;
            }
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

            int finalWidth = width;
            int finalHeight = height;

            if (bigAssLeaf) {
                finalWidth = (int) ((float) width * stretchFactor);
                finalHeight = (int) ((float) height * stretchFactor);
                if (stretchFactor > 10) {
                    BufferedImage leafImage = registry.getImageLoader().changeTransperancy(image, 1f - ((stretchFactor - 10f) / 10f));
                    g.drawImage(leafImage, xPos, yPos, finalWidth, finalHeight, null);
                } else {
                    g.drawImage(image, xPos, yPos, finalWidth, finalHeight, null);
                }
            } else {
                g.drawImage(image, xPos, yPos, null);
            }
        }
    }
}
