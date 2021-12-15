package com.weem.epicinventor.actor;

import com.weem.epicinventor.*;
import com.weem.epicinventor.GameController.MultiplayerMode;
import com.weem.epicinventor.ai.*;
import com.weem.epicinventor.inventory.*;
import com.weem.epicinventor.actor.monster.*;
import com.weem.epicinventor.actor.oobaboo.*;
import com.weem.epicinventor.network.*;
import com.weem.epicinventor.placeable.*;
import com.weem.epicinventor.projectile.*;
import com.weem.epicinventor.resource.*;
import com.weem.epicinventor.utility.*;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.*;
import java.util.ArrayList;

public class PlayerManager extends Manager {

    private HashMap<String, Player> players;
    boolean isUpdating = false;
    private Player currentPlayer;
    private boolean currentPlayerSet = false;

    public PlayerManager(GameController gc, Registry rg) {
        super(gc, rg);

        players = new HashMap<String, Player>();

        Player p;

        p = new Player(this, registry, "Player/Standing", 226);
        players.put(p.getId(), p);
        setCurrentPlayer(p);
    }

    public void clearPlayers() {
        players = null;
        currentPlayer = null;
        players = new HashMap<String, Player>();
    }

    public void giveXP(Monster m) {
        if (registry.getGameController().multiplayerMode != registry.getGameController().multiplayerMode.CLIENT) {
            Player player = null;

            try {
                for (String key : players.keySet()) {
                    player = (Player) players.get(key);
                    player.addXP(m.getXPByPlayer(player));
                }
            } catch (ConcurrentModificationException concEx) {
                //another thread was trying to modify players while iterating
                //we'll continue and the new item can be grabbed on the next update
            }
        }
    }

    public void showOobabooHUD() {
        gameController.showOobabooHUD();
    }

    public void registerPlayer(Player p) {
        if (!players.containsKey(p.getId())) {
            players.put(p.getId(), p);
            if (players.size() == 1) {
                setCurrentPlayer(p);
            }
            if (p != null) {
                if (gameController.multiplayerMode != gameController.multiplayerMode.CLIENT && registry.getNetworkThread() != null) {
                    if (registry.getNetworkThread().readyForUpdates()) {
                        registry.getNetworkThread().sendData(p);
                    }
                }
            }
        }
    }

    public void setCurrentPlayer(Player p) {
        currentPlayer = p;
        currentPlayerSet = true;
    }

    public boolean getCurrentPlayerSet() {
        return currentPlayerSet;
    }

    public void removePlayer(String playerId) {
        if (players.containsKey(playerId)) {
            Player p = players.get(playerId);
            if (p != null) {
                if (gameController.multiplayerMode != gameController.multiplayerMode.CLIENT && registry.getNetworkThread() != null) {
                    if (registry.getNetworkThread().readyForUpdates()) {
                        UpdatePlayer up = new UpdatePlayer(p.getId());
                        up.name = p.getName();
                        up.action = "Remove";
                        registry.getNetworkThread().sendData(up);
                    }
                }
            }
            players.remove(playerId);
        }
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public HashMap<String, Player> getPlayers() {
        return players;
    }

    public void stopActions() {
        gameController.stopActions(currentPlayer);
    }

    public void stopActions(Player p) {
        gameController.stopActions(p);
    }

    public Inventory getRobotInventory() {
        return currentPlayer.getRobotInventory();
    }

    public int getRobotInventorySize() {
        return currentPlayer.getRobotInventorySize();
    }

    public Player getPlayerById(String id) {
        if (players.containsKey(id)) {
            Player player = players.get(id);
            return player;
        } else {
            return null;
        }
    }

    public Player getClosestPlayer(Point p, int maxDistance) {
        Player player = null;
        Player closestPlayer = null;

        double closestDistance = 0;

        try {
            for (String key : players.keySet()) {
                player = (Player) players.get(key);
                if (p.distance(player.getCenterPoint()) < closestDistance || closestDistance == 0) {
                    closestPlayer = player;
                    closestDistance = p.distance(player.getCenterPoint());
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify players while iterating
            //we'll continue and the new item can be grabbed on the next update
        }

        if (closestPlayer != null && closestDistance <= maxDistance) {
            return closestPlayer;
        }

        return null;
    }

    public boolean isInPlayerView(Point p) {
        Player player = null;

        if (p != null) {
            try {
                for (String key : players.keySet()) {
                    player = (Player) players.get(key);
                    if (p.x >= gameController.getMapOffsetX()
                            && p.x <= gameController.getMapOffsetX() + gameController.getPWidth()
                            && p.y >= gameController.getMapOffsetY()
                            && p.y <= gameController.getMapOffsetY() + gameController.getPHeight()) {
                        return true;
                    }
                }
            } catch (ConcurrentModificationException concEx) {
                //another thread was trying to modify players while iterating
                //we'll continue and the new item can be grabbed on the next update
            }
        }

        return false;
    }

    public void playerMoveLeft() {
        playerMoveLeft(currentPlayer);
    }

    public void playerMoveLeft(Player p) {
        if (registry.getGameController().multiplayerMode != registry.getGameController().multiplayerMode.CLIENT) {
            p.moveLeft();
        }
    }

    public void playerMoveRight() {
        playerMoveRight(currentPlayer);
    }

    public void playerMoveRight(Player p) {
        if (registry.getGameController().multiplayerMode != registry.getGameController().multiplayerMode.CLIENT) {
            p.moveRight();
        }
    }

    public void playerStopMove() {
        playerStopMove(currentPlayer);
    }

    public void playerStopMove(Player p) {
        if (registry.getGameController().multiplayerMode != registry.getGameController().multiplayerMode.CLIENT) {
            p.stopMove();
        }
    }

    public void playerShowRect(Boolean r) {
        currentPlayer.setShowRect(r);
    }

    public void playerStartGather(String rt) {
        playerStartGather(currentPlayer, rt);
    }

    public void playerStartGather(Player p, String rt) {
        p.startGather(rt);
    }

    public boolean playerGoInsideRobot() {
        boolean ret = currentPlayer.setInsideRobot(true);

        return ret;
    }

    public boolean isPlayerInsideRobot() {
        return isPlayerInsideRobot(currentPlayer);
    }

    public boolean isPlayerInsideRobot(Player p) {
        return p.getInsideRobot();
    }

    public void playerGetOutOfRobot() {
        currentPlayer.setInsideRobot(false);
    }

    public boolean playerToggleInsideRobot() {
        return playerToggleInsideRobot(currentPlayer);
    }

    public boolean playerToggleInsideRobot(Player p) {
        boolean ret;
        if (p.getInsideRobot()) {
            ret = p.setInsideRobot(false);
        } else {
            ret = p.setInsideRobot(true);
        }

        return ret;
    }

    public void playerEquipHead(String armorName, int level) {
        currentPlayer.setArmorTypeHead(armorName, level);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.inventory = currentPlayer.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }
    }

    public void playerEquipChest(String armorName, int level) {
        currentPlayer.setArmorTypeChest(armorName, level);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.inventory = currentPlayer.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }
    }

    public void playerEquipLegs(String armorName, int level) {
        currentPlayer.setArmorTypeLegs(armorName, level);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.inventory = currentPlayer.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }
    }

    public void playerEquipFeet(String armorName, int level) {
        currentPlayer.setArmorTypeFeet(armorName, level);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.inventory = currentPlayer.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }
    }

    public void handleClick(Point clickPoint) {
        currentPlayer.handleClick(clickPoint);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.action = "HandleClick";
                up.dataPoint = clickPoint;
                registry.getNetworkThread().sendData(up);
            }
        }
    }

    public void handleRightClick() {
        currentPlayer.handleRightClick();
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.action = "HandleRightClick";
                registry.getNetworkThread().sendData(up);
            }
        }
    }

    public void handleReleased(Point clickPoint) {
        currentPlayer.handleReleased(clickPoint);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.action = "HandleReleased";
                up.dataPoint = clickPoint;
                registry.getNetworkThread().sendData(up);
            }
        }
    }

    public ArrayList<String> attackDamageAndKnockBack(Actor source, Arc2D.Double arc, Point mapPoint, int damage, int knockBackX, int knockBackY, int maxHits, String weaponType) {
        return gameController.attackDamageAndKnockBack(source, arc, mapPoint, damage, knockBackX, knockBackY, maxHits, 0, weaponType);
    }

    public void playerRender(Graphics g, int x, int y, boolean imageOverride) {
        currentPlayer.renderPlayer(g, x, y, imageOverride);
    }

    @Override
    public int playerAddItem(String name, int qty) {
        int ret = playerAddItem(0, name, qty);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.inventory = currentPlayer.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }

        return ret;
    }

    public int playerAddItem(Player p, String name, int qty) {
        int ret = playerAddItem(p, 0, name, qty);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(p.getId());
                up.name = p.getName();
                up.inventory = p.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }

        return ret;
    }

    public int playerAddItem(int slot, String name, int qty) {
        int ret = currentPlayer.playerAddItem(slot, name, qty);
        if (gameController.multiplayerMode != MultiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.inventory = currentPlayer.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }
        registry.getHUDManager().updateMasterHUD();

        return ret;
    }

    public int playerAddItem(Player p, int slot, String name, int qty) {
        int ret = p.playerAddItem(slot, name, qty);
        if (gameController.multiplayerMode != MultiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(p.getId());
                up.name = p.getName();
                up.inventory = p.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }

        return ret;
    }

    public int playerAddItem(int slot, String name, int qty, int level) {
        int ret = currentPlayer.playerAddItem(slot, name, qty, level);
        if (gameController.multiplayerMode != MultiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.inventory = currentPlayer.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }
        registry.getHUDManager().updateMasterHUD();

        return ret;
    }

    public int playerAddItem(Player p, int slot, String name, int qty, int level) {
        int ret = p.playerAddItem(slot, name, qty, level);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(p.getId());
                up.name = p.getName();
                up.inventory = p.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }

        return ret;
    }

    @Override
    public boolean checkPlayerProjectileHit(Projectile p) {
        if (gameController.multiplayerMode != gameController.multiplayerMode.CLIENT) {
            try {
                for (String key : players.keySet()) {
                    Player player = (Player) players.get(key);
                    if (player != null) {
                        if (player.getRobot().getIsActivated() && player.getRobot().getPerimeter().contains(p.getCenterPoint())) {
                            player.getRobot().applyDamage(p.getDamage(), p.getSource());
                            return true;
                        }
                        if (player.getPerimeter().contains(p.getCenterPoint())) {
                            player.applyDamage(p.getDamage(), p.getSource());
                            if (p.getName().equals("Goo")) {
                                player.slow(2);
                            } else if (p.getName().equals("Web")) {
                                player.slow(3);
                            }
                            return true;
                        }
                    }
                }
            } catch (ConcurrentModificationException concEx) {
                //another thread was trying to modify players while iterating
                //we'll continue and the new item can be grabbed on the next update
            }
        }

        return false;
    }

    @Override
    public String playerGetInventoryItemCategory(int slot) {
        return currentPlayer.playerGetInventoryItemCategory(slot);
    }

    @Override
    public String playerGetInventoryItemName(int slot) {
        return currentPlayer.playerGetInventoryItemName(slot);
    }

    @Override
    public int playerGetInventoryQty(int slot) {
        return currentPlayer.playerGetInventoryQty(slot);
    }

    @Override
    public int playerGetInventoryLevel(int slot) {
        return currentPlayer.playerGetInventoryLevel(slot);
    }

    public Inventory getPlayerInventory() {
        return currentPlayer.getInventory();
    }

    public void playerDeleteInventory(int slot, int qty) {
        gameController.playerDeleteInventory(slot, qty, false);
    }

    public void playerDeleteInventory(int slot, int qty, boolean giveXP) {
        currentPlayer.playerDeleteInventory(slot, qty, giveXP);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.inventory = currentPlayer.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }
    }

    public void setPlayerSlotQuantity(int slot, int qty) {
        currentPlayer.setPlayerSlotQuantity(slot, qty);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.inventory = currentPlayer.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }
    }

    public void setPlayerSelectedItem(int i) {
        currentPlayer.setPlayerSelectedItem(i);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.action = "SetSelectedItem";
                up.dataInt = i;
                registry.getNetworkThread().sendData(up);
            }
        }
    }

    public void setPlayerNames(String characterName, String robotName) {
        currentPlayer.setName(characterName);
        currentPlayer.setRobotName(robotName);
    }

    public void setUpdating(boolean u) {
        isUpdating = u;
    }

    public void initPlayers() {
        try {
            for (String key : players.keySet()) {
                Player p = (Player) players.get(key);
                if (p != null) {
                    p.init();
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify players while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public void playerSwapInventory(int from, int to) {
        currentPlayer.playerSwapInventory(from, to);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.inventory = currentPlayer.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }
    }
    
    public void playerStopLoopingSound() {
        currentPlayer.playerStopLoopingSound();
    }
    
    public void playerEquipFromInventory(int slot) {
        currentPlayer.playerEquipFromInventory(slot);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.action = "Equip";
                up.dataInt = slot;
                registry.getNetworkThread().sendData(up);
            }
        }
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.inventory = currentPlayer.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }
    }

    public void playerDied(Rectangle area) {
        gameController.playerDied(area);
    }

    public void playerUnEquipToInventory(String equipmentType, int to) {
        currentPlayer.playerUnEquipToInventory(equipmentType, to);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.action = "UnEquip";
                up.dataString = equipmentType;
                registry.getNetworkThread().sendData(up);
            }
        }
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.inventory = currentPlayer.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }
    }

    public void playerUnEquipToDelete(String equipmentType) {
        currentPlayer.playerUnEquipToDelete(equipmentType);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.action = "UnEquip";
                up.dataString = equipmentType;
                registry.getNetworkThread().sendData(up);
            }
        }
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.inventory = currentPlayer.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }
    }

    public void playerCraftItem(String itemType) {
        currentPlayer.playerCraftItem(itemType);
        if (gameController.multiplayerMode != gameController.multiplayerMode.NONE && registry.getNetworkThread() != null) {
            if (registry.getNetworkThread().readyForUpdates()) {
                UpdatePlayer up = new UpdatePlayer(currentPlayer.getId());
                up.name = currentPlayer.getName();
                up.inventory = currentPlayer.getInventory();
                up.action = "InventoryUpdate";
                registry.getNetworkThread().sendData(up);
            }
        }
    }

    public void robotSetMode(String m) {
        currentPlayer.robotSetMode(m);
    }

    public void robotToggleActivated() {
        robotToggleActivated(currentPlayer);
    }

    public void robotToggleActivated(Player p) {
        if (registry.getGameController().multiplayerMode != registry.getGameController().multiplayerMode.CLIENT) {
            p.robotToggleActivated();
        }
    }

    public void robotToggleFollow() {
        currentPlayer.robotToggleFollow();
    }

    public ArrayList<String> getItemTypeRequirements(String n) {
        return gameController.getItemTypeRequirements(n);
    }

    public void playerStopGather() {
        playerStopGather(currentPlayer);
    }

    public void playerStopGather(Player p) {
        if (registry.getGameController().multiplayerMode != registry.getGameController().multiplayerMode.CLIENT) {
            p.stopGather();
        }
    }

    public void playerStopJump() {
        playerStopJump(currentPlayer);
    }

    public void playerStopJump(Player p) {
        if (registry.getGameController().multiplayerMode != registry.getGameController().multiplayerMode.CLIENT) {
            p.stopJump();
            p.stopAscend();
        }
    }

    public void playerJump() {
        playerJump(currentPlayer);
    }

    public void playerJump(Player p) {
        if (registry.getGameController().multiplayerMode != registry.getGameController().multiplayerMode.CLIENT) {
            p.jump();
        }
    }

    public boolean isPlayerPerformingAction() {
        return isPlayerPerformingAction(currentPlayer);
    }

    public boolean isPlayerPerformingAction(Player p) {
        return p.isPlayerPerformingAction();
    }

    public boolean isPlayerMoving() {
        return isPlayerMoving(currentPlayer);
    }

    public boolean isPlayerMoving(Player p) {
        return p.isPlayerMoving();
    }

    public Placeable loadPlaceable(String n, int x, int y) {
        return gameController.loadPlaceable(n, x, y);
    }

    public void cancelPlaceable(Player p) {
        gameController.cancelPlaceable(p);
    }

    @Override
    public boolean playerStandingOnTownBlocks() {
        boolean ret = true;
        boolean allNull = true;
        short[] standingBlocks = gameController.blocksUnder(currentPlayer.getMapX() + currentPlayer.getBaseOffset(), currentPlayer.getMapX() + currentPlayer.getWidth() - currentPlayer.getBaseOffset(), currentPlayer.getMapY());
        for (int i = 0; i < standingBlocks.length; i++) {
            if (!gameController.isIdInGroup(standingBlocks[i], "None")) {
                allNull = false;
            }
            if (!(gameController.isIdInGroup(standingBlocks[i], "Town"))) {
                ret = false;
            }
        }
        if (allNull == true) {
            ret = false;
        }
        return ret;
    }

    public int[] getTownStartEndUnderPlayer() {
        return gameController.getTownStartEnd(currentPlayer.getCenterPoint().x, currentPlayer.getMapY());
    }

    public Damage getMonsterTouchDamage(Rectangle r, int x) {
        return gameController.getMonsterTouchDamage(r, x);
    }

    public Point getCenterPoint() {
        return currentPlayer.getCenterPoint();
    }

    @Override
    public void stunPlayersOnGround(long duration) {
        try {
            for (String key : players.keySet()) {
                Player p = (Player) players.get(key);
                if (p != null) {
                    if (p.getVertMoveMode() == Actor.VertMoveMode.NOT_JUMPING) {
                        p.setStatusStun(true, duration);
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify players while iterating
            //we'll continue and the new item can be grabbed on the next update
        }

        setCamera();
    }

    public void setCamera() {
        if (currentPlayer != null) {
            if (currentPlayer.getCameraReturning()) {
                boolean xGood = false;

                if (currentPlayer.cameraMoveSize < 350) {
                    if (currentPlayer.cameraMoveSize == 0) {
                        currentPlayer.cameraMoveSize = 0.25;
                    }
                    currentPlayer.cameraMoveSize *= 1.05;
                }

                if (Math.abs(currentPlayer.cameraX - currentPlayer.mapX) > 200) {
                    if (currentPlayer.cameraX > currentPlayer.mapX) {
                        currentPlayer.cameraX -= currentPlayer.cameraMoveSize;
                    } else {
                        currentPlayer.cameraX += currentPlayer.cameraMoveSize;
                    }
                } else {
                    currentPlayer.cameraX = currentPlayer.mapX;
                    xGood = true;
                }

                if (Math.abs(currentPlayer.cameraY - currentPlayer.mapY) > 200) {
                    if (currentPlayer.cameraY > currentPlayer.mapY) {
                        currentPlayer.cameraY -= currentPlayer.cameraMoveSize;
                    } else {
                        currentPlayer.cameraY += currentPlayer.cameraMoveSize;
                    }
                } else {
                    currentPlayer.cameraY = currentPlayer.mapY;
                    if (xGood) {
                        currentPlayer.setCameraReturning(false);
                    }
                }

                centerCameraOnPoint(new Point((int) currentPlayer.cameraX, (int) currentPlayer.cameraY));
            } else {
                centerCameraOnPoint(new Point(currentPlayer.mapX, currentPlayer.mapY));
            }
        }
    }

    @Override
    public void update() {
        super.update();

        try {
            for (String key : players.keySet()) {
                Player p = (Player) players.get(key);
                if (p != null) {
                    p.update();
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify players while iterating
            //we'll continue and the new item can be grabbed on the next update
        }

        //need to run this after the "smoothing" code is ran, or perhaps queue up toe udp updates and only process 1 per local update?
        setCamera();
    }

    @Override
    public void updateLong() {
        super.updateLong();

        try {
            for (String key : players.keySet()) {
                Player p = (Player) players.get(key);
                if (p != null) {
                    p.updateLong();
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify players while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public void processRobotUpdateUDP(UDPRobot up) {
        try {
            for (String key : players.keySet()) {
                Player p = (Player) players.get(key);
                if (p != null) {
                    Robot r = p.getRobot();
                    if (r != null) {
                        if (r.getId().equals(up.id)) {
                            r.processUpdate(up);
                            return;
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify players while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public void assignOobaboo(Oobaboo o) {
        if (o != null) {
            Player p = this.getPlayerById(o.getPlayerID());
            if (p != null) {
                p.setOobaboo(o);
            }
        }
    }

    public void processOobabooUpdateUDP(UDPOobaboo uo) {
        try {
            for (String key : players.keySet()) {
                Player p = (Player) players.get(key);
                if (p != null) {
                    Oobaboo o = p.getOobaboo();
                    if (o != null) {
                        if (o.getId().equals(uo.id)) {
                            o.processUpdate(uo);
                            return;
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify players while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }

    public void processPlayerUpdateUDP(UDPPlayer up) {
        if (up != null) {
            if (players.containsKey(up.id)) {
                Player player = players.get(up.id);
                if (player != null) {
                    player.processUpdate(up);
                }
            }
        }
    }

    public void processKeysUpdateUDP(UDPKeys uk) {
        if (uk != null) {
            if (players.containsKey(uk.id)) {
                Player player = players.get(uk.id);
                if (player != null) {
                    if (player.keyState == null) {
                        player.keyState = uk;
                    } else {
                        if (uk.keyLeftPressed != player.keyState.keyLeftPressed) {
                            if (uk.keyLeftPressed) {
                                stopActions(player);
                                playerMoveLeft(player);
                            } else {
                                playerStopMove(player);
                            }
                        }
                        if (uk.keyRightPressed != player.keyState.keyRightPressed) {
                            if (uk.keyRightPressed) {
                                stopActions(player);
                                playerMoveRight(player);
                            } else {
                                playerStopMove(player);
                            }
                        }
                        if (uk.keySpacePressed != player.keyState.keySpacePressed) {
                            if (uk.keySpacePressed) {
                                stopActions(player);
                                playerJump(player);
                            } else {
                                playerStopJump(player);
                            }
                        }
                        if (uk.keyGatherPressed != player.keyState.keyGatherPressed) {
                            if (uk.keyGatherPressed) {
                                stopActions(player);
                                gameController.startGather(player);
                            } else {
                                stopActions(player);
                            }
                        }
                        if (uk.keyRobotPressed != player.keyState.keyRobotPressed) {
                            if (uk.keyRobotPressed) {
                                robotToggleActivated(player);
                            }
                        }
                        player.keyState = uk;
                    }
                }
            }
        }
    }

    public void processPlayerUpdate(UpdatePlayer up) {
        if (up != null) {
            if (players.containsKey(up.id)) {
                Player player = players.get(up.id);
                if (player != null) {
                    EIError.debugMsg("Setting " + up.name + " Action: " + up.action);
                    if (up.action.equals("ApplyDamage")) {
                        player.applyDamage(up.dataInt, up.actor);
                    } else if (up.action.equals("AddXP")) {
                        SoundClip cl = new SoundClip("Player/Good");
                        player.addXP(up.dataInt);
                    } else if (up.action.equals("CollectedResource")) {
                        Resource resource = getResourceById(up.dataString);
                        registry.getResourceManager().resourceDoneCollecting(player, resource);
                    } else if (up.action.equals("Equip")) {
                        player.playerEquipFromInventory(up.dataInt);
                    } else if (up.action.equals("HandleClick")) {
                        player.handleClick(up.dataPoint);
                    } else if (up.action.equals("HandleRightClick")) {
                        player.handleRightClick();
                    } else if (up.action.equals("HandleReleased")) {
                        player.handleReleased(up.dataPoint);
                    } else if (up.action.equals("InventoryUpdate")) {
                        Inventory i = up.inventory;
                        i.setTransient(registry);

                        Player p = this.getPlayerById(up.id);

                        if (p == currentPlayer) {
                            registry.setInventory(i);
                        }

                        player.setInventory(i);
                    } else if (up.action.equals("ScrollQuickBar")) {
                        player.scrollQuickBar(up.dataInt);
                    } else if (up.action.equals("SetSelectedItem")) {
                        player.setPlayerSelectedItem(up.dataInt);
                    } else if (up.action.equals("Remove")) {
                        removePlayer(player.id);
                    } else if (up.action.equals("UnEquip")) {
                        player.unEquip(up.dataString);
                    }
                }
            }
        }
    }

    public void processRobotUpdate(UpdateRobot ur) {
        if (ur != null) {
            if (players.containsKey(ur.playerId)) {
                Player player = players.get(ur.playerId);
                if (player != null) {
                    if (ur.previousGoal != null) {
                        Goal g = ur.previousGoal;
                        g.setTransient(registry, player.getRobot().ai);
                        player.getRobot().ai.setPreviousGoal(g);
                    }
                    if (ur.currentGoal != null) {
                        Goal g = ur.currentGoal;
                        g.setTransient(registry, player.getRobot().ai);
                        player.getRobot().ai.setCurrentGoal(g);
                    }
                    if (ur.action.equals("ApplyDamage")) {
                        Actor a = ur.actor;
                        if (a != null) {
                            a.setTransient(registry, registry.getPlayerManager());
                        }
                        player.getRobot().applyDamage(ur.dataInt, a);
                    }
                }
            }
        }
    }

    public void processOobabooUpdate(UpdateOobaboo uo) {
        if (uo != null) {
            if (players.containsKey(uo.playerId)) {
                Player player = players.get(uo.playerId);
                if (player != null) {
                }
            }
        }
    }

    public void renderPointer(Graphics g, Player p) {
        int xPos = 0;
        int yPos = 0;

        if (p.getSpriteRect() != null) {
            if (!p.getSpriteRect().intersects(getPanelRect())) {
                //render arrow
                yPos = mapToPanelY(p.getMapY());
                yPos = getPHeight() - yPos - 30;

                if (yPos < 10) {
                    yPos = 10;
                }
                if (yPos > getPHeight() - 165) {
                    yPos = getPHeight() - 165;
                }

                if (p.getMapX() < currentPlayer.getMapX()) {
                    xPos = 3;
                    g.drawImage(registry.getImageLoader().getImage("Misc/ArrowLeft"), xPos, yPos, null);
                } else {
                    xPos = getPWidth() - 30 - 3;
                    g.drawImage(registry.getImageLoader().getImage("Misc/ArrowRight"), xPos, yPos, null);
                }

                //render player name
                FontMetrics fm = g.getFontMetrics();
                int messageWidth = fm.stringWidth(p.getName());
                if (p.getMapX() < currentPlayer.getMapX()) {
                    xPos = 33;
                } else {
                    xPos = getPWidth() - 40 - 10 - messageWidth;
                }

                yPos += 18;

                Font textFont = new Font("SansSerif", Font.BOLD, 14);
                g.setFont(textFont);

                registry.ghettoOutline(g, Color.BLACK, p.getName(), xPos, yPos);

                g.setColor(Color.white);
                g.drawString(p.getName(),
                        xPos,
                        yPos);
            }
        }
    }

    public void renderPointer(Graphics g, Oobaboo o) {
        int xPos = 0;
        int yPos = 0;

        if (o.getSpriteRect() != null) {
            if (!o.getSpriteRect().intersects(getPanelRect())) {
                //render arrow
                yPos = mapToPanelY(o.getMapY());
                yPos = getPHeight() - yPos - 30;

                if (yPos < 10) {
                    yPos = 10;
                }
                if (yPos > getPHeight() - 165) {
                    yPos = getPHeight() - 165;
                }

                if (o.getMapX() < currentPlayer.getMapX()) {
                    xPos = 3;
                    g.drawImage(registry.getImageLoader().getImage("Misc/ArrowLeft"), xPos, yPos, null);
                } else {
                    xPos = getPWidth() - 30 - 3;
                    g.drawImage(registry.getImageLoader().getImage("Misc/ArrowRight"), xPos, yPos, null);
                }

                //render player name
                FontMetrics fm = g.getFontMetrics();
                int messageWidth = fm.stringWidth(o.getName());
                if (o.getMapX() < currentPlayer.getMapX()) {
                    xPos = 33;
                } else {
                    xPos = getPWidth() - 40 - 10 - messageWidth;
                }

                yPos += 18;

                Font textFont = new Font("SansSerif", Font.BOLD, 14);
                g.setFont(textFont);

                registry.ghettoOutline(g, Color.BLACK, o.getName(), xPos, yPos);

                g.setColor(Color.white);
                g.drawString(o.getName(),
                        xPos,
                        yPos);
            }
        }
    }
    
    public void renderMiniMapPlayers(Graphics g, int mx, int my, int cx, int cy, int w, int h, int x, int y) {
        HashMap<String, Player> ps = new HashMap<String, Player>(players);
        Player player = null;
        int[] xy = null;
        
        try {
            for (String key : ps.keySet()) {
                player = (Player) ps.get(key);
                xy = registry.getBlockManager().getMiniMapPosition(mx, my, cx, cy, w, h, player.getMapX()/this.gameController.getBlockWidth(), player.getMapY()/this.gameController.getBlockHeight());
                if((xy[0] > mx && xy[0] < mx+w) && (xy[1] > my+1 && xy[1] < my+h)) {
                    renderMiniMapPlayer(g, xy[0], xy[1]);
                }
            }
        } catch (Exception e) {
        }
    }

    private void renderMiniMapPlayer(Graphics g, int x, int y) {
        g.setColor(Color.black);
        g.fillRect(x-2, y-7, 4, 6);
        g.setColor(Color.white);
        g.fillRect(x-1, y-6, 2, 4);
    }
    
    public void render(Graphics g) {
        try {
            for (String key : players.keySet()) {
                Player p = (Player) players.get(key);
                if (p != null) {
                    p.render(g);
                    if (p != currentPlayer) {
                        renderPointer(g, p);
                    }
                    Oobaboo o = p.getOobaboo();
                    if (o != null) {
                        renderPointer(g, o);
                    }
                }
            }
        } catch (ConcurrentModificationException concEx) {
            //another thread was trying to modify players while iterating
            //we'll continue and the new item can be grabbed on the next update
        }
    }
}
