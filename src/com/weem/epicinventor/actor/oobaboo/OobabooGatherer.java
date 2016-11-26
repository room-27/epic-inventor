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

public class OobabooGatherer extends Oobaboo implements Serializable {

    private static final long serialVersionUID = 10000L;

    public OobabooGatherer(PlayerManager pm, Player p, Registry rg, String im, int x) {
        super(pm, p, rg, im, x);
        
        disappearTime = registry.currentTime + (60 * 1000 * 5);

        ai = new AI(registry, this);

        ai.clearGoals();
        ai.setPlayer(player.getId());
        ai.addGoal(AI.GoalType.OOBABOO_GATHERER, null, 1);
        ai.activate();
    }

    @Override
    protected void updateImage() {
        if (vertMoveMode == VertMoveMode.FALLING && fallSize > startJumpSize) {
            loopImage("Oobaboo/Gatherer/Falling");
        } else if (vertMoveMode == VertMoveMode.JUMPING) {
            setImage("Oobaboo/Gatherer/Jumping");
        } else {
            if (actionMode == ActionMode.GATHERING) {
                loopImage("Oobaboo/Gatherer/Gathering");
            } else if (!isTryingToMove) {
                setImage("Oobaboo/Gatherer/Standing");
            } else {
                loopImage("Oobaboo/Gatherer/Walking");
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