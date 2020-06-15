// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *  Holds utility method 'query' to find the available times for a meeting.
 */
public final class FindMeetingQuery {
  /**
   * Finds the available times in a day in which all meeting participants can attend.
   *
   * @param events the events occurring in a single day
   * @param request the meeting that needs to be accommodated - can't be longer than a day
   * @return list of all available meeting times within a single day in ascending order
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Collections.emptyList();
    }

    List<TimeRange> availableOptionalIntervals =
        getAvailableIntervals(events, request.getOptionalAttendees(), request.getDuration());

    // Only need to search for optional intervals if there are no mandatory attendees.
    if (request.getAttendees().isEmpty()) {
      return availableOptionalIntervals;
    }

    List<TimeRange> availableMandatoryIntervals =
        getAvailableIntervals(events, request.getAttendees(), request.getDuration());

    List<TimeRange> availableIntervals = getMergedIntervals(
        availableMandatoryIntervals, availableOptionalIntervals, request.getDuration());

    /*
     * If there is no interval intersection between mandatory and optional attendees then only
     * mandatory intervals are relevant.
     */
    if (availableIntervals.isEmpty()) {
      return availableMandatoryIntervals;
    }

    return availableIntervals;
  }

  /**
   * Finds the meeting times that have no conflict between the attendees and the scheduled events.
   *
   * @param events list of events occurring in one day
   * @param attendees list of unique attendees required in the meeting
   * @param meetingDuration the minimum meeting duration
   * @return list with all non-overlapping available times for the meeting sorted in ascending order
   */
  private static List<TimeRange> getAvailableIntervals(
      Collection<Event> events, Collection<String> attendees, Long meetingDuration) {
    List<TimeRange> busyIntervals = new ArrayList<>();

    for (Event event : events) {
      if (!Collections.disjoint(event.getAttendees(), attendees)) {
        busyIntervals.add(event.getWhen());
      }
    }

    if (busyIntervals.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    Collections.sort(busyIntervals, TimeRange.ORDER_BY_START);

    busyIntervals = getMergedIntervals(busyIntervals);

    // Add boundary at the start of the day.
    busyIntervals.add(0, TimeRange.fromStartDuration(TimeRange.START_OF_DAY, 0));

    // Add boundary at the end of the day.
    busyIntervals.add(TimeRange.fromStartDuration(TimeRange.END_OF_DAY + 1, 0));

    return getAvailableIntervals(getMergedIntervals(busyIntervals), meetingDuration);
  }

  /**
   * Finds the available intervals from a list of busy intervals by calculating for their
   * complement.
   *
   * @param busyIntervals list containing busy non-overlapping intervals sorted in ascending order
   * @param meetingDuration the minimum meeting duration
   * @return list containing the available intervals in ascending order
   */
  private static List<TimeRange> getAvailableIntervals(
      List<TimeRange> busyIntervals, Long meetingDuration) {
    List<TimeRange> availableIntervals = new ArrayList<>();

    // Checks if there is free time between two consecutive intervals.
    for (int i = 0; i < busyIntervals.size() - 1; i++) {
      TimeRange interval = TimeRange.fromStartEnd(
          busyIntervals.get(i).end(), busyIntervals.get(i + 1).start(), false);

      if (interval.duration() >= meetingDuration) {
        availableIntervals.add(interval);
      }
    }

    return availableIntervals;
  }

  /**
   * Merges the time intervals that are overlapping.
   *
   * @param intervals sorted list of intervals based on starting times in ascending order
   * @return merged list of non-overlapping intervals sorted in ascending order
   */
  private static List<TimeRange> getMergedIntervals(List<TimeRange> intervals) {
    if (intervals.isEmpty()) {
      return Collections.emptyList();
    }

    List<TimeRange> mergedIntervals = new ArrayList<>();
    mergedIntervals.add(intervals.get(0));

    for (int i = 1; i < intervals.size(); i++) {
      TimeRange mergedInterval = mergedIntervals.get(mergedIntervals.size() - 1);
      TimeRange currInterval = intervals.get(i);

      if (mergedInterval.end() < currInterval.start()) {
        mergedIntervals.add(currInterval);
      } else if (mergedInterval.end() < currInterval.end()) {
        mergedInterval = TimeRange.fromStartEnd(mergedInterval.start(), currInterval.end(), false);

        mergedIntervals.remove(mergedIntervals.size() - 1);
        mergedIntervals.add(mergedInterval);
      }
    }

    return mergedIntervals;
  }

  /**
   * Merges two interval lists into one given that each merged interval is greater than or equal to
   * the meeting duration.
   *
   * @param intervalsA the first interval list to merge - must have no overlapping intervals and be
   *     sorted based in ascending order
   * @param intervalsB the second interval list to merge - must have no overlapping intervals and be
   *     sorted based in ascending order
   * @param meetingDuration the minimum meeting duration
   * @return merged list of non-overlapping intervals sorted in ascending order
   */
  private static List<TimeRange> getMergedIntervals(
      List<TimeRange> intervalsA, List<TimeRange> intervalsB, Long meetingDuration) {
    List<TimeRange> mergedIntervals = new ArrayList<>();

    int i = 0, j = 0;

    while (i < intervalsA.size() && j < intervalsB.size()) {
      int start = Math.max(intervalsA.get(i).start(), intervalsB.get(j).start());
      int end = Math.min(intervalsA.get(i).end(), intervalsB.get(j).end());

      if (end - start >= meetingDuration) {
        mergedIntervals.add(TimeRange.fromStartEnd(start, end, false));
      }

      if (intervalsA.get(i).end() < intervalsB.get(j).end()) {
        i++;
      } else {
        j++;
      }
    }

    return mergedIntervals;
  }
}
