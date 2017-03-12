import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by carl on 2017-02-27.
 */
public class Authentication {
private static Connection c = null;
private static Statement stmt = null;



    public ArrayList<String> getAll(){
        ArrayList<String> userNames = new ArrayList<>();
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:/home/Gooseberrian/ProjektArbete/Strawberrian/users.db");
//            c = DriverManager.getConnection("jdbc:sqlite:users.db");
            c.setAutoCommit(false);
            System.out.println("Successfully opened DB");
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("Select * from userBerrian;");
            while(rs.next()){
                userNames.add(rs.getString("username"));
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userNames;
    }


    public String[] getUserInformation(String username){
        String userName = "";
        String password = "";
        String root = "";
        try{
            Class.forName("org.sqlite.JDBC");
//            c = DriverManager.getConnection("jdbc:sqlite:/home/Gooseberrian/ProjektArbete/Strawberrian/users.db");
            c = DriverManager.getConnection("jdbc:sqlite:users.db");
            c.setAutoCommit(false);
            System.out.println("Successfully opened DB");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("Select * from userBerrian WHERE username ="+"'"+username+"'"+";");
            while(rs.next()){
                userName = rs.getString("username");
                password  = rs.getString("password");
                root = rs.getString("root");
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String[] strarr = {userName, password, root};
        return strarr;
    }
}
