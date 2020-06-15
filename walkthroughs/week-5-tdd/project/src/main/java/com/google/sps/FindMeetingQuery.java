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
 *  Holds utility method 'query' to find the available times for a meeting
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

    List<TimeRange> optionalIntervals = new ArrayList<>();
    Set<String> optionalAttendees = new HashSet<>(request.getOptionalAttendees());

    List<TimeRange> mandatoryIntervals = new ArrayList<>();
    Set<String> mandatoryAttendees;

    if (request.getAttendees().isEmpty()) {
      mandatoryAttendees = new HashSet<>(optionalAttendees);
    } else {
      mandatoryAttendees = new HashSet<>(request.getAttendees());
    }

    for (Event event : events) {
      if (!Collections.disjoint(event.getAttendees(), mandatoryAttendees)) {
        mandatoryIntervals.add(event.getWhen());
      }
      if (!Collections.disjoint(event.getAttendees(), optionalAttendees)) {
        optionalIntervals.add(event.getWhen());
      }
    }

    Collections.sort(mandatoryIntervals, TimeRange.ORDER_BY_START);
    Collections.sort(optionalIntervals, TimeRange.ORDER_BY_START);

    List<TimeRange> mergedMandatoryIntervals = getMergedIntervals(mandatoryIntervals);
    List<TimeRange> mergedOptionalIntervals = getMergedIntervals(optionalIntervals);

    List<TimeRange> availableMandatoryIntervals =
        getAvailableIntervals(mergedMandatoryIntervals, request);
    List<TimeRange> availableOptionalIntervals =
        getAvailableIntervals(mergedOptionalIntervals, request);

    List<TimeRange> availableIntervals =
        getMergedIntervals(availableMandatoryIntervals, availableOptionalIntervals, request);

    if (availableIntervals.isEmpty()) {
      return availableMandatoryIntervals;
    }

    return availableIntervals;
  }

  /**
   * Finds the available intervals from a list of busy intervals by calculating for their
   * complement.
   *
   * @param busyIntervals list containing busy non-overlapping intervals sorted in ascending order
   * @param request the meeting request used to validate the time interval
   * @return list containing the available intervals in ascending order
   */
  private static List<TimeRange> getAvailableIntervals(
      List<TimeRange> busyIntervals, MeetingRequest request) {
    if (busyIntervals.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // Checks if there is free time between the start of the day and the first meeting.
    List<TimeRange> availableIntervals = addIntervalIfValid(new ArrayList<>(),
        TimeRange.fromStartEnd(TimeRange.START_OF_DAY, busyIntervals.get(0).start(), false),
        request);

    // Checks if there is free time between two consecutive meetings.
    for (int i = 0; i < busyIntervals.size() - 1; i++) {
      availableIntervals = addIntervalIfValid(availableIntervals,
          TimeRange.fromStartEnd(
              busyIntervals.get(i).end(), busyIntervals.get(i + 1).start(), false),
          request);
    }

    // Checks if there is free time between the last meeting and the end of the day.
    availableIntervals = addIntervalIfValid(availableIntervals,
        TimeRange.fromStartEnd(
            busyIntervals.get(busyIntervals.size() - 1).end(), TimeRange.END_OF_DAY + 1, false),
        request);

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
   * @return merged list of non-overlapping intervals sorted in ascending order
   */
  private static List<TimeRange> getMergedIntervals(
      List<TimeRange> intervalsA, List<TimeRange> intervalsB, MeetingRequest request) {
    List<TimeRange> mergedIntervals = new ArrayList<>();

    int i = 0, j = 0;

    while (i < intervalsA.size() && j < intervalsB.size()) {
      int start = Math.max(intervalsA.get(i).start(), intervalsB.get(j).start());
      int end = Math.min(intervalsA.get(i).end(), intervalsB.get(j).end());

      if (end - start >= request.getDuration()) {
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

  /**
   * Appends an interval to the TimeRange list if its duration is greater than or equal to the
   * duration of the meeting request.
   *
   * @param intervals list in which the new interval will be added
   * @param interval the interval that is checked for validation
   * @param request the meeting request with the minimum required duration
   * @return list of intervals with a new interval added if it was valid
   */
  private static List<TimeRange> addIntervalIfValid(
      List<TimeRange> intervals, TimeRange interval, MeetingRequest request) {
    List<TimeRange> newIntervals = new ArrayList<>(intervals);

    if (interval.duration() >= request.getDuration()) {
      newIntervals.add(interval);
    }

    return newIntervals;
  }
}
