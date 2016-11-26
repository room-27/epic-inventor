package com.weem.epicinventor.actor.oobaboo;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.ai.*;
import com.weem.epicinventor.armor.*;
import com.weem.epicinventor.inventory.*;
import com.weem.epicinventor.network.*;
import com.weem.epicinventor.particle.*;
import com.weem.epicinventor.utility.*;
import com.weem.epicinventor.weapon.*;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.util.*;

public class OobabooHealer extends Oobaboo implements Serializable {

    private static final long serialVersionUID = 10000L;
    transient private BandageEmitter bandageEmitter;

    public OobabooHealer(PlayerManager pm, Player p, Registry rg, String im, int x) {
        super(pm, p, rg, im, x);
        
        disappearTime = registry.currentTime + (60 * 1000 * 5);

        ai = new AI(registry, this);

        ai.clearGoals();
        ai.setPlayer(player.getId());
        ai.addGoal(AI.GoalType.OOBABOO_HEALER, null, 1);
        ai.activate();
    }

    private void createBandageEmitter() {
        if (bandageEmitter == null) {
            ArrayList<String> images = new ArrayList<String>();
            images.add("Particles/Bandage1");
            images.add("Particles/Bandage2");
            images.add("Particles/Bandage3");
            images.add("Particles/Bandage4");
            images.add("Particles/Bandage5");
            images.add("Particles/Bandage6");
            images.add("Particles/Bandage7");
            images.add("Particles/Bandage8");
            images.add("Particles/Bandage9");
            images.add("Particles/Bandage10");
            bandageEmitter = new BandageEmitter(registry.getGameController(), registry, this, mapX + baseOffset, mapY, images, true, false, true, 0, 0.25f, 10.0f, 20, true);
            bandageEmitter.setParticlesPerGeneration(10);
            bandageEmitter.setActive(true);
        } else {
            bandageEmitter.setActive(true);
        }
    }

    private void destoryBandageEmitter() {
        if (bandageEmitter != null) {
            bandageEmitter.destroy();
            bandageEmitter = null;
        }
    }

    @Override
    public void meleeAttack() {
        isSwinging = true;
        actionMode = ActionMode.ATTACKING;
        attackRefreshTimerStart = System.currentTimeMillis();
        attackRefreshTimerEnd = System.currentTimeMillis() + 1000;

        createBandageEmitter();

        float hpToAdd = (float) player.getTotalHitPoints() / 20f;
        player.addHitPoints((int) hpToAdd);
    }

    @Override
    protected void updateImage() {
        if (actionMode == ActionMode.ATTACKING) {
            if (currentAnimationFrame == 0 || !image.equals("Oobaboo/Healer/Swinging")) {
                loopImage("Oobaboo/Healer/Swinging", 0.10f);
            }
        } else if (vertMoveMode == VertMoveMode.FALLING && fallSize > startJumpSize) {
            loopImage("Oobaboo/Healer/Falling");
        } else if (vertMoveMode == VertMoveMode.JUMPING) {
            setImage("Oobaboo/Healer/Jumping");
        } else {
            if (!isTryingToMove) {
                setImage("Oobaboo/Healer/Standing");
            } else {
                loopImage("Oobaboo/Healer/Walking", 0.10f);
            }
        }
    }

    @Override
    public void update() {
        super.update();

        if (bandageEmitter != null) {
            if (facing == Facing.RIGHT) {
                bandageEmitter.setPosition(mapX + 48, mapY + 22);
            } else {
                bandageEmitter.setPosition(mapX + 23, mapY + 22);
            }
            bandageEmitter.update();
        }
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        if (bandageEmitter != null) {
            bandageEmitter.render(g);
        }
    }

    private void readObject(ObjectInputStream aInputStream) throws Exception {
        aInputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws Exception {
        aOutputStream.defaultWriteObject();
    }
}