
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import static java.sql.DriverManager.getConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Andrew
 */
public class Main {

    public String url = "jdbc:mysql://";
    public String table = "";
    public String user = "root";
    public String password = "toor";
    public String column1 = "text1";
    public String column2 = "text2";
    public String filler = "";
    public String fillerfilename = "";
    public String regexp_phone = "\\+?(\\d{12})";

    public static void main(String[] args) {
        new Main().run(args);
    }

    public void run(String[] args) {
        if(args.length < 6)
        {
            System.out.println("Usage: java -jar utility.jar host:port db_name table username password column1 column2 filler.txt");
            System.exit(1);
        }
        
        url = url.concat(args[0] + "/" + args[1]); // arg 0 for host:port, arg 1 for db
        table = args[2]; // arg 2 for table
        user = args[3]; //  arg 3 for username
        password = args[4]; // arg 4 for password
        column1 = args[5]; // arg 5 for column with text
        column2 = args[6]; // arg 6 for column with phone number
        fillerfilename = args[7]; // arg 7 for filler.txt
        try {
            BufferedReader in = new BufferedReader(new FileReader(fillerfilename));
            String tmp;
            while((tmp = in.readLine()) != null){
                filler = filler.concat(tmp);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            Connection con;
            Statement st;
            ResultSet rs;

            con = getConnection(url, user, password);

            st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = st.executeQuery("SELECT * FROM " + table);

            while (rs.next()) {
                String s1 = rs.getString(column1);
                String s2 = rs.getString(column2);

                s1 = fillWithText(s1);
                s2 = obfuscateNumber(s2);

                rs.updateString(column1, s1);
                rs.updateString(column2, s2);
                rs.updateRow();
            }

        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String fillWithText(String text) {
        Integer fillerLength = filler.length();
        Integer textLength = text.length();
        if (fillerLength >= textLength) {
            return filler.substring(0, textLength);
        } else {
            String result = new String();
            do {
                result = result.concat(filler);
                textLength -= fillerLength;
            } while (textLength >= fillerLength);
            result = result.concat(filler.substring(0, textLength));
            return result;
        }
    }

    public String generateRandomNumber() {
        int size = 7;
        char[] result = new char[size];
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            result[i] = (char)((char)rand.nextInt()% 10 + 48);
        }
        return new String(result);
    }

    public String obfuscateNumber(String text) {
        Pattern pattern = Pattern.compile(regexp_phone);
        Matcher matcher = pattern.matcher(text);
        String res = matcher.replaceAll("+1111"+generateRandomNumber());
        return res;
    }

}
