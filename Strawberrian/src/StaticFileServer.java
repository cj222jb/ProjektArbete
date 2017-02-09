import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Mikael Andersson on 2017-02-08.
 */

@SuppressWarnings("restriction")
public class StaticFileServer implements HttpHandler {


    private static String webFolder;
    public StaticFileServer(String webFolder) {
        this.webFolder = webFolder;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String fileId = exchange.getRequestURI().getPath();
        File file =  new File (webFolder+"/style.css");
        System.out.println("hej");
        if (file == null) {
            String response = "Error 404 File not found.";
            exchange.sendResponseHeaders(404, response.length());
            OutputStream output = exchange.getResponseBody();
            output.write(response.getBytes());
            output.flush();
            output.close();
        } else {
            exchange.sendResponseHeaders(200, 0);
            OutputStream output = exchange.getResponseBody();
            FileInputStream fs = new FileInputStream(file);
            final byte[] buffer = new byte[0x10000];
            int count = 0;
            while ((count = fs.read(buffer)) >= 0) {
                output.write(buffer, 0, count);
            }
            output.flush();
            output.close();
            fs.close();
        }
    }

}