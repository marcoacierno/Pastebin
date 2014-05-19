package com.revonline.pastebin;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Marco
 * Date: 06/12/13
 * Time: 19.44
 * To change this template use File | Settings | File Templates.
 */
public class ErrorMessages {
    public final static HashMap<String, Integer> errors = new HashMap<String, Integer>();

    static
    {
        errors.put("Bad API request, invalid api_option", R.string.erroreinterno);
        errors.put("Bad API request, invalid api_dev_key", R.string.erroreinterno);
        errors.put("Bad API request, IP blocked", R.string.blockedip);
        errors.put("Bad API request, maximum number of 25 unlisted pastes for your free account", R.string.maxunlimitedpastes);
        errors.put("Bad API request, maximum number of 10 private pastes for your free account", R.string.maxprivatepastes);
        errors.put("Bad API request, api_paste_code was empty", R.string.blankcode);
        errors.put("Bad API request, maximum paste file size exceeded", R.string.maxpastesize);
        errors.put("Bad API request, invalid api_expire_date", R.string.erroreinterno);
        errors.put("Bad API request, invalid api_paste_private", R.string.erroreinterno);
        errors.put("Bad API request, invalid api_paste_format", R.string.erroreinterno);
        errors.put("Post limit, maximum pastes per 24h reached", R.string.maxpastes);
        errors.put("Bad API request, use POST request, not GET", R.string.erroreinterno);
        errors.put("Bad API request, invalid login", R.string.invaliddata);
        errors.put("Bad API request, account not active", R.string.accountnoactive);
        errors.put("Bad API request, invalid POST parameters", R.string.erroreinterno);
    }
}
