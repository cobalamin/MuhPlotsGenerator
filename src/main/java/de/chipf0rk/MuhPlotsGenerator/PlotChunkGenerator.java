package de.chipf0rk.MuhPlotsGenerator;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

public class PlotChunkGenerator extends ChunkGenerator {
	private PlotSchematic schematic;
	private final Material walkwayMaterial;
	private final int walkwayWidth;
	private final int walkwayHeight;
	
	public PlotChunkGenerator(PlotSchematic schematic, Material walkwayMaterial, int walkwayWidth, int walkwayHeight) {
		this.schematic = schematic;
		this.walkwayMaterial = walkwayMaterial;
		this.walkwayWidth = walkwayWidth;
		this.walkwayHeight = walkwayHeight;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid) {
		byte[][] result = new byte[world.getMaxHeight() / 16][];
		
		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				biomeGrid.setBiome(x, z, Biome.PLAINS);

				// bottom layer is always bedrock
				setBlock(result, x, 0, z, (byte)Material.BEDROCK.getId());

				for(int y = 1; y < world.getMaxHeight(); y++) {
					byte blockid = schematic.getBlockId(x + chunkX*16, y, z + chunkZ*16, walkwayWidth);
					if(blockid == -1 && y < this.walkwayHeight) {
						// if blockid is -1 (this indicates that there's a walkway block):
						// set walkway material, up until walkwayY
						setBlock(result, x, y, z, (byte)walkwayMaterial.getId());
					}
					else {
						setBlock(result, x, y, z, blockid);
					}
				}
			}
		}

		return result;
	}
	
	@Override
	public boolean canSpawn(World world, int x, int z) {
		return true;
	}

	private void setBlock(byte[][] result, int x, int y, int z, byte blkid) {
		if (result[y >> 4] == null) {
			result[y >> 4] = new byte[4096];
		}
		result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
	}
}