package net.punchtree.battle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import net.punchtree.battle.arena.BattleArena;
import net.punchtree.battle.arena.BattleArenaLoader;
import net.punchtree.minigames.arena.creation.ArenaManager;
import net.punchtree.minigames.lobby.Lobby;
import net.punchtree.minigames.menu.MinigameMenu;

public class BattleGameManager {

	static ArenaManager<BattleArena> battleArenaManager;
	
	private static Map<String, BattleGame> models = new HashMap<>();
	public static BattleGame getGame(String game) { return models.get(game); }

	//In practice, Lobbies to Games are 1-to-1, but in theory, this is not true.
	private static Map<String, Lobby> lobbies = new HashMap<>();
	public static Lobby getLobby(String lobby) { return lobbies.get(lobby); }
	
	private static final String MENU_NAME = "Battle Games";
	
	private static MinigameMenu menu;
	
	/*****************************************/
	
	//TODO Should this take a string arg? Any usefulness? Speculative?
	private static void createGame(BattleArena arena) {
		BattleGame game = new BattleGame(arena);
		models.put(arena.getName(), game);
		lobbies.put(arena.getName(), game.getLobby());
	}
	
	public static void createAllGames() {
		battleArenaManager = new ArenaManager<>(Battle.battleArenaFolder, BattleArenaLoader::load);
		battleArenaManager.loadArenas();
		battleArenaManager.getArenas().forEach(BattleGameManager::createGame);
	}
	
	public static void stopAllGames() {
		models.values().forEach(BattleGame::stopGame);
		// This removes a strong reference to the menu
		// that prevents it from being garbage collected when reloading
		// why? I have no idea. Probably because static
		// TODO try to take away the = null line after this class because instance-based
		models.clear();
		lobbies.clear();
		menu = null;
	}
	
	public static boolean addPlayerToGameLobby(String lobbyName, Player player) {
		
		if (! hasLobby(lobbyName)) throw new AssertionError("There is no game with the name " + lobbyName + " !");
		
		getLobby(lobbyName).addPlayerIfPossible(player);
		menu.refresh();
		
		//TODO
		return true;
	}
	
	private static void createMenu() {
		menu = new MinigameMenu(MENU_NAME, lobbies.values());
	}
	
	public static boolean hasGame(String game) {
		return models.containsKey(game);
	}

	public static Collection<BattleGame> getGamesList(){
		return models.values();
	}
	
	public static boolean hasLobby(String lobby) {
		return lobbies.containsKey(lobby);
	}
	
	public static Collection<Lobby> getLobbyList(){
		return lobbies.values();
	}
	
	public static void showMenuTo(Player player) {
		if (menu == null) {
			createMenu();
		}
		menu.showTo(player);
	}

//	public static void debugGame(String game, Player player) {
//		getGame(game).debug(player);
//	}

	public static void debugGamesList(Player player) {
		player.sendMessage("Games: " + models);
	}
			
}
