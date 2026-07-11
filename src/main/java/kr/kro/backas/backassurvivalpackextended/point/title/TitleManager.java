package kr.kro.backas.backassurvivalpackextended.point.title;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.point.PointManager;
import kr.kro.backas.backassurvivalpackextended.user.User;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataPoint;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class TitleManager {

    private TitleManager() {
    }

    @Nullable
    public static Title getEquipped(Player player) {
        // 채팅 이벤트(비동기)에서도 호출되므로 파일 IO 없이 캐시된 데이터만 사용한다.
        User user = BackasSurvivalPackExtended.getUserManager().getUserUnsafe(player.getUniqueId());
        if (user == null) return null;
        UserDataPoint data = user.getDataContainer().get(UserDataPoint.class);
        if (data == null) return null;
        return Title.byId(data.getEquippedTitle());
    }

    public static void equip(Player player, Title title) {
        PointManager.getUserDataPoint(player).setEquippedTitle(title.name());
        applyTabName(player);
    }

    public static void unequip(Player player) {
        PointManager.getUserDataPoint(player).setEquippedTitle(null);
        applyTabName(player);
    }

    public static void applyTabName(Player player) {
        Title title = Title.byId(PointManager.getUserDataPoint(player).getEquippedTitle());
        if (title == null) {
            player.playerListName(null);
            return;
        }
        player.playerListName(Component.text().append(
                title.display(),
                Component.space(),
                Component.text(player.getName())
        ).build());
    }
}
