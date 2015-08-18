
package test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import com.google.common.collect.Lists;

public class UploadVideo {

  private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static JsonFactory JSON_FACTORY = new JacksonFactory();
  private static YouTube youtube;
  private static String VIDEO_FILE_FORMAT = "video/*";
  private static String CLIENT_ID="134446106864-6vf2u560gtcnb45ccslofpui7ql9g51i.apps.googleusercontent.com";
  private static String CLIENT_SECRET="n3lMlB7NosSFZ7ESc3rGMfVI";
  private static  String CALLBACK_URL = "http://localhost:9000/Callback";
  private static Credential authorize(List<String> scopes) throws Exception {
	  
  GoogleAuthorizationCodeFlow authorizationFlow = new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(),new JacksonFactory(), CLIENT_ID,CLIENT_SECRET,Lists.newArrayList(scopes)) .setAccessType("offline").build();
  String authorizeUrl =authorizationFlow.newAuthorizationUrl().setRedirectUri(CALLBACK_URL).build();
  System.out.println("Paste this url in your browser: \n" + authorizeUrl + '\n');
  System.out.println("Type the code you received here: ");
  String authorizationCode = new BufferedReader(new InputStreamReader(System.in)).readLine();
  GoogleAuthorizationCodeTokenRequest tokenRequest = authorizationFlow.newTokenRequest(authorizationCode);
  tokenRequest.setRedirectUri(CALLBACK_URL);
  GoogleTokenResponse tokenResponse = tokenRequest.execute();
  return authorizationFlow.createAndStoreCredential(tokenResponse, CLIENT_ID); 
  
	  }
 
   
  public static void main(String[] args) 
  {

	    List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.upload");
	    try 
	    {	       
		Credential credential = authorize(scopes); 
	    youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("youtube-cmdline-uploadvideo-sample").build();
		File videoFile = getVideoFromUser();
	    System.out.println("You chose " + videoFile + " to upload.");
        Video videoObjectDefiningMetadata = new Video();
	    VideoStatus status = new VideoStatus();
	    status.setPrivacyStatus("publifc");
	    videoObjectDefiningMetadata.setStatus(status);
        VideoSnippet snippet = new VideoSnippet();
        Calendar cal = Calendar.getInstance();
	    snippet.setTitle("Mission- Impossible- Rogue Nation - Teaser Trailer - Tamil - Paramount Pictures India " + cal.getTime());
	    snippet.setDescription("Video uploaded via YouTube Data API V3 using the Java library " + "on " + cal.getTime());
	    List<String> tags = new ArrayList<String>();
	      tags.add("test");
	      tags.add("example");
	      tags.add("java");
	      tags.add("YouTube Data API V3");
	      tags.add("erase me");
	      snippet.setTags(tags);
	    videoObjectDefiningMetadata.setSnippet(snippet);
        InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT, new BufferedInputStream(new FileInputStream(videoFile)));
	    mediaContent.setLength(videoFile.length());
        Insert videoInsert = youtube.videos().insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);
        MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(false);
	    MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
	    	
	        public void progressChanged(MediaHttpUploader uploader) throws IOException {
	        	
	          switch (uploader.getUploadState()) {
	          
	            case INITIATION_STARTED:
	              System.out.println("Initiation Started");
	              break;
	            case INITIATION_COMPLETE:
	              System.out.println("Initiation Completed");
	              break;
	            case MEDIA_IN_PROGRESS:
	              System.out.println("Upload in progress");
	              System.out.println("Upload percentage: " + uploader.getProgress());
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
	    
	      System.out.println("\n================== Returned Video ==================\n");
	      System.out.println("  - Id: " + returnedVideo.getId());
	      System.out.println("  - Title: " + returnedVideo.getSnippet().getTitle());
	      System.out.println("  - Tags: " + returnedVideo.getSnippet().getTags());
	      System.out.println("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus());
	      System.out.println("  - Video Count: " + returnedVideo.getStatistics().getViewCount());
	      System.out.println("  - Details: " + returnedVideo.getLiveStreamingDetails());
	      System.out.println("kind"+ returnedVideo.getKind());
	    }
			
	   catch (GoogleJsonResponseException e)
	   {
	    System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "+ e.getDetails().getMessage());
	    e.printStackTrace();
	    }
	    catch (IOException e)
	    {
	     System.err.println("IOException: " + e.getMessage());
	     e.printStackTrace();
	     
	    } 
	    catch (Throwable t)
	    {
	      System.err.println("Throwable: " + t.getMessage());
	      t.printStackTrace();
	    }
}
	 
	  private static File getVideoFromUser() throws IOException {
	    File[] listOfVideoFiles = getLocalVideoFiles();
	    return getUserChoice(listOfVideoFiles);
	  }

	  
	  private static File[] getLocalVideoFiles() throws IOException {

	    File currentDirectory = new File(".");
	    System.out.println("Video files from " + currentDirectory.getAbsolutePath() + ":");  
	    FilenameFilter videoFilter = new FilenameFilter()
	    {
	    public boolean accept(File dir, String name)
	    {
	    String lowercaseName = name.toLowerCase();
	    if (lowercaseName.endsWith(".webm") || lowercaseName.endsWith(".flv")|| lowercaseName.endsWith(".f4v") || lowercaseName.endsWith(".mov") || lowercaseName.endsWith(".wmv")|| lowercaseName.endsWith(".mp4")) 
	    {
	          return true;
	        }  
	    else
	        {
	          return false;
	        }
	      }
	    };
	    return currentDirectory.listFiles(videoFilter);
	  }

	  private static File getUserChoice(File videoFiles[]) throws IOException {
	  if(videoFiles!=null) {
	  if (videoFiles.length < 1) {
	      throw new IllegalArgumentException("No video files in this directory.");
	    }
      for (int i = 0; i < videoFiles.length; i++) {
	      System.out.println(" " + i + " = " + videoFiles[i].getName());
	    }
		  }
	    BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
	    String inputChoice;
	    do {
	      System.out.print("Choose the number of the video file you want to upload: ");
	      inputChoice = bReader.readLine();
	    } while (!isValidIntegerSelection(inputChoice, videoFiles.length));

	    return videoFiles[Integer.parseInt(inputChoice)];
	  }

	  public static boolean isValidIntegerSelection(String input, int max) {
	    if (input.length() > 9) return false;

	    boolean validNumber = false;
	    
	    Pattern intsOnly = Pattern.compile("^\\d{1,9}$");
	    Matcher makeMatch = intsOnly.matcher(input);

	    if (makeMatch.find()) {
	      int number = Integer.parseInt(makeMatch.group());
	      if ((number >= 0) && (number < max)) {
	        validNumber = true;
	      }
	    }
	    return validNumber;
	  }
	}