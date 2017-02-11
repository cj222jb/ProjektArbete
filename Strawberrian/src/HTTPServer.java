/**
 * Created by mikaelandersson on 2017-02-02.
 */

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
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
    private static String webFolder = "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\Cranberrian";
    private static String rootFolder = "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\root";

//
/*Mikkes laptop*/
//    private static String webFolder = "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\Cranberrian";
//    private static String rootFolder = "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\root";

/*Raspberry*/
//    private static String webFolder = "/home/Gooseberrian/ProjektArbete/Cranberrian";
//    private static String rootFolder = "/home/Gooseberrian/ProjektArbete/root";

    private static  File fileIndex = new File (webFolder+"/index.html");
    private static   HttpServer server;
    private static int port = 8080;
    private static File[] fileDir;
    private static String currentFolder;
    public static void main(String[] args) throws Exception {
    currentFolder= rootFolder;

        System.out.println("Server up!");
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/js", new StaticFileServer(webFolder, "/scriptBerrian.js"));
        server.createContext("/css", new StaticFileServer(webFolder, "/fancyBerrian.css"));
        server.createContext("/", new HTMLHandler());
        pushPictures();
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private static void pushPictures() {
        File[] imgArr = fileDir = new File (webFolder+"/images").listFiles();
        for (File imgFile : imgArr) {
            server.createContext("/images/"+imgFile.getName(), new StaticFileServer(webFolder, "/images/"+imgFile.getName()));
        }

    }


    static class HTMLHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            fileDir = new File (currentFolder).listFiles();
            addFileDirToHTML(fileDir);

            exchange.sendResponseHeaders(200, 0);
            OutputStream output = exchange.getResponseBody();
            FileInputStream fs = new FileInputStream(fileIndex);
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
    private static void addFileDirToHTML(File[] fileArr) throws IOException {

        Document doc = Jsoup.parse(fileIndex,"UTF-8","");
        Element dirContent = doc.getElementById("Folders");
        Element fileContent = doc.getElementById("Files");
        dirContent.empty();
        fileContent.empty();
        int dirCounter = 0, fileCounter = 0;
        for (File dir : fileArr) {
            if (dir.isDirectory()) {
                Element liTag = doc.createElement("li");
                Element aTag = doc.createElement("a");
                aTag.append(dir.getName());
                liTag.appendChild(aTag);
                liTag.attr("id","Dir"+dirCounter++);
                liTag.attr("class", "Dir");
                dirContent.appendChild(liTag);
               // server.createContext("/"+dir.getName(), new HTMLHandler(rootFolder+"/Dir"));
            }
        }

        for (File file : fileArr) {
            if (!file.isDirectory()) {
                Element liTag = doc.createElement("li");
                Element aTag = doc.createElement("a");
                aTag.append(file.getName());
                liTag.appendChild(aTag);
                liTag.attr("id","File"+fileCounter++);
                liTag.attr("class","File");
                fileContent.appendChild(liTag);
            }
        }
        PrintWriter out = new PrintWriter(new FileWriter(fileIndex));
        out.print(doc);
        out.close();
    }
    static class StaticFileServer implements HttpHandler {


        private static String folder;
        private final String fileName;

        public StaticFileServer(String folder, String fileName) {
            this.fileName = fileName;
            this.folder = folder;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File file =  new File (webFolder+fileName);
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
//    static class GetHandler implements HttpHandler {
//
//        private static String currentFolder;
//        public GetHandler(String input) {
//            currentFolder = input;
//        }
//
//        public void handle(HttpExchange t) throws IOException {
//            Headers h = t.getResponseHeaders();
//            h.add("Content-Type", "text/html");
//            fileDir = new File (currentFolder).listFiles();
//            addFileDirToHTML(fileDir);
//
//            byte [] bytearray  = new byte [(int)fileIndex.length()];
//            FileInputStream fis = new FileInputStream(fileIndex);
//            BufferedInputStream bis = new BufferedInputStream(fis);
//            bis.read(bytearray, 0, bytearray.length);
//
//            t.sendResponseHeaders(200, fileIndex.length());
//            OutputStream os = t.getResponseBody();
//            os.write(bytearray,0,bytearray.length);
//            os.close();
//        }
//    }
}