package fr.loferga.lost_settlers.game;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.map.camps.CampMngr;
import fr.loferga.lost_settlers.tasks.ComeBack;
import fr.loferga.lost_settlers.tasks.DogAnger;
import fr.loferga.lost_settlers.tasks.Game;
import fr.loferga.lost_settlers.tasks.NaturalRegen;
import fr.loferga.lost_settlers.tasks.RespawnCooldown;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class EndGameMngr {
	

	public static void stop(Team team) {
		long gameTime = System.currentTimeMillis() - Game.getStartTime();
		Game.stop();
		if (team != null) Bukkit.broadcastMessage(Func.format("&eL'équipe des " + team.getDisplayName() + "&e a remporté la victoire!"));
		String[] rt = Func.toReadableTime(gameTime);
		Bukkit.broadcastMessage(Func.format("&eLa partie s'est terminée en &3" + rt[0] + rt[1] + rt[2] + "&e."));
		ComeBack.stop();
		DogAnger.stop();
		NaturalRegen.stop();
		RespawnCooldown.stop();
		CampMngr.clearFlags();
		CampMngr.clearCamps();
		TeamMngr.resetKillerMemory();
		Main.map.getWorldBorder().setSize(Double.MAX_VALUE);
		Main.map.setDifficulty(Difficulty.PEACEFUL);
		for (Player p : TeamMngr.getTeamedPlayers())
			if (p.getGameMode() == GameMode.SURVIVAL)
				p.setGameMode(GameMode.ADVENTURE);
		for (Team t : TeamMngr.get()) {
			t.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
		}
	}
	
	public static void winCondition(Team t) {
		if (t != null) {
			if (allCampsBelongTo(t) || noOtherTeamAlive(t)) {
				stop(t);
				for (Player p : TeamMngr.getTeamedPlayers())
					p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 10.0f, 1.0f);
			}
		} else {
			for (Team team : TeamMngr.get()) {
				if (allCampsBelongTo(team) || noOtherTeamAlive(team)) {
					stop(team);
					for (Player p : TeamMngr.getTeamedPlayers())
						p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 10.0f, 1.0f);
				}
			}
		}
	}
	
	private static boolean allCampsBelongTo(Team t) {
		for (Camp camp : CampMngr.get()) {
			if (!camp.isOwner(t))
				return false;
		}
		return true;
	}
	
	private static boolean noOtherTeamAlive(Team wteam) {
		for (Team team : TeamMngr.get())
			if (team != wteam)
				if (TeamMngr.getAliveTeamMembers(team).size() > 0)
					return false;
		return true;
	}

}
