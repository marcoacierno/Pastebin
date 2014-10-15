package com.revonline.pastebin.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import com.revonline.pastebin.R;

/**
 * Implementazione di CompatileNotification
 * per supportare i device con level API inferiore a 11
 *
 * @see com.revonline.pastebin.notification.CompatibleNotification Per sapere più informazioni su come utilizzare
 * questa classe
 */
public class OlderNotificationAPI extends CompatibleNotification {
    private final Notification notification = new Notification();
    private final Context context;
    private PendingIntent pendingIntent;
    private String contentTitle;
    private String contentText;

    OlderNotificationAPI(final Context context) {
        this.context = context;
    }

    private void setLatestEventInfo(final String contentTitle, final String contentText) {
        notification.setLatestEventInfo(context, contentTitle, contentText, pendingIntent);
    }

    @Override
    public CompatibleNotification setContentText(String text) {
        this.contentText = text;
        setLatestEventInfo(contentTitle, text);
        return this;
    }

    @Override
    public CompatibleNotification setSmallIcon(int icon) {
        notification.icon = icon;
        return this;
    }

    @Override
    public CompatibleNotification setSmallIcon(int icon, int level) {
        notification.icon = icon;
        notification.iconLevel = level;
        return this;
    }

    @Override
    public CompatibleNotification setContentTitle(String title) {
        contentTitle = title;
        setLatestEventInfo(title, contentText);
        return this;
    }

    @Override
    public CompatibleNotification setContentIntent(PendingIntent pendingIntent) {
        notification.contentIntent = this.pendingIntent = pendingIntent;
        return this;
    }

    @Override
    public CompatibleNotification setAutoCancel(boolean autoCancel) {
        if (autoCancel) {
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
        } else {
            notification.flags &= Notification.FLAG_AUTO_CANCEL;
        }

        return this;
    }

    @Override
    public Notification create() {
        return notification;
    }
}
