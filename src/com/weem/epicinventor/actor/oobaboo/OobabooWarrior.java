package com.weem.epicinventor.actor.oobaboo;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.ai.*;
import com.weem.epicinventor.armor.*;
import com.weem.epicinventor.inventory.*;
import com.weem.epicinventor.network.*;
import com.weem.epicinventor.utility.*;
import com.weem.epicinventor.weapon.*;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.util.*;

public class OobabooWarrior extends Oobaboo implements Serializable {

    private static final long serialVersionUID = 10000L;

    public OobabooWarrior(PlayerManager pm, Player p, Registry rg, String im, int x) {
        super(pm, p, rg, im, x);
        
        disappearTime = registry.currentTime + (60 * 1000 * 1);

        ai = new AI(registry, this);

        ai.clearGoals();
        ai.setPlayer(player.getId());
        ai.addGoal(AI.GoalType.OOBABOO_WARRIOR, null, 1);
        ai.activate();
    }

    @Override
    public void meleeAttack() {
        int kbX = 20;
        int kbY = 5;
        int damage = (player.getAttackBonus() * 2);
        int maxHits = 2;
        int weaponSpeed = 600;

        isSwinging = true;
        actionMode = ActionMode.ATTACKING;
        attackRefreshTimerStart = System.currentTimeMillis();
        attackRefreshTimerEnd = System.currentTimeMillis() + weaponSpeed;

        attackArc = getAttackArc();
        if (facing == Facing.LEFT) {
            kbX = -1 * kbX;
        }

        playerManager.attackDamageAndKnockBack(this, attackArc, null, damage, kbX, kbY, maxHits, "");
    }

    @Override
    protected void updateImage() {
        if (actionMode == ActionMode.ATTACKING) {
            if (currentAnimationFrame == 0 || !image.equals("Oobaboo/Warrior/Swinging")) {
                loopImage("Oobaboo/Warrior/Swinging", 0.10f);
            }
        } else if (vertMoveMode == VertMoveMode.FALLING && fallSize > startJumpSize) {
            loopImage("Oobaboo/Warrior/Falling");
        } else if (vertMoveMode == VertMoveMode.JUMPING) {
            setImage("Oobaboo/Warrior/Jumping");
        } else {
            if (!isTryingToMove) {
                setImage("Oobaboo/Warrior/Standing");
            } else {
                loopImage("Oobaboo/Warrior/Walking", 0.10f);
            }
        }
    }

    private void readObject(ObjectInputStream aInputStream) throws Exception {
        aInputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws Exception {
        aOutputStream.defaultWriteObject();
    }
}