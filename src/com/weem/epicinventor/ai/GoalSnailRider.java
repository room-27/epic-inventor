package com.weem.epicinventor.ai;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.actor.monster.*;

import com.weem.epicinventor.utility.Rand;
import java.awt.*;

public class GoalSnailRider extends Goal {

    private Point targetPoint;
    private long nextMove;
    private long nextTurnAround;
    private SnailRiderState snailRiderState;
    private int originalXMoveSize = 0;
    private int chargeSpeedBonus = 12;
    private int jumpSpeedBonus = 7;
    private long thinkTime = 1000;
    private long moveTime = 5000;
    private int lastHP = 0;

    public enum SnailRiderState {

        FLY_AROUND, FLY_UP, DIVE, FLY_AWAY
    }

    public GoalSnailRider(AI a, Registry r, String t, float b) {
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

        SnailRider actor = (SnailRider) ai.getActor();
        snailRiderState = SnailRiderState.FLY_AROUND;

        if (actor == null) {
            return;
        }

        targetPoint = null;
        nextMove = registry.currentTime + moveTime;
    }

    @Override
    protected void process() {
        super.process();

        SnailRider actor = (SnailRider) ai.getActor();
        Player player = registry.getClosestPlayer(actor.getCenterPoint(), actor.getMaxAggroRange());

        // if (actor == null) {
        //     return;
        // }

        switch (snailRiderState) {
            case FLY_AROUND:
                if (doFly(actor, player)) {
                    actor.stopMove();
                    actor.stopAttack();
                    nextMove = registry.currentTime + thinkTime;
                    if (Rand.getRange(0, 5) == 0) {
                        targetPoint = actor.getCenterPoint();
                        targetPoint.y += 2000;
                        nextMove = registry.currentTime + moveTime;
                        registry.showMessage("Success", "Flying Up");
                        snailRiderState = SnailRiderState.FLY_UP;
                    } else {
                        targetPoint = null;
                        nextMove = registry.currentTime + moveTime;
                        snailRiderState = SnailRiderState.FLY_AROUND;
                    }
                } else {
                    actor.updatePosition();
                }
                break;
            case FLY_UP:
                if (doFly(actor, player)) {
                    actor.stopMove();
                    actor.stopAttack();
                    targetPoint = null;
                    nextMove = registry.currentTime + moveTime;
                    snailRiderState = SnailRiderState.DIVE;
                } else {
                    actor.updatePosition();
                }
                break;
            case DIVE:
                if (targetPoint != null) {
                    targetPoint = player.getCenterPoint();
                }
                if (doFly(actor, player)) {
                    actor.stopMove();
                    actor.stopAttack();
                    targetPoint = null;
                    nextMove = registry.currentTime + moveTime;
                    snailRiderState = SnailRiderState.DIVE;
                } else {
                    actor.updatePosition();
                }
                break;
            case FLY_AWAY:
                actor.applyDamage(10000, null);
                /*targetPoint = player.getCenterPoint();
                if (doFly(actor, player)) {
                    actor.stopMove();
                    actor.stopAttack();
                    targetPoint = null;
                    nextMove = registry.currentTime + moveTime;
                    snailRiderState = SnailRiderState.DIVE;
                } else {
                    actor.updatePosition();
                }*/
                break;
        }
        
        //System.out.println(targetPoint);

        lastHP = actor.getHitPoints();
    }

    private boolean doFly(Actor actor, Player player) {
        //returns true if done charging
        if (registry.currentTime >= nextMove) {
            return true;
        } else if (targetPoint != null) {
            if (actor.getCenterPoint().distance(targetPoint) <= 100) {
                return true;
            } else {
                actor.moveTowardsPoint(targetPoint);
            }
        } else {
            if (player != null) {
                targetPoint = player.getCenterPoint();
                if (actor.getCenterPoint().x > player.getCenterPoint().x) {
                    targetPoint.x -= 300;
                } else {
                    targetPoint.x += 300;
                }
            }
        }
        
        return (actor.getMapX() <= 0);
    }

    @Override
    public void terminate() {
        super.terminate();
        ai.getActor().stopMove();
    }
}