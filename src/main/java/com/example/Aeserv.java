import static spark.Spark.*;
import spark.*;
import java.sql.*;
import java.net.*;

public class Aeserv {

   static Connection conn;

   public static void main(String[] args) {
      
      setPort(Integer.parseInt(System.getenv("PORT")));
      
      get (new Route("/hello") {
         @Override
         public Object handle (Request request, Response response) {
            return "Hello World!";
         }
      });

      get (new Route("/johnny") {
         @Override
         public Object handle (Request request, Response response) {
            try {
               dropTables();
               return "Okay little Johnny";
            } catch (Exception e) {
               response.status(500);
               return "Not so fast";
            }
         }
      });

      post (new Route("/new") {
         @Override
         public Object handle (Request request, Response response) {
            try {
               saveMessage(request.queryParams("to"), request.body());
               response.status(200);
               return "OK";
            } catch (Exception e) {
               e.printStackTrace();
               response.status(500);
               return "FAIL";
            }
         }
      });

      get (new Route("/read") {
         @Override
         public Object handle (Request request, Response response) {
            try {
               String msgs = getMessages(request.queryParams("from"));
               response.status(200);
               return msgs;
            } catch (Exception e) {
               e.printStackTrace();
               response.status(500);
               return "FAIL";
            }
         }
      });

      try {
         conn = getConnection();
         createTables();
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(1);
      }

   }

   static void dropTables () throws URISyntaxException, SQLException {
      Statement st = conn.createStatement();
      st.execute("DROP TABLE messages");
   }

   static void createTables () throws URISyntaxException, SQLException {
      Statement st = conn.createStatement();
      // This will throw if the table has already been created.
      try {
         st.execute("CREATE TABLE messages (usr varchar(80), msg text);");
      } catch (Exception e) {
         System.out.println("createTables threw and exception, nbd.");
      }
   }

   static void saveMessage (String user, String message) throws SQLException {
      Statement st = conn.createStatement();
      PreparedStatement ps = conn.prepareStatement("INSERT INTO messages VALUES (?, ?)");
      ps.setString(1, user);
      ps.setString(2, message);
      ps.execute();
   }

   static String getMessages (String user) throws SQLException {
      Statement st = conn.createStatement();
      PreparedStatement ps = conn.prepareStatement("SELECT msg FROM messages WHERE usr = some_user");
      ps.setString(1, user);
      ResultSet rs = ps.executeQuery();
      StringBuilder sb = new StringBuilder();
      while (rs.next()) {
         System.out.println(rs.getString(1));
         sb.append(rs.getString(1) + "\n");
      }
      rs.close();
      return sb.toString();
   }

   static Connection getConnection () throws URISyntaxException, SQLException {
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

