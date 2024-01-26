package kr.kro.backas.backassurvivalpackextended.teleport;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TeleportManager {
    private final Map<UUID, LocalDateTime> lastRequestTimes = new HashMap<>();

    // from, to
    private final Map<UUID, Map<UUID, TeleportType>> requests = new HashMap<>();

    public void clearCoolTime(UUID uuid) {
        lastRequestTimes.remove(uuid);
    }

    public void tryRequest(Player from, Player to, TeleportType type) {
        if (from.equals(to)) {
            from.sendMessage(Component.text("자기 자신에게 텔레포트 요청을 보낼 수 없습니다.", NamedTextColor.RED));
            return;
        }
        // 이미 요청했다면
        if (hasRequested(from, to, type)) {
            from.sendMessage(Component.text("이미 해당 유저에게 텔레포트 요청을 보낸 상태입니다.", NamedTextColor.RED));
            return;
        }

        if (canRequest(from)) {
            // 15분이 지남, 요청가능 상태

            /* 요청 데이터 설정 */
            requests.computeIfAbsent(from.getUniqueId(), k -> new HashMap<>())
                            .put(to.getUniqueId(), type);
            lastRequestTimes.put(from.getUniqueId(), LocalDateTime.now());

            from.sendMessage(Component.text().append(
                    Component.text(to.getName(), NamedTextColor.YELLOW),
                    Component.text(" 님에게 텔레포트 요청을 보냈습니다.", NamedTextColor.WHITE)
            ));
            String message = type == TeleportType.TO ? " 님에게 텔레포트 요청이 왔습니다. " : " 님이 자신의 위치로 텔레포트를 요청했습니다. ";
            to.sendMessage(
                    Component.text().append(
                            Component.text(from.getName(), NamedTextColor.YELLOW),
                            Component.text(message, NamedTextColor.WHITE),
                            Component.text("[수락]", NamedTextColor.GREEN)
                                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("/tpaccept " + from.getName())))
                                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + from.getName())),
                            Component.space(),
                            Component.text("[거절]", NamedTextColor.RED)
                                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("/tpadeny " + from.getName())))
                                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/tpadeny " + from.getName()))
                    )
            );
            return;
        }
        Duration leftTime = getTeleportLeftTime(from);
        from.sendMessage(Component.text().append(
                Component.text("아직 텔레포트 요청을 보낼 수 없습니다.", NamedTextColor.RED),
                Component.text(String.format(" (남은시간: %d분 %d초)", leftTime.getSeconds() / 60, leftTime.getSeconds() % 60), NamedTextColor.GRAY)
        ).build());
    }

    public void tryResponse(Player to, Player from, boolean accept) {
        if (!hasRequested(from, to)) {
            to.sendMessage(Component.text("해당 유저에게 요청받은 텔레포트가 없습니다.", NamedTextColor.RED));
            return;
        }
        if (accept) {
            to.sendMessage(Component.text().append(
                    Component.text(from.getName(), NamedTextColor.YELLOW),
                    Component.text(" 님에게 온 텔레포트 요청을 ", NamedTextColor.WHITE),
                    Component.text("수락", NamedTextColor.GREEN),
                    Component.text(" 하였습니다.", NamedTextColor.WHITE)
            ));
            from.sendMessage(Component.text().append(
                    Component.text(to.getName(), NamedTextColor.YELLOW),
                    Component.text(" 님께서 텔레포트 요청을 ", NamedTextColor.WHITE),
                    Component.text("수락", NamedTextColor.GREEN),
                    Component.text(" 하였습니다.", NamedTextColor.WHITE)
            ));
            TeleportType teleportType = requests.get(from.getUniqueId()).get(to.getUniqueId());
            if (teleportType == TeleportType.TO) {
                from.teleportAsync(to.getLocation());
            }
            else if (teleportType == TeleportType.HERE) {
                to.teleportAsync(from.getLocation());
            }
        }
        else {
            to.sendMessage(Component.text().append(
                    Component.text(from.getName(), NamedTextColor.YELLOW),
                    Component.text(" 님에게 온 텔레포트 요청을 ", NamedTextColor.WHITE),
                    Component.text("거절", NamedTextColor.RED),
                    Component.text(" 하였습니다.", NamedTextColor.WHITE)
            ));
            from.sendMessage(Component.text().append(
                    Component.text(to.getName(), NamedTextColor.YELLOW),
                    Component.text(" 님이 텔레포트 요청을 ", NamedTextColor.WHITE),
                    Component.text("거절", NamedTextColor.RED),
                    Component.text(" 하였습니다.", NamedTextColor.WHITE)
            ));
        }
        requests.computeIfAbsent(from.getUniqueId(), k -> new HashMap<>())
                .remove(to.getUniqueId());
    }

    public Duration getTeleportLeftTime(Player player) {
        LocalDateTime lastRequestTime = getLastRequestTime(player);
        return Duration.between(LocalDateTime.now(), lastRequestTime.plusMinutes(15));
    }

    public LocalDateTime getLastRequestTime(Player player) {
        return lastRequestTimes.getOrDefault(player.getUniqueId(), LocalDateTime.now());
    }

    public boolean hasRequested(Player from, Player to) {
        return requests.get(from.getUniqueId()) != null && requests.get(from.getUniqueId()).containsKey(to.getUniqueId());
    }

    public boolean hasRequested(Player from, Player to, TeleportType type) {
        return requests.get(from.getUniqueId()) != null && requests.get(from.getUniqueId()).get(to.getUniqueId()) == type;
    }

    public boolean canRequest(Player player) {
        LocalDateTime lastRequestTime = lastRequestTimes.get(player.getUniqueId());
        if (lastRequestTime == null) return true;

        LocalDateTime before15Min = LocalDateTime.now().minusMinutes(15);
        return before15Min.isAfter(lastRequestTime);
    }

    public Map<UUID, TeleportType> getPendingTeleports(Player to) {
        Map<UUID, TeleportType> results = new HashMap<>();
        for (Map.Entry<UUID, Map<UUID, TeleportType>> entry : requests.entrySet()) {
            Set<UUID> toUUIDs = entry.getValue().keySet();
            if (toUUIDs.contains(to.getUniqueId())) {
                results.put(entry.getKey(), entry.getValue().get(to.getUniqueId()));
            }
        }
        return results;
    }
}
