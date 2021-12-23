package com.weem.epicinventor.ai;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.actor.monster.*;

public class GoalAttackPlayerRanged extends Goal {

    private int maxDistance;

    public GoalAttackPlayerRanged(AI a, Registry r, String t, float b) {
        super(a, r, t, b);
        maxDistance = MonsterManager.mobSpawnRangeMax;
    }
    
    public Player getPlayerToAttack(Monster actor) {
        return registry.getClosestPlayer(actor.getCenterPoint(), actor.getMaxAggroRange());
    }

    @Override
    public float calculateDesire() {
        float desire = 0;

        //figure out the distance between player and mob
        int actorX = ai.getActor().getMapX();
        int playerX = registry.getClosestPlayerX(ai.getActor().getCenterPoint(), maxDistance);
        int distance = Math.abs(playerX - actorX);

        //see if the mob needs to be attack the player
        //closer the player is, the more the mob wants to attack
        if (distance >= maxDistance || playerX < 0) {
            desire = 0;
        } else {
            desire = 1.0f - ((float) distance / (float) maxDistance);
        }

        desire *= bias;
        desire = validateDesire(desire);
        
        ai.getActor().setDebugInfo(ai.getActor().getDebugInfo() + "Player (" + ((int) desire * 100) + ")|");

        return desire;
    }

    @Override
    protected void activate() {
        super.activate();
    }

    @Override
    protected void process() {
        super.process();

        Monster actor = (Monster) ai.getActor();
        Player player = getPlayerToAttack(actor);

        if (actor == null || player == null) {
            terminate();
            return;
        }

        if (actor.isFeared() && actor.getFearedSource() != null) {
            actor.moveAwayFromPoint(actor.getFearedSource());
        } else {
            if(actor.getCenterPoint().distance(player.getCenterPoint()) > actor.getMaxShootRange()) {
                actor.moveTowardsPoint(player.getCenterPoint());
            } else {
                if(actor.isMoving()) {
                    actor.stopMove();
                }
                actor.shoot(player.getCenterPoint());
                actor.updatePosition();
            }
        }

        if (player.getPerimeter().intersects(actor.getPerimeter())) {
            actor.attack();
        } else {
            if (actor.isAttacking()) {
                actor.stopAttack();
            }
        }
    }

    @Override
    public void terminate() {
        super.terminate();
        ai.getActor().stopMove();
    }
}