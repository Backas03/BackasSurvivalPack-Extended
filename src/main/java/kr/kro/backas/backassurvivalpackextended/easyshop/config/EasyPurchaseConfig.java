package kr.kro.backas.backassurvivalpackextended.easyshop.config;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class EasyPurchaseConfig {
    private static EasyPurchaseConfig instance;

    public static EasyPurchaseConfig getInstance() {
        return instance;
    }

    public static void load() {
        instance = new EasyPurchaseConfig();
        instance.reload();
    }

    private Map<Material, Integer> itemStackToCostMap;

    private void reload() {
        itemStackToCostMap = new LinkedHashMap<>();
        FileUtil.createIfAbsent(getFile());
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(getFile());
        for (String key : yaml.getKeys(false)) {
            Material material = Material.getMaterial(key);
            if (material == null) {
                Bukkit.getLogger().warning("purchase config 아이템 로드 실패. cause=타입을 찾을 수 없습니다. key=" + key);
                continue;
            }
            itemStackToCostMap.put(material, yaml.getInt(key));
        }
    }

    public Map<Material, Integer> getItemStackToCostMap() {
        return itemStackToCostMap;
    }

    public File getFile() {
        return new File(BackasSurvivalPackExtended.getInstance().getDataFolder(), "purchase.yml");
    }
}
