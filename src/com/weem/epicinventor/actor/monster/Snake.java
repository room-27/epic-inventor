package com.weem.epicinventor.actor.monster;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.ai.*;
import com.weem.epicinventor.utility.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.io.*;

public class Snake extends Monster {

    private static final long serialVersionUID = 10000L;

    public Snake(MonsterManager mm, Registry rg, String im, String st, int x, int y, int minDist, int maxDist) {
        super(mm, rg, im, st, x, y, minDist, maxDist, false);

        name = "Snake";
        displayName = "Snake";

        monsterManager = mm;

        difficultyFactor = 0.50f;

        adjustHPForLevel();

        topOffset = 0;
        baseOffset = 3;
        baseWidth = 37;
        startJumpSize = 20;
        jumpSize = 8;
        fallSize = 0;

        xMoveSize = 2;

        adjustTouchDamageForLevel();

        dropChances.addDropChance("Fang", 75.0f, 1, 2);
        dropChances.addDropChance("WeemsDiceBag", 0.20f, 1, 1); //1 in 500 chance
        dropChances.addDropChance("ForrestsLaptop", 0.20f, 1, 1); //1 in 500 chance
        dropChances.addDropChance("BrandonsAbacus", 0.20f, 1, 1); //1 in 500 chance

        ai = new AI(registry, this);
        ai.clearGoals();
        ai.addGoal(AI.GoalType.ATTACK_PLAYER, "", (Rand.getRange(0, 1) + Rand.getFloat()));
        if (groundLevel == 0) {
            ai.addGoal(AI.GoalType.ATTACK_PLACEABLE, "", (Rand.getRange(0, 1) + Rand.getFloat()));
        }
        ai.activate();
    }
}