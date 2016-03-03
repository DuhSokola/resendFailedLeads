
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class main {

    private int statusCode;

    public static void main(String[] args) {
        main pi = new main();
        try {
            pi.resendLeads();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resendLeads() throws IOException {

        /**DO SELECT**/
        try
        {
            // create our mysql database connection
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/leadcollector?user=root&password=abc123&useSSL=false");

            // our SQL SELECT query.
            // if you only need a few columns, specify them by name instead of using "*"
            String query = "SELECT * FROM lead WHERE campaign LIKE 'autosalon_2016_ipad' AND sent = 1";

            // create the java statement
            Statement st = conn.createStatement();

            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            int m = 0;
            // iterate through the java resultset
            while (rs.next())
            {
                if(m==0) {


                    System.out.println(rs.getString("name"));
                    System.out.println(rs.getString("surname"));

                    // Prepare the HTTP connection
                    // If authentication is enabled, also add the authentication information
                    HttpHost target = new HttpHost("qa1100ap601.amag.car.web", 50000, "http");

                    //HttpHost target = new HttpHost("pr1100ap601.amag.car.web", 50000, "http");
                    CloseableHttpClient httpClient;

                    // Set Timeout
                    RequestConfig requestConfig = RequestConfig.custom()
                            .setSocketTimeout(3 * 1000 + 500)
                            .setConnectTimeout(3 * 1000 + 500)
                            .build();

                    // Add the credentials to the request if authentication was enabled
                    if ("false".equals("true")) {
                        CredentialsProvider credsProvider = new BasicCredentialsProvider();
                        credsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()), new UsernamePasswordCredentials("", ""));

                        httpClient = HttpClients.custom()
                                .setDefaultRequestConfig(requestConfig)
                                .setDefaultCredentialsProvider(credsProvider)
                                .build();
                    } else {
                        httpClient = HttpClients.custom()
                                .setDefaultRequestConfig(requestConfig)
                                .build();
                    }

                    AuthCache authCache = new BasicAuthCache();
                    BasicScheme basicAuth = new BasicScheme();
                    authCache.put(target, basicAuth);

                    HttpClientContext localContext = HttpClientContext.create();
                    localContext.setAuthCache(authCache);

                    HttpPost httpPost = new HttpPost("http://qa1100ap601.amag.car.web:50000/HttpAdapter/HttpMessageServlet?interfaceNamespace=http://amag.ch/web/CustomerData/V1.0&interface=CustomerDataNAW_OA&senderService=BS_WEB_Q&senderParty=&qos=EO&j_username=WEBTECH&j_password=Abcd1234");
                  //HttpPost httpPost = new HttpPost("http://pr1100ap601.amag.car.web:50000/HttpAdapter/HttpMessageServlet?interfaceNamespace=http://amag.ch/web/CustomerData/V1.0&interface=CustomerDataNAW_OA&senderService=BS_WEB_P&senderParty=&qos=EO&j_username=WEBTECH&j_password=Abcd1234");
                    httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream"); // PI requires this format in order to process the values

                    // Initialize the parameter container and assign the values to it
                    List<NameValuePair> parameters = new ArrayList<NameValuePair>();

                    parameters.add(new BasicNameValuePair("offertAnfrage", "false"));
                    parameters.add(new BasicNameValuePair("kontaktaufnahme", String.valueOf(rs.getBoolean("contact"))));
                    parameters.add(new BasicNameValuePair("prospektbestellung", String.valueOf(rs.getBoolean("orderbrochures"))));
                    parameters.add(new BasicNameValuePair("probefahrt", String.valueOf(rs.getBoolean("testdrive"))));
                    parameters.add(new BasicNameValuePair("firma", rs.getString("company")));
                    parameters.add(new BasicNameValuePair("vorname", rs.getString("name")));
                    parameters.add(new BasicNameValuePair("name", rs.getString("surname")));
                    parameters.add(new BasicNameValuePair("strasse", rs.getString("street")));
                    parameters.add(new BasicNameValuePair("strasse-no", rs.getString("house_number")));
                    parameters.add(new BasicNameValuePair("ort", rs.getString("city")));
                    parameters.add(new BasicNameValuePair("plz", String.valueOf(rs.getInt("zip"))));
                    parameters.add(new BasicNameValuePair("partner", String.valueOf(rs.getInt("dealer"))));
                    parameters.add(new BasicNameValuePair("telefon-p", rs.getString("phone")));
                    parameters.add(new BasicNameValuePair("email", rs.getString("mail")));
                    parameters.add(new BasicNameValuePair("marke", rs.getString("brand").toUpperCase()));
                    parameters.add(new BasicNameValuePair("sprache", rs.getString("language")));
                    parameters.add(new BasicNameValuePair("alter", String.valueOf(rs.getInt("age"))));
                    parameters.add(new BasicNameValuePair("aktuelle-marke", rs.getString("current_brand")));
                    parameters.add(new BasicNameValuePair("aktuelles-fzg", rs.getString("current_model")));
                    parameters.add(new BasicNameValuePair("autokauf", rs.getString("intended_purchase")));
                    parameters.add(new BasicNameValuePair("GeographicalSelect", String.valueOf(rs.getBoolean("geographical_select"))));
                    parameters.add(new BasicNameValuePair("verwendungsklausel", String.valueOf(rs.getBoolean("conditions_accepted"))));

                    // Set the preferred communication for the rs (default phone)
                    if (rs.getString("mail").trim().equals("") && !rs.getString("phone").trim().equals("")) {
                        parameters.add(new BasicNameValuePair("kommunikation", "telefon"));
                    } else if (!rs.getString("mail").trim().equals("") && rs.getString("phone").trim().equals("")) {
                        parameters.add(new BasicNameValuePair("kommunikation", "mail"));
                    } else {
                        parameters.add(new BasicNameValuePair("kommunikation", "telefon"));
                    }

                    // Concatenate remarks
                    String remarks = "";

                    // Set the reachability of the rs, if it was supplied
                    if (!rs.getString("reachability").trim().equals("")) {
                        remarks += "Erreichbarkeit: " + rs.getString("reachability") + "\n";
                    }

                    // Set the salesperson, if one was involved
                    if (rs.getString("salesperson") != null && !rs.getString("salesperson").trim().equals("")) {
                        remarks += "Verkäufer: " + rs.getString("salesperson") + "\n";
                    }

                    remarks += rs.getString("remarks");
                    parameters.add(new BasicNameValuePair("bemerkung", remarks));

                    // Create a numbered parameter for each selected option
                    for (int i = 0; i < rs.getString("options").split(",").length; i++) {
                        parameters.add(new BasicNameValuePair("opt-" + i, rs.getString("options").split(",")[i]));
                    }

                    // Set the salutation accordingly
                    if (rs.getString("salutation").toLowerCase().equals("m")) {
                        parameters.add(new BasicNameValuePair("anrede", "herr"));
                    } else if (rs.getString("salutation").toLowerCase().equals("f")) {
                        parameters.add(new BasicNameValuePair("anrede", "frau"));
                    }

                    // Check the brand of the lead and build the campaign-string accordingly

                    if (rs.getString("brand").toLowerCase().equals("vwnf")) {
                        parameters.add(new BasicNameValuePair("kampagne", "webvwnf " + "autosalon_genf_2016_ipad"));
                    } else if (rs.getString("brand").toLowerCase().equals("vw")) {
                        parameters.add(new BasicNameValuePair("kampagne", "webvwpw " + "autosalon_genf_2016_ipad"));
                    } else if (rs.getString("brand").toLowerCase().equals("audi")) {
                        parameters.add(new BasicNameValuePair("kampagne", "webaudi " + "autosalon_genf_2016_ipad"));
                    } else if (rs.getString("brand").toLowerCase().equals("seat")) {
                        parameters.add(new BasicNameValuePair("kampagne", "webseat " + "autosalon_genf_2016_ipad"));
                    } else if (rs.getString("brand").toLowerCase().equals("skoda")) {
                        parameters.add(new BasicNameValuePair("kampagne", "webskoda " + "autosalon_genf_2016_ipad"));
                    } else {
                        parameters.add(new BasicNameValuePair("kampagne", "UnbekannteMarke " + "autosalon_genf_2016_ipad"));
                    }

                    // Add the parameters and send the request
                    httpPost.setEntity(new UrlEncodedFormEntity(parameters));
                    CloseableHttpResponse response = httpClient.execute(target, httpPost, localContext);
                    try {
                        EntityUtils.consume(response.getEntity());
                        this.statusCode = response.getStatusLine().getStatusCode();
                        System.out.println("Server responded with following status: {}" + response.getStatusLine());
                    } catch (Exception ex) {
                        System.out.println(ex.getStackTrace());
                        ex.printStackTrace();
                    } finally {
                        System.out.println();
                        response.close();
                    }
                }
                m++;
            }
            st.close();
        }
        catch (Exception e)
        {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
    }
}