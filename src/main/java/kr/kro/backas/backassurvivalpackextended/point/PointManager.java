package kr.kro.backas.backassurvivalpackextended.point;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.api.PlayerPointEarnEvent;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataPoint;
import net.kyori.adventure.text.Component;
import kr.kro.backas.backassurvivalpackextended.util.Palette;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class PointManager {

    public static final String POINT_UNIT = " 포인트";
    // 1분마다 5~10포인트, 동접 2명 이상이면 2배 (1포인트 = 1원 등가)
    private static final long TICK_INTERVAL = 20L * 60;
    private static final int ROLL_MIN = 5;
    private static final int ROLL_MAX = 10;
    private static final double ROLL_MEAN = (ROLL_MIN + ROLL_MAX) / 2.0;
    private static final double ROLL_NOISE = (ROLL_MAX - ROLL_MIN) / 2.0;
    // 지금까지 평균보다 많이/적게 받은 만큼 다음 롤을 반대 방향으로 당기는 보정 강도
    private static final double CORRECTION_STRENGTH = 0.4;

    private final Map<UUID, Double> rollDeviations = new HashMap<>();

    public void start() {
        Bukkit.getScheduler().runTaskTimer(BackasSurvivalPackExtended.getInstance(),
                this::tick, TICK_INTERVAL, TICK_INTERVAL);
    }

    private void tick() {
        var onlinePlayers = Bukkit.getOnlinePlayers();
        if (onlinePlayers.isEmpty()) return;

        boolean bonus = onlinePlayers.size() >= 2;
        for (Player player : onlinePlayers) {
            int roll = roll(player.getUniqueId());
            int amount = bonus ? roll * 2 : roll;
            addPoint(player, amount);
            player.sendActionBar(Component.text().append(
                    Component.text("[잠수 포인트] ", Palette.AQUA),
                    Component.text("+" + amount, Palette.YELLOW),
                    bonus
                            ? Component.text(" (동접 x2)", Palette.GRAY)
                            : Component.empty(),
                    Component.text(" 보유: ", Palette.GRAY),
                    Component.text(String.format("%,d", getPoint(player)) + POINT_UNIT, Palette.WHITE)
            ).build());
        }
    }

    /**
     * ROLL_MIN~ROLL_MAX 사이 랜덤 포인트를 굴리되, 누적 편차를 기억해서
     * 운이 나빴던 플레이어는 다음 롤이 평균 위로, 운이 좋았던 플레이어는 평균 아래로
     * 당겨지도록 보정한다. 장기적으로 모두가 평균(ROLL_MEAN/분)에 수렴한다.
     */
    private int roll(UUID uniqueId) {
        double deviation = rollDeviations.getOrDefault(uniqueId, 0.0);
        double target = ROLL_MEAN - deviation * CORRECTION_STRENGTH;
        double noise = ThreadLocalRandom.current().nextDouble(-ROLL_NOISE, ROLL_NOISE);
        int roll = (int) Math.round(target + noise);
        roll = Math.max(ROLL_MIN, Math.min(ROLL_MAX, roll));
        rollDeviations.put(uniqueId, deviation + (roll - ROLL_MEAN));
        return roll;
    }

    public static int getPoint(Player player) {
        return getUserDataPoint(player).getAmount();
    }

    public static void setPoint(Player player, int amount) {
        getUserDataPoint(player).setAmount(amount);
    }

    /** 잠수 적립: 보유량과 누적 획득량(랭킹 기준)을 함께 올린다. */
    public static void addPoint(Player player, int amount) {
        UserDataPoint data = getUserDataPoint(player);
        data.add(amount);
        data.addEarned(amount);
        Bukkit.getPluginManager().callEvent(
                new PlayerPointEarnEvent(player, amount, data.getTotalEarned()));
    }

    /** 관리자 지급 등: 보유량만 조정하고 누적 획득량(랭킹)에는 포함하지 않는다. */
    public static void addPointWithoutEarn(Player player, int amount) {
        getUserDataPoint(player).add(amount);
    }

    public static void removePoint(Player player, int amount) {
        getUserDataPoint(player).subtract(amount);
    }

    public static UserDataPoint getUserDataPoint(Player player) {
        return BackasSurvivalPackExtended.getUserManager()
                .getUser(player)
                .getDataContainer()
                .getOrLoad(UserDataPoint.class);
    }
}
