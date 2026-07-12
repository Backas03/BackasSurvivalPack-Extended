package kr.kro.backas.backassurvivalpackextended.user.data;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.api.UserDataPreLoadDoneEvent;
import kr.kro.backas.backassurvivalpackextended.user.User;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataFarming;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMining;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMoney;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMoneyUse;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataPoint;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UserDataContainer {

    public static final Set<Class<? extends UserData>> EARLY_LOADS = Set.of(
            UserDataMoney.class,
            UserDataMoneyUse.class,
            UserDataPoint.class,
            UserDataFarming.class,
            UserDataMining.class
    );

    public static final Logger LOGGER = LoggerFactory.getLogger(UserDataContainer.class);
    private final User user;
    private final Map<Class<? extends UserData>, UserData> userDataMap;

    @SuppressWarnings("unchecked")
    public UserDataContainer(User user) {
        this.user = user;
        // 비동기 프리로드와 메인 스레드 getOrLoad가 동시에 접근하므로 동시성 맵 사용
        this.userDataMap = new ConcurrentHashMap<>();

        // 접속시 바로 로드해야할 유저 데이터를 비동기적으로 불러옵니다.
        Bukkit.getScheduler().runTaskAsynchronously(BackasSurvivalPackExtended.getInstance(), () -> {
            YamlConfiguration yaml = loadYaml();
            for (Class<? extends UserData> userDataClass : EARLY_LOADS) {
                try {
                    Method loaderMethod = userDataClass.getMethod("loader");
                    UserDataLoader<? extends UserData> loader = (UserDataLoader<? extends UserData>) loaderMethod.invoke(null);
                    UserData loaded = loader.load(yaml);
                    // 프리로드가 끝나기 전에 getOrLoad로 이미 로드된 인스턴스가 있으면
                    // 그쪽에 기록된 변경(포인트/경험치 적립)이 유실되지 않도록 덮어쓰지 않는다.
                    if (loaded != null) {
                        userDataMap.putIfAbsent(userDataClass, loaded);
                    }
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    LOGGER.error("유저 데이터를 가져오는 중 오류가 발생하였습니다. userDataClass={}", userDataClass, e);
                }
            }
            Bukkit.getScheduler().runTask(BackasSurvivalPackExtended.getInstance(), () ->
                    Bukkit.getPluginManager().callEvent(new UserDataPreLoadDoneEvent(user))
            );
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends UserData> T fetch(Class<T> userDataClass) {
        try {
            Method loaderMethod = userDataClass.getMethod("loader");
            UserDataLoader<T> loader = (UserDataLoader<T>) loaderMethod.invoke(null);
            return loader.load(loadYaml());
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            LOGGER.error("유저 데이터를 가져오는 중 오류가 발생하였습니다. userDataClass={}", userDataClass, e);
        }
        return null;
    }

    public <T extends UserData> CompletableFuture<T> getOrLoadAsync(Class<T> userDataClass) {
        return CompletableFuture.supplyAsync(() -> getOrLoad(userDataClass));
    }

    public <T extends UserData> T getOrLoad(Class<T> userDataClass) {
        T userData = get(userDataClass);
        if (userData != null) {
            return userData;
        }

        userData = fetch(userDataClass);
        if (userData == null) {
            return null;
        }
        // 로드 도중 다른 스레드(프리로드 등)가 먼저 넣었다면 그 인스턴스를 사용한다.
        UserData existing = userDataMap.putIfAbsent(userDataClass, userData);
        return existing != null ? userDataClass.cast(existing) : userData;
    }

    public <T extends UserData> T get(Class<T> userDataClass) {
        // 로드된 데이터 없으면 null 반환
        return userDataClass.cast(userDataMap.get(userDataClass));
    }

    public <T extends UserData> boolean save(Class<T> userDataClass) {
        T data = get(userDataClass);
        if (data == null) return false;

        YamlConfiguration yaml = loadYaml();
        data.setToYaml(yaml);
        try {
            yaml.save(getDataFile());
            return true;
        } catch (IOException e) {
            LOGGER.error("유저 데이터를 저장하는 중 오류가 발생하였습니다. userDataClass={}", userDataClass, e);
            return false;
        }
    }

    public void saveAll() {
        YamlConfiguration yaml = loadYaml();
        for (UserData userData : userDataMap.values()) {
            userData.setToYaml(yaml);
        }
        try {
            yaml.save(getDataFile());
        } catch (IOException e) {
            LOGGER.error("유저 데이터를 저장하는 중 오류가 발생하였습니다.", e);
        }
    }

    public <T extends UserData> CompletableFuture<Boolean> saveAsync(Class<T> userDataClass) {
        return CompletableFuture.supplyAsync(() -> save(userDataClass));
    }

    public File getDataFile() {
        return new File(BackasSurvivalPackExtended.getInstance().getDataFolder(),
                user.getUniqueId() + ".yml");
    }

    public YamlConfiguration loadYaml() {
        return YamlConfiguration.loadConfiguration(getDataFile());
    }
}
