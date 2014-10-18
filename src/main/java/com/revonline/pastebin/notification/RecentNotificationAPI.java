package com.revonline.pastebin.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

/**
 * Un'implementazione della classe CompatibleNotification che fa da wrapper alla API di Android.
 *
 * @see com.revonline.pastebin.notification.CompatibleNotification Per maggior informazioni su come
 * utilizzare questa classe
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class RecentNotificationAPI extends CompatibleNotification {

  private final Context context;
  private Notification.Builder builder;

  RecentNotificationAPI(final Context context) {
    this.context = context;
    builder = new Notification.Builder(context);
  }

  @Override
  public CompatibleNotification setContentText(String text) {
    builder.setContentText(text);
    return this;
  }

  @Override
  public CompatibleNotification setSmallIcon(int icon) {
    builder.setSmallIcon(icon);
    return this;
  }

  @Override
  public CompatibleNotification setSmallIcon(int icon, int level) {
    builder.setSmallIcon(icon, level);
    return this;
  }

  @Override
  public CompatibleNotification setContentTitle(String title) {
    builder.setContentTitle(title);
    return this;
  }

  @Override
  public CompatibleNotification setContentIntent(PendingIntent pendingIntent) {
    builder.setContentIntent(pendingIntent);
    return this;
  }

  @Override
  public CompatibleNotification setAutoCancel(boolean autoCancel) {
    builder.setAutoCancel(autoCancel);
    return this;
  }

  @Override
  public Notification create() {
    return builder.build();
  }
}
