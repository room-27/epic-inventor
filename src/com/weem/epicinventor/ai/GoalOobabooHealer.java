package com.weem.epicinventor.ai;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.actor.monster.*;
import com.weem.epicinventor.actor.oobaboo.*;

import com.weem.epicinventor.utility.EIError;
import com.weem.epicinventor.utility.Rand;
import java.awt.*;

public class GoalOobabooHealer extends Goal {

    private OobabooHealerState oobabooHealerState;
    private long moveTime = 1000;
    private int healRange = 512;
    private int targetFollowDistance = 100;
    private int maxFollowDistance = 1000;

    public enum OobabooHealerState {

        STARING, FOLLOWING, HEALING
    };

    public GoalOobabooHealer(AI a, Registry r, String t, float b) {
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

        OobabooHealer actor = (OobabooHealer) ai.getActor();
        oobabooHealerState = OobabooHealerState.STARING;

        if (actor == null) {
            return;
        }
    }

    @Override
    protected void process() {
        super.process();

        OobabooHealer actor = (OobabooHealer) ai.getActor();
        Player player = registry.getPlayerManager().getPlayerById(ai.getPlayer());

        if (actor == null || player == null) {
            return;
        }

        Point p = player.getCenterPoint();
        double distance = p.distance(actor.getCenterPoint());

        if (distance > maxFollowDistance) {
            actor.setPosition(player.getMapX(), player.getMapY());
        }

        int actorX = actor.getMapX();
        int targetX = player.getMapX();

        switch (oobabooHealerState) {
            case STARING:
                if (targetX > actorX) {
                    actor.setFacing(Actor.Facing.RIGHT);
                } else {
                    actor.setFacing(Actor.Facing.LEFT);
                }

                if (distance <= healRange && player.getHitPointPercentage() < 100) {
                    //if player is low on life, try to heal
                    actor.stopMove();
                    oobabooHealerState = OobabooHealerState.HEALING;
                } else if (distance > targetFollowDistance) {
                    //if healer is too far away from player, switch state to follow
                    oobabooHealerState = OobabooHealerState.FOLLOWING;
                }
                actor.checkCollide(0);
                break;
            case FOLLOWING:
                actor.moveTowardsPoint(player.getCenterPoint());

                if (distance <= healRange && player.getHitPointPercentage() < 100) {
                    //if player is low on life, try to heal
                    actor.stopMove();
                    oobabooHealerState = OobabooHealerState.HEALING;
                } else if (distance < targetFollowDistance) {
                    //if healer has caught up to player, stop following
                    //actor.setLastMove(registry.currentTime);
                    actor.stopMove();
                    oobabooHealerState = OobabooHealerState.STARING;
                }
                break;
            case HEALING:
                if (targetX > actorX) {
                    actor.setFacing(Actor.Facing.RIGHT);
                } else {
                    actor.setFacing(Actor.Facing.LEFT);
                }

                actor.attack();

                if (distance > healRange) {
                    //if healer is too far away from player, switch state to follow
                    //actor.setLastMove(registry.currentTime);
                    oobabooHealerState = OobabooHealerState.FOLLOWING;
                } else if (player.getHitPointPercentage() == 100) {
                    //if player has full health, just stare at him...  Right in his stupid face
                    //actor.setLastMove(registry.currentTime);
                    actor.stopMove();
                    oobabooHealerState = OobabooHealerState.STARING;
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