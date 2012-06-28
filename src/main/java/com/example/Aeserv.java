import static spark.Spark.*;
import spark.*;
import java.sql.*;
import java.net.*;

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

      try {
         Connection conn = getConnection();
         // Statement st = conn.createStatement();
         // Boolean res = st.execute("CREATE TABLE cities ("
         //                               + "name            varchar(80));");
         Statement st = conn.createStatement();
         Boolean res = st.execute("INSERT INTO cities VALUES (derp)");
         st = conn.createStatement();
         ResultSet rs = st.executeQuery("SELECT * FROM cities WHERE name = 'derp'");
         while (rs.next()) {
             System.out.print("Column 1 returned ");
             System.out.println(rs.getString(1));
         }
         rs.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   private static Connection getConnection() throws URISyntaxException, SQLException {
      try {
         Class.forName("org.postgresql.Driver");
      } catch (Exception e) {
         System.out.println("FUCKFUCKFUCK");
      }
      URI dbUri = new URI(System.getenv("DATABASE_URL"));

      String username = dbUri.getUserInfo().split(":")[0];
      String password = dbUri.getUserInfo().split(":")[1];
      String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath() + ":" + dbUri.getPort();

      return DriverManager.getConnection(dbUrl, username, password);
   }

}

