/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.leakcanary;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.squareup.leakcanary.LeakCanary.leakInfo;

/**
 *
 * You can extend this class and override {@link #afterDefaultHandling(HeapDump, AnalysisResult,
 * String)} to add custom behavior, e.g. uploading the heap dump.
 */
public class DisplayLeakService extends AbstractAnalysisResultService {

  @Override protected final void onHeapAnalyzed(HeapDump heapDump, AnalysisResult result) {
    String leakInfo = leakInfo(this, heapDump, result, true);
    CanaryLog.d("%s", leakInfo);

    boolean shouldSaveResult = result.leakFound || result.failure != null;
    if (shouldSaveResult) {
      heapDump = renameHeapdump(heapDump);
      saveResult(heapDump, result);
    }
    afterDefaultHandling(heapDump, result, leakInfo);
  }

  private boolean saveResult(HeapDump heapDump, AnalysisResult result) {
    File resultFile = new File(heapDump.heapDumpFile.getParentFile(),
        heapDump.heapDumpFile.getName() + ".result");
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(resultFile);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(heapDump);
      oos.writeObject(result);
      return true;
    } catch (IOException e) {
      CanaryLog.d(e, "Could not save leak analysis result to disk.");
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException ignored) {
        }
      }
    }
    return false;
  }

  private HeapDump renameHeapdump(HeapDump heapDump) {
    String fileName =
        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS'.hprof'", Locale.US).format(new Date());

    File newFile = new File(heapDump.heapDumpFile.getParent(), fileName);
    boolean renamed = heapDump.heapDumpFile.renameTo(newFile);
    if (!renamed) {
      CanaryLog.d("Could not rename heap dump file %s to %s", heapDump.heapDumpFile.getPath(),
          newFile.getPath());
    }
    return new HeapDump(newFile, heapDump.referenceKey, heapDump.referenceName,
        heapDump.excludedRefs, heapDump.watchDurationMs, heapDump.gcDurationMs,
        heapDump.heapDumpDurationMs);
  }

  /**
   * You can override this method and do a blocking call to a server to upload the leak trace and
   * the heap dump. Don't forget to check {@link AnalysisResult#leakFound} and {@link
   * AnalysisResult#excludedLeak} first.
   */
  protected void afterDefaultHandling(HeapDump heapDump, AnalysisResult result, String leakInfo) {
    Log.i("leakCanary", "DisplayLeakService:afterDefaulthandling ");
    if (!result.leakFound || result.excludedLeak) {
      Log.i("leakCanary", "DisplayLeakService: result.leakFound = " + result.leakFound + " ;result.excludedLeak = " + result.excludedLeak);
      return;
    } else {
      Log.i("leakCanary", "DisplayLeakService: result.leakFound = " + result.leakFound + " ;result.excludedLeak = " + result.excludedLeak);
    }
  }
}
