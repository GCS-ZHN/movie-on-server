/*
 * Copyright © 2021 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 *
 * Licensed under the Apache License, Version 2.0 (thie "License");
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at
 *
 *       http://wwww.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language govering permissions and
 * limitations under the License.
 */
package top.gcszhn.movie;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.gcszhn.movie.security.ShaEncrypt;

@SpringBootApplication
@ServletComponentScan
public class App implements WebMvcConfigurer {
    public static ConfigurableApplicationContext context;
    /**工作目录 */
    public static String workDir;
    public static void main(String[] args) {
        setWorkDir();
        SpringApplication application = new SpringApplication(App.class);
        application.addListeners(new ApplicationPidFileWriter("app.pid"));
        context = application.run(args);
    }

    @Autowired
    public App(ApplicationArguments args) {
        if (args.containsOption("generatePasswd")) {
            String passwd = args.getOptionValues("generatePasswd").get(0);
            String salt = ShaEncrypt.getSalt(10);
            String digest = ShaEncrypt.encrypt(passwd, salt);
            System.out.println(String.format("Password is SHA256:%s:%s", salt, digest));
            System.exit(0);
        }
    }
    /**
     * 配置项目目录，实际上Spring自身日志有该信息，但无法获取
     */
    public static void setWorkDir() {
        workDir = App.class.getProtectionDomain().getCodeSource().getLocation().toString();
        workDir = workDir.replaceFirst("^[^/]*file:", "").replaceFirst("!/BOOT-INF/classes!/", "");
        workDir = new File(workDir).getParent();
    }
}
