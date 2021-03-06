package org.robolectric.shadows;

import android.os.SystemClock;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.HiddenApi;

/**
 * The concept of current time is base on the current time of the UI Scheduler for consistency with previous
 * implementations. This is not ideal, since both schedulers (background and foreground), can see different
 * values for the current time.
 */
@Implements(SystemClock.class)
public class ShadowSystemClock {
  private static long bootedAt = 0;
  private static long nanoTime = 0;
  private static final int MILLIS_PER_NANO = 1000000;

  static long now() {
    return ShadowLooper.getUiThreadScheduler().getCurrentTime();
  }

  @Implementation
  public static void sleep(long millis) {
    nanoTime = millis * MILLIS_PER_NANO;
    ShadowLooper.getUiThreadScheduler().advanceBy(millis);
  }

  @Implementation
  public static boolean setCurrentTimeMillis(long millis) {
    if (now() > millis) {
      return false;
    }
    nanoTime = millis * MILLIS_PER_NANO;
    ShadowLooper.getUiThreadScheduler().advanceTo(millis);
    return true;
  }
  
  @Implementation
  public static long uptimeMillis() {
    return now() - bootedAt;
  }

  @Implementation
  public static long elapsedRealtime() {
    return uptimeMillis();
  }

  @Implementation
  public static long currentThreadTimeMillis() {
    return uptimeMillis();
  }

  @HiddenApi @Implementation
  public static long currentThreadTimeMicro() {
    return uptimeMillis() * 1000;
  }

  @HiddenApi @Implementation
  public static long currentTimeMicro() {
    return now() * 1000;
  }
  
  /**
   * Implements {@link System#currentTimeMillis} through ShadowWrangler.
   *
   * @return Current time in millis.
   */
  @SuppressWarnings("unused")
  public static long currentTimeMillis() {
    return nanoTime / MILLIS_PER_NANO;
  }

  /**
   * Implements {@link System#nanoTime} through ShadowWrangler.
   *
   * @return Current time with nanos.
   */
  @SuppressWarnings("unused")
  public static long nanoTime() {
    return nanoTime;
  }

  public static void setNanoTime(long nanoTime) {
    ShadowSystemClock.nanoTime = nanoTime;
  }
}
