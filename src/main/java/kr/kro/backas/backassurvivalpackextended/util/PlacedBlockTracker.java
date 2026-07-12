package kr.kro.backas.backassurvivalpackextended.util;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 플레이어가 직접 설치한 블록(작물/광석)의 좌표를 기록한다.
 * 설치->파괴 반복으로 경험치를 버는 악용을 막기 위한 것으로,
 * 서버 재시작 후에도 유지되도록 placed-blocks.yml에 저장된다.
 */
public final class PlacedBlockTracker {

    private static final Set<String> PLACED = ConcurrentHashMap.newKeySet();
    private static final long SAVE_INTERVAL = 20L * 60 * 5;

    private PlacedBlockTracker() {
    }

    public static void load() {
        PLACED.clear();
        PLACED.addAll(YamlConfiguration.loadConfiguration(getFile()).getStringList("blocks"));
        Bukkit.getScheduler().runTaskTimerAsynchronously(BackasSurvivalPackExtended.getInstance(),
                PlacedBlockTracker::save, SAVE_INTERVAL, SAVE_INTERVAL);
    }

    public static synchronized void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("blocks", new ArrayList<>(PLACED));
        try {
            yaml.save(getFile());
        } catch (IOException e) {
            BackasSurvivalPackExtended.getInstance().getLogger()
                    .warning("설치 블록 목록을 저장하는 중 오류가 발생하였습니다: " + e.getMessage());
        }
    }

    public static void mark(Block block) {
        PLACED.add(key(block));
    }

    /** 설치 표시가 있었으면 지우고 true를 반환한다. */
    public static boolean unmark(Block block) {
        return PLACED.remove(key(block));
    }

    private static String key(Block block) {
        return block.getWorld().getName() + ";" + block.getX() + ";" + block.getY() + ";" + block.getZ();
    }

    private static File getFile() {
        return new File(BackasSurvivalPackExtended.getInstance().getDataFolder(), "placed-blocks.yml");
    }
}
