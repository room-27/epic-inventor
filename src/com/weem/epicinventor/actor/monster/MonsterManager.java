package com.weem.epicinventor.actor.monster;

import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.drop.*;
import com.weem.epicinventor.network.*;
import com.weem.epicinventor.particle.*;
import com.weem.epicinventor.placeable.*;
import com.weem.epicinventor.projectile.*;
import com.weem.epicinventor.utility.*;

import com.weem.epicinventor.*;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.io.*;
import java.util.*;
import java.util.ArrayList;

public class MonsterManager extends Manager implements Serializable, Cloneable {

    protected static final long serialVersionUID = 10000L;
    private HashMap<String, Monster> monsters;
    public static int mobSpawnRangeMin = 1600;
    public static int mobSpawnRangeMax = 2400;
    private static double spawnRatioDiff = 0.02f;
    transient private static HashMap<String, Integer> spawnCoolDowns;
    private boolean showRect;
    private boolean showGoals;
    transient private long nextBossOrcSpawn = 0;
    transient private long nextSnailRiderSpawn = 0;
    transient private boolean transmitting;
    transient private Monster selectedMob = null;

    public enum MonsterType {

        AggressiveSnake,
        BossOrc,
        LionFly,
        Orc,
        Pig,
        Porcupine,
        RedOrc,
        RockMonster,
        Snail,
        SnailRider,
        Snake,
        SpiderWorm,
        ZombieWalrus
    }

    public MonsterManager(GameController gc, Registry rg) {
        super(gc, rg);

        monsters = new HashMap<String, Monster>();
        spawnCoolDowns = new HashMap<String, Integer>();
    }

    @Override
    public void setTransient(Registry rg) {
        super.setTransient(rg);

        spawnCoolDowns = new HashMap<String, Integer>();

        try {
            for (String key : monsters.keySet()) {
                Monster monster = (Monster) monsters.get(key);
                monster.setTransient(rg, this);
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public void giveXP(Monster m) {
        gameController.giveXP(m);
    }

    public void registerMonster(Monster m) {
        if (!monsters.containsKey(m.getId())) {
            monsters.put(m.getId(), m);
        }
    }

    public Monster getMonsterById(String id) {
        if (monsters.containsKey(id)) {
            Monster monster = monsters.get(id);
            return monster;
        } else {
            return null;
        }
    }

    public void generatePlants() {
        int passes = 0;
        int blueThornCount = 0;
        int vineThornCount = 0;

        do {
            blueThornCount = this.getCountByType("BlueThorn");
            vineThornCount = this.getCountByType("VineThorn");
            spawnPlants();

            Monster monster = null;
            ArrayList deadMonsters = new ArrayList();

            try {
                for (String key : monsters.keySet()) {
                    monster = (Monster) monsters.get(key);
                    if (monster != null) {
                        monster.update();
                        if (monster.getIsDead()) {
                            deadMonsters.add(key);
                        }
                    }
                }

                if (deadMonsters.size() > 0) {
                    for (int i = 0; i < deadMonsters.size(); i++) {
                        //EIError.debugMsg((String) deadMonsters.get(i));
                        monsters.remove((String) deadMonsters.get(i));
                    }
                }
            } catch (ConcurrentModificationException concEx) {
                //another thread was trying to modify monsters while iterating
                //we'll continue and the new item can be grabbed on the next update
            }

            passes++;
        } while ((blueThornCount < 200 || vineThornCount < 200) && passes < 50);
        int g = 1;
    }

    private void spawnPlants() {
        if (gameController.multiplayerMode != gameController.multiplayerMode.CLIENT) {
            //Blue Thorns
            int blueThornCount = this.getCountByType("BlueThorn");
            if (blueThornCount < 200) {
                for (int i = 0; i < 200 - blueThornCount; i++) {
                    boolean canSpawn = true;
                    int level = Rand.getRange(1, 3);
                    Point vinePosition = new Point();
                    vinePosition.x = Rand.getRange(1, gameController.getMapWidth());
                    vinePosition.y = Rand.getRange(registry.getBlockManager().getLevelBottom(level),
                            registry.getBlockManager().getLevelTop(level));

                    vinePosition.y = this.findNextFloor(vinePosition.x, vinePosition.y, 60);

                    if (this.doesRectContainBlocks(vinePosition.x, vinePosition.y + 100, 17, 16)) {
                        Monster monster = null;
                        try {
                            for (String key : monsters.keySet()) {
                                monster = (Monster) monsters.get(key);
                                if (monster.getName().equals("BlueThorn") || monster.getName().equals("VineThorn")) {
                                    double distance = vinePosition.distance(monster.getCenterPoint());
                                    if (distance < 750) {
                                        canSpawn = false;
                                        break;
                                    }
                                }
                            }
                        } catch (ConcurrentModificationException concEx) {
                            //another thread was trying to modify monsters while iterating
                            //we'll continue and the new item can be grabbed on the next update
                        }
                        if (canSpawn) {
                            spawn("BlueThorn", "Roaming", vinePosition.x, vinePosition.y);
                        }
                    }
                }
            }

            //Vine Thorns
            int vineThornCount = this.getCountByType("VineThorn");
            if (vineThornCount < 200) {
                for (int i = 0; i < 200 - vineThornCount; i++) {
                    boolean canSpawn = true;
                    int level = Rand.getRange(1, 3);
                    Point vinePosition = new Point();
                    vinePosition.x = Rand.getRange(1, gameController.getMapWidth());
                    vinePosition.y = Rand.getRange(registry.getBlockManager().getLevelBottom(level),
                            registry.getBlockManager().getLevelTop(level));

                    vinePosition.y = this.findNextFloor(vinePosition.x, vinePosition.y, 60);

                    if (this.doesRectContainBlocks(vinePosition.x, vinePosition.y + 100, 17, 16)) {
                        Monster monster = null;
                        try {
                            for (String key : monsters.keySet()) {
                                monster = (Monster) monsters.get(key);
                                if (monster.getName().equals("BlueThorn") || monster.getName().equals("VineThorn")) {
                                    double distance = vinePosition.distance(monster.getCenterPoint());
                                    if (distance < 750) {
                                        canSpawn = false;
                                        break;
                                    }
                                }
                            }
                        } catch (ConcurrentModificationException concEx) {
                            //another thread was trying to modify monsters while iterating
                            //we'll continue and the new item can be grabbed on the next update
                        }
                        if (canSpawn) {
                            spawn("VineThorn", "Roaming", vinePosition.x, vinePosition.y);
                        }
                    }
                }
            }
        }
    }

    public float getShouldSpawn(MonsterType mt, int groundLevel) {
        float[] levelCounts = new float[5];

        switch (mt) {
            case AggressiveSnake:
                levelCounts[0] = 3.50f / 120.0f;
                levelCounts[1] = 4.5f / 120.0f;
                levelCounts[2] = 3.0f / 120.0f;
                levelCounts[3] = 2.0f / 120.0f;
                levelCounts[4] = 1.0f / 120.0f;
                break;
            case LionFly:
                levelCounts[0] = 1.50f / 120.0f;
                levelCounts[1] = 0.0f;
                levelCounts[2] = 0.0f;
                levelCounts[3] = 0.0f;
                levelCounts[4] = 0.0f;
                break;
            case Orc:
                levelCounts[0] = 0.0f;
                levelCounts[1] = 5.5f / 120.0f;
                levelCounts[2] = 8.0f / 120.0f;
                levelCounts[3] = 6.5f / 120.0f;
                levelCounts[4] = 0.0f;
                break;
            case Pig:
                levelCounts[0] = 2.5f / 120.0f;
                levelCounts[1] = 0.0f;
                levelCounts[2] = 0.0f;
                levelCounts[3] = 0.0f;
                levelCounts[4] = 0.0f;
                break;
            case Porcupine:
                levelCounts[0] = 3.50f / 120.0f;
                levelCounts[1] = 6.5f / 120.0f;
                levelCounts[2] = 0.0f;
                levelCounts[3] = 0.0f;
                levelCounts[4] = 0.0f;
                break;
            case RedOrc:
                levelCounts[0] = 0.0f;
                levelCounts[1] = 0.5f / 120.0f;
                levelCounts[2] = 4.5f / 120.0f;
                levelCounts[3] = 7.5f / 120.0f;
                levelCounts[4] = 8.5f / 120.0f;
                break;
            case RockMonster:
                levelCounts[0] = 2.0f / 120.0f;
                levelCounts[1] = 1.0f / 120.0f;
                levelCounts[2] = 1.0f / 120.0f;
                levelCounts[3] = 1.0f / 120.0f;
                levelCounts[4] = 1.0f / 120.0f;
                break;
            case Snail:
                levelCounts[0] = 3.0f / 120.0f;
                levelCounts[1] = 2.5f / 120.0f;
                levelCounts[2] = 1.0f / 120.0f;
                levelCounts[3] = 0.0f;
                levelCounts[4] = 0.0f;
                break;
            case Snake:
                levelCounts[0] = 3.75f / 120.0f;
                levelCounts[1] = 4.5f / 120.0f;
                levelCounts[2] = 3.0f / 120.0f;
                levelCounts[3] = 2.0f / 120.0f;
                levelCounts[4] = 1.0f / 120.0f;
                break;
            case SpiderWorm:
                levelCounts[0] = 0.0f;
                levelCounts[1] = 0.0f;
                levelCounts[2] = 2.0f / 120.0f;
                levelCounts[3] = 6.5f / 120.0f;
                levelCounts[4] = 8.0f / 120.0f;
                break;
            case ZombieWalrus:
                levelCounts[0] = 2.25f / 120.0f;
                levelCounts[1] = 2.75f / 120.0f;
                levelCounts[2] = 0.75f / 120.0f;
                levelCounts[3] = 0.0f;
                levelCounts[4] = 0.0f;
                break;
        }
        float ret = 0.0f;
        if (groundLevel >= 0 && groundLevel < levelCounts.length) {
            ret = levelCounts[groundLevel];
        } else {
            ret = levelCounts[4];
        }
        return ret;
    }

    public Monster getSelectedMob() {
        if (selectedMob != null) {
            if (selectedMob.getIsDead() || selectedMob.isDirty) {
                selectedMob = null;
            } else {
                if (!this.isInPlayerView(selectedMob.getCenterPoint())) {
                    selectedMob = null;
                }
            }
        }
        return selectedMob;
    }

    public boolean handleClick(Player p, Point clickPoint) {
        Point mousePos = new Point(this.panelToMapX(clickPoint.x), this.panelToMapY(clickPoint.y));

        Monster monster = null;

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                if (monster != null) {
                    if (monster.isInside(mousePos) && !monster.getIsHiding()) {
                        if (selectedMob != monster) {
                            SoundClip cl = new SoundClip("Misc/Click");
                            selectedMob = monster;
                        }
                        return true;
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }

        return false;
    }

    public Monster spawn(String type, String spawnType, int x, int y) {
        Monster monster = null;

        //for (int i = 0; i < qty; i++) {
        if (type.toLowerCase().equals("aggressivesnake")) {
            monster = new AggressiveSnake(this, registry, "Monsters/AggressiveSnake/Standing", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        } else if (type.toLowerCase().equals("bluethorn")) {
            monster = new BlueThorn(this, registry, "Monsters/BlueThorn/BlueThorn1", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        } else if (type.toLowerCase().equals("bossorc")) {
            monster = new BossOrc(this, registry, "Monsters/BossOrc/Standing", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        } else if (type.toLowerCase().equals("snailrider")) {
            monster = new SnailRider(this, registry, "Monsters/SnailRider/Standing", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        } else if (type.toLowerCase().equals("lionfly")) {
            monster = new LionFly(this, registry, "Monsters/LionFly/Standing", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        } else if (type.toLowerCase().equals("orc")) {
            monster = new Orc(this, registry, "Monsters/Orc/Standing", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        } else if (type.toLowerCase().equals("pig")) {
            monster = new Pig(this, registry, "Monsters/Pig/Standing", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        } else if (type.toLowerCase().equals("porcupine")) {
            monster = new Porcupine(this, registry, "Monsters/Porcupine/Standing", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        } else if (type.toLowerCase().equals("redorc")) {
            monster = new RedOrc(this, registry, "Monsters/RedOrc/Standing", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        } else if (type.toLowerCase().equals("rockmonster")) {
            monster = new RockMonster(this, registry, "Monsters/RockMonster/Standing", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        } else if (type.toLowerCase().equals("snail")) {
            monster = new Snail(this, registry, "Monsters/Snail/Standing", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        } else if (type.toLowerCase().equals("snake")) {
            monster = new Snake(this, registry, "Monsters/Snake/Standing", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        } else if (type.toLowerCase().equals("spiderworm")) {
            monster = new SpiderWorm(this, registry, "Monsters/SpiderWorm/Standing", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        } else if (type.toLowerCase().equals("vinethorn")) {
            monster = new VineThorn(this, registry, "Monsters/VineThorn/VineThorn1", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        } else if (type.toLowerCase().equals("zombiewalrus")) {
            monster = new ZombieWalrus(this, registry, "Monsters/ZombieWalrus/Standing", spawnType, x, y, mobSpawnRangeMin, mobSpawnRangeMax);
            registerMonster(monster);
            if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    registry.getNetworkThread().sendData(monster);
                }
            }
        }
        //}

        return monster;
    }

    public void dropLoot(Monster m, int x, int y, ArrayList<Drop> drops) {
        gameController.dropLoot(m, x, y, drops);
    }

    public Damage getMonsterTouchDamage(Rectangle r, int x) {
        Damage damage = null;
        Monster monster = null;

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                damage = monster.getMonsterTouchDamage(r, x);

                if (damage != null) {
                    break;
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }

        return damage;
    }

    public ArrayList<String> attackDamageAndKnockBack(Actor source, Arc2D.Double arc, Point mapPoint, int damage, int knockBackX, int knockBackY, int maxHits, String weaponType) {
        int dmg = 0;
        int hits = 0;
        Monster monster = null;
        ArrayList<String> monstersHit = new ArrayList<String>();

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);

                if (weaponType != null) {
                    if (weaponType.equals("Net")) {
                        damage = 1;
                    }
                }

                dmg = monster.attackDamageAndKnockBack(source, arc, mapPoint, damage, knockBackX, knockBackY, weaponType);
                if (dmg > 0) {
                    if (weaponType != null) {
                        if (weaponType.equals("FangClaw")) {
                            monster.poison(10);
                        }
                    }
                    monstersHit.add(monster.getName());
                    hits++;
                }
                if (hits >= maxHits) {
                    break;
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }

        if (hits == 0) {
            if (source != null) {
                SoundClip cl = new SoundClip(registry, "Weapon/Miss", source.getCenterPoint());
            } else {
                SoundClip cl = new SoundClip("Weapon/Miss");
            }
        }

        return monstersHit;
    }

    public void showRects(Boolean r) {
        showRect = r;

        Monster monster = null;

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                monster.setShowRect(r);
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public void showGoals(Boolean g) {
        showGoals = g;

        Monster monster = null;

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                monster.setShowGoals(g);
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public boolean getShowGoals() {
        return showGoals;
    }

    @Override
    public boolean checkMobProjectileHit(Projectile p) {
        if (gameController.multiplayerMode != gameController.multiplayerMode.CLIENT) {
            Monster monster = null;

            try {
                for (String key : monsters.keySet()) {
                    monster = (Monster) monsters.get(key);
                    if (monster.getPerimeter().intersects(p.getRect())) {
                        monster.applyDamage(p.getDamage(), p.getSource(), p.isFromPlaceable());
                        return true;
                    }
                }
            } catch (ConcurrentModificationException concEx) {
                //another thread was trying to modify monsters while iterating
                //we'll continue and the new item can be grabbed on the next update
            }
        }

        return false;
    }

    @Override
    public boolean checkMobParticleHit(Particle p) {
        if (gameController.multiplayerMode != gameController.multiplayerMode.CLIENT) {
            Monster monster = null;

            try {
                for (String key : monsters.keySet()) {
                    monster = (Monster) monsters.get(key);
                    if (monster.getPerimeter().intersects(p.getRect())) {
                        monster.applyDamage(p.getDamage(), p.getSource(), p.isFromPlaceable(), false);
                        return true;
                    }
                }
            } catch (ConcurrentModificationException concEx) {
                //another thread was trying to modify monsters while iterating
                //we'll continue and the new item can be grabbed on the next update
            }
        }

        return false;
    }

    @Override
    public void update() {
        if (!transmitting) {
            super.update();

            ArrayList deadMonsters = new ArrayList();

            Monster monster = null;

            try {
                for (String key : monsters.keySet()) {
                    monster = (Monster) monsters.get(key);
                    if (monster != null) {
                        monster.update();
                        if (monster.getIsDead()) {
                            deadMonsters.add(key);
                        }
                    }
                }

                if (deadMonsters.size() > 0) {
                    for (int i = 0; i < deadMonsters.size(); i++) {
                        //EIError.debugMsg((String) deadMonsters.get(i));
                        monsters.remove((String) deadMonsters.get(i));
                    }
                }
            } catch (ConcurrentModificationException concEx) {
                //another thread was trying to modify monsters while iterating
                //we'll continue and the new item can be grabbed on the next update
            }
        }
    }

    private void removeMonster(String monsterType) {
        Monster monster = null;

        ArrayList deadMonsters = new ArrayList();

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                if (monster.getName().equals(monsterType)) {
                    deadMonsters.add(key);
                    break;
                }
            }

            if (deadMonsters.size() > 0) {
                for (int i = 0; i < deadMonsters.size(); i++) {
                    //EIError.debugMsg((String) deadMonsters.get(i));
                    monsters.remove((String) deadMonsters.get(i));
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    private int getSpawnCount(String monsterType) {
        int count = 0;
        Monster monster = null;

        ArrayList deadMonsters = new ArrayList();

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                if (monster.getName().equals(monsterType)) {
                    count++;
                    break;
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }

        return count;
    }

    public void removeAllMonsters(Rectangle area) {
        Monster monster = null;

        ArrayList deadMonsters = new ArrayList();

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                if (monster != null) {
                    if (!monster.getName().equals("Pig") && !monster.getName().equals("BlueThorn") && !monster.getName().equals("VineThorn") && monster.getPerimeter().intersects(area)) {
                        deadMonsters.add(key);
                        if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                            if (registry.getNetworkThread().readyForUpdates()) {
                                UpdateMonster um = new UpdateMonster(monster.getId());
                                um.mapX = monster.getMapX();
                                um.mapY = monster.getMapY();
                                um.action = "Die";
                                registry.getNetworkThread().sendData(um);
                            }
                        }
                    }
                }
            }

            if (deadMonsters.size() > 0) {
                for (int i = 0; i < deadMonsters.size(); i++) {
                    monsters.remove((String) deadMonsters.get(i));
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public void removeAllMonsters() {
        Monster monster = null;

        ArrayList deadMonsters = new ArrayList();

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                if (!monster.getName().equals("Pig") && !monster.getName().equals("BlueThorn") && !monster.getName().equals("VineThorn")) {
                    deadMonsters.add(key);
                    if (gameController.multiplayerMode == gameController.multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                        if (registry.getNetworkThread().readyForUpdates()) {
                            UpdateMonster um = new UpdateMonster(monster.getId());
                            um.mapX = monster.getMapX();
                            um.mapY = monster.getMapY();
                            um.action = "Die";
                            registry.getNetworkThread().sendData(um);
                        }
                    }
                }
            }

            if (deadMonsters.size() > 0) {
                for (int i = 0; i < deadMonsters.size(); i++) {
                    monsters.remove((String) deadMonsters.get(i));
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public int getCountByTypeWithinXRange(String type, int xStart, int xEnd) {
        int count = 0;
        Monster monster = null;

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                if (monster.getName().equals(type) && monster.getMapX() >= xStart && monster.getMapX() <= xEnd) {
                    count++;
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }

        return count;
    }

    private int getCountByType(String type) {
        int count = 0;
        Monster monster = null;

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                if (monster.getName().equals(type)) {
                    count++;
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }

        return count;
    }

    public int getAnimalCount(Rectangle r) {
        int count = 0;
        Monster monster = null;

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                if (monster.getName().equals("Pig")) {
                    if (monster.getSpriteRect().intersects(r)) {
                        count++;
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }

        return count;
    }

    public boolean spawnBossOrc(Player player) {
        if (getSpawnCount("BossOrc") < 1) {
            registry.showMessage("Success", "Beware, Melvin lurks nearby...");
            spawn("BossOrc", "Roaming", player.getMapX(), player.getMapY());
            return true;
        }
        return false;
    }

    public boolean spawnSnailRider(Player player) {
        if (getSpawnCount("SnailRider") < 1) {
            registry.showMessage("Success", "Suddenly, a wild Snail Rider appears!");
            spawn("SnailRider", "Roaming", player.getMapX(), player.getMapY());
            return true;
        }
        return false;
    }

    public void spawnNearPlayers() {
        Monster monster = null;
        float prob, rand;
        int count;
        HashMap<String, Player> players = registry.getPlayerManager().getPlayers();
        Player player = null;
        Integer spawnCoolDown = null;
        for (String key : players.keySet()) {
            player = (Player) players.get(key);

            //check for spawning Melvin
            if (registry.currentTime >= nextBossOrcSpawn && nextBossOrcSpawn != 0) {
                //to spawn melvin, player must have 40 AP, be within a range on the map and be on the surface
                if (player.getMapX() >= 2000 && player.getMapX() <= 5000 && player.getLevel() >= 10 && player.getMapY() == this.findFloor(player.getMapX())) {
                    spawnBossOrc(player);
                }
            }

            if (spawnCoolDowns.containsKey(key)) {
                spawnCoolDown = spawnCoolDowns.get(key);
            } else {
                spawnCoolDown = new Integer(0);
                spawnCoolDowns.put(key, spawnCoolDown);
            }
            spawnCoolDown--;
            if (spawnCoolDown < 0) {
                int x = player.getMapX();
                int y = player.getMapY();
                int groundLevel = registry.getBlockManager().getLevelByY(y);
                count = -groundLevel;
                try {
                    for (String key2 : monsters.keySet()) {
                        monster = (Monster) monsters.get(key2);
                        if (monster.getCenterPoint().distance(player.getCenterPoint()) < MonsterManager.mobSpawnRangeMax * 3 / 2) {
                            if (monster.getTouchDamage() > 0 && !monster.getName().equals("BlueThorn") && !monster.getName().equals("VineThorn")) {
                                count++;
                            }
                        }
                    }
                } catch (ConcurrentModificationException concEx) {
                    //another thread was trying to modify monsters while iterating
                    //we'll continue and the new item can be grabbed on the next update
                }
                if (count < 4) {
                    for (MonsterType monsterType : MonsterType.values()) {
                        if (count < 4) {
                            rand = Rand.getFloat();
                            prob = getShouldSpawn(monsterType, groundLevel);
                            if (prob > 0.0f) {
                                if ((prob - count * spawnRatioDiff) > 0.005f) {
                                    prob += -count * spawnRatioDiff;
                                } else {
                                    prob = 0.005f;
                                }
                            }
                            if (rand <= prob) {
                                //System.out.println("spawn near player " + monsterType.name());
                                spawn(monsterType.name(), "Roaming", x, y);
                                count++;
                            }
                        }
                    }
                } else {
                    //EIError.debugMsg("too many to spawn");
                    spawnCoolDown = 20;
                }
            }
        }
    }

    public void spawnNearPlaceable() {
        float prob, rand;
        Placeable placeable = registry.getPlaceableManager().getRandomPlacable();
        if (placeable != null && !placeable.getType().equals("TownHall") && !placeable.getType().equals("Cabin") && !placeable.getType().equals("Chest")) {

            Point p = placeable.getCenterPoint();
            HashMap<String, Player> players = registry.getPlayerManager().getPlayers();
            Player player = null;
            boolean playerNear = false;
            for (String key : players.keySet()) {
                player = (Player) players.get(key);
                Point p2 = new Point(player.getMapX(), player.getMapY());
                if (p.distance(p2) < mobSpawnRangeMin * 2) {
                    playerNear = true;
                }
            }
            if (!playerNear) {
                for (MonsterType monsterType : MonsterType.values()) {
                    if (monsterType.toString().equals("Porcupine") || monsterType.toString().equals("Snail") || monsterType.toString().equals("Snake") || monsterType.toString().equals("ZombieWalrus")) {
                        rand = Rand.getFloat();
                        prob = getShouldSpawn(monsterType, 0);
                        if (rand <= prob / 3.0f) {
                            //System.out.println("spawn near placeable " + monsterType.name());
                            spawn(monsterType.name(), "Roaming", p.x, p.y);
                        }
                    }
                }
            }
        }
    }

    public void setNextBossOrcSpawn(int s) {
        nextBossOrcSpawn = s;
    }

    public void setNextSnailRiderSpawn(int s) {
        nextSnailRiderSpawn = s;
    }

    @Override
    public void updateLong() {
        if (!transmitting) {
            if (nextBossOrcSpawn == 0) {
                //spawn melvin every 10 - 30 min
                nextBossOrcSpawn = registry.currentTime + Rand.getRange(10 * 60 * 1000, 30 * 60 * 1000);
            }
            if (nextSnailRiderSpawn == 0) {
                //spawn melvin every 10 - 30 min
                nextSnailRiderSpawn = registry.currentTime + Rand.getRange(10 * 60 * 1000, 30 * 60 * 1000);
            }

            Monster monster = null;
            ArrayList deadMonsters = new ArrayList();

            //make sure we have enough bad guys on the map
            if (gameController.multiplayerMode != gameController.multiplayerMode.CLIENT) {
                spawnNearPlayers();
                spawnNearPlaceable();
            }

            try {
                for (String key : monsters.keySet()) {
                    monster = (Monster) monsters.get(key);
                    if (!monster.isFeared()) {
                        gameController.checkIfFeared(monster);
                    }
                    gameController.checkPlaceableDamageAgainstMob(monster);

                    monster.updateLong();
                    if (monster.isDirty()) {
                        deadMonsters.add(key);
                    }
                }

                if (deadMonsters.size() > 0) {
                    for (int i = 0; i < deadMonsters.size(); i++) {
                        //EIError.debugMsg((String) deadMonsters.get(i));
                        monsters.remove((String) deadMonsters.get(i));
                    }
                }
            } catch (ConcurrentModificationException concEx) {
                //another thread was trying to modify monsters while iterating
                //we'll continue and the new item can be grabbed on the next update
            }
        }
    }

    public void render(Graphics g) {
        if (!transmitting) {
            Monster monster = null;

            try {
                for (String key : monsters.keySet()) {
                    monster = (Monster) monsters.get(key);
                    monster.render(g);
                }
            } catch (ConcurrentModificationException concEx) {
                //another thread was trying to modify monsters while iterating
                //we'll continue and the new item can be grabbed on the next update
            }
        }
    }

    private int getRoamingCount() {
        int count = 0;

        Monster monster = null;

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                if (monster.getSpawnType().equals("Roaming")) {
                    count++;
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }

        return count;
    }

    public void resetAggro() {
        int count = 0;

        Monster monster = null;

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                monster.setPlayerDamage(0);
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public HashMap<String, Monster> getMonsters() {
        return monsters;
    }

    public Monster getClosestInPanel(Point p) {
        Monster monster = null;
        Monster closestMonster = null;

        double closestDistance = 0;

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                if (!monster.getName().equals("Pig") && !monster.getName().equals("BlueThorn") && !monster.getName().equals("VineThorn")) {
                    if (monster.getIsInPanel()) {
                        double distance = p.distance(monster.getCenterPoint());
                        if (distance < closestDistance || closestDistance == 0) {
                            closestMonster = monster;
                            closestDistance = distance;
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }

        return closestMonster;
    }

    public Monster getClosestWithinMax(Point p, int r) {
        Monster monster = null;
        Monster closestMonster = null;

        double closestDistance = 0;

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                if (!monster.getName().equals("Pig") && !monster.getName().equals("BlueThorn") && !monster.getName().equals("VineThorn")) {
                    if (monster.getIsInPanel()) {
                        double distance = p.distance(monster.getCenterPoint());
                        if ((distance < closestDistance || closestDistance == 0) && distance <= r) {
                            closestMonster = monster;
                            closestDistance = distance;
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }

        return closestMonster;
    }

    public Monster getMostAggroInPanel(Point p) {
        Monster monster = null;
        Monster mostAggroMonster = null;

        double mostAggro = 0;

        try {
            for (String key : monsters.keySet()) {
                monster = (Monster) monsters.get(key);
                if (!monster.getName().equals("Pig") && !monster.getName().equals("BlueThorn") && !monster.getName().equals("VineThorn")) {
                    if (monster.getIsInPanel()) {
                        double distance = p.distance(monster.getCenterPoint());
                        if (monster.getPlayerDamage() > mostAggro) {
                            mostAggroMonster = monster;
                            mostAggro = monster.getPlayerDamage();
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify monsters while iterating
            //we'll continue and the new item can be grabbed on the next update
        }

        return mostAggroMonster;
    }

    public void processMonsterUpdateUDP(UDPMonster up) {
        if (up != null) {
            if (monsters.containsKey(up.id)) {
                Monster monster = monsters.get(up.id);
                if (monster != null) {
                    monster.processUpdate(up);
                }
            }
        }
    }

    public void processMonsterUpdate(UpdateMonster um) {
        if (um != null) {
            if (monsters.containsKey(um.id)) {
                Monster monster = monsters.get(um.id);
                if (monster != null) {
                    EIError.debugMsg("Setting " + um.id + " to " + um.mapX + ":" + um.mapY + ", Action: " + um.action);
                    monster.setPosition(um.mapX, um.mapY);
                    if (um.previousGoal != null) {
                        monster.ai.setPreviousGoal(um.previousGoal);
                    }
                    if (um.currentGoal != null) {
                        monster.ai.setCurrentGoal(um.currentGoal);
                    }
                    if (um.action.equals("ApplyDamage")) {
                        monster.applyDamage(um.dataInt, um.actor);
                    } else if (um.action.equals("ApplyKnockBack")) {
                        monster.applyKnockBack(um.dataInt, um.dataInt2);
                    } else if (um.action.equals("Die")) {
                        monster.setHitPoints(0);
                    } else if (um.action.equals("Fear")) {
                        monster.applyKnockBack(um.dataInt, um.dataInt2);
                        monster.fear(um.dataPoint, um.dataLong);
                    }
                }
            } else {
                if (gameController.multiplayerMode == gameController.multiplayerMode.CLIENT && registry.getNetworkThread() != null) {
                    if (registry.getNetworkThread().readyForUpdates()) {
                        EIError.debugMsg("Monster not found - need " + um.id);
                        registry.getNetworkThread().sendData("send monster data: " + um.id);
                    }
                }
            }
        }
    }

    private void readObject(ObjectInputStream aInputStream) throws Exception {
        aInputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws Exception {
        transmitting = true;
        aOutputStream.defaultWriteObject();
        transmitting = false;
    }

    @Override
    public Object clone() {
        Object ret = null;
        try {
            ret = super.clone();
        } catch (CloneNotSupportedException e) {
        }
        return ret;
    }
}
