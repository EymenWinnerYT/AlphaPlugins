/*
  The AlphaEssentials plugin for HMod by EymenWinnerYT
  Last update: 21.3.2026 (DD.MM.YYYY)
 */

import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;

public class AlphaProtection extends Plugin {
	private MainListener listener = new MainListener();
	private Logger logger = Logger.getLogger("Minecraft");
	private HashSet<String> protectQueue = new HashSet<>();
	private HashSet<String> unprotectQueue = new HashSet<>();
	private HashSet<String> frozenPlayers = new HashSet<>();
	private Properties userDb = new Properties();
	
	@Override
	public void disable() {
		
	}
	
	@Override
	public void enable() {
		etc.getLoader().addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_DESTROYED, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_CREATED, listener, this, PluginListener.Priority.MEDIUM);
		logger.info("[AlphaProtection] Enabled");
		try (FileInputStream inStr = new FileInputStream("AlphaProtection\\doors.properties")){
			userDb.load(inStr);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private class MainListener extends PluginListener {
		@Override
		public boolean onCommand(Player player, String[] args) {
			if(args[0].equalsIgnoreCase("/protection")) {
				player.sendMessage(Colors.Rose + "=== PROTECTION PLUGIN GUIDE ===");
				player.sendMessage("");
				player.sendMessage(Colors.LightBlue + "To protect your doors, use " + Colors.Green+"/dprotect "+Colors.LightBlue+"command. ");
				player.sendMessage(Colors.LightBlue+"You must do that command in both blocks of the door.");
				return true;
			}else if(args[0].equalsIgnoreCase("/dprotect")) {
				if(protectQueue.contains(player.getName())) {
					player.sendMessage(Colors.Rose+"You are already in the queue!");
					return true;
				}
				protectQueue.add(player.getName());
				player.sendMessage(Colors.Green+"Left click on the door your want to protect.");
				return true;
			}else if(args[0].equalsIgnoreCase("/duprotect")) {
				if(unprotectQueue.contains(player.getName())) {
					player.sendMessage(Colors.Rose+"You are already in the queue!");
					return true;
				}
				unprotectQueue.add(player.getName());
				player.sendMessage(Colors.Green+"Left click on the door your want to unprotect.");
				return true;
			}
			return false;
		}
		public void onPlayerMove(Player player, Location prevLoc, Location newLoc) {
			if(frozenPlayers.contains(player.getName())) {
				player.teleportTo(prevLoc);
			}
		}
		public boolean onBlockCreate(Player player, Block blockPlaced, Block blockClicked, int itemInHand) {
			String coordInStr = "" + blockClicked.getX() + "_" + blockClicked.getY() + "_" + blockClicked.getZ();
			String pname = player.getName();
			if(blockClicked.getType() == 64) {
				frozenPlayers.add(pname);
				String owner = userDb.getProperty(coordInStr);
				if(owner != null && !pname.equals(owner)) {
					player.sendMessage(Colors.Rose+"This door is owned by "+owner+"!");
					frozenPlayers.remove(pname);
					return true;
				}
				frozenPlayers.remove(pname);
			}
			return false;
		}
		public boolean onBlockDestroy(Player player, Block block) {
			String coordInStr = "" + block.getX() + "_" + block.getY() + "_" + block.getZ();
			String pname = player.getName();
			Block blockAbove = null;
			if(block.getY() < 127)
				blockAbove = etc.getServer().getBlockAt(block.getX(), block.getY()+1, block.getZ());
			if(block.getType()==64 || (blockAbove.getType() == 64 && blockAbove != null)) {
				if(protectQueue.contains(pname)) {
					String owner = userDb.getProperty(coordInStr);
					if(owner != null && !owner.equals(pname)) {
						player.sendMessage(Colors.Rose+"This door is already owned!");
						protectQueue.remove(pname);
						return true;
					}
					
					userDb.setProperty(coordInStr, pname);
					try (FileOutputStream outStr = new FileOutputStream("AlphaProtection\\doors.properties")){
						userDb.store(outStr, "Player protected doors file");
						player.sendMessage(Colors.Green+"Your door protected successfully!");
					}catch(Exception e) {
						e.printStackTrace();
					}
					protectQueue.remove(pname);
					return true;
				}if(unprotectQueue.contains(pname)) {
					String owner = userDb.getProperty(coordInStr);
					if(owner == null) {
						player.sendMessage(Colors.Rose+"This door isn't owned yet!");
						unprotectQueue.remove(pname);
						return true;
					}
					if(owner != null && !owner.equals(pname)) {
						player.sendMessage(Colors.Rose+"You don't own this door!");
						unprotectQueue.remove(pname);
						return true;
					}
					
					userDb.remove(coordInStr);
					try (FileOutputStream outStr = new FileOutputStream("AlphaProtection\\doors.properties")){
						userDb.store(outStr, "Player protected doors file");
						player.sendMessage(Colors.Green+"Your door unprotected successfully!");
					}catch(Exception e) {
						e.printStackTrace();
					}
					unprotectQueue.remove(pname);
					return true;
				}
				frozenPlayers.add(pname);
				String owner = userDb.getProperty(coordInStr);
				if(owner != null && !pname.equals(owner)) {
					player.sendMessage(Colors.Rose+"This door is owned by "+owner+"!");
					frozenPlayers.remove(pname);
					return true;
				}
				frozenPlayers.remove(pname);
			}
			return false;
		}
	}
}
