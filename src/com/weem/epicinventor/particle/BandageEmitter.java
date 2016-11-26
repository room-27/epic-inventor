package com.weem.epicinventor.particle;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;

import com.weem.epicinventor.utility.Rand;
import java.awt.*;
import java.util.*;
import java.util.ArrayList;
import java.awt.geom.Arc2D;

public class BandageEmitter extends ParticleEmitter {

    protected float particlesPerGenerationChange = 0.25f;

    public BandageEmitter(GameController gc, Registry rg, Actor as, int x, int y, ArrayList<String> im, boolean f, boolean p, boolean dt, int d, float sp, float ms, int md) {
        super(gc, rg, as, x, y, im, f, p, dt, d, sp, ms, md);
    }

    public BandageEmitter(GameController gc, Registry rg, Actor as, int x, int y, ArrayList<String> im, boolean f, boolean p, boolean dt, int d, float sp, float ms, int md, boolean vbr) {
        super(gc, rg, as, x, y, im, f, p, dt, d, sp, ms, md, vbr);
    }

    @Override
    public void update() {
        Particle particle = null;

        if (active) {
            for (int i = 0; i < particlesPerGeneration; i++) {
                if (i % 100 == 0) {
                    particles.add(generateNewParticle());
                } else {
                    particles.add(generateNewParticle());
                }
            }
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

    @Override
    protected Particle generateNewParticle() {
        float velocityX = (float) Rand.getRange(0, 1) + Rand.getFloat();
        float velocityY = (float) Rand.getRange(0, 1) + Rand.getFloat();
        if (Rand.getRange(0, 1) == 1) {
            velocityX *= -1f;
        }
        if (Rand.getRange(0, 1) == 1) {
            velocityY *= -1f;
        }

        float angle = 0;
        float angularVelocity = ((float) Rand.getRange(1, 5));

        double v = Math.sqrt(Math.pow(velocityY, 2) + Math.pow(velocityX, 2));
        int ttl = (int) (maxDistance / v);

        return new Bandage(
                this,
                registry,
                source,
                images.get(Rand.getRange(0, images.size() - 1)),
                mapX,
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
                velocityBasedRotation);
    }
}
