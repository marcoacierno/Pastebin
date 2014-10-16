package com.revonline.pastebin.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.revonline.pastebin.ShareCodeActivity;

/**
 * Unica classe per gestire le notifiche su tutti
 * i devices con tutti i livelli di API.
 *
 * @see CompatibleNotification#createNotification(android.content.Context) per creare una notifica senza la necessità
 * di eseguire nessun controllo sul livello API attuale
 */
public abstract class CompatibleNotification {
    public static CompatibleNotification createNotification(Context context) {
        return !ShareCodeActivity.apiLower11 ? new RecentNotificationAPI(context) :
                new OlderNotificationAPI(context);
    }

    public abstract CompatibleNotification setContentText(String text);
    public abstract CompatibleNotification setSmallIcon(int icon);
    public abstract CompatibleNotification setSmallIcon(int icon, int level);
    public abstract CompatibleNotification setContentTitle(String title);
    public abstract CompatibleNotification setContentIntent(PendingIntent pendingIntent);
    public abstract CompatibleNotification setAutoCancel(boolean autoCancel);
    public abstract Notification create();
}
