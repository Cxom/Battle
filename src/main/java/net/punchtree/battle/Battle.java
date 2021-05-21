package net.punchtree.battle;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.punchtree.battle.arena.BattleArena;
import net.punchtree.battle.arena.BattleArenaLoader;
import net.punchtree.minigames.arena.creation.ArenaManager;
import net.punchtree.minigames.game.GameManager;

public class Battle extends JavaPlugin {

	public static final String BATTLE_CHAT_PREFIX = ChatColor.DARK_RED + "[" + ChatColor.RED + "Battle" + ChatColor.DARK_RED + "]" + ChatColor.RESET + " ";
 	
	private static Plugin plugin;
	public static Plugin getPlugin() { return plugin; }
	
	private static File battleArenaFolder;
	
	private ArenaManager<BattleArena> battleArenaManager;
	
	private GameManager<BattleGame> battleGameManager;
	
	@Override
	public void onEnable() {
		plugin = this;
		
		battleArenaFolder = new File(getDataFolder().getAbsoluteFile() + File.separator + "Arenas");
		battleArenaManager = new ArenaManager<>(Battle.battleArenaFolder, BattleArenaLoader::load);
		battleGameManager = new GameManager<>("Battle Games");
		
		createAllGames();
	}
	
	private void createAllGames() {
		battleArenaManager.loadArenas();
		battleArenaManager.getArenas().forEach(battleArena -> {
			BattleGame game = new BattleGame(battleArena);
			battleGameManager.addGame(battleArena.getName(), game, game.getLobby());
		});
	}
	
	@Override
	public void onDisable() {
		battleGameManager.stopAllGames();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if ( ! label.equalsIgnoreCase("battle") ) return true;
		if ( ! ( sender instanceof Player) ) return true;
		Player player = (Player) sender;
		
		if ( args.length > 0 ) {
			switch (args[0]) {
			case "leave":
				/*
				 * This is preprocessed in BattleGame in order to determine the game,
				 * and cancelled if it goes through.
				 */
		    	player.sendMessage(Battle.BATTLE_CHAT_PREFIX + ChatColor.RED + "You're not in a game!");
				
				return true;
			}
			
			//default just won't return --> opens the /melee menu
		}
		
		battleGameManager.showMenuTo(player);
		
		return true;
	}
	
}
