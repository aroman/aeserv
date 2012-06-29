import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.codehaus.jackson.map.*;

import spark.*;
import static spark.Spark.*;

public class Aeserv {

   static Connection conn;
   static ObjectMapper mapper = new ObjectMapper();

   public static void main(String[] args) {
      
      setPort(Integer.parseInt(System.getenv("PORT")));

      // Initialize database
      try {
         conn = getConnection();
         createTables();
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(1);
      }

      // Define route handlers
      get (new Route("/johnny") {
         @Override
         public Object handle (Request request, Response response) {
            try {
               dropTables();
               createTables();
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
               String from = request.queryParams("from");
               String to = request.queryParams("to");
               String body = request.body();
               saveMessage(from, to, body);
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
               String from = request.queryParams("from");
               String to = request.queryParams("to");
               String msgs = getMessages(from, to);
               response.status(200);
               return msgs;
            } catch (Exception e) {
               e.printStackTrace();
               response.status(500);
               return "FAIL";
            }
         }
      });

   }

   // Secret method to drop the tables
   static void dropTables () throws URISyntaxException, SQLException {
      Statement st = conn.createStatement();
      st.execute("DROP TABLE messages");
   }

   // Create the tables; will not explode if they already exist
   static void createTables () throws URISyntaxException, SQLException {
      Statement st = conn.createStatement();
      // This will throw if the table has already been created
      st.execute("CREATE TABLE messages (from varchar(30), to varchar(80), msg text)");
   }

   // Saves a message from one user to another
   static void saveMessage (String from, String to, String message) throws SQLException {
      Statement st = conn.createStatement();
      PreparedStatement ps = conn.prepareStatement("INSERT INTO messages VALUES (?, ?, ?)");
      ps.setString(1, from);
      ps.setString(2, to);
      ps.setString(3, message);
      ps.execute();
   }

   // TODO: Return List, not String
   // Returns a List of the messages
   static String getMessages (String from, String to) throws SQLException, IOException {
      Statement st = conn.createStatement();
      PreparedStatement ps = conn.prepareStatement("SELECT from, msg FROM messages WHERE from = ? AND to = ?");
      ps.setString(1, from);
      ps.setString(2, to);
      ResultSet rs = ps.executeQuery();
      ArrayList<HashMap<String, String>> messages = new ArrayList<HashMap<String, String>>();
      StringWriter sw = new StringWriter();
      while (rs.next()) {
         HashMap<String, String> message = new HashMap<String, String>();
         message.put("username", rs.getString(1));
         message.put("message", rs.getString(2));
         messages.add(message);
      }
      rs.close();
      mapper.writeValue(sw, messages);
      return sw.toString();
   }

   static Connection getConnection () throws URISyntaxException, SQLException, ClassNotFoundException {
      Class.forName("org.postgresql.Driver");
      URI dbUri = new URI(System.getenv("DATABASE_URL"));

      String username = dbUri.getUserInfo().split(":")[0];
      String password = dbUri.getUserInfo().split(":")[1];
      String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath() + ":" + dbUri.getPort();

      return DriverManager.getConnection(dbUrl, username, password);
   }

}