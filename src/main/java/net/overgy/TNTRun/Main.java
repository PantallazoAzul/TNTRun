package net.overgy.TNTRun;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaSetup;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.config.ArenasConfig;
import com.comze_instancelabs.minigamesapi.config.DefaultConfig;
import com.comze_instancelabs.minigamesapi.config.MessagesConfig;
import com.comze_instancelabs.minigamesapi.config.StatsConfig;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class Main extends JavaPlugin implements Listener {
	static Main m = null;
	MinigamesAPI api = null;
	PluginInstance pli = null;

	@SuppressWarnings({ "static-access" })
	public void onEnable() {
		m = this;
		api = MinigamesAPI.getAPI().setupAPI(this, "TNTRun", IArena.class, new ArenasConfig(this),
				new MessagesConfig(this), new IClassesConfig(this), new StatsConfig(this, false),
				new DefaultConfig(this, false), false);
		PluginInstance pinstance = api.pinstances.get(this);
		Bukkit.getPluginManager().registerEvents(this, this);
		pinstance.addLoadedArenas(loadArenas(this, pinstance.getArenasConfig()));
		pinstance.arenaSetup = new IArenaSetup();
		pli = pinstance;
	}

	public static ArrayList<Arena> loadArenas(JavaPlugin plugin, ArenasConfig cf) {
		ArrayList<Arena> ret = new ArrayList<Arena>();
		FileConfiguration config = cf.getConfig();
		if (!config.isSet("arenas")) {
			return ret;
		}
		for (String arena : config.getConfigurationSection("arenas.").getKeys(false)) {
			if (Validator.isArenaValid(plugin, arena, cf.getConfig())) {
				ret.add(initArena(arena));
			}
		}
		return ret;
	}

	@SuppressWarnings({ "static-access" })
	public static IArena initArena(String arena) {
		IArena a = new IArena(m, arena);
		ArenaSetup s = MinigamesAPI.getAPI().pinstances.get(m).arenaSetup;
		a.init(Util.getSignLocationFromArena(m, arena), Util.getAllSpawns(m, arena), Util.getMainLobby(m),
				Util.getComponentForArena(m, arena, "lobby"), s.getPlayerCount(m, arena, true),
				s.getPlayerCount(m, arena, false), s.getArenaVIP(m, arena));
		return a;
	}

	@SuppressWarnings({ "static-access" })
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return api.getCommandHandler().handleArgs(this, "tntrun", "/" + cmd.getName(), sender, args);
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		if (pli.global_players.containsKey(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (pli.global_players.containsKey(p.getName())) {
				IArena a = (IArena) pli.global_players.get(p.getName());
				if (a.getArenaState() == ArenaState.INGAME) {
					if (event.getCause() == DamageCause.ENTITY_ATTACK) {
						p.setHealth(20D);
						event.setCancelled(true);
						return;
					} else if (event.getCause() == DamageCause.FALL) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (pli.global_players.containsKey(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}

}
