package com.solace.qk.solace;

import com.solace.qk.QKConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SolConfig {

    public SolConfig() {}
    public SolConfig(String host, String msgVpn, String clientUsername, String clientPassword) {
        this.host = host;
        this.msgVpn = msgVpn;
        this.clientUsername = clientUsername;
        this.clientPassword = clientPassword;
    }

    static public SolConfig fromFile(String filename) {
        File file = new File(filename);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            Yaml yaml = new Yaml();
            SolConfig config = yaml.load(fis);
            return config;
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        return null;

    }

    public String getHost() {
        return host;
    }
    public String getMsgVpn() {
        return msgVpn;
    }
    public String getClientUsername() {
        return clientUsername;
    }
    public String getClientPassword() {
        return clientPassword;
    }

    public void setHost(String host) { this.host = host; }
    public void setMsgVpn(String msgVpn) { this.msgVpn = msgVpn; }
    public void setClientUsername(String clientUsername) { this.clientUsername = clientUsername; }
    public void setClientPassword(String clientPassword) { this.clientPassword = clientPassword; }

    @Override
    public String toString() {
        return "SolConfig{" +
                "host='" + host + '\'' +
                ", msgVpn='" + msgVpn + '\'' +
                ", clientUsername='" + clientUsername + '\'' +
                ", clientPassword='" + clientPassword + '\'' +
                '}';
    }

    private String host;
    private String msgVpn;
    private String clientUsername;
    private String clientPassword;

}
