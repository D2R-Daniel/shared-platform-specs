package com.platform.sdk.users;

import java.util.Map;

/**
 * User preferences.
 */
public class UserPreferences {
    private String theme;
    private String language;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean smsNotifications;
    private String digestFrequency;
    private Map<String, Object> custom;

    public UserPreferences() {
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public Boolean getPushNotifications() {
        return pushNotifications;
    }

    public void setPushNotifications(Boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }

    public Boolean getSmsNotifications() {
        return smsNotifications;
    }

    public void setSmsNotifications(Boolean smsNotifications) {
        this.smsNotifications = smsNotifications;
    }

    public String getDigestFrequency() {
        return digestFrequency;
    }

    public void setDigestFrequency(String digestFrequency) {
        this.digestFrequency = digestFrequency;
    }

    public Map<String, Object> getCustom() {
        return custom;
    }

    public void setCustom(Map<String, Object> custom) {
        this.custom = custom;
    }
}
