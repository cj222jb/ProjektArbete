import java.sql.*;
import java.util.ArrayList;

/**
 * Created by carl on 2017-02-27.
 */
public class DBHandler {
    private static Connection db_Connection = null;
    private static Statement db_Statement = null;



    public ArrayList<String> getAll(){
        ArrayList<String> userNames = new ArrayList<>();
        try{
            Class.forName("org.sqlite.JDBC");
//            db_Connection = DriverManager.getConnection("jdbc:sqlite:/home/Gooseberrian/ProjektArbete/Strawberrian/users.db"); //Filepath for raspberry
            db_Connection = DriverManager.getConnection("jdbc:sqlite:users.db");    //Filepath for non-raspberry
            db_Connection.setAutoCommit(false);
            db_Statement = db_Connection.createStatement();
            ResultSet rs = db_Statement.executeQuery("Select * from userBerrian;");
            while(rs.next()){
                userNames.add(rs.getString("username"));
            }
            rs.close();
            db_Statement.close();
            db_Connection.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userNames;
    }


    public String[] getUserInformation(String username){
        String userName = "", password = "", root = "";
        try{
            /*Creates a connection to the database*/
            Class.forName("org.sqlite.JDBC");
            //            db_Connection = DriverManager.getConnection("jdbc:sqlite:/home/Gooseberrian/ProjektArbete/Strawberrian/users.db"); //Filepath for raspberry
            db_Connection = DriverManager.getConnection("jdbc:sqlite:users.db");    //Filepath for non-raspberry
            db_Connection.setAutoCommit(false);

            /*Query's the database from the table 'userBerrian' to find information of user.*/
            db_Statement = db_Connection.createStatement();
            ResultSet rs = db_Statement.executeQuery("SELECT * FROM userBerrian WHERE username ="+"'"+username+"'"+";");
            while(rs.next()){
                userName = rs.getString("username");
                password  = rs.getString("password");
                root = rs.getString("root");
            }
            rs.close();
            db_Statement.close();
            db_Connection.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        /*Returns the corresponding password and path to folder from the user.*/
        String[] strarr = {userName, password, root};
        return strarr;
    }
}
