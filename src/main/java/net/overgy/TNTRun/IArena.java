package net.overgy.TNTRun;

import java.util.HashSet;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.ArenaType;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class IArena extends Arena {

	public Main m;

	private int block_destroyer;

	private HashSet<Block> blockstodestroy = new HashSet<Block>();
	private LinkedList<BlockState> blocks = new LinkedList<BlockState>();

	public IArena(Main m, String arena_id) {
		super(m, arena_id, ArenaType.REGENERATION);
		this.m = m;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void joinPlayerLobby(String playername) {
		// Test for working.
		// Bukkit.getLogger().info("Test joinPlayerLobby()");
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

		Bukkit.getLogger().info("Test started()");

		final IArena a = this;

		block_destroyer = Bukkit.getScheduler().scheduleSyncRepeatingTask(m, new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				// if (a.getPlayerAlive() == 0) {
				// a.stop();
				// return;
				// }
				// handle players
				for (String p_ : a.getArena().getAllPlayers()) {
					Player p = Bukkit.getPlayer(p_);
					// if (a.getArena().getPlayerAlive() > 1) {
					// a.stop();
					// return;
					// }
					handlePlayer(p);
				}
			}
		}, 0, 1);
	}

	@Override
	public void stop() {

		final IArena a = this;

		Bukkit.getScheduler().cancelTask(block_destroyer);
		super.stop();
		Bukkit.getScheduler().runTaskLater(m, new Runnable() {
			public void run() {
				a.reset();
			}
		}, 100);
	}

	// Para destruir el bloque.
	private final int SCAN_DEPTH = 2;

	public void destroyBlock(Location loc) {

		final IArena a = this;

		int y = loc.getBlockY() + 1;
		Block block = null;
		for (int i = 0; i <= SCAN_DEPTH; i++) {
			block = getBlockUnderPlayer(y, loc);
			y--;
			if (block != null) {
				break;
			}
		}
		if (block != null) {
			final Block fblock = block;
			if (!blockstodestroy.contains(fblock)) {
				blockstodestroy.add(fblock);
				Bukkit.getScheduler().scheduleSyncDelayedTask(m, new Runnable() {
					@SuppressWarnings("deprecation")
					@Override
					public void run() {
						if (a.getArenaState() == ArenaState.INGAME) {
							blockstodestroy.remove(fblock);
							fblock.getWorld().playEffect(fblock.getLocation(), Effect.STEP_SOUND, fblock.getTypeId());
							removeGLBlocks(fblock);
						}
					}
				}, 8);
			}
		}
	}

	private void removeGLBlocks(Block block) {
		blocks.add(block.getState());
		saveBlock(block);
		block = block.getRelative(BlockFace.DOWN);
		blocks.add(block.getState());
		saveBlock(block);
	}

	public void saveBlock(Block b) {
		b.setType(Material.AIR);
	}

	// Obtener el bloque debajo del jugador.
	private static double PLAYER_BOUNDINGBOX_ADD = 0.3;

	private Block getBlockUnderPlayer(int y, Location location) {
		PlayerPosition loc = new PlayerPosition(location.getX(), y, location.getZ());
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

	// player handlers
	public void handlePlayer(final Player player) {

		final IArena a = this;

		Location plloc = player.getLocation();
		Location plufloc = plloc.clone().add(0, -1, 0);
		// remove block under player feet
		destroyBlock(plufloc);
		// check for win
		// TODO
		// last player won
		if (a.getPlayerAlive() == 1) {
			a.stop();
			return;
		}
		// check for lose
		// not yet.
	}

	private static class PlayerPosition {

		private double x;
		private int y;
		private double z;

		public PlayerPosition(double x, int y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public Block getBlock(World world, double addx, double addz) {
			return world.getBlockAt(NumberConversions.floor(x + addx), y, NumberConversions.floor(z + addz));
		}

	}
}