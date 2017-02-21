package assign2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class WebServer implements Runnable
{
	public static final String 		SERVER_PATH 		= "/webserver";
	public static final int 		MYPORT				= 4950;
	public static final int 		BUFSIZE 			= 1500;
	public static final int 		MAX_REQUEST_SIZE 	= 1024*1024*10;	//Maximum 10 MB

	//Valid formats to upload:
	private static final String[] 	VALID_FORMATS = 
		{
			"jpg", "jpeg", "png", "gif", "gifv",
			"mp3", "mp4", "avi", 
			"html", "txt", "docx", "pdf"
		};
	
	//Thread members:
	private Socket clientSocket;
	private static String clientID;


	public static void main(String[] args){

		try
		{
			/* Create socket */
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(MYPORT);
			System.out.println("Listening for connection on port: " + MYPORT);


			while(true)
			{
				/* Create thread for client */
				WebServer clientThread = new WebServer(serverSocket.accept());
				Thread thread = new Thread(clientThread);
				thread.setDaemon(true);
				thread.start();
			}


		}catch(IOException e){
			System.err.println("ERROR: " + e.getMessage());
		}

	}

	protected WebServer(Socket client)
	{
		clientSocket = client;
		clientID = client.getInetAddress().getHostName();

		System.out.println("[" + clientID + "] Connected on remote port: " + client.getPort());
	}

	@Override
	public void run() 
	{

		try 
		{
			
			// Entire request:
			String receivedInput = "";
			
			// Request separated into header + content:
			String header = "";
			String content = "";
			
			// Request header parts:
			String requestType = "";
			String pagePath = "";
			
			//Attach I/O streams to socket:
			InputStream in = clientSocket.getInputStream();
			OutputStream out = clientSocket.getOutputStream();
			
			while(true)
			{
				int contentLength = 0;
				boolean validInput = true;
				byte[] buffer = new byte[BUFSIZE];
				
				// Read input from socket:
				int bytesRead = in.read(buffer);
				if(bytesRead == -1)
					continue;	// No input received, jump to next iteration.
				
				//Convert the received data to string:
				receivedInput = new String(buffer, 0, bytesRead, "ISO-8859-1");

				// Split package into header and content:
				String parts[] = receivedInput.split("\r\n\r\n", 2);
				
				header = parts[0];
				if(parts.length>=2)
					content = parts[1];

				//Split header:
				String headerParts[] = header.split(" ", 3);
				if(headerParts.length>=2)
				{
					requestType = headerParts[0].toUpperCase(); 
					pagePath = headerParts[1];
				}else
				{
					validInput = false;
				}
				
				if (validInput == true && !header.isEmpty() && (requestType.equals("POST") || requestType.equals("POST")))
				{
					int receivedContent = content.length();
					
					try
					{
						// Content length should be included in header:
						contentLength = Integer.parseInt(searchString("Content-Length: ", header));
					
						// Check if client request is to large:
						if(contentLength<MAX_REQUEST_SIZE)
						{
						
							//Read remaining data:
							while(receivedContent < contentLength)
							{
								bytesRead = in.read(buffer);
								if (bytesRead!=-1)
								{
									receivedContent += bytesRead;
									content += new String(buffer, 0, bytesRead, "ISO-8859-1");
								}else
								{
									System.err.println("["+clientID+"] Client has aborted the transmission.");
									break;
								}
							}
							
						}
						else
						{
							// Client request is to large:
							System.out.println("[" + clientID + "] Request to large.");
							byte[] page = HTTPStatusCode.createPage(HTTPStatusCode.s413);
							out.write(page);
							out.flush();
	
							in.skip(contentLength-receivedContent);
	
							validInput=false;
						}
					
					}catch(NumberFormatException e)
					{
						System.out.println("["+clientID+"] Expected content length.");
						
						//Send 411 Length Required:
						byte[] page = HTTPStatusCode.createPage(HTTPStatusCode.s411);
						out.write(page);
						out.flush();
						
						validInput = false;
					}
				}
				
				// Disregard remaining data:
				in.skip(in.available());
				
				if(!receivedInput.isEmpty() && validInput)
				{
					
					byte[] replyPage;
					
					switch(requestType)
					{
						case "GET":
							replyPage = getRequest(pagePath);
							break;
							
						case "POST":
							replyPage = postRequest(pagePath, header, content);
							break;
							
						case "PUT":
							replyPage = putRequest(pagePath, content);
							break;
							
						default:
							replyPage = HTTPStatusCode.createPage(HTTPStatusCode.s400);
							break;
					}
					
					out.write(replyPage);
					out.flush();
					
				}else
				{
					// Send 400 Bad Request:
					byte[] page = HTTPStatusCode.createPage(HTTPStatusCode.s400);
					out.write(page);
					out.flush();
					
					System.out.println("["+clientID+"] Request dismissed");
				}
			}
		} catch (Exception e) {
			System.err.println("["+clientID+"] Client disconnected!");
		}

	}
	private static String trimURL(String message){
		boolean slash = false;
		for(int i=0; i<message.length(); i++)
		{
			if(message.charAt(i) == '/')
			{
				if (slash) {
					message = message.substring(0, i-1) + message.substring(i);
					i--;
				}
				slash = true;
			}else
				slash = false;

		}
		return message.toLowerCase();
	}
	private static byte[] getRequest(String message)
	{
		try
		{
			message = trimURL(message);

			Path path = Paths.get(message);

			String urlParts[] = message.split("/");


			//Attempt to fix URL:
			if (urlParts.length==0)
			{
				path = Paths.get(SERVER_PATH + "/index.html");	
			}else
			{
				String rootPath = new String();
				if(urlParts.length>=2)
					rootPath = urlParts[1];

				String fileName = urlParts[urlParts.length-1];

				if(rootPath.equals("admin"))
				{
					return HTTPStatusCode.createPage(HTTPStatusCode.s403);
				}

				if (!fileName.contains("."))
				{
					//Common URL endings:
					String endings[] = {".htm", ".html", ".png", "/index.htm", "/index.html", "/index.png"};

					//Try common URL endings:
					for(int i=0; i<endings.length; i++)
					{
						path = Paths.get(SERVER_PATH + message + endings[i]);
						if(Files.exists(path))
							break;
					}
				}else
					path = Paths.get(SERVER_PATH + message);
			}

			System.out.println("["+clientID+"] Requested " + path);
			// Header + data:
			byte[] fileData = Files.readAllBytes(path);
			byte[] headerData = HTTPStatusCode.getHeader(HTTPStatusCode.s200, fileData.length);

			// Combined array:
			byte[] combined = new byte[headerData.length + fileData.length];
			System.arraycopy(headerData, 0,  combined, 0, headerData.length);
			System.arraycopy(fileData, 0, combined, headerData.length, fileData.length);

			// Return combined page:
			return combined;
		} 
		catch (IOException e) 
		{
			System.out.println("["+clientID+"] 404: cause " + e.getLocalizedMessage());
			return HTTPStatusCode.createPage(HTTPStatusCode.s404);
		}
		catch (Exception e) 
		{
			System.err.println("["+clientID+"] 500: Could not process url");
			return HTTPStatusCode.createPage(HTTPStatusCode.s500);
		}
	}
	private byte[] putRequest(String path, String content)
	{
		try 
		{
			path = trimURL(path);
			String urlParts[] = path.split("/");
			if(urlParts.length>=2)
				if(urlParts[1].equals("admin"))
					return HTTPStatusCode.createPage(HTTPStatusCode.s403);

			//Upload file:
			FileOutputStream out = new FileOutputStream(SERVER_PATH+path);
			byte[] fileData = content.getBytes("ISO-8859-1");

			out.write(fileData);
			out.flush();
			out.close();

			byte[] page = HTTPStatusCode.createPage(HTTPStatusCode.s200, "Upload file",
					"<a href=\"" + path + "\"> Your file was successfully uploaded! </url><p>");

			return page;

		} catch (IOException e) {
			return HTTPStatusCode.createPage(HTTPStatusCode.s404);
		} catch (Exception e)
		{
			return HTTPStatusCode.createPage(HTTPStatusCode.s500);
		}
	}
	private byte[] postRequest(String path, String header, String content)
	{
		try 
		{
			// Split content "header" from file content: (contains file information)
			String contentParts[] = content.split("\r\n\r\n", 2);

			// Retrieve file end delimiter from header:
			String delimiter = searchString("boundary=", header);

			// Retrieve file format from content "header"
			String fileName = searchString("filename=", contentParts[0]);

			// Remove fnuffs surrounding filename:
			fileName = fileName.replaceAll("\"", "");

			// If no filename assume package is empty:
			if (fileName.isEmpty())
			{
				System.out.println("No content");
				return HTTPStatusCode.createPage(HTTPStatusCode.s204);
			}

			// Separate suffix from filename:
			String fileParts[] = fileName.split("\\.");

			/* Check if valid file format: */
			String fileSuffix = fileParts[fileParts.length-1].toLowerCase();
			if (!Arrays.asList(VALID_FORMATS).contains(fileSuffix))
			{
				System.err.println("["+clientID+"] 415 Unsupported Media Type");

				return HTTPStatusCode.createPage(HTTPStatusCode.s415);
			}

			String fileData = new String();

			int dataEnd = contentParts[1].indexOf(delimiter);

			if(dataEnd!=-1)
				fileData = contentParts[1].substring(0, dataEnd-2);
			else
				fileData = contentParts[1].substring(0, contentParts[1].length());

			if (fileData.length()==0) {
				return HTTPStatusCode.createPage(HTTPStatusCode.s204);
			}
			FileOutputStream out = new FileOutputStream(SERVER_PATH+"/files/"+fileName);
			out.write(fileData.getBytes("ISO-8859-1"));
			out.flush();
			out.close();

			byte[] page = HTTPStatusCode.createPage(HTTPStatusCode.s200, "Upload file",
					"<a href=\"/files/" + fileName + "\"> Your file was successfully uploaded! </a><p>" +
					"<a href=/upload.html> Upload new file </a>");

			return page;

		} catch (IOException e) {
			return HTTPStatusCode.createPage(HTTPStatusCode.s500);

		} catch (Exception e)
		{
			return HTTPStatusCode.createPage(HTTPStatusCode.s500);
		}
	}
	
	/**
	 * searchString
	 * <p>
	 * Searches for a variable in a string
	 * @param var - the variable to search for
	 * @param data - the string containing the variable
	 * @return - variable data
	 */
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
}
