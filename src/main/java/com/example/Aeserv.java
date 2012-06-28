import static spark.Spark.*;
import spark.*;

public class Aeserv {

   public static void main(String[] args) {
      
      setPort(Integer.parseInt(System.getenv("PORT")));
      get (new Route("/hello") {
         @Override
         public Object handle (Request request, Response response) {
            return "Hello World!";
         }
      });

      post (new Route("/new") {
         @Override
         public Object handle (Request request, Response response) {
            return "Username was: " + request.queryParams("from") + "\n\nWhat you want, baby I got it: " + request.body();
         }
      });

   }

}

