package com.revonline.pastebin.trending_pastes;

import com.revonline.pastebin.PasteInfo;

import java.util.List;

/**
 *
 */
public interface DeserializePastesListener {
  /**
   * @param pastes If null something went wrong during the deserialization process.
   */
  void onDeserializePastesResult(final List<PasteInfo> pastes);
}
