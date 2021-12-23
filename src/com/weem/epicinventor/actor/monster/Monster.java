package com.weem.epicinventor.actor.monster;

import com.weem.epicinventor.*;
import com.weem.epicinventor.GameController.MultiplayerMode;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.ai.*;
import com.weem.epicinventor.drop.*;
import com.weem.epicinventor.network.*;
import com.weem.epicinventor.placeable.*;
import com.weem.epicinventor.utility.*;

import com.weem.epicinventor.world.block.BlockManager;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.Map.Entry;

public abstract class Monster extends Actor {

    private static final long serialVersionUID = 10000L;
    protected transient MonsterManager monsterManager;
    protected DropChanceCollection dropChances;
    protected AI ai;
    protected String name;
    protected String displayName;
    protected String spawnType;
    protected int maxAggroRange;
    protected int touchDamage = 10;
    protected int meleeDamage = 10;
    protected int meleeSpeed = 1000;
    protected float mapXMin;
    protected float mapXMax;
    protected boolean isInPanel;
    protected static int levelWeights[];
    protected int groundLevel;
    protected boolean isDirty;
    protected long nextSoundPlay;
    protected int playerDamage;
    protected static final int MIN_X_SPAWN = 800;
    protected int placeableDamage;
    protected int level;
    protected boolean hideDisplayName = false;
    protected float difficultyFactor = 0.50f;

    protected Monster(MonsterManager mm, Registry rg, String im, String st, int x, int y, int minDist, int maxDist, boolean spawnTop) {
        super(mm, rg, im, x, y);

        monsterManager = mm;

        spawnType = st;

        isStill = true;
        facing = Facing.LEFT;
        vertMoveMode = VertMoveMode.NOT_JUMPING;

        yx = new int[2];
        ycm = new int[2];


        maxAggroRange = MonsterManager.mobSpawnRangeMax;

        ai = new AI(registry, this);

        maxFollowDistance = 5;

        dropChances = new DropChanceCollection();

        Point p = findSpawnNearPoint(new Point(x, y), minDist, maxDist, spawnTop);
        mapX = p.x;
        mapY = p.y;

        groundLevel = registry.getBlockManager().getLevelByY(mapY);
        if (monsterManager.doesRectContainBlocks(mapX, mapY, width, height)) {
            isDirty = true;
        }

        determineLevel();

        showGoals = monsterManager.getShowGoals();
    }

    private Point findSpawnNearPoint(Point p, int minDist, int maxDist, boolean spawnTop) {
        int dist = Rand.getRange(minDist, maxDist);
        int angle = Rand.getRange(0, 360);
        Point retP = new Point(-1, -1);
        int x;
        int y;
        int newDist;
        for (double i = 0; i < 360; i += 20) {
            x = (int) (dist * Math.cos(Math.toRadians(angle + i))) + p.x;
            y = (int) (dist * Math.sin(Math.toRadians(angle + i))) + p.y;
            if (x >= MIN_X_SPAWN && y > 0) {
                if (spawnTop) {
                    y = monsterManager.findFloor(x);
                } else {
                    y = monsterManager.findNextFloor(x, y, height + BlockManager.getBlockHeight() * 4);
                }
                newDist = (int) Math.sqrt(Math.pow(Math.abs(p.x - x), 2.0f) + Math.pow(Math.abs(p.y - y), 2.0f));
                if (newDist > minDist && newDist < maxDist
                && !monsterManager.doesRectContainBlocks(x, y, width, height)) {
                    boolean touchingPlaceable = false;
                    Placeable placeable = registry.getPlaceableManager().getClosest(new Point(x, y));
                    if (placeable != null && placeable.getPerimeter().intersects(new Rectangle(x, y, 120, 120))) {
                        touchingPlaceable = true;
                    }
                    if (!touchingPlaceable) {
                        retP.x = x;
                        retP.y = y;
                        break;
                    }
                }
            }
        }
        return retP;
    }

    public void setTransient(Registry rg, MonsterManager mm) {
        super.setTransient(rg, mm);

        yx = new int[2];
        ycm = new int[2];

        registry = rg;
        monsterManager = mm;
        manager = mm;

        ai.setTransient(this, rg);
        dropChances.setTransient();

        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public static int getTotalForMap() {
        int total = 0;

        for (int i = 0; i < levelWeights.length; i++) {
            total += levelWeights[i];
        }

        return total;
    }

    public int getTouchDamage() {
        return touchDamage;
    }

    protected void determineLevel() {
        int l = 1;
        int playerLevel = registry.getPlayerManager().getCurrentPlayer().getLevel();

        switch (groundLevel) {
            case 2:
                l = Rand.getRange(playerLevel - 1, playerLevel + 4);
                if (l > 15) {
                    l = 15;
                }
                break;
            case 3:
                l = Rand.getRange(playerLevel, playerLevel + 6);
                if (l > 20) {
                    l = 20;
                }
                break;
            case 4:
                l = Rand.getRange(playerLevel + 1, playerLevel + 8);
                if (l > 25) {
                    l = 25;
                }
                break;
            case 5:
                l = Rand.getRange(playerLevel + 2, playerLevel + 10);
                if (l > 30) {
                    l = 30;
                }
                break;
            default:
                l = Rand.getRange(playerLevel - 2, playerLevel + 2);
                if (l > 10) {
                    l = 10;
                }
                break;
        }

        if (l < 1) {
            l = 1;
        }

        level = l;
    }

    protected void adjustHPForLevel() {
        int[] hpTable = new int[31];
        hpTable[0] = 0;
        hpTable[1] = 37;
        hpTable[2] = 61;
        hpTable[3] = 96;
        hpTable[4] = 139;
        hpTable[5] = 188;
        hpTable[6] = 240;
        hpTable[7] = 296;
        hpTable[8] = 354;
        hpTable[9] = 413;
        hpTable[10] = 473;
        hpTable[11] = 535;
        hpTable[12] = 597;
        hpTable[13] = 659;
        hpTable[14] = 722;
        hpTable[15] = 786;
        hpTable[16] = 849;
        hpTable[17] = 913;
        hpTable[18] = 977;
        hpTable[19] = 1042;
        hpTable[20] = 1106;
        hpTable[21] = 1171;
        hpTable[22] = 1235;
        hpTable[23] = 1300;
        hpTable[24] = 1365;
        hpTable[25] = 1430;
        hpTable[26] = 1495;
        hpTable[27] = 1560;
        hpTable[28] = 1625;
        hpTable[29] = 1691;
        hpTable[30] = 1756;

        if (level > 0 && level <= hpTable.length) {
            totalHitPoints = hpTable[level];
        } else {
            totalHitPoints = hpTable[1];
        }

        //hp varies 10% based on difficulty rating
        float variance = totalHitPoints * 0.10f;
        variance *= 1.00f - difficultyFactor;
        totalHitPoints = (int) (totalHitPoints - variance);

        hitPoints = totalHitPoints;
    }

    protected void adjustTouchDamageForLevel() {
        int[] damageTable = new int[31];
        damageTable[0] = 0;
        damageTable[1] = 10;
        damageTable[2] = 20;
        damageTable[3] = 30;
        damageTable[4] = 40;
        damageTable[5] = 50;
        damageTable[6] = 60;
        damageTable[7] = 70;
        damageTable[8] = 80;
        damageTable[9] = 90;
        damageTable[10] = 100;
        damageTable[11] = 110;
        damageTable[12] = 120;
        damageTable[13] = 130;
        damageTable[14] = 140;
        damageTable[15] = 150;
        damageTable[16] = 160;
        damageTable[17] = 170;
        damageTable[18] = 180;
        damageTable[19] = 190;
        damageTable[20] = 200;
        damageTable[21] = 210;
        damageTable[22] = 220;
        damageTable[23] = 230;
        damageTable[24] = 240;
        damageTable[25] = 250;
        damageTable[26] = 260;
        damageTable[27] = 270;
        damageTable[28] = 280;
        damageTable[29] = 290;
        damageTable[30] = 300;

        if (level > 0 && level <= damageTable.length) {
            touchDamage = damageTable[level];
        } else {
            touchDamage = damageTable[1];
        }

        //damage varies 20% based on difficulty rating
        float variance = (float) touchDamage * 0.20f;
        variance *= 1.00f - difficultyFactor;
        touchDamage = (int) ((float) touchDamage - variance);
    }

    protected void registerAttacker(Actor a, int d, boolean fromPlaceable) {
        if (a != null && d > 0) {
            if (attackers == null) {
                attackers = new HashMap<Actor, Integer>();
            }

            if (fromPlaceable) {
                lastAttacker = null;
                placeableDamage += d;
            } else {
                lastAttacker = a;

                if (attackers.containsKey(a)) {
                    attackers.put(a, attackers.get(a) + d);
                } else {
                    attackers.put(a, d);
                }
            }
        }
    }

    public int applyDamage(int damage, Actor a, boolean fromPlaceable) {
        return applyDamage(damage, a, fromPlaceable, true);
    }

    public int applyDamage(int damage, Actor a, boolean fromPlaceable, boolean sound) {
        if (damage <= 0) {
            return 0;
        }

        damage -= getArmorPoints() / 5;

        registerAttacker(a, damage, fromPlaceable);

        if (damage <= 0) {
            damage = 1;
        }

        hitPoints -= damage;
        registry.getIndicatorManager().createIndicator(mapX + (width / 2), mapY + 50, "-" + Integer.toString(damage));
        if (sound) {
            //SoundClip cl = new SoundClip(registry, "Monster/Hurt" + name + Rand.getRange(1, 2), getCenterPoint());
        }

        if (registry.getGameController().multiplayerMode == MultiplayerMode.SERVER && registry.getNetworkThread() != null
        && registry.getNetworkThread().readyForUpdates()) {
            UpdateMonster um = new UpdateMonster(this.getId());
            um.mapX = this.getMapX();
            um.mapY = this.getMapY();
            um.action = "ApplyDamage";
            um.dataInt = damage;
            um.actor = a;
            registry.getNetworkThread().sendData(um);
        }

        return damage;
    }

    protected Color getColorBasedOnPlayerLevel(int l) {
        if (level == -1) {
            return Color.MAGENTA;
        } else if (level < l - 2) {
            return Color.LIGHT_GRAY;
        } else if (level >= l - 2 && level < l) {
            return Color.GREEN;
        } else if (level == l) {
            return Color.YELLOW;
        } else if (level > l && level <= l + 2) {
            return Color.ORANGE;
        } else if (level > l + 2) {
            return Color.RED;
        }

        return Color.RED;
    }

    @Override
    public int getXMoveSize() {
        return xMoveSize;
    }

    @Override
    public void setXMoveSize(int s) {
        xMoveSize = s;
    }

    public String getSpawnType() {
        return spawnType;
    }

    public int getPlayerDamage() {
        return playerDamage;
    }

    public int getMaxAggroRange() {
        return maxAggroRange;
    }

    public String getName() {
        return name;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean isAttacking() {
        return (actionMode == ActionMode.ATTACKING);
    }

    public boolean getIsInPanel() {
        return isInPanel;
    }

    public void setPlayerDamage(int d) {
        playerDamage = 0;
    }

    public Point getNewXY(int mapWidth, int l) {
        Point p = new Point(0, 0);

        p.x = Rand.getRange(1000, mapWidth);

        int diff = (registry.getBlockManager().getLevelTop(l) - registry.getBlockManager().getLevelBottom(l));
        p.y = Rand.getRange(registry.getBlockManager().getLevelBottom(l) + diff / 4,
                registry.getBlockManager().getLevelTop(l));

        p.y = monsterManager.findNextFloor(p.x, p.y, height + BlockManager.getBlockHeight() * 4) + BlockManager.getBlockHeight() * 2;

        return p;
    }

    protected int randomX(int mapWidth) {
        int rangeMin = (int) (mapXMin * mapWidth);
        int rangeMax = (int) (mapXMax * mapWidth);

        return Rand.getRange(rangeMin, rangeMax);
    }

    public Damage getMonsterTouchDamage(Rectangle r) {
        return getMonsterTouchDamage(r, 0);
    }

    public Damage getMonsterTouchDamage(Rectangle r, int x) {
        if (hitPoints > 0 && spriteRect != null && r != null && spriteRect.intersects(r)) {
            playerDamage += touchDamage;
            return new Damage(this, touchDamage);
        }

        return null;
    }

    public boolean isMoving() {
        if (isStill && vertMoveMode == VertMoveMode.NOT_JUMPING) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isAttacking(Player p) {
        boolean ret = false;
        Goal g = ai.getCurrentGoal();

        if (g != null && g.getGoalType().equals("GoalAttackPlayer")) {
            Player player = ((GoalAttackPlayer) g).getPlayerToAttack(this);
            if (player == p) {
                ret = true;
            }
        }
        return ret;
    }

    public void stopJump() {
        if (vertMoveMode == VertMoveMode.JUMPING) {
            vertMoveMode = VertMoveMode.FALLING;
        }
    }

    public void stopAscend() {
        if (vertMoveMode == VertMoveMode.FLYING) {
            vertMoveMode = VertMoveMode.FALLING;
        }
    }

    @Override
    public void attack() {
        if (attackRefreshTimerEnd < System.currentTimeMillis()) {
            if (actionMode != ActionMode.ATTACKING) {
                actionMode = ActionMode.ATTACKING;
            }
            attackRefreshTimerStart = System.currentTimeMillis();
            attackRefreshTimerEnd = System.currentTimeMillis() + meleeSpeed;
            attackRect = new Rectangle(mapX + (width / 2), mapY, (width / 2) + attackRange, height);
            if (facing == Facing.LEFT) {
                attackRect = new Rectangle(mapX - attackRange, mapY, (width / 2) + attackRange, height);
            }
            monsterManager.monsterAttackPlaceable(this, attackRect, meleeDamage);
        }
    }

    public void stopAttack() {
        actionMode = ActionMode.NONE;
    }

    public int getXPByPlayer(Actor a) {
        int total = 0;
        int player = 0;

        //figure out the total damage and the damage form the given player
        if (attackers != null) {
            try {
                for (Entry<Actor, Integer> entry : attackers.entrySet()) {
                    int value = entry.getValue();
                    total += value;
                    if (a == entry.getKey()) {
                        player += value;
                    }
                }
            } catch (ConcurrentModificationException concEx) {
                //another thread was trying to modify attackers while iterating
            }
        }

        player -= placeableDamage;

        if (total > 0 && player > 0) {
            float totalXP = getXPTotalValue(a);
            return (int) (((float) player / (float) total) * totalXP);
        }
        return 0;
    }

    public int getXPTotalValue(Actor a) {
        int[] xpTable = new int[31];
        xpTable[0] = 0;
        xpTable[1] = 23;
        xpTable[2] = 32;
        xpTable[3] = 41;
        xpTable[4] = 52;
        xpTable[5] = 65;
        xpTable[6] = 79;
        xpTable[7] = 96;
        xpTable[8] = 114;
        xpTable[9] = 135;
        xpTable[10] = 157;
        xpTable[11] = 181;
        xpTable[12] = 206;
        xpTable[13] = 234;
        xpTable[14] = 263;
        xpTable[15] = 295;
        xpTable[16] = 328;
        xpTable[17] = 362;
        xpTable[18] = 399;
        xpTable[19] = 438;
        xpTable[20] = 478;
        xpTable[21] = 520;
        xpTable[22] = 564;
        xpTable[23] = 610;
        xpTable[24] = 657;
        xpTable[25] = 707;
        xpTable[26] = 758;
        xpTable[27] = 811;
        xpTable[28] = 866;
        xpTable[29] = 923;
        xpTable[30] = 981;

        if (level > 0 && level <= xpTable.length) {
            //xp varies 10% based on difficulty rating
            float variance = xpTable[level] * 0.10f;
            variance *= 1.00f - difficultyFactor;
            return (int) (xpTable[level] - variance);
        } else {
            return 0;
        }
    }

    public void setHitPoints(int hp) {
        hitPoints = hp;
    }

    @Override
    public void update() {
        super.update();

        debugInfo = "";

        if (isActive && isAnimating && animationFrameUpdateTime <= registry.currentTime) {
            currentAnimationFrame++;
            if (currentAnimationFrame >= numAnimationFrames) {
                currentAnimationFrame = 0;
            }
            animationFrameUpdateTime = registry.currentTime + animationFrameDuration;
        }

        if (hitPoints > 0) {
            if (registry.getGameController().multiplayerMode != MultiplayerMode.CLIENT) {
                if (knockBackX > 0) {
                    mapX += checkCollide(knockBackX);
                } else if (knockBackX < 0) {
                    mapX -= checkCollide(knockBackX);
                } else {
                    if (registry.getGameController().multiplayerMode == MultiplayerMode.CLIENT) {
                        ai.process(true);
                    } else {
                        ai.process();
                    }
                    if (ai.getChanged()) {
                        ai.setChanged(false);
                        if (registry.getGameController().multiplayerMode == MultiplayerMode.SERVER && registry.getNetworkThread() != null
                        && registry.getNetworkThread().readyForUpdates()) {
                            UpdateMonster um = new UpdateMonster(this.getId());
                            um.mapX = this.getMapX();
                            um.mapY = this.getMapY();
                            um.previousGoal = ai.getPreviousGoal();
                            um.currentGoal = ai.getCurrentGoal();
                            registry.getNetworkThread().sendData(um);
                        }
                    }
                }
            }
            if (monsterManager.getSelectedMob() == this) {
                registry.setPortraitImage("Mob" + name);
                registry.setPortraitHPCurrent(hitPoints);
                registry.setPortraitHP(totalHitPoints);
                registry.setPortraitAttack(touchDamage);
                registry.getHUDManager().showPortrait(true);
            } else if (monsterManager.getSelectedMob() == null) {
                Point mousePos = new Point(monsterManager.panelToMapX(registry.getMousePosition().x), monsterManager.panelToMapY(registry.getMousePosition().y));
                if (this.isInside(mousePos) && !this.getIsHiding()) {
                    registry.setPortraitImage("Mob" + name);
                    registry.setPortraitHPCurrent(hitPoints);
                    registry.setPortraitHP(totalHitPoints);
                    registry.setPortraitAttack(touchDamage);
                    registry.getHUDManager().showPortrait(true);
                }
            }
        } else {
            if (registry.getGameController().multiplayerMode == MultiplayerMode.SERVER && registry.getNetworkThread() != null
            && registry.getNetworkThread().readyForUpdates()) {
                UpdateMonster um = new UpdateMonster(this.getId());
                um.mapX = this.getMapX();
                um.mapY = this.getMapY();
                um.action = "Die";
                registry.getNetworkThread().sendData(um);
            }

            //SoundClip cl = new SoundClip(registry, "Monster/Die" + name, getCenterPoint());
            isDead = true;
            ai.terminate();

            if (registry.getGameController().multiplayerMode != MultiplayerMode.CLIENT) {
                ArrayList<Drop> drops = dropChances.generateDrops();
                if (drops.size() > 0) {
                    monsterManager.dropLoot(this, mapX + (width / 2), mapY + 32, drops);
                }
                monsterManager.giveXP(this);
            }

            BufferedImage im = registry.getImageLoader().getImage(image);
            if (facing == Facing.LEFT) {
                AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
                tx.translate(-width, 0);
                AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
                BufferedImage imLeft = op.filter(im, null);
                if (imLeft != null) {
                    registry.getPixelizeManager().pixelize(imLeft, mapX, mapY);
                }
            } else {
                registry.getPixelizeManager().pixelize(im, mapX, mapY);
            }
        }

        if (registry.getGameController().multiplayerMode != MultiplayerMode.CLIENT) {
            if (vertMoveMode == VertMoveMode.JUMPING) {
                updateJumping();
            } else if (vertMoveMode == VertMoveMode.FLYING) {
                updateAscending();
            } else if (vertMoveMode == VertMoveMode.FALLING) {
                updateFalling();
            }

            mapX = monsterManager.checkMapX(mapX, width);
            mapY = monsterManager.checkMapY(mapY, height);
        }

        // Player p = registry.getPlayerManager().getCurrentPlayer();

        if (spriteRect.intersects(manager.getPanelRect())) {
            isInPanel = true;
        } else {
            isInPanel = false;
        }
       /*if (p != null) {
            if (mapX >= (p.getMapX() - (monsterManager.getPWidth() / 2))
                    && mapX <= (p.getMapX() + (monsterManager.getPWidth() / 2))
                    && mapY >= (p.getMapY() - (monsterManager.getPHeight() / 2))
                    && mapY  <= (p.getMapY() + (monsterManager.getPHeight() / 2))) {
                isInPanel = true;
            } else {
                isInPanel = false;
            }
        } else {
            isInPanel = true;
        }*/
        
        //debugInfo = Boolean.toString(isInPanel)
        //showGoals = true;

        if (registry.getGameController().multiplayerMode != MultiplayerMode.CLIENT && vertMoveMode != VertMoveMode.FALLING) {
            checkIfFalling();
        }

        updateImage();
    }

    protected boolean isInside(Point p) {
        return (p.x >= mapX
                && p.x <= (mapX + width)
                && p.y >= mapY
                && p.y <= (mapY + height));
    }

    public void updateLong() {
        if (isPoisoned) {
            applyDamage(1, null, false, false);
        }

        if (registry.getClosestPlayer(getCenterPoint(), MonsterManager.mobSpawnRangeMin * 2) == null) {
            Point p = getCenterPoint();
            if (p.x > 0 && p.y > 0 && !registry.getPlaceableManager().isPlaceableWithin(getCenterPoint(), MonsterManager.mobSpawnRangeMin * 2)) {
                isDirty = true;
            }
        }
        if (nextSoundPlay <= registry.currentTime) {
            SoundClip cl = new SoundClip(registry, "Monster/Ambient" + name, getCenterPoint());
            nextSoundPlay = registry.currentTime + Rand.getRange(10000, 15000);
            if (name.equals("ZombieWalrus") && cl.getWasHeard()) {
                loopImage("Monsters/" + name + "/Attacking", 0.50);
                monsterManager.shakeCamera(500, 15);
            }
        }
    }

    public int getMaxShootRange() {
        return 0;
    }

    public int getMaxLungeRange() {
        return 0;
    }

    public void shoot(Point targetPoint) {
    }

    @Override
    public void render(Graphics g) {
        if (shouldRender) {
            int statusCount = 0;
            int statusX = 0;
            int statusNewXPos = 0;
            BufferedImage im;

            if (isAnimating) {
                im = registry.getImageLoader().getImage(image, currentAnimationFrame);
            } else {
                im = registry.getImageLoader().getImage(image);
            }

            int xPos = monsterManager.mapToPanelX(mapX);
            int yPos = monsterManager.mapToPanelY(mapY);

            //flip the yPos since drawing happens top down versus bottom up
            yPos = monsterManager.getPHeight() - yPos;

            //subtract the height since points are bottom left and drawing starts from top left
            yPos -= height;

            if (im != null) {
                if (facing == Facing.LEFT) {
                    AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
                    tx.translate(-width, 0);
                    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
                    BufferedImage imLeft = op.filter(im, null);
                    if (imLeft != null) {
                        g.drawImage(imLeft, xPos, yPos, null);
                    }
                } else {
                    g.drawImage(im, xPos, yPos, null);
                }
            }


            //render statuses
            if (statusFear) {
                statusCount++;
            }
            if (statusHeal) {
                statusCount++;
            }
            if (statusPoison) {
                statusCount++;
            }
            if (statusSlowed) {
                statusCount++;
            }

            if (statusCount > 0) {
                int totalWidth = (statusCount * STATUS_WIDTH) + ((statusCount - 1) * STATUS_SPACING);
                statusX = getCenterPoint().x - (totalWidth / 2);
                int i = 0;

                if (statusFear) {
                    statusNewXPos = statusX + (i * STATUS_WIDTH) + (i * STATUS_SPACING);

                    xPos = manager.mapToPanelX(statusNewXPos);
                    yPos = manager.mapToPanelY(mapY + 50);

                    //flip the yPos since drawing happens top down versus bottom up
                    yPos = manager.getPHeight() - yPos;

                    //subtract the height since points are bottom left and drawing starts from top left
                    yPos -= height;

                    im = registry.getImageLoader().getImage("Effects/Fear");
                    if (im != null) {
                        g.drawImage(im, xPos, yPos, null);
                    }
                    i++;
                }

                if (statusHeal) {
                    statusNewXPos = statusX + (i * STATUS_WIDTH) + (i * STATUS_SPACING);

                    xPos = manager.mapToPanelX(statusNewXPos);
                    yPos = manager.mapToPanelY(mapY + 50);

                    //flip the yPos since drawing happens top down versus bottom up
                    yPos = manager.getPHeight() - yPos;

                    //subtract the height since points are bottom left and drawing starts from top left
                    yPos -= height;

                    im = registry.getImageLoader().getImage("Effects/Heal");
                    if (im != null) {
                        g.drawImage(im, xPos, yPos, null);
                    }
                    i++;
                }

                if (statusPoison) {
                    statusNewXPos = statusX + (i * STATUS_WIDTH) + (i * STATUS_SPACING);

                    xPos = manager.mapToPanelX(statusNewXPos);
                    yPos = manager.mapToPanelY(mapY + 50);

                    //flip the yPos since drawing happens top down versus bottom up
                    yPos = manager.getPHeight() - yPos;

                    //subtract the height since points are bottom left and drawing starts from top left
                    yPos -= height;

                    im = registry.getImageLoader().getImage("Effects/Poison");
                    if (im != null) {
                        g.drawImage(im, xPos, yPos, null);
                    }
                    i++;
                }

                if (statusSlowed) {
                    statusNewXPos = statusX + (i * STATUS_WIDTH) + (i * STATUS_SPACING);

                    xPos = manager.mapToPanelX(statusNewXPos);
                    yPos = manager.mapToPanelY(mapY + 50);

                    //flip the yPos since drawing happens top down versus bottom up
                    yPos = manager.getPHeight() - yPos;

                    //subtract the height since points are bottom left and drawing starts from top left
                    yPos -= height;

                    im = registry.getImageLoader().getImage("Effects/Slowed");
                    if (im != null) {
                        g.drawImage(im, xPos, yPos, null);
                    }
                    //i++;
                }
            }

            if (!hideDisplayName) {
                /*
                 * xPos = manager.mapToPanelX(this.getCenterPoint().x - 81);
                 * yPos = manager.mapToPanelY(mapY + height);
                 *
                 * //flip the yPos since drawing happens top down versus bottom
                 * up yPos = manager.getPHeight() - yPos;
                 *
                 * //subtract the height since points are bottom left and
                 * drawing starts from top left yPos -= height;
                 *
                 * im = registry.getImageLoader().getImage("Misc/NameBG"); if
                 * (im != null) { g.drawImage(im, xPos, yPos, null); }
                 */
                Font textFont = new Font("SansSerif", Font.BOLD, 14);
                g.setFont(textFont);

                FontMetrics fm = g.getFontMetrics();
                int messageWidth = fm.stringWidth(displayName + " (" + level + ")");

                xPos = monsterManager.mapToPanelX(mapX + (width / 2) - (messageWidth / 2));
                yPos = monsterManager.mapToPanelY(mapY + height);
                yPos = monsterManager.getPHeight() - yPos;

                if (level == -1) {
                    registry.ghettoOutline(g, Color.BLACK, displayName + " (?)", xPos, yPos);

                    g.setColor(getColorBasedOnPlayerLevel(registry.getPlayerManager().getCurrentPlayer().getLevel()));
                    g.drawString(displayName + " (?)", xPos, yPos);
                } else {
                    registry.ghettoOutline(g, Color.BLACK, displayName + " (" + level + ")", xPos, yPos);

                    PlayerManager playerManager = registry.getPlayerManager();
                    if (playerManager != null) {
                        Player p = registry.getPlayerManager().getCurrentPlayer();
                        if (p != null) {
                            g.setColor(getColorBasedOnPlayerLevel(p.getLevel()));
                            g.drawString(displayName + " (" + level + ")", xPos, yPos);
                        }
                    }
                }
            }

            if (hitPoints < totalHitPoints) {
                float percentage;

                int x = mapX + (width / 2);
                int y = mapY + height;

                percentage = ((float) hitPoints / (float) totalHitPoints) * 100;

                monsterManager.displayHP(g,
                        x,
                        y,
                        (int) percentage);
            }

            if (monsterManager.getSelectedMob() == this) {
                xPos = manager.mapToPanelX(mapX + (baseWidth / 2) + baseOffset - 12);
                yPos = manager.mapToPanelY(mapY);

                //flip the yPos since drawing happens top down versus bottom up
                yPos = manager.getPHeight() - yPos;

                im = registry.getImageLoader().getImage("Monsters/Selected");
                if (im != null) {
                    g.drawImage(im, xPos, yPos, null);
                }
            }

            super.render(g);
        }
    }

    @Override
    protected void updateImage() {
        if (vertMoveMode == VertMoveMode.JUMPING) {
            setImage("Monsters/" + name + "/Jumping");
        } else if (vertMoveMode == VertMoveMode.FLYING) {
            loopImage("Monsters/" + name + "/Flapping");
        } else if (vertMoveMode == VertMoveMode.FALLING) {
            loopImage("Monsters/" + name + "/Falling");
        } else {
            if (this.isAttacking()) {
                loopImage("Monsters/" + name + "/Attacking");
            } else if (isStill) {
                setImage("Monsters/" + name + "/Standing");
            } else {
                loopImage("Monsters/" + name + "/Walking");
            }
        }
    }

    public UDPMonster createUpdate() {
        UDPMonster udpUpdate = new UDPMonster(id);
        udpUpdate.mapX = mapX;
        udpUpdate.lastMapY = lastMapY;
        udpUpdate.mapY = mapY;
        udpUpdate.width = width;
        udpUpdate.height = height;
        udpUpdate.xMoveSize = xMoveSize;
        udpUpdate.actionMode = actionMode;
        udpUpdate.vertMoveMode = vertMoveMode;
        udpUpdate.facing = facing;
        udpUpdate.jumpSize = jumpSize;
        udpUpdate.ascendOriginalSize = ascendOriginalSize;
        udpUpdate.ascendSize = ascendSize;
        udpUpdate.ascendCount = ascendCount;
        udpUpdate.ascendMax = ascendMax;
        udpUpdate.fallSize = fallSize;
        udpUpdate.isStill = isStill;
        udpUpdate.isTryingToMove = isTryingToMove;
        udpUpdate.startJumpSize = startJumpSize;
        udpUpdate.maxFallSize = maxFallSize;
        udpUpdate.gravity = gravity;
        udpUpdate.totalFall = totalFall;
        udpUpdate.completeFall = completeFall;
        udpUpdate.totalHitPoints = totalHitPoints;
        udpUpdate.hitPoints = hitPoints;
        udpUpdate.totalArmorPoints = totalArmorPoints;
        udpUpdate.armorPoints = armorPoints;
        udpUpdate.knockBackX = knockBackX;
        udpUpdate.isDead = isDead;
        udpUpdate.touchDamage = touchDamage;
        udpUpdate.meleeDamage = meleeDamage;
        udpUpdate.meleeSpeed = meleeSpeed;
        udpUpdate.mapXMin = mapXMin;
        udpUpdate.mapXMax = mapXMax;
        udpUpdate.groundLevel = groundLevel;
        udpUpdate.isDirty = isDirty;
        udpUpdate.playerDamage = playerDamage;
        udpUpdate.placeableDamage = placeableDamage;
        udpUpdate.level = level;

        return udpUpdate;
    }

    public void processUpdate(UDPMonster udpUpdate) {
        mapX = udpUpdate.mapX;
        lastMapY = udpUpdate.lastMapY;
        mapY = udpUpdate.mapY;
        width = udpUpdate.width;
        height = udpUpdate.height;
        xMoveSize = udpUpdate.xMoveSize;
        actionMode = udpUpdate.actionMode;
        vertMoveMode = udpUpdate.vertMoveMode;
        facing = udpUpdate.facing;
        jumpSize = udpUpdate.jumpSize;
        ascendOriginalSize = udpUpdate.ascendOriginalSize;
        ascendSize = udpUpdate.ascendSize;
        ascendCount = udpUpdate.ascendCount;
        ascendMax = udpUpdate.ascendMax;
        fallSize = udpUpdate.fallSize;
        isStill = udpUpdate.isStill;
        isTryingToMove = udpUpdate.isTryingToMove;
        startJumpSize = udpUpdate.startJumpSize;
        maxFallSize = udpUpdate.maxFallSize;
        gravity = udpUpdate.gravity;
        totalFall = udpUpdate.totalFall;
        completeFall = udpUpdate.completeFall;
        totalHitPoints = udpUpdate.totalHitPoints;
        hitPoints = udpUpdate.hitPoints;
        totalArmorPoints = udpUpdate.totalArmorPoints;
        armorPoints = udpUpdate.armorPoints;
        knockBackX = udpUpdate.knockBackX;
        isDead = udpUpdate.isDead;
        touchDamage = udpUpdate.touchDamage;
        meleeDamage = udpUpdate.meleeDamage;
        meleeSpeed = udpUpdate.meleeSpeed;
        mapXMin = udpUpdate.mapXMin;
        mapXMax = udpUpdate.mapXMax;
        groundLevel = udpUpdate.groundLevel;
        isDirty = udpUpdate.isDirty;
        playerDamage = udpUpdate.playerDamage;
        placeableDamage = udpUpdate.placeableDamage;
        level = udpUpdate.level;
    }

    private void readObject(ObjectInputStream aInputStream) throws IOException, ClassNotFoundException {
        aInputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.defaultWriteObject();
    }
}