/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.kaltura.dtg.exoparser.source.dash.manifest;

import android.net.Uri;

import com.kaltura.dtg.exoparser.C;
import com.kaltura.dtg.exoparser.Format;
import com.kaltura.dtg.exoparser.source.dash.DashSegmentIndex;

import java.util.Collections;
import java.util.List;

/**
 * A DASH representation.
 */
public abstract class Representation {

  /**
   * A default value for {@link #revisionId}.
   */
  public static final long REVISION_ID_DEFAULT = -1;

  /**
   * Identifies the piece of content to which this {@link Representation} belongs.
   * <p>
   * For example, all {@link Representation}s belonging to a video should have the same content
   * identifier that uniquely identifies that video.
   */
  private final String contentId;
  /**
   * Identifies the revision of the content.
   * <p>
   * If the media for a given ({@link #contentId} can change over time without a change to the
   * {@link #format}'s {@link Format#id} (e.g. as a result of re-encoding the media with an
   * updated encoder), then this identifier must uniquely identify the revision of the media. The
   * timestamp at which the media was encoded is often a suitable.
   */
  private final long revisionId;
  /**
   * The format of the representation.
   */
  public final Format format;
  /**
   * The base URL of the representation.
   */
  private final String baseUrl;
  /**
   * The offset of the presentation timestamps in the media stream relative to media time.
   */
  private final long presentationTimeOffsetUs;
  /**
   * The in-band event streams in the representation. Never null, but may be empty.
   */
  private final List<Descriptor> inbandEventStreams;

  private final RangedUri initializationUri;

  /**
   * Constructs a new instance.
   *
   * @param contentId Identifies the piece of content to which this representation belongs.
   * @param revisionId Identifies the revision of the content.
   * @param format The format of the representation.
   * @param baseUrl The base URL.
   * @param segmentBase A segment base element for the representation.
   * @return The constructed instance.
   */
  public static Representation newInstance(String contentId, long revisionId, Format format,
      String baseUrl, SegmentBase segmentBase) {
    return newInstance(contentId, revisionId, format, baseUrl, segmentBase, null);
  }

  /**
   * Constructs a new instance.
   *
   * @param contentId Identifies the piece of content to which this representation belongs.
   * @param revisionId Identifies the revision of the content.
   * @param format The format of the representation.
   * @param baseUrl The base URL.
   * @param segmentBase A segment base element for the representation.
   * @param inbandEventStreams The in-band event streams in the representation. May be null.
   * @return The constructed instance.
   */
  public static Representation newInstance(String contentId, long revisionId, Format format,
      String baseUrl, SegmentBase segmentBase, List<Descriptor> inbandEventStreams) {
    return newInstance(contentId, revisionId, format, baseUrl, segmentBase, inbandEventStreams,
        null);
  }

  /**
   * Constructs a new instance.
   *
   * @param contentId Identifies the piece of content to which this representation belongs.
   * @param revisionId Identifies the revision of the content.
   * @param format The format of the representation.
   * @param baseUrl The base URL of the representation.
   * @param segmentBase A segment base element for the representation.
   * @param inbandEventStreams The in-band event streams in the representation. May be null.
   * @param customCacheKey A custom value to be returned from {@link #getCacheKey()}, or null. This
   *     parameter is ignored if {@code segmentBase} consists of multiple segments.
   * @return The constructed instance.
   */
  private static Representation newInstance(String contentId, long revisionId, Format format,
                                            String baseUrl, SegmentBase segmentBase, List<Descriptor> inbandEventStreams,
                                            String customCacheKey) {
    if (segmentBase instanceof SegmentBase.SingleSegmentBase) {
      return new SingleSegmentRepresentation(contentId, revisionId, format, baseUrl,
          (SegmentBase.SingleSegmentBase) segmentBase, inbandEventStreams, customCacheKey, C.LENGTH_UNSET);
    } else if (segmentBase instanceof SegmentBase.MultiSegmentBase) {
      return new MultiSegmentRepresentation(contentId, revisionId, format, baseUrl,
          (SegmentBase.MultiSegmentBase) segmentBase, inbandEventStreams);
    } else {
      throw new IllegalArgumentException("segmentBase must be of type SingleSegmentBase or "
          + "MultiSegmentBase");
    }
  }

  private Representation(String contentId, long revisionId, Format format, String baseUrl,
      SegmentBase segmentBase, List<Descriptor> inbandEventStreams) {
    this.contentId = contentId;
    this.revisionId = revisionId;
    this.format = format;
    this.baseUrl = baseUrl;
    this.inbandEventStreams = inbandEventStreams == null ? Collections.<Descriptor>emptyList()
        : Collections.unmodifiableList(inbandEventStreams);
    initializationUri = segmentBase.getInitialization(this);
    presentationTimeOffsetUs = segmentBase.getPresentationTimeOffsetUs();
  }

  /**
   * Returns a {@link RangedUri} defining the location of the representation's initialization data,
   * or null if no initialization data exists.
   */
  public RangedUri getInitializationUri() {
    return initializationUri;
  }

  /**
   * Returns a {@link RangedUri} defining the location of the representation's segment index, or
   * null if the representation provides an index directly.
   */
  public abstract RangedUri getIndexUri();

  /**
   * Returns an index if the representation provides one directly, or null otherwise.
   */
  public abstract DashSegmentIndex getIndex();

  /**
   * Returns a cache key for the representation if a custom cache key or content id has been
   * provided and there is only single segment.
   */
  public abstract String getCacheKey();

  // Custom Code: We need the Base URL in case the segment doesn't use the default one [RT]
  public String getBaseUrl() {
    return baseUrl;
  }
  // end Custom Code

  /**
   * A DASH representation consisting of a single segment.
   */
  public static class SingleSegmentRepresentation extends Representation {

    /**
     * The uri of the single segment.
     */
    public final Uri uri;

    /**
     * The content length, or {@link C#LENGTH_UNSET} if unknown.
     */
    final long contentLength;

    private final String cacheKey;
    private final RangedUri indexUri;
    private final SingleSegmentIndex segmentIndex;

    /**
     * @param contentId Identifies the piece of content to which this representation belongs.
     * @param revisionId Identifies the revision of the content.
     * @param format The format of the representation.
     * @param uri The uri of the media.
     * @param initializationStart The offset of the first byte of initialization data.
     * @param initializationEnd The offset of the last byte of initialization data.
     * @param indexStart The offset of the first byte of index data.
     * @param indexEnd The offset of the last byte of index data.
     * @param inbandEventStreams The in-band event streams in the representation. May be null.
     * @param customCacheKey A custom value to be returned from {@link #getCacheKey()}, or null.
     * @param contentLength The content length, or {@link C#LENGTH_UNSET} if unknown.
     */
    public static SingleSegmentRepresentation newInstance(String contentId, long revisionId,
        Format format, String uri, long initializationStart, long initializationEnd,
        long indexStart, long indexEnd, List<Descriptor> inbandEventStreams, String customCacheKey,
        long contentLength) {
      RangedUri rangedUri = new RangedUri(null, initializationStart,
          initializationEnd - initializationStart + 1);
      SegmentBase.SingleSegmentBase segmentBase = new SegmentBase.SingleSegmentBase(rangedUri, 1, 0, indexStart,
          indexEnd - indexStart + 1);
      return new SingleSegmentRepresentation(contentId, revisionId,
          format, uri, segmentBase, inbandEventStreams, customCacheKey, contentLength);
    }

    /**
     * @param contentId Identifies the piece of content to which this representation belongs.
     * @param revisionId Identifies the revision of the content.
     * @param format The format of the representation.
     * @param baseUrl The base URL of the representation.
     * @param segmentBase The segment base underlying the representation.
     * @param inbandEventStreams The in-band event streams in the representation. May be null.
     * @param customCacheKey A custom value to be returned from {@link #getCacheKey()}, or null.
     * @param contentLength The content length, or {@link C#LENGTH_UNSET} if unknown.
     */
    SingleSegmentRepresentation(String contentId, long revisionId, Format format,
                                String baseUrl, SegmentBase.SingleSegmentBase segmentBase, List<Descriptor> inbandEventStreams,
                                String customCacheKey, long contentLength) {
      super(contentId, revisionId, format, baseUrl, segmentBase, inbandEventStreams);
      this.uri = Uri.parse(baseUrl);
      this.indexUri = segmentBase.getIndex();
      this.cacheKey = customCacheKey != null ? customCacheKey
          : contentId != null ? contentId + "." + format.id + "." + revisionId : null;
      this.contentLength = contentLength;
      // If we have an index uri then the index is defined externally, and we shouldn't return one
      // directly. If we don't, then we can't do better than an index defining a single segment.
      segmentIndex = indexUri != null ? null
          : new SingleSegmentIndex(new RangedUri(null, 0, contentLength));
    }

    @Override
    public RangedUri getIndexUri() {
      return indexUri;
    }

    @Override
    public DashSegmentIndex getIndex() {
      return segmentIndex;
    }

    @Override
    public String getCacheKey() {
      return cacheKey;
    }

  }

  /**
   * A DASH representation consisting of multiple segments.
   */
  public static class MultiSegmentRepresentation extends Representation
      implements DashSegmentIndex {

    private final SegmentBase.MultiSegmentBase segmentBase;

    /**
     * @param contentId Identifies the piece of content to which this representation belongs.
     * @param revisionId Identifies the revision of the content.
     * @param format The format of the representation.
     * @param baseUrl The base URL of the representation.
     * @param segmentBase The segment base underlying the representation.
     * @param inbandEventStreams The in-band event streams in the representation. May be null.
     */
    MultiSegmentRepresentation(String contentId, long revisionId, Format format,
                               String baseUrl, SegmentBase.MultiSegmentBase segmentBase, List<Descriptor> inbandEventStreams) {
      super(contentId, revisionId, format, baseUrl, segmentBase, inbandEventStreams);
      this.segmentBase = segmentBase;
    }

    @Override
    public RangedUri getIndexUri() {
      return null;
    }

    @Override
    public DashSegmentIndex getIndex() {
      return this;
    }

    @Override
    public String getCacheKey() {
      return null;
    }

    // DashSegmentIndex implementation.

    @Override
    public RangedUri getSegmentUrl(long segmentIndex) {
      return segmentBase.getSegmentUrl(this, segmentIndex);
    }

    @Override
    public long getSegmentNum(long timeUs, long periodDurationUs) {
      return segmentBase.getSegmentNum(timeUs, periodDurationUs);
    }

    @Override
    public long getTimeUs(long segmentIndex) {
      return segmentBase.getSegmentTimeUs(segmentIndex);
    }

    @Override
    public long getDurationUs(long segmentIndex, long periodDurationUs) {
      return segmentBase.getSegmentDurationUs(segmentIndex, periodDurationUs);
    }

    @Override
    public long getFirstSegmentNum() {
      return segmentBase.getFirstSegmentNum();
    }

    @Override
    public int getSegmentCount(long periodDurationUs) {
      return segmentBase.getSegmentCount(periodDurationUs);
    }

    @Override
    public boolean isExplicit() {
      return segmentBase.isExplicit();
    }

  }

}
