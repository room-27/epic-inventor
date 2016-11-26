package com.weem.epicinventor.network;

import com.weem.epicinventor.*;
import com.weem.epicinventor.actor.*;
import com.weem.epicinventor.actor.monster.*;
import com.weem.epicinventor.hud.*;
import com.weem.epicinventor.actor.oobaboo.*;
import com.weem.epicinventor.placeable.*;
import com.weem.epicinventor.resource.*;
import com.weem.epicinventor.utility.*;
import com.weem.epicinventor.world.block.*;

import java.io.*;
import java.net.*;

public class TCPClient extends Thread {

    private Registry registry;
    private GameController gameController;
    private Socket socket;
    private String ip;
    private int port;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private TCPClientBlockManagerDataReceive blockManagerReceive;
    protected int currentStartCollumn;
    public boolean keepRunning = true;
    public boolean readyForUpdates = false;
    public ClientReceiveThread clientReceiveThread;
    protected static int collumnChunkSize;
    protected int udpPort = 5555;

    public TCPClient(Registry r, GameController gc, String i, int p) {
        registry = r;
        gameController = gc;
        ip = i;
        port = p;
        currentStartCollumn = 0;
        collumnChunkSize = 50;
    }

    @Override
    public void run() // read server messages and act on them.
    {
        doClient();

        if (clientReceiveThread != null) {
            clientReceiveThread.setRunning(false);
        }

        return;
    }

    private void doClient() {
        Object data;
        String[] parts;

        gameController.setLoading(true);
        registry.getHUDManager().loadHUD(HUDManager.HUDType.ScreenLoading);

        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
            gameController.setLoading(false);
            registry.getHUDManager().unloadHUD("ScreenLoading");
            gameController.showMessage("Error", "Couldn't connect to " + ip + ":" + port);
            return;
        }

        gameController.setNetworkMode(true);
        gameController.multiplayerMode = gameController.multiplayerMode.CLIENT;

        try {
            Game.loadingText = "Establishing Connection";
            EIError.debugMsg("Creating output stream");

            output = new ObjectOutputStream(socket.getOutputStream());

            EIError.debugMsg("Creating input stream");
            input = new ObjectInputStream(socket.getInputStream());

            EIError.debugMsg("IO Created");

            output.writeObject("send block manager");
            data = input.readObject();
            BlockManager bm = (BlockManager) data;
            bm.resetBlockArray(registry.getBlockManager().getMapCols(), registry.getBlockManager().getMapRows());
            bm.setTransient(registry);
            gameController.setBlockManager(bm);
            bm = null;
            EIError.debugMsg("Block Manager set");

            data = input.readObject();
            parts = data.toString().split(" ");
            if (parts.length == 2) {
                EIError.debugMsg("Setting UDP port to: " + parts[1]);
                udpPort = Integer.parseInt(parts[1]);
            }
            output.writeObject("send block manager chunk");
            boolean sendMore = true;
            for (int i = 0; i < 4; i++) {
                data = input.readObject();
                if(i == 3) {
                    sendMore = false;
                }
                blockManagerReceive = new TCPClientBlockManagerDataReceive(registry, this, output, data, currentStartCollumn, collumnChunkSize, false, sendMore);
                currentStartCollumn += collumnChunkSize;
            }
            sendData("send placable manager");
            Game.loadingText = "Getting World Data...";

            while (keepRunning && socket.isConnected()) {
                data = input.readObject();
                
                EIError.debugMsg("Received data: " + data.toString());

                if (data.getClass().equals(String.class)) {
                    data = (String) data;
                    if (data.toString().equals("goodbye")) {
                        keepRunning = false;
                    } else if (data.toString().length() == 8 && data.toString().substring(0, 4).equals("udp ")) {
                        parts = data.toString().split(" ");
                        if (parts.length == 2) {
                            EIError.debugMsg("Setting UDP port to: " + parts[1]);
                            udpPort = Integer.parseInt(parts[1]);
                        }
                    } else {
                        if (!data.toString().isEmpty()) {
                            gameController.showMessage("Success", "Message: " + data);
                        }
                    }
//                } else if (data.getClass().equals(BlockManager.class)) {
//                    EIError.debugMsg("Setting Block Manager...");
//                    BlockManager bm = (BlockManager) data;
//                    bm.setTransient(registry);
//                    gameController.setBlockManager(bm);
//                    bm = null;
//                    EIError.debugMsg("Block Manager set");
//                    sendData("send placable manager");
//                    Game.loadingText = "Getting Building Data...";
                } else if (data.getClass().equals(short[][].class)) {
                    blockManagerReceive = new TCPClientBlockManagerDataReceive(registry, this, output, data, currentStartCollumn, collumnChunkSize, true, true);
                    currentStartCollumn += collumnChunkSize;

//                    EIError.debugMsg("Setting Block Manager...");
//                    short[][] blockChunk = (short[][]) data;
//                    if (blockChunk.length >= collumnChunkSize) {
//                        registry.getBlockManager().setBlockCollumns(blockChunk, currentStartCollumn, currentStartCollumn + collumnChunkSize);
//                        if (currentStartCollumn == 0) {
//                            sendData("send placable manager");
//                        }
//                        currentStartCollumn += collumnChunkSize;
//                        EIError.debugMsg("Block Manager chunk " + currentStartCollumn + " " + currentStartCollumn + collumnChunkSize);
//                        output.writeObject("send block manager chunk");
//                        try {
//                            sleep(10);
//                        } catch (InterruptedException ex) {
//                        }
//                    } else {
//                        EIError.debugMsg("Block Manager set");
//                    }
//                    blockChunk = null;
//                    Game.loadingText = "Getting Building Data...";
                } else if (data.getClass().equals(PlaceableManager.class)) {
                    EIError.debugMsg("Setting Placeable Manager...");
                    PlaceableManager pm = (PlaceableManager) data;
                    pm.setTransient(registry);
                    gameController.setPlaceableManager(pm);
                    pm = null;
                    registry.getPlayerManager().getCurrentPlayer().init();
                    EIError.debugMsg("Placeable Manager set");
                    sendData("send resource manager");
                    Game.loadingText = "Making Shinies...";
                } else if (data.getClass().equals(ResourceManager.class)) {
                    EIError.debugMsg("Setting Resource Manager...");
                    ResourceManager rm = (ResourceManager) data;
                    rm.setTransient(registry);
                    gameController.setResourceManager(rm);
                    rm = null;
                    EIError.debugMsg("Resource Manager set");
                    sendData("send monster manager");
                    Game.loadingText = "Spawning Evil Bad Guys";
                } else if (data.getClass().equals(MonsterManager.class)) {
                    EIError.debugMsg("Setting Monster Manager...");
                    MonsterManager mm = (MonsterManager) data;
                    mm.setTransient(registry);
                    gameController.setMonsterManager(mm);
                    mm = null;
                    EIError.debugMsg("Monster Manager set");
                    sendData("send player");
                    Game.loadingText = "Initializing Inventors";
                } else if (data.getClass().equals(Player.class)) {
                    EIError.debugMsg("Adding Player...");
                    Player p = (Player) data;
                    p.setTransient(registry);
                    registry.getPlayerManager().registerPlayer(p);
                    EIError.debugMsg("Player Added");

                    if (registry.getPlayerManager().getPlayers().size() <= 2) {
                        EIError.debugMsg("Sending Player Data...");
                        sendData(registry.getPlayerManager().getCurrentPlayer());
                        EIError.debugMsg("Player Data Sent");

                        Game.loadingText = "Here we go!";
                        EIError.debugMsg("send block manager chunk");
                        
                        gameController.setLoading(false);
                        registry.getHUDManager().unloadHUD("ScreenLoading");
                        registry.getBlockManager().updateResolution();
                        gameController.setIsInGame(true);
                        readyForUpdates = true;

                        try {
                            DatagramSocket socket = new DatagramSocket();
                            clientReceiveThread = new ClientReceiveThread(registry, gameController, socket, ip, udpPort);
                            clientReceiveThread.start();

                            System.out.println("Socket Created");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        output.writeObject("send block manager chunk");
                    }
                } else if (data.getClass().equals(Resource.class)) {
                    Resource r = (Resource) data;
                    EIError.debugMsg("Adding Resource (" + r.getId() + ")...");
                    r.setTransient(registry, registry.getResourceManager());
                    registry.getResourceManager().registerResource(r);
                    EIError.debugMsg("Resource Added");
                } else if (data.getClass().equals(OobabooGatherer.class)) {
                    OobabooGatherer o = (OobabooGatherer) data;
                    registry.getPlayerManager().assignOobaboo(o);
                } else if (data.getClass().equals(OobabooHealer.class)) {
                    OobabooHealer o = (OobabooHealer) data;
                    registry.getPlayerManager().assignOobaboo(o);
                } else if (data.getClass().equals(OobabooWarrior.class)) {
                    OobabooWarrior o = (OobabooWarrior) data;
                    registry.getPlayerManager().assignOobaboo(o);
                } else if (data.getClass().equals(AggressiveSnake.class)) {
                    AggressiveSnake m = (AggressiveSnake) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(BlueThorn.class)) {
                    BlueThorn m = (BlueThorn) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(BossOrc.class)) {
                    BossOrc m = (BossOrc) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(SnailRider.class)) {
                    SnailRider m = (SnailRider) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(LionFly.class)) {
                    LionFly m = (LionFly) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(Orc.class)) {
                    Orc m = (Orc) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(Pig.class)) {
                    Pig m = (Pig) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(Porcupine.class)) {
                    Porcupine m = (Porcupine) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(RedOrc.class)) {
                    RedOrc m = (RedOrc) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(RockMonster.class)) {
                    RockMonster m = (RockMonster) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(Snail.class)) {
                    Snail m = (Snail) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(Snake.class)) {
                    Snake m = (Snake) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(SpiderWorm.class)) {
                    SpiderWorm m = (SpiderWorm) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(VineThorn.class)) {
                    VineThorn m = (VineThorn) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(ZombieWalrus.class)) {
                    ZombieWalrus m = (ZombieWalrus) data;
                    m.setTransient(registry, registry.getMonsterManager());
                    registry.getMonsterManager().registerMonster(m);
                } else if (data.getClass().equals(AutoXBow.class)) {
                    AutoXBow p = (AutoXBow) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(BookShelf.class)) {
                    BookShelf p = (BookShelf) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(BladeTrap.class)) {
                    BladeTrap p = (BladeTrap) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(Box.class)) {
                    Box p = (Box) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(Chest.class)) {
                    Chest p = (Chest) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(CopperMine.class)) {
                    CopperMine p = (CopperMine) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(Crate.class)) {
                    Crate p = (Crate) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(EmeraldTeleporter.class)) {
                    EmeraldTeleporter p = (EmeraldTeleporter) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(Forge.class)) {
                    Forge p = (Forge) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(GoldMine.class)) {
                    GoldMine p = (GoldMine) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(IronMine.class)) {
                    IronMine p = (IronMine) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(ItemContainer.class)) {
                    ItemContainer p = (ItemContainer) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(LargeFarm.class)) {
                    LargeFarm p = (LargeFarm) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(LargeSafe.class)) {
                    LargeSafe p = (LargeSafe) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(LionFlyStatue.class)) {
                    LionFlyStatue p = (LionFlyStatue) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(MedicKit.class)) {
                    MedicKit p = (MedicKit) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(MediumSafe.class)) {
                    MediumSafe p = (MediumSafe) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(Pasture.class)) {
                    Pasture p = (Pasture) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(PlatinumMine.class)) {
                    PlatinumMine p = (PlatinumMine) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(PottedFlower.class)) {
                    PottedFlower p = (PottedFlower) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(RubyTeleporter.class)) {
                    RubyTeleporter p = (RubyTeleporter) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(SapphireTeleporter.class)) {
                    SapphireTeleporter p = (SapphireTeleporter) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(SawMill.class)) {
                    SawMill p = (SawMill) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(ScareCrow.class)) {
                    ScareCrow p = (ScareCrow) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(SilverMine.class)) {
                    SilverMine p = (SilverMine) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(SmallFarm.class)) {
                    SmallFarm p = (SmallFarm) data;
                    p.setTransient(registry);
                } else if (data.getClass().equals(SmallSafe.class)) {
                    SmallSafe p = (SmallSafe) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(SteamEngine.class)) {
                    SteamEngine p = (SteamEngine) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(StoneMine.class)) {
                    StoneMine p = (StoneMine) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(ThornTrap.class)) {
                    ThornTrap p = (ThornTrap) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(TownBlock.class)) {
                    TownBlock p = (TownBlock) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(Building.class)) {
                    Building p = (Building) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(WeaponRack.class)) {
                    WeaponRack p = (WeaponRack) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(WindMill.class)) {
                    WindMill p = (WindMill) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(WorkBench.class)) {
                    WorkBench p = (WorkBench) data;
                    p.setTransient(registry);
                    registry.getPlaceableManager().registerPlaceable(p);
                } else if (data.getClass().equals(UpdatePlayer.class)) {
                    UpdatePlayer up = (UpdatePlayer) data;
                    EIError.debugMsg("Updating Player (" + up.id + ")...");
                    registry.getPlayerManager().processPlayerUpdate(up);
                    EIError.debugMsg("Player Updated");
                } else if (data.getClass().equals(UpdateMonster.class)) {
                    UpdateMonster um = (UpdateMonster) data;
                    EIError.debugMsg("Updating Monster (" + um.id + ")...");
                    registry.getMonsterManager().processMonsterUpdate(um);
                    EIError.debugMsg("Monster Updated");
                } else if (data.getClass().equals(UpdateRobot.class)) {
                    UpdateRobot ur = (UpdateRobot) data;
                    EIError.debugMsg("Updating Robot (" + ur.id + ")...");
                    registry.getPlayerManager().processRobotUpdate(ur);
                    EIError.debugMsg("Robot Updated");
                } else if (data.getClass().equals(UpdateOobaboo.class)) {
                    UpdateOobaboo uo = (UpdateOobaboo) data;
                    EIError.debugMsg("Updating Oobaboo (" + uo.id + ")...");
                    registry.getPlayerManager().processOobabooUpdate(uo);
                    EIError.debugMsg("Oobaboo Updated");
                } else if (data.getClass().equals(UpdatePlaceable.class)) {
                    UpdatePlaceable up = (UpdatePlaceable) data;
                    EIError.debugMsg("Updating Placeable (" + up.id + ")...");
                    registry.getPlaceableManager().processPlaceableUpdate(up);
                    EIError.debugMsg("Placeable Updated");
                } else if (data.getClass().equals(UpdateResource.class)) {
                    UpdateResource ur = (UpdateResource) data;
                    EIError.debugMsg("Updating Resource (" + ur.id + ")...");
                    registry.getResourceManager().processResourceUpdate(ur);
                    EIError.debugMsg("Resource Updated");
                } else if (data.getClass().equals(UpdateProjectile.class)) {
                    UpdateProjectile up = (UpdateProjectile) data;
                    EIError.debugMsg("Updating Projectile...");
                    registry.getProjectileManager().processProjectileUpdate(up);
                    EIError.debugMsg("Projectile Updated");
                } else {
                    System.out.println("ERROR DATA: " + data.getClass());
                }

                data = null;
            }
        } catch (IOException e) {
            EIError.debugMsg("Network IO Error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            EIError.debugMsg("Network Class Error: " + e.getMessage());
        } catch (Exception e) {
            EIError.debugMsg("Network Error: " + e.getMessage());
        }

        if (clientReceiveThread != null) {
            clientReceiveThread.setRunning(false);
        }

        readyForUpdates = false;

        gameController.setNetworkMode(false);

        gameController.showMessage("Error", "Server went offline");
        gameController.saveAndQuit();
        gameController.multiplayerMode = gameController.multiplayerMode.NONE;

        return;
    }

    public void close() {
        EIError.debugMsg("Closing");

        if (clientReceiveThread != null) {
            clientReceiveThread.setRunning(false);
        }
        if (socket != null) {
            try {
                sendData("goodbye");
                socket.close();
            } catch (IOException e) {
                //oh well, ain't no thang
            }
        }
        keepRunning = false;
        socket = null;
    }

    public boolean sendData(Object data) {
        if (output == null || !socket.isConnected()) {
            return false;
        }

        synchronized (this) {
            try {
                output.flush();
                output.writeObject(data);
                output.flush();
                output.reset();

                if (data.getClass().equals(String.class)) {
                    if (data.equals("goodbye")) {
                        if (clientReceiveThread != null) {
                            clientReceiveThread.setRunning(false);
                        }
                        socket.close();
                        if (socket != null) {
                            socket.close();
                        }

                        keepRunning = false;
                    }
                }
            } catch (IOException e) {
                EIError.debugMsg("Network IO Error - Sending");
            }

            return true;
        }
    }
}
