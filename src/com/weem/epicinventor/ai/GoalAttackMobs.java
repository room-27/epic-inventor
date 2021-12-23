package com.weem.epicinventor.ai;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.actor.monster.*;
import java.awt.geom.Arc2D;

import java.awt.*;

public class GoalAttackMobs extends Goal {

    private String newTarget;

    public GoalAttackMobs(AI a, Registry r, String t, float b) {
        super(a, r, t, b);
    }

    @Override
    public float calculateDesire() {
        float desire = 0f;

        Actor actor = ai.getActor();

        if (actor == null) {
            return desire;
        }
        
        Player p = registry.getPlayerManager().getPlayerById(ai.getPlayer());

        //if robot is in aggressive mode, it will always choose this goal
        if (registry.getRobotMode(p).equals("Aggressive")) {
            Monster m = registry.getMonsterManager().getClosestInPanel(actor.getCenterPoint());

            if (m == null) {
                newTarget = "";
                return desire;
            } else {
                newTarget = m.getId();
            }

            desire = 1;
        } else if (registry.getRobotMode(p).equals("Defensive")) {
            Monster m = registry.getMonsterManager().getMostAggroInPanel(actor.getCenterPoint());

            if (m == null) {
                newTarget = "";
                return desire;
            } else {
                newTarget = m.getId();
            }

            desire = 1;
        }

        return desire;
    }

    @Override
    protected void activate() {
        super.activate();
    }

    @Override
    protected void process() {
        super.process();

        Actor actor = ai.getActor();

        String currentTarget = newTarget;
        Monster monster = registry.getMonsterManager().getMonsterById(currentTarget);

        if (actor == null || monster == null) {
            terminate();
            return;
        }

        boolean shouldAttack = false;
        if (actor.getCurrentAttackType() == Actor.AttackType.MELEE) {
            Arc2D.Double attackArc = actor.getAttackArc(actor.getAttackRange());
            if (attackArc != null) {
                Rectangle sr = monster.getSpriteRect();
                if (sr != null && attackArc.intersects(sr)) {
                    shouldAttack = true;
                }
            }
        } else if (actor.isWithinAttackRange(monster.getCenterPoint())) {
            shouldAttack = true;
        }
        if (shouldAttack) {
            if (actor.getActionMode() == Actor.ActionMode.NONE) {
                actor.attack();
            }
        } else {
            actor.moveTowardsPoint(monster.getCenterPoint());
        }
    }

    @Override
    public void terminate() {
        super.terminate();

        Actor actor = ai.getActor();

        if (actor == null) {
            return;
        }

        actor.setActionMode(Actor.ActionMode.NONE);
    }
}