package net.punchtree.battle.gui;

import net.md_5.bungee.api.ChatColor;
import net.punchtree.battle.BattleTeam;
import net.punchtree.minigames.gui.MinigameBossBar;

public class BattleBossBar extends MinigameBossBar {

	String waveMessage = "";
	String secondsMessage = "";
	
	public void setWave(BattleTeam team) {
		// TODO Auto-generated method stub
		waveMessage = team.getChatColor() + team.getName() + " is attacking!";
//		setMessage(waveMessage + " " + secondsMessage);
	}
	
	public void setSeconds(double waveTimerSeconds) {
		String seconds = waveTimerSeconds <= 3 ? String.format("%.1f", waveTimerSeconds) : String.valueOf(Math.floor(waveTimerSeconds));
		secondsMessage = ChatColor.WHITE + seconds;
		setMessage(waveMessage + " " + secondsMessage);
	}
	
}
