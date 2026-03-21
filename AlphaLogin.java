/*
  The AlphaLogin plugin for HMod by EymenWinnerYT
  Last update: 21.3.2026 (DD.MM.YYYY)
 */

import java.util.logging.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.io.IOException;
import java.util.HashSet;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AlphaLogin extends Plugin{
	private Logger logger = Logger.getLogger("Minecraft");
	private MainListener listener = new MainListener();
	private HashSet<String> unloggedPlayers = new HashSet<String>();
	private Properties userDb = new Properties();
	private static final int SALT_LENGTH = 16;
	private static final int ITERATIONS = 100_000;
	private static final int KEY_LENGTH = 256;
	
	public static String hashPassword(char[] password) throws Exception{
		byte[] salt = new byte[SALT_LENGTH];
		SecureRandom random = new SecureRandom();
		random.nextBytes(salt);
		PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		byte[] hash = skf.generateSecret(spec).getEncoded();
		return ITERATIONS + ":" + 
				Base64.getEncoder().encodeToString(salt) + ":" +
				Base64.getEncoder().encodeToString(hash);
	}
	
	public static boolean verifyPassword(char[] password, String stored) throws Exception {
	    String[] parts = stored.split(":");
	    int iterations = Integer.parseInt(parts[0]);
	    byte[] salt = Base64.getDecoder().decode(parts[1]);
	    byte[] hash = Base64.getDecoder().decode(parts[2]);
	    PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, hash.length * 8);
	    SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	    byte[] testHash = skf.generateSecret(spec).getEncoded();
	    if (hash.length != testHash.length) return false;
	    int diff = 0;
	    for (int i = 0; i < hash.length; i++) {
	        diff |= hash[i] ^ testHash[i];
	    }
	    return diff == 0;
	}

	
	@Override
	public void disable() {
		
	}
	
	@Override
	public void enable() {
		etc.getLoader().addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.LOGIN, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.PLAYER_MOVE, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.CHAT, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_CREATED, listener, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_DESTROYED, listener, this, PluginListener.Priority.MEDIUM);
		logger.info("[AlphaLogin] Enabled");
		try (FileInputStream inStr = new FileInputStream("AlphaLogin\\players.properties")){
			userDb.load(inStr);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private class MainListener extends PluginListener {
		@Override
		public boolean onCommand(Player player, String[] args){
			if(args[0].equalsIgnoreCase("/register")) {
				if(userDb.getProperty(player.getName()) == null) {
					try (FileOutputStream outStr = new FileOutputStream("AlphaLogin\\players.properties")) {
						userDb.setProperty(player.getName(), hashPassword(args[1].toCharArray()));
					    userDb.store(outStr, "The user passwords file");
					} catch(Exception e) {
						e.printStackTrace();
						return true;
					}
					player.sendMessage(Colors.Green + "Registered successfully");
					unloggedPlayers.remove(player.getName());
					return true;
				}else {
					player.sendMessage(Colors.Rose + "You are already registered!");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("/login")) {
				if(!unloggedPlayers.contains(player.getName())) {
					player.sendMessage(Colors.Rose + "You are already logged in!");
				}
				try {
					if(verifyPassword(args[1].toCharArray(), userDb.getProperty(player.getName()))) {
						player.sendMessage(Colors.Green + "Logged in successfully");
						unloggedPlayers.remove(player.getName());
						return true;
					}else {
						player.sendMessage(Colors.Rose + "Invalid password!");
						return true;
					}
				}catch(Exception e) {
					player.sendMessage(Colors.Rose+"Failed to login! Please try again!");
					e.printStackTrace();
					return true;
				}
			}else {
				if(unloggedPlayers.contains(player.getName())) {
					return true;
				}
			}
			return false;
		}
		
		public void onLogin(Player player) {
			player.sendMessage(Colors.Green + "Welcome, " + Colors.Blue + player.getName() + Colors.Green + "!");
			player.sendMessage(Colors.LightBlue+"If you haven't registered, type "+Colors.Green+"/register <password>");
			player.sendMessage(Colors.LightBlue+"Type "+Colors.Green+"/login <password>"+Colors.LightBlue+"to login");
			player.sendMessage(Colors.LightBlue+"Because of updates sometimes, you may have to re-register."); //you can remove this line
			unloggedPlayers.add(player.getName());
		}
		
		public void onPlayerMove(Player player, Location prevLoc, Location newLoc) {
			if(unloggedPlayers.contains(player.getName())) {
				player.teleportTo(prevLoc);
			}
		}
		
		public boolean onBlockDestroy(Player player, Block block){
			if(unloggedPlayers.contains(player.getName())) {
				return true;
			}
			return false;
		}

		public boolean onBlockCreate(Player player, Block blockPlaced, Block blockClicked, int itemInHand){
			if(unloggedPlayers.contains(player.getName())) {
				return true;
			}
			return false;
		}

		public boolean onChat(Player player, String message) {
			if(unloggedPlayers.contains(player.getName())) {
				return true;
			}
			return false;
		}
	}
}