/**
 * Created by mikaelandersson on 2017-02-02.
 */

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.InetSocketAddress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class HTTPServer {

    /*Mikkes stationära*/
//    private static String webFolder = "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\Cranberrian";
//    private static String rootFolder = "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\root";
//
/*Mikkes laptop*/
//    private static String webFolder = "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\Cranberrian";
//    private static String rootFolder = "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\root";

/*Raspberry*/
    private static String webFolder = "/home/Gooseberrian/ProjektArbete/Cranberrian";
    private static String rootFolder = "/home/Gooseberrian/ProjektArbete/root";

    private static  File fileIndex = new File (webFolder+"/index.html");

    private static int port = 8080;
    public static void main(String[] args) throws Exception {
        System.out.println("Up!");
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new GetHandler());
        server.createContext("/js", new StaticFileServer(webFolder, "/scriptBerrian.js"));
        server.createContext("/css", new StaticFileServer(webFolder, "/fancyBerrian.css"));
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class GetHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {

            Headers h = t.getResponseHeaders();
            h.add("Content-Type", "text/html");

            File[] fileDir = new File (rootFolder).listFiles();
            addFileDirToHTML(fileDir);

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

    private static void addFileDirToHTML(File[] fileArr) throws IOException {

        Document doc = Jsoup.parse(fileIndex,"UTF-8","");
        Element content = doc.getElementById("Content");
        content.empty();
        int dirCounter = 0, fileCounter = 0;
        for (File file : fileArr) {
            if (file.isDirectory()) {
                Element liTag = doc.createElement("li");
                Element aTag = doc.createElement("a");
                aTag.append(file.getName());
                liTag.appendChild(aTag);
                liTag.attr("id","Dir"+dirCounter++);
                content.appendChild(liTag);
            }
        }
        for (File file : fileArr) {
            if (!file.isDirectory()) {
                Element liTag = doc.createElement("li");
                Element aTag = doc.createElement("a");
                aTag.append(file.getName());
                liTag.appendChild(aTag);
                liTag.attr("id","File"+dirCounter++);
                content.appendChild(liTag);
            }
        }
        PrintWriter out = new PrintWriter(new FileWriter(fileIndex));
        out.print(doc);
        out.close();
    }
}