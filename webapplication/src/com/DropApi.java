package com;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxSessionStore;
import com.dropbox.core.DbxStandardSessionStore;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.DbxWebAuth.BadRequestException;
import com.dropbox.core.DbxWebAuth.BadStateException;
import com.dropbox.core.DbxWebAuth.CsrfException;
import com.dropbox.core.DbxWebAuth.NotApprovedException;
import com.dropbox.core.DbxWebAuth.ProviderException;
import com.dropbox.core.DbxWriteMode;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.gson.Gson;

@Controller
@RequestMapping("/")
public class DropApi {

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView firstPage() {
		ModelAndView model = new ModelAndView("drop");
		return model;
	}

	@RequestMapping(value = "login", method = RequestMethod.GET)
	public void auth(HttpServletRequest req, HttpServletResponse resp)

	throws IOException, ServletException {
		DbxWebAuth auth = getDbxWebAuth(req);
		String authorizePageUrl = auth.start();
		resp.sendRedirect(authorizePageUrl);
	}

	DbxWebAuth getDbxWebAuth(HttpServletRequest request) {

		HttpSession session = request.getSession(true);
		final String APP_KEY = "9kodgd99ux1xj9t";
		final String APP_SECRET = "c3zeqr4hea7u3y1";
		DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
		DbxRequestConfig config = new DbxRequestConfig("SamDrop/1.0", Locale
				.getDefault().toString(), AppengineHttpRequestor.Instance);
		String redirectUri = "http://localhost:8888/dropbox";
		DbxSessionStore csrfTokenStore = new DbxStandardSessionStore(session,
				"dropbox-key");
		return new DbxWebAuth(config, appInfo, redirectUri, csrfTokenStore);
	}

	DbxClient getClient(HttpSession session) {

		DbxRequestConfig config = new DbxRequestConfig("SamDrop/1.0", Locale
				.getDefault().toString(), AppengineHttpRequestor.Instance);
		String access = (String) session.getAttribute("accessToken");
		System.out.println(access+"getclient");
		return new DbxClient(config, access);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("dropbox")
	public void code(HttpServletRequest req, HttpSession session)
			throws BadRequestException, BadStateException, CsrfException,
			NotApprovedException, ProviderException, DbxException,
			MalformedURLException, IOException {

		DbxWebAuth auth = getDbxWebAuth(req);
		
		DbxAuthFinish authFinish = auth.finish(req.getParameterMap());
		session.setAttribute("accessToken", authFinish.accessToken);
		DbxClient client = getClient(session);
		String userName = client.getAccountInfo().displayName;
		System.out.println(userName);
//		return new ModelAndView("display", "name", userName);
	}

	@RequestMapping(value = "filesList")
	@ResponseBody
	public String fileList(HttpSession session) throws DbxException {
		DbxClient client = getClient(session);
		String folderPath = "/";
		DbxEntry.WithChildren listing = client
				.getMetadataWithChildren(folderPath);
		System.out.println("Files List:");
		ArrayList<String> ar = new ArrayList<String>();
		int i = 0;
		for (DbxEntry child : listing.children) {

			System.out.println("	" + child.name + ": " + child.toString());
			if (child.isFile()) {
				ar.add(i, child.name);
				i++;
			}
		}
		System.out.println(ar);
		Gson gson = new Gson();
		String json = gson.toJson(ar);
		return json.toString();
	}

	@RequestMapping("download")
	public void download(HttpSession session) throws DbxException, IOException, InterruptedException {
		DbxClient client = getClient(session);
		System.out.println("Linked account: "
				+ client.getAccountInfo().displayName);
		
	//	Socket socket = new Socket();
	//	int timeout=50000;
		
		DbxClient.Downloader downloader = client.startGetFile("/sharmi.mp4", null);

//		socket.getKeepAlive();
		InputStream in;
		UploadVideo m = new UploadVideo();
		System.out.println("downloading");
		try {
			System.out.println(downloader.metadata.name);
			long size = downloader.metadata.numBytes;
			System.out.println(size);
			in = downloader.body;
			
			m.buff(in, size);
		} finally {
			downloader.close();
		}

	}

	@RequestMapping(value = "metadata", method = RequestMethod.GET)
	@ResponseBody
	public String metadata(HttpSession session, String path)
			throws DbxException, FileNotFoundException, IOException {
		String path1 = "/";
		path1 += path;
		DbxClient client = getClient(session);
		String previewUrl = client.createShareableUrl(path1);
		System.out.println(previewUrl);

		return previewUrl;
	}
}
