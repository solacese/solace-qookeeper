package com.solace.qk;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Client configuration object. This is serialized for event transmission by org.yaml.snakeyaml
 */
public class QKClientConfig {

    public QKClientConfig() {}
    public QKClientConfig(String groupName, String serviceAddress) {
        this.groupName = groupName;
        this.serviceAddress = serviceAddress;
    }

    static public QKClientConfig fromFile(String filename) {
        File file = new File(filename);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            Yaml yaml = new Yaml();
            QKClientConfig config = yaml.load(fis);
            return config;
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        return null;

    }

    public String getGroupName() {
        return groupName;
    }
    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setGroupName(String groupName) { this.groupName = groupName; }
    public void setServiceAddress(String serviceAddress) { this.serviceAddress = serviceAddress; }

    @Override
    public String toString() {
        return "QKClientConfig{" +
                "groupName='" + groupName + '\'' +
                ", serviceAddress='" + serviceAddress + '\'' +
                '}';
    }

    private String groupName;
    private String serviceAddress;
}
