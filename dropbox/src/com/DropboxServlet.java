package com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Permission;
import java.util.Iterator;
import java.util.Locale;
import java.util.Scanner;
import java.io.FilePermission;

import com.dropbox.core.*;

import javax.servlet.http.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@SuppressWarnings("serial")
@Controller
@RequestMapping("/")
public class DropboxServlet extends HttpServlet {
	

	@RequestMapping("dropbox1")
	public ModelAndView oauth(Model model,HttpServletRequest req, HttpServletResponse resp)
			throws IOException, DbxException {
		ModelAndView m=new ModelAndView("display");
		System.out.println("dropbox!!!");
		String code = req.getParameter("code");
	//	System.out.println(code);
		String client_id = "9kodgd99ux1xj9t";
		String client_secret = "c3zeqr4hea7u3y1";
		String redirect_uri = "http://localhost:8080/dropbox";
		if (code != null) {
			
			//token getting
			
			HttpURLConnection connection = (HttpURLConnection) new URL(
					"https://api.dropbox.com/1/oauth2/token?code=" + code
							+ "&client_id=" + client_id + "&client_secret="
							+ client_secret + "&redirect_uri=" + redirect_uri
							+ "&grant_type=authorization_code")
					.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			String line, outputString = "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			while ((line = reader.readLine()) != null) {
				outputString += line;
			}
		//	System.out.println(outputString);
			JsonObject json = (JsonObject) new JsonParser().parse(outputString);
			String access_token = json.get("access_token").getAsString();
		//	System.out.println("accesstoken:" + access_token);
		//	Token t=new Token();
		//	t.setToken(access_token);
			
			String token = "Bearer ";
			token += access_token;
			if (access_token != null) {

				//account info 
				
				URL url = new URL(
						"https://api.dropbox.com/1/account/info?locale=English");
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestProperty("Authorization", token);
				outputString = "";
				reader = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					outputString += line;
				}
				System.out.print(outputString);
				JsonObject json2 = (JsonObject) new JsonParser()
						.parse(outputString);
				String name = json2.get("display_name").getAsString();
		//		System.out.println("name" + name);
				reader.close();
				
				String path = "present.ppt";
				
		//meta data about the file
				
					URL url1 = new URL(
							"https://api.dropbox.com/1/metadata/auto/" + path);
					connection = (HttpURLConnection) url1.openConnection();
					connection.setRequestProperty("Authorization", token);
					connection.setRequestMethod("GET");
					connection.setRequestProperty("Content-Type", "application/pdf");
					String outputString1 = "";
					String line1;
					reader = new BufferedReader(new InputStreamReader(
							connection.getInputStream()));
					while ((line1 = reader.readLine()) != null) {
						outputString1 += line1;
					}
					System.out.println(connection.getResponseCode());
					JsonObject json1 = (JsonObject) new JsonParser()
							.parse(outputString1);
					String r = json1.get("rev").getAsString();
					System.out.print(outputString1);
					System.out.println(r);
					reader.close();

		//setting the preview for a file
					
			/*			url = new URL(
								"https://api-content.dropbox.com/1/previews/auto/"+path);
						connection = (HttpURLConnection) url.openConnection();
						connection.setRequestProperty("Authorization", token);
						connection.setRequestProperty("Content-Type", "application/pdf");
						connection.setRequestMethod("GET");
						outputString = "";
						reader = new BufferedReader(new InputStreamReader(
								connection.getInputStream()));
						while ((line = reader.readLine()) != null) {
							outputString += line;
						}
						System.out.print(outputString);
						reader.close();
//geting the file
						/*
						url = new URL(
								"https://api-content.dropbox.com/1/files/auto/"+path+"?rev="+r);
						connection = (HttpURLConnection) url.openConnection();
						connection.setRequestProperty("Authorization", token);
						connection.setRequestProperty("Content-Type", "application/vnd.ms-powerpoint");
						connection.setRequestMethod("GET");
						outputString = "";
						reader = new BufferedReader(new InputStreamReader(
								connection.getInputStream()));
						while ((line = reader.readLine()) != null) {
							outputString += line;
						}
						System.out.print(outputString);
						reader.close();
*/
					/*							
			      
			String Vpath="samson.txt";
					URL url3 = new URL(
							"https://api.dropbox.com/1/media/auto/"+Vpath);
					connection = (HttpURLConnection) url3.openConnection();
					connection.setRequestProperty("Authorization", token);
					connection.setRequestProperty("Content-Type", "text/plain");
					connection.addRequestProperty("enctype","multipart/form-data");
					connection.setRequestMethod("POST");
					outputString1 = "";
					reader = new BufferedReader(new InputStreamReader(
							connection.getInputStream()));
					while ((line = reader.readLine()) != null) {
						outputString1 += line;
					}
					System.out.print(outputString1);
					reader.close();
				*/
					
					url = new URL(
							"https://api.dropbox.com/1/search/auto/?query=.");
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestProperty("Authorization", token);
					connection.setRequestMethod("GET");
					outputString = "";
					reader = new BufferedReader(new InputStreamReader(
							connection.getInputStream()));
					while ((line = reader.readLine()) != null) {
						outputString += line;
					}
					System.out.print(outputString);
					
					JsonArray jsonA = (JsonArray) new JsonParser().parse(outputString);
					Iterator<JsonElement> iterator = jsonA.iterator();
				//	System.out.println("files in the dropbox");
					String fileName;
					String newFile="";
	                while (iterator.hasNext())
	                {
	                   	String a=iterator.next().toString();
	                	JsonObject json4 = (JsonObject) new JsonParser().parse(a);
	                	fileName= json4.get("path").getAsString();
	                	newFile+= fileName.replaceAll("/","");
	                	System.out.println(newFile);
	                }
	                				
					reader.close();
			//		model.addAttribute("FileNames", "newFile");
					m.addObject("FileNames",newFile);
			}
			else{
				System.out.println("access token is not generated");
			}
		
	}
		return m; 
}
}
