/**
 * Created by mikaelandersson on 2017-02-02.
 */

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class HTTPServer {


    private static String webFolder = "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\Cranberrian";
    private static String rootFolder = "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\root";

    private static  File fileIndex = new File (webFolder+"\\index.html");

    private static int port = 8080;
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new GetHandler());
        server.createContext("/static", new StaticFileServer(webFolder));
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class GetHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {

            Headers h = t.getResponseHeaders();
            h.add("Content-Type", "text/html");

            File[] fileDir = new File (rootFolder).listFiles();
            showFiles(fileDir,webFolder);


            byte [] bytearray  = new byte [(int)fileIndex.length()];
            FileInputStream fis = new FileInputStream(fileIndex);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(bytearray, 0, bytearray.length);

            t.sendResponseHeaders(200, fileIndex.length());
            OutputStream os = t.getResponseBody();
            os.write(bytearray,0,bytearray.length);
            os.close();
        }
    }
    public static void showFiles(File[] files, String filename){
        //Shows every file and folder of requested address as a HTML index list.
        PrintWriter writer;
        try {
            writer = new PrintWriter(filename+"/index.html","UTF-8" );

          writer.println("<html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"/static/>\"> </head> <body><h1>");
//            writer.println("<HTML><html><head><script src=\"/static\"></script></head> <body><h1>");
            for (File file : files) {
                if (file.isDirectory()) {
                    writer.println(("<li class=\"dir\">"  + file.getName()+"</li>"));
                    showFiles(file.listFiles(), filename); // Calls same method again.
                } else {
                    writer.println("<li class=\"file\">"  + file.getName()+"</li>");
                }
            }
            writer.println("</h1></body></html>");
            writer.close();
        } catch (FileNotFoundException e) {


        } catch (UnsupportedEncodingException e) {

        }
    }

}