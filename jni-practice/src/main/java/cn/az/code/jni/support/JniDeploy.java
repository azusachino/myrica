package cn.az.code.jni.support;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import cn.az.code.jni.consts.CommonConsts;

/**
 * Helper (deploy so files to current directory)
 */
public class JniDeploy {

    private static final JniDeploy INSTANCE = new JniDeploy();

    private JniDeploy() {
    }

    public static JniDeploy getInstance() {
        return INSTANCE;
    }

    public void deploy() {
        File libFolder = new File("./");
        File helloLib = new File(libFolder.getPath(), CommonConsts.HELLO_JNI);
        if (!helloLib.exists()) {
            ClassPathResource cpr = new ClassPathResource(CommonConsts.HELLO_JNI);
            try (InputStream is = cpr.getInputStream()) {
                FileUtils.copyInputStreamToFile(is, helloLib);
            } catch (Exception e) {
                throw new RuntimeException("fail to deploy so libraries");
            }
        }
    }
}
