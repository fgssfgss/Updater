
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrew
 */
public class Main {

    private class ConfigParser {

        private Element root;
        private NodeList nodeList;

        public ConfigParser(String filename) {
            try {
                File file = new File(filename);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                root = document.getDocumentElement();
                nodeList = root.getElementsByTagName("column");

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

        public String getTable(int node) {
            Element elem = (Element) ((Element) nodeList.item(node)).getElementsByTagName("table").item(0);
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

        public String getFiller() {
            Element elem = (Element) root.getElementsByTagName("filler").item(0);
            return elem.getTextContent();
        }

        public String getType(int node) {
            Element elem = (Element) ((Element) nodeList.item(node)).getElementsByTagName("type").item(0);
            return elem.getTextContent();
        }

        public String getName(int node) {
            Element elem = (Element) ((Element) nodeList.item(node)).getElementsByTagName("name").item(0);
            return elem.getTextContent();
        }

        public String getCondition(int node) {
            Element elem = (Element) ((Element) nodeList.item(node)).getElementsByTagName("condition").item(0);
            return elem.getTextContent();
        }

        public Integer getLengthOfColumns() {
            return nodeList.getLength();
        }
    }

    public String filler = "";
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
        filler = cp.getFiller();
        
        try {
            Connection con;
            Statement st;
            ResultSet rs;

            con = getConnection(cp.getUrl(), cp.getUserName(), cp.getPassword());

            st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            for (int i = 0; i < cp.getLengthOfColumns(); i++) {
                rs = st.executeQuery("SELECT * FROM " + cp.getTable(i) + " WHERE " + cp.getCondition(i));

                while (rs.next()) {
                    String string = rs.getString(cp.getName(i));

                    if(cp.getType(i).equals("phone")){
                        string = obfuscateNumber(string);
                    } else {
                        string = fillWithText(string);
                    }

                    rs.updateString(cp.getName(i), string);
                    rs.updateRow();
                }
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
            result[i] = (char) ((char) rand.nextInt(10) + 48);
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
