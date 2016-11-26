package com.weem.epicinventor.ai;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.actor.monster.*;
import com.weem.epicinventor.actor.oobaboo.*;
import com.weem.epicinventor.resource.*;

import com.weem.epicinventor.utility.EIError;
import com.weem.epicinventor.utility.Rand;
import java.awt.*;

public class GoalOobabooGatherer extends Goal {

    private OobabooGathererState oobabooGathererState;
    private long moveTime = 1000;
    private int healRange = 512;
    private int targetFollowDistance = 100;
    private int maxFollowDistance = 1000;
    private long nextMove = 0;
    private Point searchPoint;
    private double lastDistance;
    private int stillMovements = 0;

    public enum OobabooGathererState {

        GATHERING, MOVING_TOWARDS_RESOURCE, KEEP_SEARCHING, ARRIVING, LEAVING
    };

    public GoalOobabooGatherer(AI a, Registry r, String t, float b) {
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

        OobabooGatherer actor = (OobabooGatherer) ai.getActor();
        oobabooGathererState = OobabooGathererState.LEAVING;

        if (actor == null) {
            return;
        }
    }

    @Override
    protected void process() {
        super.process();

        OobabooGatherer actor = (OobabooGatherer) ai.getActor();
        Player player = registry.getPlayerManager().getPlayerById(ai.getPlayer());

        if (actor == null || player == null) {
            return;
        }

        if (actor.getActionMode() == Actor.ActionMode.GATHERING) {
            return;
        }

        switch (oobabooGathererState) {
            case MOVING_TOWARDS_RESOURCE:
                Resource r = registry.getResourceManager().getClosest(actor.getCenterPoint());

                if (r == null) {
                    actor.stopMove();
                } else {
                    actor.moveTowardsPoint(r.getCenterPoint());

                    Point p = player.getCenterPoint();
                    double distance = r.getCenterPoint().distance(actor.getCenterPoint());

                    if (distance <= 35) {
                        actor.stopMove();
                        nextMove = 0;
                        oobabooGathererState = OobabooGathererState.ARRIVING;
                    } else if (distance >= lastDistance && lastDistance > 0) {
                        stillMovements++;
                        if (stillMovements >= 2) {
                            stillMovements = 0;
                            //he hasn't found a resource in 15 seconds, move right and then check again
                            nextMove = 0;
                            searchPoint = new Point(actor.getMapX() + 3000, actor.getMapY());
                            oobabooGathererState = OobabooGathererState.KEEP_SEARCHING;
                        }
                    }

                    lastDistance = distance;
                }
                break;
            case KEEP_SEARCHING:
                actor.moveTowardsPoint(searchPoint);
                if (nextMove == 0) {
                    nextMove = registry.currentTime + Rand.getRange(10000, 15000);
                } else {
                    if (nextMove < registry.currentTime) {
                        actor.stopMove();
                        lastDistance = 0;
                        oobabooGathererState = OobabooGathererState.MOVING_TOWARDS_RESOURCE;
                    }
                }
                break;
            case GATHERING:
                String id = registry.getResourceManager().startGather(player, actor.getCenterPoint(), 35, true);
                if (id.equals("")) {
                    actor.stopMove();
                    lastDistance = 0;
                    oobabooGathererState = OobabooGathererState.MOVING_TOWARDS_RESOURCE;
                } else {
                    actor.setActionMode(Actor.ActionMode.GATHERING);
                    actor.setCurrentResourceType(registry.getPlayerManager().getResourceTypeByResourceId(id));
                    nextMove = 0;
                    oobabooGathererState = OobabooGathererState.LEAVING;
                }
                break;
            case ARRIVING:
                if (nextMove == 0) {
                    nextMove = registry.currentTime + Rand.getRange(250, 750);
                } else {
                    if (nextMove < registry.currentTime) {
                        actor.stopMove();
                        oobabooGathererState = OobabooGathererState.GATHERING;
                    }
                }
                break;
            case LEAVING:
                if (Rand.getRange(1, 20) == 1) {
                    turnAround(actor);
                }
                if (nextMove == 0) {
                    nextMove = registry.currentTime + Rand.getRange(1500, 4000);
                } else {
                    if (nextMove < registry.currentTime) {
                        actor.stopMove();
                        lastDistance = 0;
                        oobabooGathererState = OobabooGathererState.MOVING_TOWARDS_RESOURCE;
                    }
                }
                break;
        }
    }

    private void turnAround(Actor actor) {
        if (actor != null) {
            if (actor.getFacing() == Actor.Facing.RIGHT) {
                actor.setFacing(Actor.Facing.LEFT);
            } else {
                actor.setFacing(Actor.Facing.RIGHT);
            }
            actor.stopMove();
        }
    }

    @Override
    public void terminate() {
        super.terminate();
        ai.getActor().stopMove();
    }
}