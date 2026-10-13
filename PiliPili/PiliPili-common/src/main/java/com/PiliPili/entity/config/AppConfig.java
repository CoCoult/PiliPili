package com.PiliPili.entity.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
public class AppConfig {
    /**
     * 读取文件
     */
    @Value("${project.folder:}")
    private String projectFolder;

    /**
     * 读取配置
     */
    @Value("${admin.account:}")
    private String adminAccount;

    /**
     * 读取配置
     */
    @Value("${admin.password:}")
    private String adminPassword;

    @Getter
    @Value("${es.host.port:127.0.0.1:9200}")
    private String esHostPort;

    @Value("${es.index.video.name:pilipili_video}")
    private String esIndexVideoName;

    public void setEsHostPort(String esHostPort) {
        this.esHostPort = esHostPort;
    }

    public String getEsIndexVideoName() {
        return esIndexVideoName;
    }

    public void setEsIndexVideoName(String esIndexVideoName) {
        this.esIndexVideoName = esIndexVideoName;
    }

    public String getProjectFolder() {
        return projectFolder;
    }

    public void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    public String getAdminAccount() {
        return adminAccount;
    }

    public void setAdminAccount(String adminAccount) {
        this.adminAccount = adminAccount;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}
