
import java.io.File;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrew
 */
public class Main {

    private class ConfigParser {

        private Element root;

        public ConfigParser(String filename) {
            try {
                File file = new File(filename);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                root = document.getDocumentElement();

            } catch (ParserConfigurationException | SAXException | IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public String getUrl() {
            String result = "jdbc:mysql://";
            Element elem = (Element) root.getElementsByTagName("host").item(0);
            result = result.concat(elem.getTextContent());
            elem = (Element) root.getElementsByTagName("port").item(0);
            result = result.concat(":" + elem.getTextContent());
            elem = (Element) root.getElementsByTagName("db_name").item(0);
            result = result.concat("/" + elem.getTextContent());
            return result;
        }

        public String getTable() {
            Element elem = (Element) root.getElementsByTagName("table").item(0);
            return elem.getTextContent();
        }

        public String getUserName() {
            Element elem = (Element) root.getElementsByTagName("user").item(0);
            return elem.getTextContent();
        }

        public String getPassword() {
            Element elem = (Element) root.getElementsByTagName("password").item(0);
            return elem.getTextContent();
        }

        public String getColumn1() {
            Element elem = (Element) root.getElementsByTagName("column1").item(0);
            return elem.getTextContent();
        }

        public String getColumn2() {
            Element elem = (Element) root.getElementsByTagName("column2").item(0);
            return elem.getTextContent();
        }

        public String getFiller() {
            Element elem = (Element) root.getElementsByTagName("filler").item(0);
            return elem.getTextContent();
        }

        public String getCondition() {
            Element elem = (Element) root.getElementsByTagName("condition").item(0);
            return elem.getTextContent();
        }
    }

    public String url = "";
    public String table = "";
    public String user = "";
    public String password = "";
    public String column1 = "";
    public String column2 = "";
    public String filler = "";
    public String condition = "";
    public String regexp_phone = "\\+?(\\d{12})";

    public static void main(String[] args) {
        new Main().run(args);
    }

    public void run(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar utility.jar config.xml");
            System.exit(1);
        }

        ConfigParser cp = new ConfigParser(args[0]);

        url = cp.getUrl();
        table = cp.getTable();
        user = cp.getUserName();
        password = cp.getPassword();
        column1 = cp.getColumn1();
        column2 = cp.getColumn2();
        filler = cp.getFiller();
        condition = cp.getCondition();

        try {
            Connection con;
            Statement st;
            ResultSet rs;

            con = getConnection(url, user, password);

            st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = st.executeQuery("SELECT * FROM " + table + " WHERE " + condition);

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
        Random rand = new Random();
        Integer fillerLength = filler.length();
        Integer textLength = text.length();
        Integer length = rand.nextInt(fillerLength - 5) + 1;
        Integer beginpos = rand.nextInt(fillerLength - length);
        String str = filler.substring(beginpos, beginpos + length);
        if (length >= textLength) {
            return str.substring(0, textLength);
        } else {
            String result = new String();
            do {
                result = result.concat(str);
                textLength -= length;
            } while (textLength >= length);
            result = result.concat(str.substring(0, textLength));
            return result;
        }
    }

    public String generateRandomNumber() {
        int size = 8;
        char[] result = new char[size];
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            result[i] = (char) ((char) rand.nextInt() % 10 + 48);
        }
        return new String(result);
    }

    public String obfuscateNumber(String text) {
        Pattern pattern = Pattern.compile(regexp_phone);
        Matcher matcher = pattern.matcher(text);
        String res = matcher.replaceAll("+1111" + generateRandomNumber());
        return res;
    }

}
