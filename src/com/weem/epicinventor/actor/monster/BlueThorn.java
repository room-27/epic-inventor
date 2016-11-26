package com.weem.epicinventor.actor.monster;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.ai.*;
import com.weem.epicinventor.drop.*;
import com.weem.epicinventor.network.*;
import com.weem.epicinventor.utility.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.io.*;

public class BlueThorn extends Monster {

    private static final long serialVersionUID = 10000L;
    private String finalImageName = "";

    public BlueThorn(MonsterManager mm, Registry rg, String im, String st, int x, int y, int minDist, int maxDist) {
        super(mm, rg, im, st, x, y, minDist, maxDist, false);

        name = "BlueThorn";
        displayName = "Blue Thorn";

        hideDisplayName = true;
        finalImageName = "BlueThorn" + Rand.getRange(1, 3);

        monsterManager = mm;

        difficultyFactor = 0.50f;

        adjustHPForLevel();
        
        disregardKnockBack = true;

        topOffset = 0;
        baseOffset = 6;
        baseWidth = 5;
        startJumpSize = 20;
        jumpSize = 8;
        fallSize = 0;
        
        updateImage();

        xMoveSize = 0;
        mapX = x;
        mapY = y;
        //sometimes these are spawning slightly in the air...
        for (int i = 0; i < 32; i++) {
            if (!monsterManager.doesRectContainBlocks(mapX + baseOffset, mapY - 1, baseWidth, 16)) {
                mapY--;
            } else {
                break;
            }
        }
        if (monsterManager.doesRectContainBlocks(mapX, mapY, width, height)) {
            isDirty = true;
        }

        adjustTouchDamageForLevel();

        ai = new AI(registry, this);
        ai.clearGoals();
    }

    @Override
    public Damage getMonsterTouchDamage(Rectangle r, int x) {
        if (hitPoints > 0) {
            if (spriteRect != null && r != null) {
                if (spriteRect.intersects(r)) {
                    playerDamage += touchDamage;
                    if (x > getCenterPoint().x) {
                        return new Damage(this, touchDamage, 7, 7);
                    } else {
                        return new Damage(this, touchDamage, -7, 7);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void update() {
        lastMapY = mapY;
        spriteRect = null;
        int offsetX = -1 * spriteRectOffestX;
        if (facing == Facing.RIGHT) {
            offsetX = spriteRectOffestX;
        }
        spriteRect = new Rectangle(mapX + baseOffset + offsetX, mapY - topOffset + spriteRectOffestY, baseWidth, height - topOffset);

        if (spriteRect.intersects(manager.getPanelRect())) {
            shouldRender = true;
        } else {
            shouldRender = false;
        }

        if (isActive && isAnimating) {
            if (animationFrameUpdateTime <= registry.currentTime) {
                currentAnimationFrame++;
                if (currentAnimationFrame >= numAnimationFrames) {
                    currentAnimationFrame = 0;
                }
                animationFrameUpdateTime = registry.currentTime + animationFrameDuration;
            }
        }

        if (hitPoints > 0) {
        } else {
            if (registry.getGameController().multiplayerMode == registry.getGameController().multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    UpdateMonster um = new UpdateMonster(this.getId());
                    um.mapX = this.getMapX();
                    um.mapY = this.getMapY();
                    um.action = "Die";
                    registry.getNetworkThread().sendData(um);
                }
            }

            SoundClip cl = new SoundClip(registry, "Monster/Die" + name, getCenterPoint());
            isDead = true;
            ai.terminate();

            if (registry.getGameController().multiplayerMode != registry.getGameController().multiplayerMode.CLIENT) {
                ArrayList<Drop> drops = dropChances.generateDrops();
                if (drops.size() > 0) {
                    monsterManager.dropLoot(this, mapX + (width / 2), mapY + 32, drops);
                }
            }

            BufferedImage im = registry.getImageLoader().getImage(image);
            if (facing == Facing.LEFT) {
                AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
                tx = AffineTransform.getScaleInstance(-1, 1);
                tx.translate(-width, 0);
                AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
                BufferedImage imLeft = op.filter(im, null);
                if (imLeft != null) {
                    registry.getPixelizeManager().pixelize(imLeft, mapX, mapY);
                }
            } else {
                registry.getPixelizeManager().pixelize(im, mapX, mapY);
            }
        }

        if (spriteRect.intersects(manager.getPanelRect())) {
            isInPanel = true;
        } else {
            isInPanel = false;
        }

        updateImage();
    }

    @Override
    public void updateLong() {
        if (registry.getClosestPlayer(getCenterPoint(), MonsterManager.mobSpawnRangeMin * 2) == null) {
            Point p = getCenterPoint();
            if (p.x > 0 && p.y > 0) {
                if (!registry.getPlaceableManager().isPlaceableWithin(getCenterPoint(), MonsterManager.mobSpawnRangeMin * 2)) {
                    isDirty = true;
                }
            }
        }
    }

    @Override
    protected void updateImage() {
        setImage("Monsters/" + name + "/" + finalImageName);
    }
}