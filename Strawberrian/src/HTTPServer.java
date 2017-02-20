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
import java.text.DecimalFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HTTPServer {

    /*Mikkes stationära*/
//    private  String webFolder = "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\Cranberrian";
    /*Mikkes laptop*/
//    private static String webFolder = "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\Cranberrian";
//    private static String rootFolder = "C:\\Users\\Mikael Andersson\\Documents\\Projects\\ProjektArbete\\root";
    /*Raspberry*/
    private static String webFolder = "/home/Gooseberrian/ProjektArbete/Cranberrian";

    private  String rootFolder;
    private  File fileIndex;
    private  HttpServer server;
    private  int port;
    private  File[] fileDir;
    private  String currentFolder;
    public HTTPServer(String url){
        rootFolder = url;
        port = 8080;
        fileIndex = new File (webFolder+"/index.html");
        try {
            run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void run() throws IOException {
        currentFolder= rootFolder;
        System.out.println("[SERVER UP, RUNNING ON PORT: "+port+"]");
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/js", new StaticFileServer(webFolder, "/scriptBerrian.js"));
        server.createContext("/css", new StaticFileServer(webFolder, "/fancyBerrian.css"));
        server.createContext("/", new HTMLHandler(""));
        server.createContext("/download", new GETFolderHandler());
        pushPictures();
        iterateFolders("/");
        server.setExecutor(null); // creates a default executor
        server.start();
    }
    private void pushPictures() {
        System.out.println(webFolder+"/images");
        File[] imgArr = fileDir = new File (webFolder+"/images").listFiles();
        for (File imgFile : imgArr) {
            server.createContext("/images/"+imgFile.getName(), new StaticFileServer(webFolder, "/images/"+imgFile.getName()));
        }
    }
    private void iterateFolders(String folderURL){
        File[] fileArr = new File(rootFolder+folderURL).listFiles();
        String folder;
        for (File file : fileArr) {
            if (file.isDirectory()) {
                folder = folderURL+file.getName()+"/";
                server.createContext(folder, new HTMLHandler(folder));
                server.createContext(folder + "download", new GETFolderHandler());
                iterateFolders(folder);
            }
            else {
                server.createContext(folderURL+file.getName(), new GETFileHandler(file.getName()));
            }
        }
    }
    private void addFileDirToHTML(File[] fileArr) throws IOException {

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
                Element fileSizeTag = doc.createElement("div");
                aTag.append(file.getName());
                fileSizeTag.append(fileSize(file.length()));
                fileSizeTag.attr("class","fileSize");
                liTag.appendChild(aTag);
                liTag.appendChild(fileSizeTag);
                fileContent.appendChild(liTag);
            }
        }
        PrintWriter out = new PrintWriter(new FileWriter(fileIndex));
        out.print(doc);
        out.close();
    }

    private String fileSize(long fileLength) {
        DecimalFormat df = new DecimalFormat("0.0");
        float temp = fileLength;
        if(!(temp>1024)){
            return df.format(temp)+"B";
        }
        temp = temp/1024;
        if(!(temp>1024)){
            return df.format(temp)+"KB";
        }
        temp = temp/1024;
        if(!(temp>1024)){
            return df.format(temp)+"MB";
        }
        temp = temp/1024;
        if(!(temp>1024)){
            return df.format(temp)+"GB";
        }
        return null;
    }

    private void zipFolder(String url) throws IOException {
        File[] fileArr = new File(url).listFiles();
        System.out.println(url);
        File zipFile = new File(url+"/folder.zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        FileInputStream in;
        byte[] buffer = new byte[1024];
        for (File file : fileArr) {
            if (!file.isDirectory()) {
                ZipEntry e = new ZipEntry(file.getName());
                out.putNextEntry(e);
                in = new FileInputStream(url+"/"+file.getName());
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                in.close();
            }
        }
        out.closeEntry();
        out.close();
    }

    private String convertStreamToString(InputStream is) throws IOException {
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
    private String searchString(String var, String data){
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
    private class HTMLHandler implements HttpHandler {
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
    private class GETFileHandler implements HttpHandler {
        private final String name;
        public GETFileHandler(String name) {

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
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            bufferedInputStream.read(bytearray, 0, bytearray.length);
            exchange.sendResponseHeaders(200, file.length());
            OutputStream outputStream = exchange.getResponseBody();

            outputStream.write(bytearray,0,bytearray.length);

            fileInputStream.close();
            bufferedInputStream.close();
            outputStream.close();
            file.delete();
        }
    }
    private class GETFolderHandler implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Client requested   : "+ currentFolder);
            Headers header = exchange.getResponseHeaders();
            header.add("Content-Disposition", "attachment; filename=\"bob.zip\"");
            header.add("Content-Type", "application/force-download");
            header.add("Content-Transfer-Encoding", "binary");

            zipFolder(currentFolder);

            File file = new File (currentFolder+"folder.zip");

            byte [] bytearray  = new byte [(int)file.length()];
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            bufferedInputStream.read(bytearray, 0, bytearray.length);
            exchange.sendResponseHeaders(200, file.length());
            OutputStream outputStream = exchange.getResponseBody();

            outputStream.write(bytearray,0,bytearray.length);

            fileInputStream.close();
            bufferedInputStream.close();
            outputStream.close();
            file.delete();
        }


    }
    private class StaticFileServer implements HttpHandler {
        private  String folder;
        private  String fileName;

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
    private class MyHandler implements HttpHandler {

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



}