package com.weem.epicinventor.particle;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;

import com.weem.epicinventor.utility.Rand;
import java.awt.*;
import java.util.*;
import java.util.ArrayList;
import java.awt.geom.Arc2D;

public class LeafEmitter extends ParticleEmitter {

    protected float particlesPerGenerationChange = 0.25f;

    public LeafEmitter(GameController gc, Registry rg, Actor as, int x, int y, ArrayList<String> im, boolean f, boolean p, boolean dt, int d, float sp, float ms, int md) {
        super(gc, rg, as, x, y, im, f, p, dt, d, sp, ms, md);
    }

    public LeafEmitter(GameController gc, Registry rg, Actor as, int x, int y, ArrayList<String> im, boolean f, boolean p, boolean dt, int d, float sp, float ms, int md, boolean vbr) {
        super(gc, rg, as, x, y, im, f, p, dt, d, sp, ms, md, vbr);
    }

    @Override
    public void update() {
        Particle particle = null;

        if (active) {
            for (int i = 0; i < particlesPerGeneration; i++) {
                if (i % 100 == 0) {
                    particles.add(generateNewParticle(true));
                } else {
                    particles.add(generateNewParticle(false));
                }
            }
        }

        particlesPerGeneration -= particlesPerGenerationChange;
        particlesPerGenerationChange += 0.25f;

        if (particlesPerGeneration <= 0) {
            active = false;
        }

        for (int i = 0; i < particles.size(); i++) {
            particle = particles.get(i);
            particle.update();
            if (particle.isDirty) {
                particles.remove(i);
            }
        }
    }

    protected Particle generateNewParticle(boolean bigAssLeaf) {
        float velocityX = 0;
        float velocityY = 0;

        targetPoint = new Point(mapX, mapY + 300);

        if (targetPoint != null) {
            double angle = getAngleFromSlope();
            double spread = (Rand.getFloat() - 0.5f) * Math.PI * maxSpread / 180.0f;
            angle += spread;
            velocityX = (float) (speed * Math.cos(angle));
            velocityY = (float) (speed * Math.sin(angle));
        } else {
            velocityX = ((float) Rand.getRange(0, 7)) + Rand.getFloat() + 0.25f;
            velocityY = Rand.getFloat() + 0.05f;
            if (Rand.getRange(0, 1) == 1) {
                velocityY *= -1;
            }
        }

        float angle = 0;
        float angularVelocity = ((float) Rand.getRange(1, 5));

        double v = Math.sqrt(Math.pow(velocityY, 2) + Math.pow(velocityX, 2));
        int ttl = (int) (maxDistance / v);

        int newMapX = mapX;
        if (Rand.getRange(0, 1) == 0) {
            newMapX += Rand.getRange(0, 10);
        } else {
            newMapX -= Rand.getRange(0, 10);
        }

        if (bigAssLeaf) {
            return new Leaf(
                    this,
                    registry,
                    source,
                    images.get(Rand.getRange(5, images.size() - 1)),
                    newMapX,
                    mapY,
                    velocityX,
                    velocityY,
                    0f,
                    angularVelocity,
                    Rand.getFloat(),
                    ttl,
                    friendly,
                    placeable,
                    true,
                    damage,
                    velocityBasedRotation,
                    bigAssLeaf);
        } else {
            return new Leaf(
                    this,
                    registry,
                    source,
                    images.get(Rand.getRange(0, images.size() - 1)),
                    newMapX,
                    mapY,
                    velocityX,
                    velocityY,
                    0f,
                    angularVelocity,
                    Rand.getFloat(),
                    ttl,
                    friendly,
                    placeable,
                    disregardTerrain,
                    damage,
                    velocityBasedRotation,
                    bigAssLeaf);
        }
    }
}
