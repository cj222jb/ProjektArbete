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
    private static int port = 8081;
    private static File[] fileDir;
    private static String currentFolder;
    public static void main(String[] args) throws Exception {
        currentFolder= rootFolder;

        System.out.println("[SERVER UP, RUNNING ON PORT: "+port+"]");
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/js", new StaticFileServer(webFolder, "/scriptBerrian.js"));
        server.createContext("/css", new StaticFileServer(webFolder, "/fancyBerrian.css"));
        server.createContext("/", new HTMLHandler(""));
        pushPictures();
        iterateFolders("/");
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private static void pushPictures() {
        File[] imgArr = fileDir = new File (webFolder+"/images").listFiles();
        for (File imgFile : imgArr) {
            server.createContext("/images/"+imgFile.getName(), new StaticFileServer(webFolder, "/images/"+imgFile.getName()));
        }
    }
    private static void iterateFolders(String folderURI){
        File[] fileArr = new File(rootFolder+folderURI).listFiles();
        String folder;
        for (File file : fileArr) {
            if (file.isDirectory()) {
                folder = folderURI+file.getName()+"/";
                server.createContext(folder, new HTMLHandler(folder));
                iterateFolders(folder);
            }
            else {
                server.createContext(folderURI+file.getName(), new GETHandler(file.getName()));
            }
        }
    }
    private static void addFileDirToHTML(File[] fileArr) throws IOException {

        Document doc = Jsoup.parse(fileIndex,"UTF-8","");
        Element dirContent = doc.getElementById("folders");
        Element fileContent = doc.getElementById("files");
        dirContent.empty();
        fileContent.empty();
        for (File dir : fileArr) {
            if (dir.isDirectory()) {
                Element liTag = doc.createElement("li");
                Element aTag = doc.createElement("a");
                aTag.append(dir.getName());
                liTag.appendChild(aTag);
                dirContent.appendChild(liTag);
            }
        }
        for (File file : fileArr) {
            if (!file.isDirectory()) {
                Element liTag = doc.createElement("li");
                Element aTag = doc.createElement("a");
                aTag.append(file.getName());
                liTag.appendChild(aTag);
                fileContent.appendChild(liTag);
            }
        }
        PrintWriter out = new PrintWriter(new FileWriter(fileIndex));
        out.print(doc);
        out.close();
    }

    static class HTMLHandler implements HttpHandler {
        private final String folderName;
        public HTMLHandler(String name) {
            folderName = name;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            currentFolder= rootFolder+"/"+folderName;
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



    static class GETHandler implements HttpHandler {
        private final String name;
        public GETHandler(String name) {
            this.name = name;
        }
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Client requested: "+ currentFolder+name);
            Headers header = exchange.getResponseHeaders();
            header.add("Content-Disposition", "attachment; filename=\""+name+"\"");
            header.add("Content-Type", "application/force-download");
            header.add("Content-Transfer-Encoding", "binary");

            File file = new File (currentFolder+name);
            byte [] bytearray  = new byte [(int)file.length()];
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(bytearray, 0, bytearray.length);
            exchange.sendResponseHeaders(200, file.length());
            OutputStream os = exchange.getResponseBody();
            os.write(bytearray,0,bytearray.length);
            os.close();
        }
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
    static class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            System.out.println("Serving the request");

            if (he.getRequestMethod().equalsIgnoreCase("POST")) {
                he.sendResponseHeaders(200,0);
                try {
                    Headers requestHeaders = he.getRequestHeaders();
                    //  Set<Map.Entry<String, List<String>>> entries = requestHeaders.entrySet();

                    int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));
                    System.out.println("" + requestHeaders.getFirst("Content-length"));

                    InputStream is = he.getRequestBody();

                    //   convertStreamToString(is);
                    //  System.out.println(convertStreamToString(is));


                    byte[] data = new byte[contentLength];
                    int length = is.read(data);

                    System.out.println(length);

                    FileOutputStream fos = new FileOutputStream("C:\\Users\\carl\\Documents\\GitHub\\2dt301\\ProjektArbete\\Strawberrian\\src\\imgage.txt");
                    fos.write(data);
                    fos.close();


                    he.close();

                } catch (NumberFormatException | IOException e) {
                }
            }

        }
    }
    static String convertStreamToString(InputStream is) throws IOException {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        //Reads from inputstream into a string
        String temp = s.hasNext() ? s.next() : "";
        System.out.println(temp);
        //Splits temp into header and body
        String parts[] = temp.split("\r\n\r\n", 2);
        //Search header for the files name
        String fileName = searchString("filename=", parts[0]);
        // Delete " from filename
        fileName = fileName.replace("\"","");
        // Search header for contentType
        String contentType  = searchString("Content-Type: image/", parts[0]);


        parts[1] = parts[1].replaceAll("------WebKitFormBoundaryPAGtmGUpmBYfBpgK--", "");
        String content[] = parts[1].split("------WebKit", 2);
        byte[] b = content[0].getBytes("ISO-8859-1");

        FileOutputStream fos = new FileOutputStream("C:\\Users\\carl\\Documents\\GitHub\\2dt301\\ProjektArbete\\Strawberrian\\src\\"+fileName);
        fos.write(b);
        fos.close();

        System.out.println(content[0]);


        return temp; //s.hasNext() ? s.next() : "";
    }
    /*  static String fileType(InputStream is){
          java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
          if(s.hasNext() ? s.next() : "" == "filename")
          return filename;
      }
  */
    private static String searchString(String var, String data){
        int startIndex = data.indexOf(var);
        int endIndex = data.indexOf("\r\n", startIndex);

        //Check if variable is found:
        if (startIndex == -1)
            return null;

        //Check if variable is on last line:
        if (endIndex == -1)
            endIndex = data.length();

        return data.substring(startIndex + var.length(), endIndex);
    }
}


}