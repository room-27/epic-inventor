package com.weem.epicinventor.actor.monster;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.Actor;
import com.weem.epicinventor.ai.*;
import com.weem.epicinventor.utility.*;

import java.awt.*;

public class SnailRider extends Monster {

    private static final long serialVersionUID = 10000L;
    private float recastTotalTime;
    private float RECAST_TIME = 1.5f;
    private boolean canFire = true;
    private long flyAwayTime = 0;
    private final static int MAX_RANGE = 400;

    public SnailRider(MonsterManager mm, Registry rg, String im, String st, int x, int y, int minDist, int maxDist) {
        super(mm, rg, im, st, x, y, minDist, maxDist, true);

        name = "SnailRider";
        displayName = "Snail Rider";

        monsterManager = mm;

        difficultyFactor = 00.01f;

        adjustHPForLevel();

        topOffset = 4;
        baseOffset = 7;
        baseWidth = 58;
        startJumpSize = 20;
        jumpSize = 8;
        fallSize = 0;

        xMoveSize = 2;

        adjustTouchDamageForLevel();
        
        canFly = true;

        dropChances.addDropChance("TulipSandwich", 100.0f, 1, 1);
        dropChances.addDropChance("Bacon", 75.0f, 5, 10);
        dropChances.addDropChance("LargeShell", 75.0f, 1, 1);
        dropChances.addDropChance("Emerald", 100.0f, 1, 3);
        dropChances.addDropChance("Ruby", 100.0f, 1, 3);
        dropChances.addDropChance("Sapphire", 100.0f, 1, 3);
        dropChances.addDropChance("Iron", 20.0f, 1, 3);
        dropChances.addDropChance("Tusk", 15.0f, 1, 2);
        dropChances.addDropChance("Skin", 15.0f, 1, 1);
        dropChances.addDropChance("WeemsDiceBag", 0.20f, 1, 1); //1 in 500 chance
        dropChances.addDropChance("ForrestsLaptop", 0.20f, 1, 1); //1 in 500 chance
        dropChances.addDropChance("BrandonsAbacus", 0.20f, 1, 1); //1 in 500 chance
        
        flyAwayTime = registry.currentTime + (3 * 60 * 1000);

        ai = new AI(registry, this);
        ai.clearGoals();
        ai.addGoal(AI.GoalType.SNAIL_RIDER, "", 1f);
        ai.activate();
    }

    @Override
    public int getMaxShootRange() {
        return MAX_RANGE;
    }
    


    @Override
    public void shoot(Point targetPoint) {
        if (actionMode != ActionMode.ATTACKING) {
            actionMode = ActionMode.ATTACKING;
        }
        if (canFire) {
            registry.getProjectileManager().createProjectile(this,
                    "Goo",
                    10,
                    new Point(
                    getMapX(),
                    getMapY()),
                    targetPoint,
                    false,
                    false,
                    false,
                    (int) ((float) touchDamage * 0.75f));
            canFire = false;
            recastTotalTime = 0;
        } else {
            long p = registry.getImageLoader().getPeriod();
            recastTotalTime = (recastTotalTime
                    + registry.getImageLoader().getPeriod())
                    % (long) (1000 * RECAST_TIME * 2);

            if ((recastTotalTime / (RECAST_TIME * 1000)) > 1) {
                canFire = true;
                recastTotalTime = 0;
            }
        }
    }

    @Override
    protected void updateImage() {
        if (vertMoveMode == VertMoveMode.JUMPING) {
            setImage("Monsters/" + name + "/Jumping");
        } else if (vertMoveMode == VertMoveMode.FLYING) {
            loopImage("Monsters/" + name + "/Flapping");
        } else if (vertMoveMode == VertMoveMode.FALLING) {
            loopImage("Monsters/" + name + "/Falling");
        } else {
            if (actionMode == ActionMode.ATTACKING) {
                setImage("Monsters/" + name + "/RangeAttacking");
                //loopImage("Monsters/" + name + "/RangeAttacking", 0.50);
            } else if (isStill) {
                setImage("Monsters/" + name + "/Standing");
            } else {
                loopImage("Monsters/" + name + "/Walking");
            }
        }
    }

    @Override
    public void update() {
        super.update();

        if (registry.getGameController().multiplayerMode != registry.getGameController().multiplayerMode.CLIENT) {
            if (isDead) {
                monsterManager.setNextSnailRiderSpawn(0);
            }

            if (isDead) {
                registry.setBossFight(false);
            }
        }
    }
}