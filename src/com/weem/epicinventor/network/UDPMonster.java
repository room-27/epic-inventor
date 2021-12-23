package com.weem.epicinventor.network;

import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.armor.*;

import java.io.*;

public class UDPMonster implements Serializable {

    protected static final long serialVersionUID = 10000L;
    public String id = "";
    public int mapX, lastMapY, mapY;
    public int width, height;
    public int xMoveSize;
    //public boolean stateChanged;
    //public String image = "";
    //public int numAnimationFrames;
    //public int currentAnimationFrame;
    //public int animationFrameDuration;
    //public long animationFrameUpdateTime = 0;
    //public final static double DEFAULT_ANIMATION_DURATION = 0.20;
    //public boolean isAnimating;
    //public boolean isActive;
    public Player.ActionMode actionMode;
    public Player.VertMoveMode vertMoveMode;
    public Player.Facing facing;
    //public AttackType currentAttackType;
    //public int topOffset;
    public int jumpSize;
    public int ascendOriginalSize;
    public int ascendSize;
    public int ascendCount;
    public int ascendMax;
    public int fallSize;
    //public int baseOffset;
    //public int baseWidth;
    public boolean isStill;
    public boolean isTryingToMove;
    //public boolean isStomping;
    public int startJumpSize;
    public int maxFallSize;
    public int gravity;
    public int totalFall;
    public int completeFall;
    public int totalHitPoints;
    public int hitPoints;
    public int totalArmorPoints;
    public int armorPoints;
    //public int baseHitPoints;
    //public int baseArmorPoints;
    //public int spriteRectOffestX, spriteRectOffestY;
    //public boolean showRect;
    //public boolean showGoals;
    public int knockBackX;
    public boolean isDead;
    //public boolean isFeared;
    //public boolean isSlowed;
    //public boolean isPoisoned;
    //public Point fearedSource;
    //public long fearedDuration;
    //public long fearedTotalTime;
    //public long slowedDuration;
    //public long slowedTotalTime;
    //public long poisonedDuration;
    //public long poisonedTotalTime;
    //public int attackRange;
    //public long attackRefreshTimerStart;
    //public long attackRefreshTimerEnd;
    //public int maxFollowDistance;
    //public boolean statusAttackBonus;
    //public boolean statusFear;
    //public boolean statusHeal;
    //public boolean statusPoison;
    //public boolean statusStun;
    //public boolean statusRezSickness;
    //public boolean statusSlowed;
    //public boolean canFly;
    //public SoundClip loopingSound;
    //public boolean disregardKnockBack;
    //public long stunEnded = 0;
    //public String debugInfo;
    //public long chatEnd = 0;
    //public boolean isChatting;
    //public boolean projectileOut;
    //public DropChanceCollection dropChances;
    //public AI ai;
    //public String name;
    //public String displayName;
    //public String spawnType;
    //public int maxAggroRange;
    public int touchDamage;
    public int meleeDamage;
    public int meleeSpeed;
    public float mapXMin;
    public float mapXMax;
    //public boolean isInPanel;
    public int groundLevel;
    public boolean isDirty;
    //public long nextSoundPlay;
    public int playerDamage;
    public int placeableDamage;
    public int level;
    //public boolean hideDisplayName;
    //public float difficultyFactor;
    public boolean isHiding;

    public UDPMonster(String pid) {
        id = pid;
    }

    private void readObject(ObjectInputStream aInputStream) throws IOException, ClassNotFoundException {
        aInputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.defaultWriteObject();
    }
}
