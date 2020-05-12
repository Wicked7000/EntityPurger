package com.wicked.entitypurger.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {
    private static final String CONFIG_NAME = "entitypurger.json";

    private final ObjectMapper objectMapper;
    private final String version;
    private final Logger logger;
    private final File configFileRaw;

    private List<String> whitelist;
    private List<String> blacklist;
    private boolean loggingEnabled = true;
    private int defaultThreshold = 20;
    private int defaultLifetime = 120;
    private Map<String, EntitySettings> entitySettings;
    private int checkTimeSeconds = 20;
    private boolean purgeTamedEntities = false;
    private EntitySettings defaultEntitySettings;

    private boolean enabled = false;
    private boolean lookMode = false;

    public ConfigManager(FMLPreInitializationEvent event, String version, Logger logger) {
        this.logger = logger;
        this.version = version;
        this.configFileRaw = new File(event.getModConfigurationDirectory().getAbsolutePath() + "/"+CONFIG_NAME);
        this.objectMapper =  new ObjectMapper();

        createDefaultConfigIfNeeded();
        JsonNode config = safeReadConfig().getConfiguration();
        if(Objects.isNull(config)){
            throw new ConfigException("EntityPurger: Default configuration could not be loaded!");
        }

        defaultEntitySettings = new EntitySettings(defaultThreshold, true, defaultLifetime, "*");
        ConfigLoadResult result = loadDataFromConfiguration(config);
        if(!result.isSuccessful()){
            throw new ConfigException(String.format("EntityPurger: Failed to load config: %s, in config: %s", result.getErrorMessage(), CONFIG_NAME));
        }
    }

    private void replaceOutdatedConfigFile(List<String> defaultLines) throws IOException, ConfigException{
        if(configFileRaw.delete()){
            Files.write(configFileRaw.toPath(), defaultLines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        }else{
            throw new ConfigException(String.format("Configuration '%s' is outdated please delete it!", CONFIG_NAME));
        }
    }

    private void createDefaultConfigIfNeeded(){
        InputStream configDefaultRaw = getClass().getClassLoader().getResourceAsStream("config.json");
        if(configDefaultRaw != null){
            List<String> defaultLines = new BufferedReader(new InputStreamReader(configDefaultRaw)).lines().collect(Collectors.toList());

            try {
                if(!configFileRaw.exists()){
                    if(configFileRaw.createNewFile()){
                        Files.write(configFileRaw.toPath(), defaultLines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                    }
                }else{
                    try{
                        JsonNode config = safeReadConfig().getConfiguration();
                        JsonNode version = safeGetProperty(config, "version");
                        if(!version.asText().equals(this.version)){
                            replaceOutdatedConfigFile(defaultLines);
                        }
                    }catch(ConfigException e){
                        replaceOutdatedConfigFile(defaultLines);
                    }
                }
            }catch(IOException e){
                logger.error("Unable to create default config!");
                e.printStackTrace();
            }
        }else{
            logger.error(String.format("Unable to obtain a stream for default %s!", CONFIG_NAME));
        }
    }

    private ConfigLoadResult safeReadConfig() {
        try{
            return new ConfigLoadResult(true, objectMapper.readTree(configFileRaw), null);
        } catch(IOException userConfigError){
            userConfigError.printStackTrace();
            logger.error("Unable to load user/saved config, reverting to internal default config");
            try{
                InputStream configDefaultRaw = getClass().getClassLoader().getResourceAsStream(CONFIG_NAME);
                return new ConfigLoadResult(false, objectMapper.readTree(configDefaultRaw), userConfigError.getMessage());
            }catch(IOException defaultConfigError){
                logger.fatal("Unable to load default config!");
                return new ConfigLoadResult(false, null, defaultConfigError.getMessage());
            }
        }
    }

    private JsonNode safeGetProperty(JsonNode config, String propertyName) throws ConfigException{
        JsonNode property = config.get(propertyName);
        if(Objects.isNull(property)){
            throw new ConfigException(String.format("Expected property not found '%s'", propertyName));
        }
        return property;
    }

    private boolean safeGetBoolean(JsonNode property, String propertyName) throws ConfigException{
        if(property.asToken().isBoolean()){
            return property.asBoolean();
        }
        throw new ConfigException(String.format("Expected true/false for property %s, got: %s", propertyName, property.toString()));
    }

    private int safeGetInt(JsonNode property, String propertyName) throws ConfigException{
        if(property.asToken().isNumeric()){
            if(property.asText().contains(".")){
                throw new ConfigException(String.format("Expected integer value for property %s, got: %s", propertyName, property.toString()));
            }
            return property.asInt();
        }
        throw new ConfigException(String.format("Expected integer value for property %s, got: %s", propertyName, property.toString()));
    }

    private List<String> getPropertyAsStringList(JsonNode node, String propertyName) {
        try{
            return objectMapper.readValue(node.get(propertyName).toString(), TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
        }catch(JsonProcessingException e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private Map<String, EntitySettings> getEntitySettings(JsonNode node){
        try{
            List<EntitySettings> entitySettings = objectMapper.readValue(node.get("entitySettings").toString(), TypeFactory.defaultInstance().constructCollectionType(List.class, EntitySettings.class));
            return entitySettings.stream().collect(Collectors.toMap(EntitySettings::getEntityId, setting -> setting));
        }catch(JsonProcessingException e){
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private ConfigLoadResult loadDataFromConfiguration(JsonNode config) {
        try{
            whitelist = getPropertyAsStringList(config, "whitelist");
            blacklist = getPropertyAsStringList(config, "blacklist");
            defaultThreshold = safeGetInt(safeGetProperty(config,"defaultThreshold"), "defaultThreshold");
            defaultLifetime = safeGetInt(safeGetProperty(config,"defaultLifetimeSeconds"), "defaultLifetimeSeconds");
            loggingEnabled = safeGetBoolean(safeGetProperty(config,"logging"), "logging");
            entitySettings = getEntitySettings(config);
            checkTimeSeconds = safeGetInt(safeGetProperty(config,"checkTimeSeconds"), "checkTimeSeconds");
            purgeTamedEntities = safeGetBoolean(safeGetProperty(config,"purgeTamedEntities"), "purgeTamedEntities");

            defaultEntitySettings = new EntitySettings(defaultThreshold, true, defaultLifetime, "*");
            return new ConfigLoadResult(true, config, null);
        }catch(ConfigException e){
            return new ConfigLoadResult(false, config, e.getMessage());
        }
    }

    public ConfigLoadResult reload(){
        ConfigLoadResult loadResult = safeReadConfig();
        JsonNode config = loadResult.getConfiguration();
        if(Objects.nonNull(config)){
            return loadDataFromConfiguration(loadResult.getConfiguration());
        }
        return loadResult;
    }

    public void resetState(){
        enabled = true;
        lookMode = false;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public int getCheckTimeSeconds() {
        return checkTimeSeconds;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public EntitySettings getSettingsForEntity(String entityId) {
        return entitySettings.getOrDefault(entityId, defaultEntitySettings);
    }

    public boolean canPurgeTamedEntities() {
        return purgeTamedEntities;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setLookMode(boolean lookMode) {
        this.lookMode = lookMode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isLookMode() {
        return lookMode;
    }

    @SubscribeEvent
    public void onServerDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent clientDisconnectionFromServerEvent){
        resetState();
    }
}

