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
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HTTPServer {

    private  String rootFolder;
    private  String webFolder;
    private  File htmlSPA;
    private  HttpServer server;
    private  int port;
    private  File[] fileDirectory;
    private  String currentFolder;
    private final int limitSize = 10000000;
    public HTTPServer(String webFolder, int port){
        this.port = port;
        this.webFolder = webFolder;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("[SERVER UP, RUNNING ON PORT: "+port+"]");
            server.createContext("/js", new StaticFileServer(webFolder, "/scriptBerrian.js"));
            server.createContext("/css", new StaticFileServer(webFolder, "/fancyBerrian.css"));
            pushPictures();
            server.setExecutor(null); // creates a default executor
            server.start();
            server.createContext("/", new HTMLHandler(rootFolder,"/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void run(String rootFolder, String user)  {
        this.rootFolder = rootFolder;
        htmlSPA = new File (webFolder+"/dir.html");
        System.out.println("[USER CONNECTED, ROOT FOLDER: "+rootFolder+"]");
        currentFolder= rootFolder;
        String userURL = "/"+user+"/";
        server.createContext(userURL, new HTMLHandler(rootFolder,"/"));
        server.createContext(userURL + "download", new GETFolderHandler(user));
        server.createContext(userURL + "upload", new POSTHandler());
        iterateFolders(rootFolder,"/"+user+"/","");
    }
    private void pushPictures() {
        File[] imgArr = fileDirectory = new File (webFolder+"/images").listFiles();
        for (File imgFile : imgArr) {
            server.createContext("/images/"+imgFile.getName(), new StaticFileServer(webFolder, "/images/"+imgFile.getName()));
        }
    }
    private void iterateFolders(String rootFolder,String folderURL, String nextURL){
        File[] fileArr = new File(rootFolder+"/"+nextURL).listFiles();
        String folder ;
        for (File file : fileArr) {
            if (file.isDirectory()) {
                folder = nextURL+file.getName()+"/";
                server.createContext(folderURL+folder, new HTMLHandler(rootFolder,folder));
                server.createContext(folderURL+folder + "download", new GETFolderHandler(file.getName()));
                server.createContext(folderURL+ folder + "upload", new POSTHandler());
                iterateFolders(rootFolder,folderURL,folder);
            }
            else {
                server.createContext(folderURL+nextURL+file.getName(), new GETFileHandler(file.getName()));
            }
        }
    }
    private void addFileDirToHTML(File[] fileArr) throws IOException {

        Document doc = Jsoup.parse(htmlSPA,"UTF-8","");
        Element dirContent = doc.getElementById("folders");
        Element fileContent = doc.getElementById("files");
        dirContent.empty();
        fileContent.empty();
        for (File dir : fileArr) {
            if (dir.isDirectory()) {
                Element liTag = doc.createElement("li");
                Element aTag = doc.createElement("a");
                liTag.attr("class","folder");
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
                Element deleteFile = doc.createElement("div");
                aTag.append(file.getName());
                fileSizeTag.append(fileSize(file.length()));
                fileSizeTag.attr("class","fileSize");
                deleteFile.attr("class","deleteFile");
                liTag.appendChild(fileSizeTag);
                liTag.appendChild(aTag);
                liTag.appendChild(deleteFile);

                fileContent.appendChild(liTag);
            }
        }
        PrintWriter out = new PrintWriter(new FileWriter(htmlSPA));
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
    private void errorHandler(HttpExchange exchange, String response, int errorCode) throws IOException {
        //TODO need to fix window.location(-1)
        exchange.sendResponseHeaders(errorCode, response.length());
        OutputStream output = exchange.getResponseBody();
        output.write(response.getBytes());
        output.flush();
        output.close();
    }
    private class HTMLHandler implements HttpHandler {
        private final String folderName;
        private final String rootFolder;
        public HTMLHandler(String rootFolder,String name) {
            this.rootFolder = rootFolder;
            folderName = name;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            currentFolder= rootFolder+"/"+folderName;
            fileDirectory = new File (currentFolder).listFiles();
            addFileDirToHTML(fileDirectory);

            exchange.sendResponseHeaders(200, 0);
            OutputStream output = exchange.getResponseBody();
            FileInputStream fs = new FileInputStream(htmlSPA);
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

        }
    }
    private class GETFolderHandler implements HttpHandler {
        private final String name;
        public GETFolderHandler(String name) {
            this.name = name;
        }

        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Client requested   : "+ currentFolder);
            Headers header = exchange.getResponseHeaders();
            header.add("Content-Disposition", "attachment; filename=\""+name+".zip\"");
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
                String response = "Error 404: File not found.";
              errorHandler(exchange, response, 404);
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
    private class POSTHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            Headers header = exchange.getRequestHeaders();

            int contentLength = Integer.parseInt(header.getFirst("Content-Length"));
            if(contentLength>limitSize){
                String response = "Error 413: Payload Too Large";
                errorHandler(exchange,response, 413);
            }
            else {
                String referer = header.getFirst("Referer");
                referer = getURLFromRequest(referer);
                String receivedInput = "";
                final byte[] buffer = new byte[64000];
                int count;
                while ((count = exchange.getRequestBody().read(buffer)) >= 0) {
                    receivedInput += new String(buffer, 0, count, "ISO-8859-1");
                }
                String parts[] = receivedInput.split("\r\n\r\n", 2);
                String head = parts[0];
                String fileName = searchString("filename=", head);

                fileName = fileName.replaceAll("\"", "");
                parts = parts[1].split("------", 2);
                String payload = parts[0];

                File file = new File(currentFolder + fileName);
                FileOutputStream f_output = new FileOutputStream(file);
                f_output.write(payload.getBytes("ISO-8859-1"));
                f_output.close();

                String fileSize = fileSize(payload.length());
                System.out.println("Client posted: " + fileName + ", size: " + fileSize);

                exchange.sendResponseHeaders(200, 0);
                OutputStream output = exchange.getResponseBody();
                FileInputStream fs = new FileInputStream(htmlSPA);
                while ((count = fs.read(buffer)) >= 0) {
                    output.write(buffer, 0, count);
                }
                output.flush();
                output.close();
//TODO      fixa nya get till det som är postat.
                server.createContext(referer + fileName, new GETFileHandler(fileName));
            }
        }
        private String searchString(String var, String data)
        {
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
        private String getURLFromRequest(String referer){
            try {
                URI uri = new URI(referer);
                referer = uri.getPath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return referer;
        }


    }



}