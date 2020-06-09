package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import java.util.Optional;

/**
 * Utility class used to find the nickname of a given User Entity.
 */
final class NicknameFinder {
  /**
   * Searches for the nickname of a User Entity with a given ID.
   *
   * @param id The ID property associated with the entity whose nickname we want to find
   * @return The nickname of the entity wrapped in an {@link Optional}
   */
  static Optional<String> getNickname(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query =
        new Query(UserKeys.USER_KIND)
            .setFilter(new FilterPredicate(UserKeys.ID_PROPERTY, FilterOperator.EQUAL, id));
    PreparedQuery queryResults = datastore.prepare(query);

    Entity userEntity = queryResults.asSingleEntity();

    if (userEntity == null) {
      return Optional.empty();
    }

    return Optional.ofNullable((String) userEntity.getProperty(UserKeys.NICKNAME_PROPERTY));
  }

  private NicknameFinder() {};
}
