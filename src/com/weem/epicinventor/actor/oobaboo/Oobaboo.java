package com.weem.epicinventor.actor.oobaboo;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.ai.*;
import com.weem.epicinventor.armor.*;
import com.weem.epicinventor.inventory.*;
import com.weem.epicinventor.network.*;
import com.weem.epicinventor.particle.*;
import com.weem.epicinventor.resource.*;
import com.weem.epicinventor.utility.*;
import com.weem.epicinventor.weapon.*;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.util.*;

public abstract class Oobaboo extends Actor implements Serializable {

    private static final long serialVersionUID = 10000L;
    transient protected PlayerManager playerManager;
    transient protected Player player;
    protected AI ai;
    protected String playerID = "";
    protected String name;
    protected long timeRemaining = 60000;
    protected long timeTotal = 60000;
    protected boolean invulnerable;
    protected boolean invulnerableShow;
    protected float invulnerableTotalTime;
    protected float INVULNERABLE_MAX_TIME = 1.0f;
    protected long meleeAnimationTotalTime;
    protected double meleeAnimationFrameDuration; //duration of a single frame
    protected int currentMeleeAnimationFrame;
    protected int numMeleeAnimationFrames;
    transient private BufferedImage[] meleeImages;
    protected boolean isSwinging;
    private ResourceType currentResourceType;
    private long lastResourceSoundPlay;
    private long lastMove;
    protected long nextSoundPlay;
    protected long disappearTime = 0;
    transient private LeafEmitter leafEmitter;

    public Oobaboo(PlayerManager pm, Player p, Registry rg, String im, int x) {
        super(pm, rg, im, x, 0);

        playerManager = pm;
        player = p;
        
        playerID = player.getId();

        isStill = true;
        facing = Facing.RIGHT;
        setVertMoveMode(VertMoveMode.NOT_JUMPING);

        hitPoints = baseHitPoints;

        topOffset = 20;
        baseOffset = 28;
        baseWidth = 17;
        startJumpSize = 20;
        jumpSize = 8;
        fallSize = 0;
        completeFall = 0;

        xMoveSize = 7;

        currentAttackType = AttackType.MELEE;

        mapX = player.getMapX();
        mapY = player.getMapY();

        String[] names = {"Kekososo", "Scelolo", "Bobobo", "Chikinono", "Glaydurdur", "Slushush", "Rogogo", "Tuptup", "Cosisi", "Zizozo", "Jerlolo", "Wexoxo", "Danilil", "Vortactac", "Slusheshe", "Doctoto", "Tankank", "Ratatat", "Nikoko", "Seanosos", "Stevenothoth", "Totalbisbis", "Smitteetee", "Notchotch", "Weem", "Brandon", "Forrest"};
        //String[] names = {"Abidemi", "Abimbola", "Abiodun", "Abioye", "Aboubacar", "Adebowale", "Adegoke", "Adetokunbo", "Adisa", "Afolabi", "Akachi", "Akuchi", "Andile", "Awotwi", "Ayodele", "Ayokunle", "Ayotunde", "Azubuike", "Baako", "Babajide", "Babatunde", "Bamidele", "Berko", "Boipelo", "Bongani", "Bosede", "Chibueze", "Chibuike", "Chibuzo", "Chidi", "Chidiebere", "Chidiebube", "Chidiegwu", "Chidike", "Chidubem", "Chiemeka", "Chijindum", "Chike", "Chikelu", "Chikere", "Chima", "Chinedu", "Chinonso", "Chinwe", "Chinweike", "Chinwendu", "Chinweuba", "Chiumbo", "Chizoba", "Chuks", "Chukwudi", "Chukwuemeka", "Chukwuma", "Dakarai", "Dubaku", "Dumisani", "Ekene", "Ekenedilichukwu", "Ekwueme", "Emeka", "Emem", "Enitan", "Enu", "Enyinnaya", "Farai", "Faraji", "Femi", "Folami", "Fungai", "Gwandoya", "Ibrahima", "Idowu", "Ikenna", "Imamu", "Ime", "Isingoma", "Itumeleng", "Jelani", "Jengo", "Jumaane", "Katlego", "Kato", "Kayode", "Kefilwe", "Kgosi", "Khamisi", "Kibwe", "Kirabo", "Kobina", "Kofi", "Kojo", "Kwabena", "Kwadwo", "Kwaku", "Kwame", "Kwasi", "Kweku", "Kwesi", "Lanre", "Lekan", "Mamadou", "Masamba", "Masozi", "Melisizwe", "Mosi", "Munashe", "Mwenye", "Ndidi", "Nkemdilim", "Nkosana", "Nkruma", "Nnamdi", "Nsia", "Nsonowa", "Nthanda", "Obi", "Ochieng", "Odhiambo", "Olabode", "Olamilekan", "Olanrewaju", "Oluchi", "Olufemi", "Olufunke", "Olujimi", "Olukayode", "Olumide", "Oluwakanyinsola", "Oluwasegun", "Oluwaseun", "Oluwatoyin", "Onyekachukwu", "Opeyemi", "Otieno", "Paki", "Refilwe", "Rudo", "Sefu", "Simba", "Sipho", "Sizwe", "Tafadzwa", "Tafari", "Tatenda", "Tau", "Tendai", "Tendaji", "Thabo", "Themba", "Thulani", "Tichaona", "Tinashe", "Tumelo", "Uduak", "Unathi", "Uzochi", "Uzoma", "Wasswa", "Wekesa"};
        name = names[Rand.getRange(0, names.length - 1)];

        nextSoundPlay = registry.currentTime + Rand.getRange(6000, 10000);

        createLeafEmitter();
    }

    public void init() {
        mapY = playerManager.findFloor(mapX);
    }

    public void setTransient(Registry rg, Player p) {
        yx = new int[2];
        ycm = new int[2];

        playerManager = rg.getPlayerManager();
        registry = rg;
        manager = rg.getPlayerManager();
        player = p;
        
        ai.setTransient(this, rg);
        ai.setPlayer(player.getId());

        attackArcOffsetX = 0;
        attackArcOffsetY = 0;

        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    private void createLeafEmitter() {
        if (leafEmitter == null) {
            ArrayList<String> images = new ArrayList<String>();
            images.add("Particles/Leaf1");
            images.add("Particles/Leaf2");
            images.add("Particles/Leaf3");
            images.add("Particles/Leaf4");
            images.add("Particles/Leaf5");
            images.add("Particles/Leaf6");
            images.add("Particles/Leaf7");
            images.add("Particles/Leaf8");
            images.add("Particles/Leaf9");
            images.add("Particles/Leaf10");
            leafEmitter = new LeafEmitter(registry.getGameController(), registry, this, mapX + baseOffset, mapY, images, true, false, false, 0, 8.0f, 10.0f, 400, true);
            leafEmitter.setParticlesPerGeneration(20);
            leafEmitter.setActive(true);
        } else {
            leafEmitter.setActive(true);
        }
    }

    private void destoryLeafEmitter() {
        if (leafEmitter != null) {
            leafEmitter.destroy();
            leafEmitter = null;
        }
    }

    public void die() {
        BufferedImage im;
        if (isAnimating) {
            im = registry.getImageLoader().getImage(image, currentAnimationFrame);
        } else {
            im = registry.getImageLoader().getImage(image);
        }
        registry.getPixelizeManager().pixelize(im, mapX, mapY);
    }

    public Player getPlayer() {
        return player;
    }
    
    public String getPlayerID() {
        return playerID;
    }

    public String getName() {
        return name;
    }

    public boolean isMoving() {
        if (isStill && vertMoveMode == VertMoveMode.NOT_JUMPING) {
            return false;
        } else {
            return true;
        }
    }

    public void setFallSize(int fs) {
        fallSize = fs;
    }

    public void stopJump() {
        if (vertMoveMode == VertMoveMode.JUMPING) {
            setVertMoveMode(VertMoveMode.FALLING);
        }
    }

    public void setCurrentResourceType(ResourceType rt) {
        currentResourceType = rt;
    }

    @Override
    public void attack() {
        if (actionMode != ActionMode.ATTACKING && attackRefreshTimerEnd < System.currentTimeMillis()) {
            meleeAttack();
        }
    }

    @Override
    public void meleeAttack(WeaponType newWeaponType, int level) {
        meleeAttack();
    }

    public void meleeAttack() {
        int kbX = 20;
        int kbY = 5;
        int damage = (player.getAttackBonus() * 2);
        int maxHits = 2;
        int weaponSpeed = 600;

        /*
         * if (newWeaponType != null) { int[] damages =
         * newWeaponType.getDamage();
         *
         * kbX = newWeaponType.getKnockBackX(); kbY =
         * newWeaponType.getKnockBackY(); damage += damages[level]; maxHits =
         * newWeaponType.getMaxHits(); weaponSpeed = newWeaponType.getSpeed(); }
         */

        isSwinging = true;
        actionMode = ActionMode.ATTACKING;
        attackRefreshTimerStart = System.currentTimeMillis();
        attackRefreshTimerEnd = System.currentTimeMillis() + weaponSpeed;

        attackArc = getAttackArc();
        if (facing == Facing.LEFT) {
            kbX = -1 * kbX;
        }

        String itemName = "";
        /*
         * if (newWeaponType != null) { itemName = newWeaponType.getItemName();
         * }
         */
        playerManager.attackDamageAndKnockBack(this, attackArc, null, damage, kbX, kbY, maxHits, itemName);
    }

    @Override
    public int applyDamage(int damage, Actor a) {
        if (damage <= 0) {
            return 0;
        }

        damage = getAdjustedDamage(damage);

        registerAttacker(a, damage);

        if (damage <= 0) {
            damage = 1;
        }

        if (damage > 0) {
            SoundClip cl = new SoundClip("Player/Hurt" + Rand.getRange(1, 5));
            registry.getIndicatorManager().createIndicator(mapX + (width / 2), mapY + 50, "-" + Integer.toString(damage));
            if (hitPoints - damage < 0) {
                hitPoints = 0;
            } else {
                hitPoints -= damage;
            }
            invulnerable = true;
        }

        if (registry.getGameController().multiplayerMode == registry.getGameController().multiplayerMode.SERVER && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(this.getId());
                up.action = "ApplyDamage";
                up.dataInt = damage;
                up.actor = a;
                registry.getNetworkThread().sendData(up);
            }
        }

        return damage;
    }

    public int getAdjustedDamage(int damage) {
        damage -= Math.floor(getArmorPoints() / 5);
        return damage;
    }

    @Override
    public boolean getIsFollowing() {
        return true;
    }

    protected void createMeleeFrames() {
        AffineTransform tx = null;
        AffineTransformOp op = null;

        meleeImages = null;
        meleeImages = new BufferedImage[16];
        meleeImages[0] = null;

        /*
         * if (meleeWeaponType != null) { BufferedImage im =
         * registry.getImageLoader().getImage("Attachments/" +
         * meleeWeaponType.getItemName());
         *
         * meleeAnimationFrameDuration = 0.05; numMeleeAnimationFrames = 8;
         * currentMeleeAnimationFrame = 0;
         *
         * //right for (int i = 0; i < 8; i++) { int rotation = i * 45;
         *
         * tx = new AffineTransform(); tx.rotate(Math.toRadians(rotation),
         * im.getWidth() / 2, im.getHeight() / 2);
         *
         * op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
         * meleeImages[i] = op.filter(im, null); }
         *
         * //left for (int i = 8; i < 16; i++) { int rotation = i * 45;
         * rotation += 180;
         *
         * tx = new AffineTransform(); tx.rotate(Math.toRadians(-rotation),
         * im.getWidth() / 2, im.getHeight() / 2);
         *
         * op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
         * meleeImages[i] = op.filter(im, null); } }
         */
    }

    public long getLastMove() {
        System.out.println(lastMove);
        return lastMove;
    }

    @Override
    public void update() {
        super.update();

        //update animation
        if (isActive && isAnimating && actionMode == ActionMode.ATTACKING) {
            currentAnimationFrame++;
            if (currentAnimationFrame >= 12) {
                currentAnimationFrame = 0;
                actionMode = ActionMode.NONE;
                updateImage();
            }
        } else if (isActive && isAnimating) {
            if (animationFrameUpdateTime <= registry.currentTime) {
                currentAnimationFrame++;
                if (currentAnimationFrame >= numAnimationFrames) {
                    currentAnimationFrame = 0;
                }
                animationFrameUpdateTime = registry.currentTime + animationFrameDuration;
            }
        }

        if (hitPoints > 0) {
            if (registry.getGameController().multiplayerMode != registry.getGameController().multiplayerMode.CLIENT) {
                if (knockBackX > 0) {
                    mapX += checkCollide(knockBackX);
                } else if (knockBackX < 0) {
                    mapX -= checkCollide(knockBackX);
                } else {
                    int oldMapX = mapX;
                    if (registry.getGameController().multiplayerMode == registry.getGameController().multiplayerMode.CLIENT) {
                        ai.process(true);
                    } else {
                        ai.process();
                    }
                    if (mapX != oldMapX) {
                        lastMove = registry.currentTime;
                    }
                    if (ai.getChanged()) {
                        ai.setChanged(false);
                        if (registry.getGameController().multiplayerMode == registry.getGameController().multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                            if (registry.getNetworkThread().readyForUpdates()) {
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
            }
        } else {
            if (registry.getGameController().multiplayerMode == registry.getGameController().multiplayerMode.SERVER && registry.getNetworkThread() != null) {
                if (registry.getNetworkThread().readyForUpdates()) {
                    UpdateMonster um = new UpdateMonster(this.getId());
                    um.mapX = this.getMapX();
                    um.mapY = this.getMapY();
                    um.action = "Die";
                    registry.getNetworkThread().sendData(um);
                }
            }

            SoundClip cl = new SoundClip(registry, "Monster/Die" + name, getCenterPoint());
            isDead = true;
            ai.terminate();

            BufferedImage im = registry.getImageLoader().getImage(image);
            if (facing == Facing.LEFT) {
                AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
                tx = AffineTransform.getScaleInstance(-1, 1);
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

        if (registry.getGameController().multiplayerMode != registry.getGameController().multiplayerMode.CLIENT) {
            if (vertMoveMode == VertMoveMode.JUMPING) {
                updateJumping();
            } else if (vertMoveMode == VertMoveMode.FLYING) {
                updateAscending();
            } else if (vertMoveMode == VertMoveMode.FALLING) {
                updateFalling();
            }

            mapX = playerManager.checkMapX(mapX, width);
            mapY = playerManager.checkMapY(mapY, height);
        }

        if (registry.getGameController().multiplayerMode != registry.getGameController().multiplayerMode.CLIENT) {
            if (vertMoveMode != VertMoveMode.FALLING) {
                checkIfFalling();
            }
        }

        if (leafEmitter != null) {
            leafEmitter.update();
        }
        if (disappearTime <= registry.currentTime) {
            SoundClip cl = new SoundClip(registry, "Oobaboo/Appear", getCenterPoint());
            die();
            player.oobabooDie();
        }

        updateImage();
    }

    public void updateLong() {
        if (nextSoundPlay <= registry.currentTime) {
            SoundClip cl = new SoundClip(registry, "Oobaboo/Oobaboo" + Rand.getRange(1, 5), getCenterPoint());
            nextSoundPlay = registry.currentTime + Rand.getRange(6000, 10000);
            doChat(2000);
        }
    }

    @Override
    public void render(Graphics g) {
        int offsetX = 0;
        int offsetY = 0;
        BufferedImage im;
        BufferedImage imLeft;
        AffineTransform tx;
        AffineTransformOp op;

        if (isAnimating) {
            im = registry.getImageLoader().getImage(image, currentAnimationFrame);
        } else {
            im = registry.getImageLoader().getImage(image);
        }

        int xPos = playerManager.mapToPanelX(mapX);
        int yPos = playerManager.mapToPanelY(mapY);

        //flip the yPos since drawing happens top down versus bottom up
        yPos = playerManager.getPHeight() - yPos;

        //subtract the height since points are bottom left and drawing starts from top left
        yPos -= height;

        if (im != null) {
            if (facing == Facing.LEFT) {
                tx = AffineTransform.getScaleInstance(1, -1);
                tx = AffineTransform.getScaleInstance(-1, 1);
                tx.translate(-width, 0);
                op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
                imLeft = op.filter(im, null);
                if (imLeft != null) {
                    g.drawImage(imLeft, xPos, yPos, null);
                }
            } else {
                g.drawImage(im, xPos, yPos, null);
            }
        }

        super.render(g);

        FontMetrics fm = g.getFontMetrics();
        int messageWidth = fm.stringWidth(name);

        xPos = playerManager.mapToPanelX((int) mapX + (width / 2) - (messageWidth / 2));
        yPos = playerManager.mapToPanelY((int) mapY);
        yPos = playerManager.getPHeight() - yPos;

        Font textFont = new Font("SansSerif", Font.BOLD, 14);
        g.setFont(textFont);

        registry.ghettoOutline(g, Color.BLACK, name, xPos, yPos - 50);

        g.setColor(Color.white);
        g.drawString(name,
                xPos,
                yPos - 50);

        if (actionMode == ActionMode.GATHERING && currentAnimationFrame == 0 && currentResourceType != null) {
            if (registry.currentTime - lastResourceSoundPlay > 500) {
                //only play if we haven't played for at least a half second
                lastResourceSoundPlay = registry.currentTime;
                SoundClip cl = new SoundClip(registry, "Player/Gather" + currentResourceType.getType() + Rand.getRange(1, 3), getCenterPoint());
            }
        }

        if (leafEmitter != null) {
            leafEmitter.render(g);
        }
    }

    public void stopAttacks() {
        actionMode = ActionMode.NONE;
        isSwinging = false;
        updateImage();
    }

    public UDPOobaboo createUpdate() {
        UDPOobaboo udpUpdate = new UDPOobaboo(id);
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
        udpUpdate.isSwinging = isSwinging;
        udpUpdate.currentResourceType = currentResourceType;

        return udpUpdate;
    }

    public void processUpdate(UDPOobaboo udpUpdate) {
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
        isSwinging = udpUpdate.isSwinging;
        currentResourceType = udpUpdate.currentResourceType;
    }

    private void readObject(ObjectInputStream aInputStream) throws IOException, ClassNotFoundException {
        aInputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.defaultWriteObject();
    }
}