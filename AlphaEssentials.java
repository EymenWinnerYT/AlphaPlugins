/*
  The AlphaEssentials plugin for HMod by EymenWinnerYT
  Last update: 21.3.2026 (DD.MM.YYYY)

  Code is a bit messy because i've accidently deinted the source code and decompiled from the existing jar.
 */

import java.util.HashSet;
import java.util.Hashtable;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Random;

public class AlphaEssentials extends Plugin {
  private Logger logger = Logger.getLogger("Minecraft");
  
  private MainListener listener = new MainListener();
  
  private HashSet<String> afkPlayers = new HashSet<>();
  
  private Hashtable<String, String> tpSenders = new Hashtable<>();
  
  private Hashtable<String, String> tpReceivers = new Hashtable<>();
  
  private Hashtable<String, Location> playerLastLocs = new Hashtable<>();

  private HashSet<String> admins = new HashSet<>(); //names are case-sensitive

  private Random wildRand = new Random();

  Server server = etc.getServer();
  
  public void disable() {}
  
  public void enable() {
    etc.getLoader().addListener(PluginLoader.Hook.COMMAND, this.listener, this, PluginListener.Priority.MEDIUM);
    etc.getLoader().addListener(PluginLoader.Hook.PLAYER_MOVE, this.listener, this, PluginListener.Priority.MEDIUM);
    etc.getLoader().addListener(PluginLoader.Hook.CHAT, this.listener, this, PluginListener.Priority.MEDIUM);
    etc.getLoader().addListener(PluginLoader.Hook.BLOCK_CREATED, this.listener, this, PluginListener.Priority.MEDIUM);
    etc.getLoader().addListener(PluginLoader.Hook.BLOCK_DESTROYED, this.listener, this, PluginListener.Priority.MEDIUM);
    etc.getLoader().addListener(PluginLoader.Hook.TELEPORT, this.listener, this, PluginListener.Priority.MEDIUM);
    File configObj = new File("ops.txt");
    try(Scanner scanner = new Scanner(configObj)){
      while(scanner.hasNextLine()){
        admins.add(scanner.nextLine());
      }
    }catch(FileNotFoundException e){
      e.printStackTrace();
      etc.getLoader().disablePlugin("AlphaEssentials");
    }
    this.logger.info("[AlphaEssentials] Enabled");
  }
  
  public class MainListener extends PluginListener {
    public void broadcastMessage(String msg) {
      for (Player p : server.getPlayerList()) {
        try {
          p.sendMessage(msg);
        } catch (Exception exception) {}
      } 
    }
    
    @Override
    public boolean onCommand(Player player, String[] args) {
      if (AlphaEssentials.this.afkPlayers.contains(player.getName())) {
        AlphaEssentials.this.afkPlayers.remove(player.getName());
        broadcastMessage(Colors.LightGray + player.getName() + " is no longer AFK.");
      } 
      if (args[0].equalsIgnoreCase("/time")) {
        if (!admins.contains(player.getName())) {
          player.sendMessage(Colors.Rose + "Only staff can use this command.");
          return true;
        } 
        if (args.length < 2) {
          player.sendMessage("Usage: /time set/add <time>");
          return true;
        } 
        if (args[1].equalsIgnoreCase("set")) {
          try {
            server.setTime(Long.parseLong(args[2]));
          } catch (NumberFormatException e) {
            if (args[2].equalsIgnoreCase("midnight")) {
              server.setTime(18000L);
            } else if (args[2].equalsIgnoreCase("day")) {
              server.setTime(0L);
            } else if (args[2].equalsIgnoreCase("noon")) {
              server.setTime(6000L);
            } else if (args[2].equalsIgnoreCase("evening")) {
              server.setTime(12000L);
            } else if (args[2].equalsIgnoreCase("night")) {
              server.setTime(13000L);
            } else {
              player.sendMessage("Invalid time: " + args[2] + ".");
            } 
          } 
        } else if (args[1].equalsIgnoreCase("add")) {
          try {
            server.setTime(server.getTime() + Long.parseLong(args[2]));
          } catch (NumberFormatException e) {
            player.sendMessage("Time: " + args[2] + ".");
          } 
        } else {
          player.sendMessage("Option: " + args[1] + ".");
        } 
        return true;
      } 
      if (args[0].equalsIgnoreCase("/wild")) {
        Location spawn = server.getSpawnLocation();
        for (int i = 0; i < 1024; i++) {
          int x = (int)((spawn.x + 256) + wildRand.nextFloat() * 2048.0D * 2.0D - 2048.0D);
          int z = (int)((spawn.z + 256) + wildRand.nextFloat() * 2048.0D * 2.0D - 2048.0D);
          int y = server.getHighestBlockY(x, z);
          if (y >= 64 && y < 127) {
            player.sendMessage("Teleporting to a random location... (don't worry if you start falling or game freezing for a short time)");
            player.teleportTo(new Location(x, (y + 1), z));
            return true;
          } 
        } 
        player.sendMessage("Couldn't found a safe location, please try again.");
        return true;
      } 
      if (args[0].equalsIgnoreCase("/give")) {
        if (!admins.contains(player.getName())) {
          player.sendMessage(Colors.Rose+"Only staff can use this command.");
          return true;
        } 
        if (args.length < 4) {
          player.sendMessage("Usage: /give <player> <item> <count>");
          return true;
        } 
        Player targetPlayer = server.matchPlayer(args[1]);
        if(targetPlayer == null) {
        	player.sendMessage(Colors.Rose+"Couldn't found this player!");
        	return true;
        }
        try {
          targetPlayer.giveItem(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        } catch (NumberFormatException e) {
          targetPlayer.sendMessage("Invalid syntax!");
        } 
        return true;
      } 
      if (args[0].equalsIgnoreCase("/tpp")) {
        if (!admins.contains(player.getName())) {
          player.sendMessage(Colors.Rose + "Only staff can use this command."); 
          return true;
        }
        if (args.length < 3) {
          player.sendMessage("Syntax: /tpp <sourcePlayer> <targetPlayer>");
          return true;
        } 
        Player sourcePlayer = server.matchPlayer(args[1]);
        Player targetPlayer = server.matchPlayer(args[1]);
        if (sourcePlayer == null || targetPlayer == null) {
          player.sendMessage("while teleporting: player(s) isn't/aren't online.");
          return true;
        } 
        sourcePlayer.teleportTo((BaseEntity)targetPlayer);
        return true;
      } 
      if (args[0].equalsIgnoreCase("/tp")) {
        int x, y, z;
        if (!admins.contains(player.getName())) {
          player.sendMessage(Colors.Rose + "Only staff can use this command.");
          return true;
        }
        if (args.length < 4) {
          player.sendMessage("Syntax: /tp <x> <y> <z>");
          return true;
        } 
        try {
          x = Integer.parseInt(args[1]);
          y = Integer.parseInt(args[2]);
          z = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
          player.sendMessage("Invalid coordinate(s)!");
          return true;
        } 
        if (!AlphaEssentials.this.playerLastLocs.containsKey(player.getName())) {
          AlphaEssentials.this.playerLastLocs.put(player.getName(), player.getLocation());
        } else {
          AlphaEssentials.this.playerLastLocs.replace(player.getName(), player.getLocation());
        } 
        player.teleportTo(new Location(x, y, z));
        return true;
      } 
      if (args[0].equalsIgnoreCase("/rules")) {
        player.sendMessage("1. No cheating!");
        player.sendMessage("2. Don't grief a structure that belongs to someone!");
        player.sendMessage("3. No item stealing or looting from other players!");
        player.sendMessage("4. Don't swear in the chat! (if it doesn't go so serious it is allowed)");
        player.sendMessage("5. Avoid drama in the chat!");
        player.sendMessage("6. Don't join with usernames that belongs to someone!");
        player.sendMessage("7. Don't overload the server!");
        player.sendMessage("");
        player.sendMessage("More info at forum.eymenwinneryt.42web.io");
        return true;
      } 
      if (args[0].equalsIgnoreCase("/ping")) {
        player.sendMessage("Pong!");
        return true;
      } 
      if (args[0].equalsIgnoreCase("/coord")) {
        Player targetPlayer;
        if (args.length > 1) {
          targetPlayer = server.getPlayer(args[1]);
          if (targetPlayer == null)
            return true;
        } else {
          targetPlayer = player;
        }
        player.sendMessage(Colors.LightGray + "X: " + targetPlayer.getX());
        player.sendMessage(Colors.LightGray + "Y: " + targetPlayer.getY());
        player.sendMessage(Colors.LightGray + "Z: " + targetPlayer.getZ());
        player.sendMessage(Colors.LightGray + "Rotation: " + targetPlayer.getRotation());
        player.sendMessage(Colors.LightGray + "Pitch: " + targetPlayer.getPitch());
        return true;
      } 
      if (args[0].equalsIgnoreCase("/afk")) {
        AlphaEssentials.this.afkPlayers.add(player.getName());
        broadcastMessage(Colors.LightGray + player.getName() + " is now AFK.");
        return true;
      } 
      if (args[0].equalsIgnoreCase("/tpa")) {
        String playerName = player.getName().toLowerCase();
        if (args.length < 2) {
          player.sendMessage("Usage: /tpa <player>");
          return true;
        } 
        AlphaEssentials.this.tpSenders.put(playerName, args[1].toLowerCase());
        AlphaEssentials.this.tpReceivers.put(args[1].toLowerCase(), playerName);
        Player receiver = server.getPlayer(args[1]);
        if (receiver != null) {
          receiver.sendMessage(Colors.LightGray + player.getName() + " sent you a teleportation request.");
          receiver.sendMessage(Colors.LightGray + "Type " + Colors.Green + "/tpac" + Colors.LightGray + " or " + Colors.Rose + "/tpdeny" + Colors.LightGray + ".");
        } else {
          player.sendMessage(Colors.Rose + "Player couldn't found!");
          return true;
        } 
        return true;
      } 
      if (args[0].equalsIgnoreCase("/tpac")) {
        String playerName = player.getName().toLowerCase();
        if (!AlphaEssentials.this.tpReceivers.containsKey(playerName)) {
          player.sendMessage(Colors.Rose +"No teleportation request to you yet!");
          return true;
        } 
        String senderPlayerName = (String)AlphaEssentials.this.tpReceivers.get(playerName);
        Player senderPlayer = server.getPlayer(senderPlayerName);
        if (senderPlayer != null) {
          senderPlayer.sendMessage(Colors.Green + player.getName() + " accepted your teleportation request.");
          playerLastLocs.put(senderPlayerName, senderPlayer.getLocation());
          senderPlayer.teleportTo((BaseEntity)player);
        } 
        AlphaEssentials.this.tpReceivers.remove(playerName);
        AlphaEssentials.this.tpSenders.remove(senderPlayerName);
      } else if (args[0].equalsIgnoreCase("/tpdeny")) {
        String playerName = player.getName().toLowerCase();
        if (!AlphaEssentials.this.tpReceivers.containsKey(playerName)) {
          player.sendMessage(Colors.Rose + "No teleportation request to you yet!");
          return true;
        } 
        String senderPlayerName = (String)AlphaEssentials.this.tpReceivers.get(playerName);
        Player senderPlayer = server.getPlayer(senderPlayerName);
        if (senderPlayer != null)
          senderPlayer.sendMessage(Colors.Rose + player.getName() + " denied your teleportation request.");
        AlphaEssentials.this.tpReceivers.remove(playerName);
        AlphaEssentials.this.tpSenders.remove(senderPlayerName);
      } else {
        if (args[0].equalsIgnoreCase("/back")) {
          if (!AlphaEssentials.this.playerLastLocs.containsKey(player.getName())) {
            player.sendMessage(Colors.Rose+"You don't have a previous location yet!");
            return true;
          } 
          Location loc = player.getLocation();
          player.teleportTo((Location)AlphaEssentials.this.playerLastLocs.get(player.getName()));
          AlphaEssentials.this.playerLastLocs.replace(player.getName(), loc);
          return true;
        } 
        if (args[0].equalsIgnoreCase("/spawn")) {
          Location loc = player.getLocation();
          player.teleportTo(new Location(50.0D, 70.0D, 119.0D));
          if (!AlphaEssentials.this.playerLastLocs.containsKey(player.getName())) {
            AlphaEssentials.this.playerLastLocs.put(player.getName(), loc);
          } else {
            AlphaEssentials.this.playerLastLocs.replace(player.getName(), loc);
          } 
          return true;
        } if (args[0].equalsIgnoreCase("/gettime")) {
            long ticks = server.getTime();
            long totalSeconds = ticks / 20;
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            player.sendMessage(Colors.LightGray + "Time of the day in-real-life time format: "+minutes+":"+seconds);
            return true;
        } if (args[0].equalsIgnoreCase("/circle")) {
          if(args.length < 3){
            player.sendMessage("Usage: /circle <id> <radius>");
            return true;
          }
          int id, r;
          try{
            id = Integer.parseInt(args[1]);
            r = Integer.parseInt(args[2]);
          }catch(NumberFormatException e){
            e.printStackTrace();
            return true;
          }

          int x = 0;
          int y = r;
          int d = 3 - r * 2;
          int cx = (int)player.getX();
          int cy = (int)player.getZ();
          int playerY = (int)player.getY();
          while(x <= y){
            int[][] points = {
              {cx+x, playerY, cy+y},
              {cx-x, playerY, cy+y},
              {cx+x, playerY, cy-y},
              {cx-x, playerY, cy-y},
              {cx+y, playerY, cy+x},
              {cx-y, playerY, cy+x},
              {cx+y, playerY, cy-x},
              {cx-y, playerY, cy-x},
            };
            for(int[] point: points){
              server.setBlockAt(id, point[0], point[1], point[2]);
            }

            if(d < 0){
              d += x * 4 + 6;
            }else{
              y--;
              d += 4 * (x - y) + 10;
            }
            x++;
          }
          return true;
        }
      } 
      return false;
    }
    
    public boolean onBlockCreate(Player player, Block block, Block blockClicked, int itemInHand) {
      if (AlphaEssentials.this.afkPlayers.contains(player.getName())) {
        AlphaEssentials.this.afkPlayers.remove(player.getName());
        broadcastMessage(Colors.LightGray + player.getName() + " is no longer AFK.");
      } 
      if (!admins.contains(player.getName())) {
        Location spawnLoc = server.getSpawnLocation();
        boolean isInSpawnX = (Math.abs(block.getX() - spawnLoc.x) <= 64.0D);
        boolean isInSpawnZ = (Math.abs(block.getZ() - spawnLoc.z) <= 48.0D);
        if (isInSpawnX && isInSpawnZ) {
          server.setBlockData(block.getX(), block.getY(), block.getZ(), 0);
          player.sendMessage(Colors.Rose + "You can't place blocks in the spawn area.");
          return true;
        } 
      } else {
        return false;
      }
      return false;
    }
    
    public void onPlayerMove(Player player, Location prevLoc, Location newLoc) {
    	String playerName = player.getName();
      if (AlphaEssentials.this.afkPlayers.contains(playerName)) {
        AlphaEssentials.this.afkPlayers.remove(playerName);
        broadcastMessage(Colors.LightGray + playerName + " is no longer AFK.");
      }
      Location spawnLoc = server.getSpawnLocation();
      boolean isOldLocInSpawn = Math.abs(spawnLoc.x - prevLoc.x) <= 64.0D && Math.abs(spawnLoc.z - prevLoc.z) <= 48.0D;
      boolean isNewLocInSpawn = Math.abs(spawnLoc.x - newLoc.x) <= 64.0D && Math.abs(spawnLoc.z - newLoc.z) <= 48.0D;
      boolean isOldLocOutSpawn = !isOldLocInSpawn;
      boolean isNewLocOutSpawn = !isNewLocInSpawn;
      if(isOldLocInSpawn && isNewLocOutSpawn) {
    	  player.sendMessage(Colors.Yellow+"You've left the spawn area. Now you are able to build.");
      }else if(isOldLocOutSpawn && isNewLocInSpawn) {
    	  player.sendMessage(Colors.Yellow+"You've entered to the spawn area. You aren't able to build.");
      }
    }
    
    public boolean onBlockDestroy(Player player, Block block) {
    	String playerName = player.getName();
      if (AlphaEssentials.this.afkPlayers.contains(playerName)) {
        AlphaEssentials.this.afkPlayers.remove(playerName);
        broadcastMessage(Colors.LightGray + playerName + " is no longer AFK.");
      }
      if (!admins.contains(playerName)) {
        if (server.getBlockAt(block.getX(), block.getY(), block.getZ()).getType() != block.getType())
          return false;
        Location spawnLoc = server.getSpawnLocation();
        boolean isInSpawnX = (Math.abs(block.getX() - spawnLoc.x) <= 64.0D);
        boolean isInSpawnZ = (Math.abs(block.getZ() - spawnLoc.z) <= 48.0D);
        if (isInSpawnX && isInSpawnZ) {
          server.setBlock(block);
          player.sendMessage(Colors.Rose + "You can't break blocks in the spawn area.");
          return true;
        } 
      }
      return false;
    }
    
    public boolean onChat(Player player, String message) {
    	etc.getMCServer().a();
      if (AlphaEssentials.this.afkPlayers.contains(player.getName())) {
        AlphaEssentials.this.afkPlayers.remove(player.getName());
        broadcastMessage(Colors.LightGray + player.getName() + " is no longer AFK.");
      } 
      if (admins.contains(player.getName())) {
        message = message.replaceAll("&(\\w)", "");
        broadcastMessage(Colors.LightGray + "[" + Colors.Red + "Owner" + Colors.LightGray + "] " + Colors.White + player.getName() + ": " + message);
        AlphaEssentials.this.logger.info("[Owner] " + player.getName() + ": " + message);
      } else {
    	  broadcastMessage(Colors.LightGray + "[" + Colors.Green + "Player" + Colors.LightGray + "] " + Colors.White + player.getName() + ": " + message);
        AlphaEssentials.this.logger.info("[Player] " + player.getName() + ": " + message);
      } 
      return true;
    }
  }
}
