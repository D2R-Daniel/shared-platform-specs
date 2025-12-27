package com.platform.sdk.notifications;

import java.util.Map;

/**
 * User notification preferences.
 */
public class NotificationPreferences {
    private Boolean emailEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private Boolean inAppEnabled;
    private String digestFrequency;
    private String digestTime;
    private QuietHours quietHours;
    private Map<String, CategoryPreferences> categories;

    public NotificationPreferences() {
    }

    public Boolean getEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(Boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public Boolean getSmsEnabled() {
        return smsEnabled;
    }

    public void setSmsEnabled(Boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }

    public Boolean getPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(Boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public Boolean getInAppEnabled() {
        return inAppEnabled;
    }

    public void setInAppEnabled(Boolean inAppEnabled) {
        this.inAppEnabled = inAppEnabled;
    }

    public String getDigestFrequency() {
        return digestFrequency;
    }

    public void setDigestFrequency(String digestFrequency) {
        this.digestFrequency = digestFrequency;
    }

    public String getDigestTime() {
        return digestTime;
    }

    public void setDigestTime(String digestTime) {
        this.digestTime = digestTime;
    }

    public QuietHours getQuietHours() {
        return quietHours;
    }

    public void setQuietHours(QuietHours quietHours) {
        this.quietHours = quietHours;
    }

    public Map<String, CategoryPreferences> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, CategoryPreferences> categories) {
        this.categories = categories;
    }

    /**
     * Quiet hours configuration.
     */
    public static class QuietHours {
        private boolean enabled;
        private String start;
        private String end;
        private String timezone;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }
    }

    /**
     * Per-category notification preferences.
     */
    public static class CategoryPreferences {
        private Boolean email;
        private Boolean sms;
        private Boolean push;
        private Boolean inApp;

        public Boolean getEmail() {
            return email;
        }

        public void setEmail(Boolean email) {
            this.email = email;
        }

        public Boolean getSms() {
            return sms;
        }

        public void setSms(Boolean sms) {
            this.sms = sms;
        }

        public Boolean getPush() {
            return push;
        }

        public void setPush(Boolean push) {
            this.push = push;
        }

        public Boolean getInApp() {
            return inApp;
        }

        public void setInApp(Boolean inApp) {
            this.inApp = inApp;
        }
    }
}
