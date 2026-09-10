package com.todolist.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.esportes")
public class EsportesApiProperties {

    private boolean enabled = true;
    private String apiBaseUrl = "https://www.thesportsdb.com/api/v1/json";
    private String apiKey = "123";
    private String cacheName = "esportes";
    private int connectTimeoutMs = 4000;
    private int readTimeoutMs = 6000;
    private List<Liga> ligas = new ArrayList<>();
    private List<String> esportesDia = List.of("Soccer", "Basketball", "Fighting", "Motorsport");

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public List<Liga> getLigas() {
        return ligas;
    }

    public void setLigas(List<Liga> ligas) {
        this.ligas = ligas;
    }

    public List<String> getEsportesDia() {
        return esportesDia;
    }

    public void setEsportesDia(List<String> esportesDia) {
        this.esportesDia = esportesDia;
    }

    public static class Liga {
        private int id;
        private String esporte;
        private String nome;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getEsporte() {
            return esporte;
        }

        public void setEsporte(String esporte) {
            this.esporte = esporte;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }
    }
}
