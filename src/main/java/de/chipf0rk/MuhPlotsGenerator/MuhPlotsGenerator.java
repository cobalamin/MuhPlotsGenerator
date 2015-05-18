package de.chipf0rk.MuhPlotsGenerator;

import java.io.File;
import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public class MuhPlotsGenerator extends JavaPlugin {
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		getLogger().info(getName() + " v" + getDescription().getVersion() + " has been enabled!");
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		try {
			return getChunkGenerator(worldName);
		} catch(Exception e) {
			getLogger().severe("Couldn't create the world generator for world " + worldName + "!");
			e.printStackTrace();
			return super.getDefaultWorldGenerator(worldName, id);
		}
	}
	
	private PlotChunkGenerator getChunkGenerator(String worldName) throws IllegalArgumentException, IOException {
		// reading the plot schematic
		String schematicFilename = getDataFolder() + File.separator + "schematics"
			+ File.separator + worldName + ".schematic";
		PlotSchematic schematic = new PlotSchematic(schematicFilename);
		
		// collecting the world config
		ConfigurationSection worldConfig = getWorldConfig(worldName);
		if(worldConfig == null) {
			throw new IllegalArgumentException("World configuration for world " + worldName + " does not exist. "
				+ "Please refer to the default configuration and configure this world.");
		}
		
		Material walkwayMaterial = Material.valueOf(worldConfig.getString("walkway_material"));
		int walkwayWidth = worldConfig.getInt("walkway_width");
		int walkwayHeight = worldConfig.getInt("walkway_height");
		
		if(walkwayMaterial == null || walkwayWidth < 0 || walkwayHeight < 0) {
			throw new IllegalArgumentException("Invalid configuration for world " + worldName + ". Check that all configuration values exist and are valid.");
		}
		
		return new PlotChunkGenerator(schematic, walkwayMaterial, walkwayWidth, walkwayHeight);
	}
	
	private ConfigurationSection getWorldConfig(String worldName) {
		return this.getConfig().getConfigurationSection("plotworlds." + worldName);
	}
}
