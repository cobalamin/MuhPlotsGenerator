package de.chipf0rk.MuhPlotsGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.flowpowered.nbt.ByteArrayTag;
import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.ShortTag;
import com.flowpowered.nbt.StringTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.stream.NBTInputStream;

public class PlotSchematic {
	public final int width;
	public final int height;
	public final int length;
	public final byte[] blocks;
	public final byte[] data;

	@SuppressWarnings("resource")
	public PlotSchematic(String filename) throws IOException, IllegalArgumentException {
		File file = new File(filename);
		FileInputStream stream = new FileInputStream(file);
		NBTInputStream nbtStream = new NBTInputStream(stream);

		CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
		try {
			if (!schematicTag.getName().equals("Schematic")) {
				throw new IllegalArgumentException("Tag \"Schematic\" does not exist or is not first");
			}
	
			CompoundMap schematic = schematicTag.getValue();
			if (!schematic.containsKey("Blocks")) {
				throw new IllegalArgumentException("Schematic file is missing a \"Blocks\" tag");
			}
	
			this.width = getChildTag(schematic, "Width", ShortTag.class).getValue();
			this.length = getChildTag(schematic, "Length", ShortTag.class).getValue();
			this.height = getChildTag(schematic, "Height", ShortTag.class).getValue();
	
			String materials = getChildTag(schematic, "Materials", StringTag.class).getValue();
			if (!materials.equals("Alpha")) {
				throw new IllegalArgumentException("Schematic file is not an Alpha schematic");
			}
			
			this.blocks = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
			this.data = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
		} catch(IllegalArgumentException e) {
			stream.close();
			nbtStream.close();
			throw e;
		}

		stream.close();
		nbtStream.close();
	}
	
	private byte Nibble4(byte[] arr, int index) {
		return (byte) (index % 2 == 0 ?
			arr[index/2] & 0x0F :
			(arr[index/2] >> 4) & 0x0F);
	}
	
	public byte getBlockId(int x, int y, int z, int walkwayWidth) {
		int blockPos = getBlockPos(x, y, z, walkwayWidth);
		if(blockPos == -1) return -1;

		byte blockID = this.blocks[blockPos];
		return blockID;
	}
	
	public byte getBlockData(int x, int y, int z) {
		int blockPos = getBlockPos(x, y, z);
		byte blockData = Nibble4(data, blockPos);
		
		return blockData;
	}
	
	private int getBlockPos(int x, int y, int z, int walkwayWidth) {
		// check if walkway
		int fullWidth = width + walkwayWidth;
		int fullLength = length + walkwayWidth;
		int xClamped = gaussianMod(x, fullWidth);
		int zClamped = gaussianMod(z, fullLength);
		
		// => walkway block
		
		if(xClamped >= width
				|| zClamped >= length) {
			return -1;
		}
		// => otherwise, it's not a walkway block
		else {
			return getBlockPos(xClamped, y, zClamped);
		}
	}
	
	private int getBlockPos(int x, int y, int z) {
		x = gaussianMod(x, width);
		z = gaussianMod(z, length);
		
		return y*width*length + z*width + x;
	}

	private static <T extends Tag<?>> T getChildTag(CompoundMap items, String key, Class<T> expected) throws IllegalArgumentException {
		if (!items.containsKey(key)) {
			throw new IllegalArgumentException("Schematic file is missing a \"" + key + "\" tag");
		}
		Tag<?> tag = items.get(key);
		if (!expected.isInstance(tag)) {
			throw new IllegalArgumentException(key + " tag is not of tag type " + expected.getName());
		}
		return expected.cast(tag);
	}
	
	private int gaussianMod(int num, int denom) {
		int m = num % denom;
		if(m < 0) m = denom + m;
		return m;
	}
}
