/**
 * Created by mikaelandersson on 2017-02-02.
 */

import com.sun.net.httpserver.*;
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
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HTTPServer {

    private  String rootFolder;
    private  String webFolder;
    private  String root;
    private  File htmlSPA;
    private  File htmlIndex;
    private  HttpServer server;
    private  int port;
    private  File[] fileDirectory;
    private  String currentFolder;
    private final int limitSize = 10000000;

    public HTTPServer(String webFolder,String root ,int port){
        this.port = port;
        this.root = root;
        this.webFolder = webFolder;
        try {
            Authentication authenticate = new Authentication();
            ArrayList<String> userList = authenticate.getAll();
            server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("[SERVER UP, RUNNING ON PORT: "+port+"]");
            server.createContext("/js", new StaticFileHandler("/scriptBerrian.js"));
            server.createContext("/css", new StaticFileHandler("/fancyBerrian.css"));
            pushPictures();
            server.setExecutor(null); // creates a default executor
            server.start();
            //   server.createContext("/", new SPAContextHandler(rootFolder,"/"));
            server.createContext("/", new HTMLIndexHandler());
            for(int i = 0; i < userList.size(); i++){
                System.out.println(userList.get(i));
                System.out.println(authenticate.getUserInformation(userList.get(i))[2]);
                run(root+authenticate.getUserInformation(userList.get(i))[2], userList.get(i) );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void run(String rootFolder, String user) throws IOException {
        Authentication authenticate = new Authentication();
        ArrayList<String> userNames = authenticate.getAll();
        this.rootFolder = rootFolder;
        System.out.println(rootFolder);
        htmlSPA = new File (webFolder+"/dir.html");
        htmlIndex = new File(webFolder+"/index.html");
        System.out.println("[USER CONNECTED, ROOT FOLDER: "+rootFolder+"]");

        currentFolder= rootFolder;
        String userURL = "/"+user+"/";
        //  server.createContext(userURL, new SPAContextHandler(rootFolder,"/"));
        server.createContext(userURL + "download", new GETFolderHandler(user));
        server.createContext(userURL + "upload", new POSTFileHandler());

        server.createContext(userURL+"addfolder", new ADDFolderHandler(user));
        HttpContext hc1 = server.createContext(userURL, new SPAContextHandler(rootFolder, ""));
        hc1.setAuthenticator(new BasicAuthenticator(user) {
            @Override
            public boolean checkCredentials(String user, String pwd) {
                return user.equals(user) && pwd.equals(authenticate.getUserInformation(user)[1]);
            }
        });
        displayUsersToHTML(userNames);
        iterateFolders(rootFolder,"/"+user+"/","");
    }
    private void pushPictures() {
        File[] imgArr = fileDirectory = new File (webFolder+"/images").listFiles();
        for (File imgFile : imgArr) {
            server.createContext("/images/"+imgFile.getName(), new StaticFileHandler("/images/"+imgFile.getName()));
        }
    }
    private void displayUsersToHTML(ArrayList<String> userNames) throws IOException {
        Document doc = Jsoup.parse(htmlIndex,"UTF-8","");
        Element userContent = doc.getElementById("userDiv");
        userContent.empty();
        for (String userName : userNames) {
            Element aTag = doc.createElement("a");
            aTag.append(userName);
            userContent.appendChild(aTag);
        }
        PrintWriter out = new PrintWriter(new FileWriter(htmlIndex));
        out.print(doc);
        out.close();
    }
    private void iterateFolders(String rootFolder,String folderURL, String nextURL){
        File[] fileArr = new File(rootFolder+"/"+nextURL).listFiles();
        String folder ;
        for (File file : fileArr) {
            if (file.isDirectory()) {

                folder = nextURL+file.getName()+"/";
                server.createContext(folderURL+folder, new SPAContextHandler(rootFolder,folder));
                server.createContext(folderURL+folder + "download", new GETFolderHandler(file.getName()));
                server.createContext(folderURL+ folder + "upload", new POSTFileHandler());
                server.createContext(folderURL+folder+"deletefolder", new DELETEFolderHandler(file.getName()));
                server.createContext(folderURL+folder+"addfolder", new ADDFolderHandler(file.getName()));
                iterateFolders(rootFolder,folderURL,folder);
            }
            else {
                server.createContext(folderURL+nextURL+file.getName(), new GETFileHandler(file.getName()));
                server.createContext(folderURL+nextURL+file.getName()+"/deletefile", new DELETEFileHandler(file.getName()));
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
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
    private void errorHandler(HttpExchange exchange, String response, int errorCode){

        try {
            exchange.sendResponseHeaders(errorCode, response.length());
            OutputStream output = exchange.getResponseBody();
            output.write(response.getBytes());
            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void htmlHandler(HttpExchange exchange, File file){
        try {
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
        } catch (IOException e) {
            String response = "Error 500: Internal Server Error.";
            errorHandler(exchange, response, 500);
        }
    }
    private String searchString(String var, String data) {
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
    private class SPAContextHandler implements HttpHandler {
        private final String folderName;
        private final String rootFolder;
        public SPAContextHandler(String rootFolder, String name) {
            this.rootFolder = rootFolder;
            folderName = name;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            currentFolder= rootFolder;//+"/"+folderName
            System.out.println(currentFolder);
            fileDirectory = new File (currentFolder).listFiles();
            addFileDirToHTML(fileDirectory);
            htmlHandler(exchange, htmlSPA);
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
            try {
                File file = new File (currentFolder+name);
                if(!file.exists())
                    throw new Exception();
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
            } catch (Exception e) {
                String response = "Error 404: File not found.";
                errorHandler(exchange, response, 404);
            }
        }
    }
    private class HTMLIndexHandler implements HttpHandler{
        public HTMLIndexHandler(){

        }
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            htmlHandler(exchange, htmlIndex);
        }
    }
    private class GETFolderHandler implements HttpHandler {
        private final String name;
        public GETFolderHandler(String name) {
            this.name = name;
        }

        public void handle(HttpExchange exchange){
            System.out.println("Client requested   : "+ currentFolder);
            Headers header = exchange.getResponseHeaders();
            header.add("Content-Disposition", "attachment; filename=\""+name+".zip\"");
            header.add("Content-Type", "application/force-download");
            header.add("Content-Transfer-Encoding", "binary");

            try {
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
            } catch (IOException e) {
                String response = "Error 500: Internal Server Error.";
                errorHandler(exchange, response, 500);
            }
        }


    }
    private class StaticFileHandler implements HttpHandler {

        private  String fileName;
        public StaticFileHandler(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void handle(HttpExchange exchange){
            try {
                File file = new File(webFolder + fileName);
                if (file == null) {
                    String response = "Error 404: File not found.";
                    errorHandler(exchange, response, 404);
                } else {
                    htmlHandler(exchange, file);
                }
            }catch (Exception e){
                String response = "Error 500: Internal Server Error.";
                errorHandler(exchange, response, 500);
            }
        }

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
    private class POSTFileHandler implements HttpHandler {
        public void handle(HttpExchange exchange){
            try{
                Headers header = exchange.getRequestHeaders();
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
                if (receivedInput.length() > limitSize) {
                    throw new Exception();
                }

                System.out.println(currentFolder);
                File file = new File(currentFolder + fileName);
                FileOutputStream f_output = new FileOutputStream(file);
                f_output.write(payload.getBytes("ISO-8859-1"));
                f_output.close();

                String fileSize = fileSize(payload.length());
                System.out.println("Client posted: " + fileName + ", size: " + fileSize);

                server.createContext(referer + fileName, new GETFileHandler(fileName));
                server.createContext(referer + file.getName() + "/deletefile", new DELETEFileHandler(fileName));
                htmlHandler(exchange, htmlSPA);
                }catch (Exception e){
                String response = "Error 413: Payload to Large";
                errorHandler(exchange, response, 413);
            }
        }
    }

private class DELETEFileHandler implements HttpHandler {
    private final String name;
    public DELETEFileHandler(String name) {
        this.name = name;
    }
    public void handle(HttpExchange exchange) {
        try {
            System.out.println("[CLIENT REQUESTED REMOVAL OF FILE: "+name+"]");
            Headers header = exchange.getRequestHeaders();
            String referer = header.getFirst("Referer");

            File file = new File (currentFolder+name);
            if(file.exists()){
                referer = getURLFromRequest(referer);
                server.removeContext(referer+file.getName());
                file.delete();
                htmlHandler(exchange, htmlSPA);
            }
            else{
                throw new Exception();
            }
        } catch (Exception e) {
            String response = "Error 404: File not found.";
            errorHandler(exchange, response, 404);
        }

    }
}
private class DELETEFolderHandler implements HttpHandler {
    private final String name;
    public DELETEFolderHandler(String name) {
        this.name = name;
    }
    public void handle(HttpExchange exchange){
        try {
            System.out.println("[CLIENT REQUESTED REMOVAL OF FOLDER: "+name+"]");
            Headers header = exchange.getRequestHeaders();
            String referer = header.getFirst("Referer");

            File dir = new File (currentFolder);
            if(dir.exists()){
                referer = getURLFromRequest(referer);
                server.removeContext(referer);
                deleteDir(dir);
                htmlHandler(exchange, htmlSPA);
            }
            else{
                throw new Exception();
            }
        } catch (Exception e) {
            String response = "Error 404: Folder not found.";
            errorHandler(exchange, response, 404);
        }

    }
}
private class ADDFolderHandler implements HttpHandler {
    private final String name;
    public ADDFolderHandler(String name) {
        this.name = name;
    }
    public void handle(HttpExchange exchange){
        System.out.println("[Client requested to add folder at: " + name + "]");
        Headers header = exchange.getRequestHeaders();
        File currentDir = new File(currentFolder);
        try {
            if (!currentDir.exists()) {
                throw new IOException();
            } else {
                String referer = header.getFirst("Referer");
                referer = getURLFromRequest(referer);
                String receivedInput = "";
                final byte[] buffer = new byte[6000];
                int count;
                while ((count = exchange.getRequestBody().read(buffer)) >= 0) {
                    receivedInput += new String(buffer, 0, count, "ISO-8859-1");
                }
                String parts[] = receivedInput.split("\r\n\r\n", 2);
                parts = parts[1].split("------", 2);
                String folderName = parts[0].substring(0, parts[0].length() - 2);
                File newDir = new File(currentFolder + "/" + folderName);
                if (!newDir.exists()) {
                    newDir.mkdir();
                    String tempURL = referer + folderName+"/";
                    server.createContext(tempURL, new SPAContextHandler(currentFolder, folderName+"/"));
                    server.createContext(tempURL + "download", new GETFolderHandler(folderName));
                    server.createContext(tempURL + "upload", new POSTFileHandler());
                    server.createContext(tempURL + "addfolder", new ADDFolderHandler(folderName));
                    server.createContext(tempURL + "deletefolder", new DELETEFolderHandler(folderName));
                    htmlHandler(exchange, htmlSPA);

                } else {
                    throw new Exception();
                }
            }

        }catch (IOException e) {
            String response = "Error 500: Internal Server Error";
            errorHandler(exchange, response, 500);
        } catch (Exception e) {
            String response = "Error 403: Forbidden, Folder already exists";
            errorHandler(exchange, response, 403);
        }
    }
}


}