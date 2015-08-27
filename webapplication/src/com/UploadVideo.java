package com;

import java.io.BufferedInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Videos.Insert;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.collect.Lists;

@Controller
@RequestMapping("/")
public class UploadVideo {

	private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static JsonFactory JSON_FACTORY = new JacksonFactory();
	private static YouTube youtube;
	private static String VIDEO_FILE_FORMAT = "video/*";
	private static String CLIENT_ID = "344471254901-13o30uel7g7rk1rh596tqs4m30rpuol2.apps.googleusercontent.com";
	private static String CLIENT_SECRET = "4ZUy4hpphMNoy99lhw0epXCA";
	private static String CALLBACK_URL = "http://localhost:8888/youtube";
//
//	@RequestMapping("a")
//	public void k() throws IOException{
//		URL url=new URL("www.google.com");
//		HttpsURLConnection a=(HttpsURLConnection) url.openConnection();
//		System.out.println(a.getDate());
//		
//	}
	@RequestMapping("you")
	public void authorize(HttpServletRequest request,

	HttpServletResponse response) throws IOException {

		GoogleAuthorizationCodeFlow authorizationFlow = getAuthFlow();
		String authorizeUrl = authorizationFlow.newAuthorizationUrl()
				.setRedirectUri(CALLBACK_URL).build();
		if (request.getParameter("code") == null) {
			System.out.println("code is null");
			response.sendRedirect(authorizeUrl);
		} else {
			System.out.println("code is there");
		}
	}

	@RequestMapping("youtube")
	public static void a(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		if (request.getParameter("code") == null) {
			System.out.println("code is null");
			// response.sendRedirect(authorizeUrl);
		} else {
			System.out.println("code is there");
			String authorizationCode = request.getParameter("code");
			GoogleAuthorizationCodeFlow authorizationFlow = getAuthFlow();
			GoogleAuthorizationCodeTokenRequest tokenRequest = authorizationFlow
					.newTokenRequest(authorizationCode);
			tokenRequest.setRedirectUri(CALLBACK_URL);
			GoogleTokenResponse tokenResponse = tokenRequest.execute();
			Credential c = authorizationFlow.createAndStoreCredential(
					tokenResponse, CLIENT_ID);
			System.out.println(c.getAccessToken());
			youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, c)
					.setApplicationName("youtube-uploadvideo").build();
		}
	}

	private static GoogleAuthorizationCodeFlow getAuthFlow() {

		List<String> scopes = Lists
				.newArrayList("https://www.googleapis.com/auth/youtube.upload");

		return new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(),
				new JacksonFactory(), CLIENT_ID, CLIENT_SECRET,
				Lists.newArrayList(scopes)).setAccessType("offline").build();
	}

	public void buff(InputStream b, long size) throws IOException {

		Video videoObjectDefiningMetadata = new Video();
		VideoStatus status = new VideoStatus();
		status.setPrivacyStatus("public");
		videoObjectDefiningMetadata.setStatus(status);
		VideoSnippet snippet = new VideoSnippet();
		Calendar cal = Calendar.getInstance();
		snippet.setTitle("sharmi video  upload " + cal.getTime());
		snippet.setDescription("sfsfs-trailer upload " + "on "
				+ cal.getTime());
		List<String> tags = new ArrayList<String>();
		tags.add("java");
		tags.add("j2ee");
		snippet.setTags(tags);
		videoObjectDefiningMetadata.setSnippet(snippet);
		InputStreamContent mediaContent = new InputStreamContent(
				VIDEO_FILE_FORMAT, b);
		mediaContent.setLength(size);
		Insert videoInsert = youtube.videos().insert(
				"snippet,statistics,status", videoObjectDefiningMetadata,
				mediaContent);
		MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();

		uploader.setDirectUploadEnabled(false);

		MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {

			public void progressChanged(MediaHttpUploader uploader)
					throws IOException {

				switch (uploader.getUploadState()) {

				case INITIATION_STARTED:
					System.out.println("Initiation Started");
					break;
				case INITIATION_COMPLETE:
					System.out.println("Initiation Completed");
					break;
				case MEDIA_IN_PROGRESS:
					System.out.println("Upload in progress");
					System.out.println("Upload percentage: "
							+ uploader.getProgress());
					break;
				case MEDIA_COMPLETE:
					System.out.println("Upload Completed!");
					break;
				case NOT_STARTED:
					System.out.println("Upload Not Started!");
					break;

				}
			}
		};

		uploader.setProgressListener(progressListener);
		Video returnedVideo = videoInsert.execute();

		System.out
				.println("\n================== Returned Video ==================\n");
		System.out.println("  - Id: " + returnedVideo.getId());
		System.out.println("  - Title: "
				+ returnedVideo.getSnippet().getTitle());
		System.out.println("  - Tags: " + returnedVideo.getSnippet().getTags());
		System.out.println("  - Privacy Status: "
				+ returnedVideo.getStatus().getPrivacyStatus());
		System.out.println("  - Video Count: "
				+ returnedVideo.getStatistics().getViewCount());
		System.out.println("  - Details: "
				+ returnedVideo.getLiveStreamingDetails());
		System.out.println("kind" + returnedVideo.getKind());
		
	}
}