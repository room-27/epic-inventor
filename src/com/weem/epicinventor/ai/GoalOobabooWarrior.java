package com.weem.epicinventor.ai;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.actor.monster.*;
import com.weem.epicinventor.actor.oobaboo.*;
import java.awt.*;
import java.awt.geom.Arc2D;

public class GoalOobabooWarrior extends Goal {

    private OobabooWarriorState oobabooWarriorState;
    private String currentTarget;
    private long moveTime = 1000;
    private int healRange = 512;
    private int targetFollowDistance = 50;
    private int maxFollowDistance = 1000;
    private long nextMove = 0;
    private Point searchPoint;
    private double lastDistance;
    private int stillMovements = 0;

    public enum OobabooWarriorState {

        STARING, FOLLOWING, ATTACKING
    };

    public GoalOobabooWarrior(AI a, Registry r, String t, float b) {
        super(a, r, t, b);
    }

    @Override
    public float calculateDesire() {
        float desire = 0;

        desire *= bias;
        desire = validateDesire(desire);

        return desire;
    }

    @Override
    protected void activate() {
        super.activate();

        OobabooWarrior actor = (OobabooWarrior) ai.getActor();
        oobabooWarriorState = OobabooWarriorState.STARING;
    }

    @Override
    protected void process() {
        super.process();

        OobabooWarrior actor = (OobabooWarrior) ai.getActor();
        Player player = registry.getPlayerManager().getPlayerById(ai.getPlayer());
        
        Monster m = registry.getMonsterManager().getClosestWithinMax(actor.getCenterPoint(), 1000);

        if (player == null) {
            return;
        }

        if (m == null) {
            currentTarget = "";
        } else {
            currentTarget = m.getId();
        }

        Point p = player.getCenterPoint();
        double distance = p.distance(actor.getCenterPoint());

        if (distance > maxFollowDistance) {
            actor.setPosition(player.getMapX(), player.getMapY());
        }

        switch (oobabooWarriorState) {
            case STARING:
                int actorX = actor.getMapX();
                int targetX = player.getMapX();

                if (targetX > actorX) {
                    actor.setFacing(Actor.Facing.RIGHT);
                } else {
                    actor.setFacing(Actor.Facing.LEFT);
                }

                if (!currentTarget.equals("")) {
                    actor.stopMove();
                    oobabooWarriorState = OobabooWarriorState.ATTACKING;
                } else if (distance > targetFollowDistance) {
                    actor.stopMove();
                    oobabooWarriorState = OobabooWarriorState.FOLLOWING;
                }
                actor.checkCollide(0);
                break;
            case FOLLOWING:
                actor.moveTowardsPoint(player.getCenterPoint());

                if (!currentTarget.equals("")) {
                    actor.stopMove();
                    oobabooWarriorState = OobabooWarriorState.ATTACKING;
                } else if (distance < targetFollowDistance) {
                    actor.stopMove();
                    oobabooWarriorState = OobabooWarriorState.STARING;
                }
                break;
            case ATTACKING:
                boolean shouldAttack = false;

                Monster monster = registry.getMonsterManager().getMonsterById(currentTarget);

                if (monster == null) {
                    actor.stopMove();
                    oobabooWarriorState = OobabooWarriorState.FOLLOWING;
                } else {
                    Arc2D.Double attackArc = actor.getAttackArc(actor.getAttackRange());
                    if (attackArc != null) {
                        Rectangle sr = monster.getSpriteRect();
                        if (sr != null && attackArc.intersects(sr)) {
                            shouldAttack = true;
                        }
                    }
                    if (shouldAttack) {
                        if (actor.getActionMode() == Actor.ActionMode.NONE) {
                            actor.attack();
                        }
                    } else {
                        actor.moveTowardsPoint(monster.getCenterPoint());
                    }
                }
                break;
        }
    }

    @Override
    public void terminate() {
        super.terminate();
        ai.getActor().stopMove();
    }
}