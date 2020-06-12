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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *  Holds utility method 'query' to find the available times for a meeting
 */
public final class FindMeetingQuery {
  /**
   * Finds the available times in a day in which all meeting participants can attend.
   *
   * @param events The events occurring in a single day
   * @param request The meeting that needs to be accommodated
   * @return List of all available meeting times within a single day
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Collections.emptyList();
    }

    List<TimeRange> conflictingIntervals = new ArrayList<>();

    for (Event event : events) {
      if (eventConflictsWithRequest(event, request)) {
        conflictingIntervals.add(event.getWhen());
      }
    }

    if (conflictingIntervals.isEmpty()) {
      List<TimeRange> availableTimes = new ArrayList<>();
      availableTimes.add(TimeRange.WHOLE_DAY);

      return availableTimes;
    }

    Collections.sort(conflictingIntervals, TimeRange.ORDER_BY_START);

    List<TimeRange> mergedIntervals = getMergedIntervals(conflictingIntervals);
    List<TimeRange> availableTimes = new ArrayList<>();

    int availableDuration = mergedIntervals.get(0).start() - TimeRange.START_OF_DAY;

    if (availableDuration >= request.getDuration()) {
      availableTimes.add(TimeRange.fromStartDuration(TimeRange.START_OF_DAY, availableDuration));
    }

    for (int i = 0; i < mergedIntervals.size() - 1; ++i) {
      availableDuration = mergedIntervals.get(i + 1).start() - mergedIntervals.get(i).end();

      if (availableDuration >= request.getDuration()) {
        availableTimes.add(
            TimeRange.fromStartDuration(mergedIntervals.get(i).end(), availableDuration));
      }
    }

    availableDuration =
        TimeRange.END_OF_DAY + 1 - mergedIntervals.get(mergedIntervals.size() - 1).end();

    if (availableDuration >= request.getDuration()) {
      availableTimes.add(TimeRange.fromStartDuration(
          mergedIntervals.get(mergedIntervals.size() - 1).end(), availableDuration));
    }

    return availableTimes;
  }

  /**
   * Checks whether an event conflicts with any of the candidates in the meeting request.
   *
   * @param event The event to examine
   * @param request The meeting request holding the candidates we want to check
   * @return True if the event has a candidate that is needed in the meeting request, otherwise
   *     false
   */
  private static boolean eventConflictsWithRequest(Event event, MeetingRequest request) {
    for (String attendee : event.getAttendees()) {
      if (request.getAttendees().contains(attendee)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Merges the time intervals that are overlapping.
   *
   * @param intervals Sorted list of intervals based on starting times in ascending order
   * @return A new list with no overlapping intervals
   */
  private static List<TimeRange> getMergedIntervals(List<TimeRange> intervals) {
    List<TimeRange> mergedIntervals = new ArrayList<>();
    mergedIntervals.add(intervals.get(0));

    for (int i = 1; i < intervals.size(); ++i) {
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
}
