package com.brestopencampus.spark;

import com.brestopencampus.spark.model.Beer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import com.mongodb.*;
import org.bson.types.ObjectId;

/**
 * Created by sca on 05/12/16.
 */
public class BeerDao {

  private final DB db;
  private final DBCollection collection;

  public BeerDao(DB db){
    this.db = db;
    this.collection = db.getCollection("beers");
  }

  public List<Beer> all() {
    List<Beer> beers = new ArrayList<>();
    DBCursor dbObjects = collection.find();
    while (dbObjects.hasNext()) {
      DBObject dbObject = dbObjects.next();
      beers.add(new Beer((BasicDBObject) dbObject));
    }
    return beers;
  }

  public Beer find(String id) {
    return new Beer((BasicDBObject) collection.findOne(new BasicDBObject("_id", new ObjectId(id))));
  }

  public void add(Beer beer) {
    collection.insert(new BasicDBObject("name", beer.getName())
            .append("alcohol", beer.getAlcohol()));
  }

  public void remove(String id) {
    collection.remove(new BasicDBObject("_id", new ObjectId(id)));

  }
}
