package kr.kro.backas.backassurvivalpackextended.teleport;

import java.util.UUID;

public record TeleportRequest(UUID to, TeleportType type) {
}
