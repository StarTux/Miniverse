package com.cavetale.miniverse;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;

public final class MaterialMap {
    Map<Material, Material> replace = new EnumMap<>(Material.class);

    MaterialMap() {
        for (Material mat : Material.values()) {
            Material full = replaceFullBlock(mat);
            replace.put(mat, full != null ? full : mat);
        }
        replace.put(Material.SNOW, Material.SNOW_BLOCK);
        replace.put(Material.TALL_GRASS, Material.GRASS);
    }

    static Material replaceFullBlock(Material mat) {
        for (String ending : Arrays.asList("_SLAB", "_STAIRS", "_WALL")) {
            String name = mat.name();
            if (!name.endsWith(ending)) continue;
            String pre = name.substring(0, name.length() - ending.length());
            try {
                Material result = Material.valueOf(pre);
                if (result.isBlock()) return result;
            } catch (Exception e) { }
            try {
                Material result = Material.valueOf(pre + "_PLANKS");
                if (result.isBlock()) return result;
            } catch (Exception e) { }
            try {
                Material result = Material.valueOf(pre + "_BLOCK");
                if (result.isBlock()) return result;
            } catch (Exception e) { }
            try { // BRICK => BRICKS, STONE_BRICK => STONE_BRICKS
                Material result = Material.valueOf(pre + "S");
                if (result.isBlock()) return result;
            } catch (Exception e) { }
            return null;
        }
        return null;
    }

    Material of(Material in) {
        return replace.get(in);
    }
}
