package com.revonline.pastebin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: Marco Date: 06/12/13 Time: 19.44 To change this template use
 * File | Settings | File Templates.
 */
public class ErrorMessages {

  private final static Map<String, Integer> errors;

  static {
    final Map<String, Integer> tempErrors = new HashMap<>();

    tempErrors.put("Bad API request, invalid api_option", R.string.erroreinterno);
    tempErrors.put("Bad API request, invalid api_dev_key", R.string.erroreinterno);
    tempErrors.put("Bad API request, IP blocked", R.string.blockedip);
    tempErrors.put("Bad API request, maximum number of 25 unlisted pastes for your free account",
         R.string.maxunlimitedpastes);
    tempErrors.put("Bad API request, maximum number of 10 private pastes for your free account",
         R.string.maxprivatepastes);
    tempErrors.put("Bad API request, api_paste_code was empty", R.string.blankcode);
    tempErrors.put("Bad API request, maximum paste file size exceeded", R.string.maxpastesize);
    tempErrors.put("Bad API request, invalid api_expire_date", R.string.erroreinterno);
    tempErrors.put("Bad API request, invalid api_paste_private", R.string.erroreinterno);
    tempErrors.put("Bad API request, invalid api_paste_format", R.string.erroreinterno);
    tempErrors.put("Post limit, maximum pastes per 24h reached", R.string.maxpastes);
    tempErrors.put("Bad API request, use POST request, not GET", R.string.erroreinterno);
    tempErrors.put("Bad API request, invalid login", R.string.invaliddata);
    tempErrors.put("Bad API request, account not active", R.string.accountnoactive);
    tempErrors.put("Bad API request, invalid POST parameters", R.string.erroreinterno);
    tempErrors.put("Bad API request, invalid permission to remove paste", R.string.invalid_permission_to_remove_paste);

    errors = Collections.unmodifiableMap(tempErrors);
  }

  public static int getErrorFor(final String message) {
    return errors.get(message);
  }

  public static boolean containsError(final String message) {
    return errors.containsKey(message);
  }
}
