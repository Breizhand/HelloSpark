package com.brestopencampus.spark;

import com.brestopencampus.spark.model.Beer;
import spark.Request;
import spark.Response;
import spark.Route;

import com.mongodb.*;
import org.bson.types.ObjectId;


import static spark.Spark.*;

/**
 * Created by sca on 29/10/16.
 */
public class Application {

  public static void main(String[] args) throws Exception {

    exception(Exception.class, (e, req, res) -> e.printStackTrace()); // print all exceptions
    port(8080);

    enableCORS("*", "*", "*");

    BeerDao dao = new BeerDao(mongo());

    get("/", (req, res) -> "Hello Beers");

    /**
     * REST Part
     */
    JSonTransformer jsonT = new JSonTransformer();
    get("/json/Beers", (req, res) -> dao.all(), jsonT);
    get("/json/Beers/:id", "application/json",  (req, res) ->  dao.find(req.params("id"))
            , jsonT);

    post("/json/Beers", (req, res) -> {
      Beer b = dao.add(new Beer(req.queryParams("name"), Float.parseFloat(req.queryParams("alcohol"))));
      res.status(201);
      return b;
    }, jsonT);
    delete("/json/Beers/:id", (ICRoute) (req) -> dao.remove(req.params("id")));
  }

  // Enables CORS on requests. This method is an initialization method and should be called once.
  private static void enableCORS(final String origin, final String methods, final String headers) {

    options("/*", (request, response) -> {

      String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
      if (accessControlRequestHeaders != null) {
        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      }

      String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
      if (accessControlRequestMethod != null) {
        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }

      return "OK";
    });

    before((request, response) -> {
      response.header("Access-Control-Allow-Origin", origin);
      response.header("Access-Control-Request-Method", methods);
      response.header("Access-Control-Allow-Headers", headers);
      // Note: this may or may not be necessary in your particular application
      response.type("application/json");
    });
  }

  private static DB mongo() throws Exception {
    String host = System.getenv("MONGODB_ADDON_HOST");
    if (host == null) {
      MongoClient mongoClient = new MongoClient("localhost");
      return mongoClient.getDB("todoapp");
    }
    int port = Integer.parseInt(System.getenv("MONGODB_ADDON_PORT"));
    String dbname = System.getenv("MONGODB_ADDON_DB");
    String username = System.getenv("MONGODB_ADDON_USER");
    String password = System.getenv("MONGODB_ADDON_PASSWORD");
    MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
    MongoClient mongoClient = new MongoClient(new ServerAddress(host, port), mongoClientOptions);
    mongoClient.setWriteConcern(WriteConcern.SAFE);
    DB db = mongoClient.getDB(dbname);

    if (db.authenticate(username, password.toCharArray())) {
      return db;
    } else {
      throw new RuntimeException("Not able to authenticate with MongoDB");
    }
  }



  @FunctionalInterface
  private interface ICRoute extends Route {
    default Object handle(Request request, Response response) throws Exception {
      handle(request);
      return "";
    }
    void handle(Request request) throws Exception;
  }

}
