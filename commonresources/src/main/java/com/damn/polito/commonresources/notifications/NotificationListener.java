package com.damn.polito.commonresources.notifications;

public interface NotificationListener {
    public void addNotificationBadge();
    public void refreshNotificationBadge(boolean visible);
}
