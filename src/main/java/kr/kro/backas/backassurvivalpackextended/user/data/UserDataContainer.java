package kr.kro.backas.backassurvivalpackextended.user.data;

import kr.kro.backas.backassurvivalpackextended.BackasSurvivalPackExtended;
import kr.kro.backas.backassurvivalpackextended.api.UserDataPreLoadDoneEvent;
import kr.kro.backas.backassurvivalpackextended.user.User;
import kr.kro.backas.backassurvivalpackextended.user.data.model.UserDataMoney;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class UserDataContainer {

    public static final Set<Class<? extends UserData>> EARLY_LOADS = Set.of(
            UserDataMoney.class
    );

    public static final Logger LOGGER = LoggerFactory.getLogger(UserDataContainer.class);
    private final User user;
    private final Map<Class<? extends UserData>, UserData> userDataMap;

    @SuppressWarnings("unchecked")
    public UserDataContainer(User user) {
        this.user = user;
        this.userDataMap = new HashMap<>();

        // 접속시 바로 로드해야할 유저 데이터를 비동기적으로 불러옵니다.
        Bukkit.getScheduler().runTaskAsynchronously(BackasSurvivalPackExtended.getInstance(), () -> {
            YamlConfiguration yaml = loadYaml();
            for (Class<? extends UserData> userDataClass : EARLY_LOADS) {
                try {
                    Method loaderMethod = userDataClass.getMethod("loader");
                    UserDataLoader<? extends UserData> loader = (UserDataLoader<? extends UserData>) loaderMethod.invoke(null);
                    userDataMap.put(userDataClass, loader.load(yaml));
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    LOGGER.error("유저 데이터를 가져오는 중 오류가 발생하였습니다. userDataClass={}", userDataClass, e);
                }
            }
            Bukkit.getPluginManager().callEvent(new UserDataPreLoadDoneEvent(user));
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
        if (userData != null) return userData;

        userData = fetch(userDataClass);
        userDataMap.put(userDataClass, userData);
        return userData;
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
