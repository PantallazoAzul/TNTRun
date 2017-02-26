package net.overgy.TNTRun;

import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.config.ClassesConfig;

public class IClassesConfig extends ClassesConfig {
	public IClassesConfig(JavaPlugin plugin) {
		super(plugin, true);
		this.getConfig().options().header("No hay clases para OvergyTNTRun.");
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}
}
