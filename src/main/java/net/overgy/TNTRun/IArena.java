package net.overgy.TNTRun;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.ArenaType;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class IArena extends Arena {

	public Main m;

	private int block_destroyer;

	public IArena(Main m, String arena_id) {
		super(m, arena_id, ArenaType.REGENERATION);
		this.m = m;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void joinPlayerLobby(String playername) {
		super.joinPlayerLobby(playername);
		return;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void start(boolean tp) {

		final IArena a = this;

		super.start(tp);
		Bukkit.getScheduler().runTaskLater(m, new Runnable() {
			public void run() {
				for (String p_ : a.getAllPlayers()) {
					if (Validator.isPlayerOnline(p_)) {
						Player p = Bukkit.getPlayer(p_);
						p.setWalkSpeed(0.2F);
						p.setFoodLevel(20);
					}
				}
			}
		}, 20L);
	}

	@Override
	public void started() {

		final IArena a = this;

		block_destroyer = Bukkit.getScheduler().scheduleSyncRepeatingTask(m, new Runnable() {
			@Override
			public void run() {
				removeBlocksUnderPlayer(a);
			}
		}, 0, 1);
	}

	@Override
	public void stop() {

		Bukkit.getScheduler().cancelTask(block_destroyer);
		super.stop();
	}

	// Para destruir bloques.
	@SuppressWarnings("deprecation")
	public static void removeBlocksUnderPlayer(IArena arena) {

		final IArena a = arena;
		final Main m = Main.m;

		for (String player : a.getAllPlayers()) {
			Player p = Bukkit.getPlayer(player);
			if (m.pli.global_players.containsKey(p.getName()) && !m.pli.global_lost.containsKey(p.getName())) {
				if (a.getArenaState() == ArenaState.INGAME) {
					Location loc = p.getPlayer().getLocation().add(0, -1, 0);
					// remove block under player feet
					Block blocktoremove1 = getBlockUnderPlayer(loc);
					if (blocktoremove1 == null)
						return;
					Block blocktoremove2 = blocktoremove1.getRelative(BlockFace.DOWN);
					if (blocktoremove1 != null && blocktoremove2 != null) {
						final Location blockloc1 = blocktoremove1.getLocation();
						final Location blockloc2 = blocktoremove2.getLocation();

						Bukkit.getScheduler().scheduleSyncDelayedTask(m, new Runnable() {
							@Override
							public void run() {
								// Check if game hasn't stopped meanwhile
								if (a.getArenaState() == ArenaState.INGAME) {
									a.getSmartReset().addChanged(blockloc1.getBlock(), false);
									blockloc1.getBlock().setType(Material.AIR);
									a.getSmartReset().addChanged(blockloc2.getBlock(), false);
									blockloc2.getBlock().setType(Material.AIR);
								}
							}
						}, 8);
					}
				}
			}
		}
	}

	// Obtener el bloque debajo del jugador.
	private static double PLAYER_BOUNDINGBOX_ADD = 0.3;

	private static Block getBlockUnderPlayer(Location location) {
		PlayerPosition loc = new PlayerPosition(location);
		Block b11 = loc.getBlock(location.getWorld(), +PLAYER_BOUNDINGBOX_ADD, -PLAYER_BOUNDINGBOX_ADD);
		if (b11.getType() != Material.AIR) {
			return b11;
		}
		Block b12 = loc.getBlock(location.getWorld(), -PLAYER_BOUNDINGBOX_ADD, +PLAYER_BOUNDINGBOX_ADD);
		if (b12.getType() != Material.AIR) {
			return b12;
		}
		Block b21 = loc.getBlock(location.getWorld(), +PLAYER_BOUNDINGBOX_ADD, +PLAYER_BOUNDINGBOX_ADD);
		if (b21.getType() != Material.AIR) {
			return b21;
		}
		Block b22 = loc.getBlock(location.getWorld(), -PLAYER_BOUNDINGBOX_ADD, -PLAYER_BOUNDINGBOX_ADD);
		if (b22.getType() != Material.AIR) {
			return b22;
		}
		return null;
	}

	private static class PlayerPosition {

		private double x;
		private int y;
		private double z;

		@SuppressWarnings("unused")
		public PlayerPosition(double x, int y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public PlayerPosition(Location location) {
			this.x = location.getX();
			this.y = (int) location.getY();
			this.z = location.getZ();
		}

		public Block getBlock(World world, double addx, double addz) {
			return world.getBlockAt(NumberConversions.floor(x + addx), y, NumberConversions.floor(z + addz));
		}

	}
}
