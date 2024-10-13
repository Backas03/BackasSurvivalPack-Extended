package kr.kro.backas.backassurvivalpackextended.util;

import java.io.File;

public class FileUtil {

    public static void createIfAbsent(File file) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
