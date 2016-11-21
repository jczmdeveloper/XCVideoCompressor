package com.netcompss.ffmpeg4android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

//import com.examples.ffmpeg4android_demo_native.R;


public class GeneralUtils {
	
	public static String[] fixComplexCommand(String[] complexCommand) {
		for (int i = 0; i < complexCommand.length; i++) {
			if (complexCommand[i].startsWith("\"") || complexCommand[i].startsWith(" ")) {
				StringBuffer sb = new StringBuffer();
				sb.append(complexCommand[i]);
				
				while (sb.charAt(0) == ' ' || sb.charAt(0) == '"' ) {
					sb.deleteCharAt(0);
				}
				while (sb.charAt(sb.length() - 1) == ' ' || sb.charAt(sb.length() - 1) == '"') {
					sb.deleteCharAt(sb.length() - 1);
				}
				
				complexCommand[i] =  sb.toString();
				Log.d(Prefs.TAG, "command " + i + ": " + sb.toString());
			}
		}
		return complexCommand;
	}
	
	public static void printCommand(String[] command) {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (int i = 0; i < command.length; i++ ) {
			sb.append("\"");
			sb.append(command[i]);
			sb.append("\"");
			if (i < command.length -1)
			  sb.append(",");
		}
		sb.append("}");
		Log.d(Prefs.TAG, sb.toString());
	}
	
	
	// {"ffmpeg", "-y" ,"-i", "/sdcard/videokit/sample.mp4",
	//  "-strict","experimental","-s", "160x120","-r","25", "-vcodec", "mpeg4",
	//  "-b", "150k", "-ab","48000", "-ac", "2", "-ar", "22050", "/sdcard/videokit/out.mp4"};
	public static boolean isValidCommand(String[] args) {
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("libx264") || args[i].equals("-preset")) {
				Log.w(Prefs.TAG, "Command validation detected libx264 use");
				Log.w(Prefs.TAG, "Make sure you use the extra libs that support libx264");
				Log.w(Prefs.TAG, "Note that you will have to target api 22 or below in your Manifest when using libx264");
				break;
			}
		}
		
		// find the input files path
		ArrayList<Integer> inputFilesArrLocation = new ArrayList<Integer>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-i"))
				inputFilesArrLocation.add(i+1);
		}
		
		// verify the input files exists
		boolean okFlag = true;
		for (int i = 0; i < inputFilesArrLocation.size(); i++) {
			String path = args[inputFilesArrLocation.get(i)];
			if (!(okFlag = checkIfFileExistAndNotEmpty(path))) {
				Log.e(Prefs.TAG, "Command validation failed.");
				Log.e(Prefs.TAG, "Check if input file exists: " + path);
				return okFlag;
			}
		}
		
		
		String outputPath = args[args.length -1];
		if (isStream(outputPath)){
			Log.i(Prefs.TAG, "output is a stream");
			return okFlag;
		}
		
		// verify output folder exists
		int indexOfLastSlash = outputPath.lastIndexOf("/");
		if (indexOfLastSlash == -1) {
			Log.e(Prefs.TAG, "Command validation failed.");
			Log.e(Prefs.TAG, "No slashes in output path looks like the : " + outputPath + " is not valid.");
			return false;
		}
		String outputFolder = outputPath.substring(0, indexOfLastSlash);
		if (!(okFlag = checkIfFolderExists(outputFolder))) {
			Log.e(Prefs.TAG, "Command validation failed.");
			Log.e(Prefs.TAG, "Check if output folder exists: " + outputFolder);
		}
		
		return okFlag;
	}
	
	public static boolean isStream(String filePath) {
		// handling streaming case e.g: udp://
		if (filePath.startsWith("udp://")) {
			Log.i(Prefs.TAG,"mached stream");
			return true;
		}
		else return false;
	}
	
	@TargetApi(23) 
	public static void checkForPermissionsMAndAbove(Activity act, boolean isBlocking) {
		Log.i(Prefs.TAG, "checkForPermissions() called");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// Here, thisActivity is the current activity
			if (act.checkSelfPermission(
					Manifest.permission.WRITE_EXTERNAL_STORAGE)
					!= PackageManager.PERMISSION_GRANTED) {


				// No explanation needed, we can request the permission.
				act.requestPermissions(
						new String[]{
								Manifest.permission.WRITE_EXTERNAL_STORAGE
						},
						0);
				if (isBlocking) {
					while (true) {
						if (act.checkSelfPermission(
								Manifest.permission.WRITE_EXTERNAL_STORAGE)
								== PackageManager.PERMISSION_GRANTED) {

							Log.i(Prefs.TAG, "Got permissions, exiting block loop");
							break;
						}
						Log.i(Prefs.TAG, "Sleeping, waiting for permissions");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

			}
			// permission already granted
			else {
				Log.i(Prefs.TAG, "permission already granted");
			}
		}
		else {
			Log.i(Prefs.TAG, "Below M, permissions not via code");
		}

    }
    
	
	public static long getVKLogSizeRandomAccess(String vkLogpath) {
		RandomAccessFile f = null;
		long ret = -1;
		try {
			f = new RandomAccessFile(vkLogpath, "r");
			ret = f.length();
			f.close();
		} catch (FileNotFoundException e) {
			Log.i(Prefs.TAG,"waiting for file to be created: " + e.getMessage());

		} catch (IOException e) {
			Log.i(Prefs.TAG,"waiting for file to be created: " + e.getMessage());
		}
		
		return ret;
	}
	
	@SuppressLint("DefaultLocale") 
	public static boolean isValidPicExtension(String fileName) {
		int endIndex = fileName.lastIndexOf(".");
		String ext = fileName.substring(endIndex + 1);
		ext = ext.toLowerCase();
		if (    (ext.toLowerCase().equals("jpg") ) ||
				(ext.toLowerCase().equals("bmp") ) ||
				(ext.toLowerCase().equals("png") ) ||
				(ext.toLowerCase().equals("jpeg") ) 
				) {
			return true;
		}
		else {
			return false;
		}

	}

	public static boolean checkIfFileExistAndNotEmpty(String fullFileName) {
		// the case of input images with %d: /sdcard/videokit/pic%03d.jpg
		if (fullFileName.contains("%") && isValidPicExtension(fullFileName)) {
			Log.i(Prefs.TAG,"mached picture array input");
			return true;
		}
	
		if (isStream(fullFileName)) return true;
	
		///////////////////////////////////
		File f = new File(fullFileName);
		long lengthInBytes = f.length();
		Log.d(Prefs.TAG, fullFileName + " length in bytes: " + lengthInBytes);
		if (lengthInBytes > 100)
			return true;
		else {
			return false;
		}

	}
	
	public static boolean deleteFile(String fullFileName) {
		File f = new File(fullFileName);
		boolean isdeleted = f.delete();
		return isdeleted;
	}

	public static boolean deleteFileUtil(String fullFileName) {
		File f = new File(fullFileName);
		boolean isdeleted = f.delete();
		return isdeleted;
		//Log.d(Prefs.TAG, "deleteing: " + fullFileName + " isdeleted: " + isdeleted);
	}

	public static String getReturnCodeFromLog(String filePath)  {
		String status = "Transcoding Status: Unknown";

		RandomAccessFile f = null;
		try {
			f = new RandomAccessFile(filePath, "r");
			long endLocation  = f.length();
			long seekLocation = -1;
			if (( seekLocation = endLocation - 100) < 0) {
				seekLocation = 0;
			}
			f.seek(seekLocation);

			String line;
			while ((line = f.readLine()) != null){ 
				if (line.startsWith("ffmpeg4android: 0")) {
					status = "Transcoding Status: Finished OK";
					break;
				}
				else if (line.startsWith("ffmpeg4android: 1") ) {
					status = "Transcoding Status: Failed";
					break;
				}
				
				else if (line.startsWith("ffmpeg4android: 2") ) {
					status = "Transcoding Status: Stopped";
					break;
				}

			}
			f.close();
		} catch (Exception e) {
			Log.e(Prefs.TAG, e.getMessage());
		}

		return status;
	}
	
	public static String[] utilConvertToComplex(String str) {
		 String[] complex = str.split(" ");
		 return complex;
	 }
	
	public static String getDutationFromVCLogRandomAccess(String vkLogpath) {
		String duration = null;
		try {
			RandomAccessFile f = new RandomAccessFile(vkLogpath, "r");
			String line;
			//f.seek(0);
			
			while ((line = f.readLine()) != null){ 
				//Log.d(Prefs.TAG, line);
				// Duration: 00:00:04.20, start: 0.000000, bitrate: 1601 kb/s 
				int i1 = line.indexOf("Duration:");
				int i2 = line.indexOf(", start");
				if (i1 != -1 && i2 != -1) {
					duration = line.substring(i1 + 10, i2 );
					break;
				}
			}
			f.close();
		} catch (FileNotFoundException e) {
			Log.i(Prefs.TAG,"waiting for file to be created: " + e.getMessage());

		} catch (IOException e) {
			Log.i(Prefs.TAG,"waiting for file to be created: " + e.getMessage());
		}
		return duration;
	}
	
	public static String readLastTimeFromVKLogUsingRandomAccess(String vkLogPath) {
		String timeStr = "00:00:00.00";
		try {
			// TODO elih 26.9.2013 changed to vk from ffmpeg log
			RandomAccessFile f = null;
			f = new RandomAccessFile(vkLogPath, "r");
			String line;
			long endLocation  = f.length();
			long seekLocation = -1;
			if (( seekLocation = endLocation - 100) < 0) {
				seekLocation = 0;
			}
			f.seek(seekLocation);
			//Log.d(Prefs.TAG, "Starting while loop seekLocation: " + seekLocation);

			while ((line = f.readLine()) != null){ 
				Log.i("line", line);
				int i1 = line.indexOf("time=");
				int i2 = line.indexOf("bitrate=");
				if (i1 != -1 && i2 != -1) {
					timeStr = line.substring(i1 + 5, i2 - 1);
				}
				else if (line.startsWith("ffmpeg4android: 0")) {
					timeStr = "exit";
				}
				else if (line.startsWith("ffmpeg4android: 1") ) {
						Log.w(Prefs.TAG, "error line: " + line);
						Log.w(Prefs.TAG, "Looks like error in the log");
						timeStr = "error";
				}
			}
			f.close();
		} catch (FileNotFoundException e) {
			Log.i(Prefs.TAG,"waiting for file to be created: " + e.getMessage());

		} catch (IOException e) {
			Log.i(Prefs.TAG,"waiting for file to be created: " + e.getMessage());
		}
		return timeStr.trim();
		
	}
	
	public static void copyLicenseFromAssetsToSDIfNeeded(Context context, String destinationFolderPath) {
		InputStream is = null;
		BufferedOutputStream o = null;
		boolean copyLic = true;
		File destLic = null;
		try {
			is = context.getAssets().open("ffmpeglicense.lic");
		} catch (Exception e) {
			Log.i(Prefs.TAG, "License file does not exist in the assets.");
			copyLic = false;
		}

		if (copyLic) {
			File dir = new File(destinationFolderPath);
			if((!dir.exists()) || (!dir.isDirectory())){
				dir.mkdir();
			}
			destLic = new File(destinationFolderPath + "ffmpeglicense.lic");
			Log.i(Prefs.TAG, "Adding lic file at " + destLic.getAbsolutePath());

			o = null;
			try {
				byte[] buff = new byte[10000];
				int read = -1;
				o = new BufferedOutputStream(new FileOutputStream(destLic), 10000);
				while ((read = is.read(buff)) > -1) { 
					o.write(buff, 0, read);
				}
				Log.i(Prefs.TAG, "Copy " + destLic.getAbsolutePath() + " from assets to SDCARD finished succesfully");
			}
			catch (Exception e) {
				Log.e(Prefs.TAG, "Error when coping license file from assets to working folder: " + e.getMessage());
			}
			finally {
				try {
					is.close();
					if (o != null) o.close();
				} catch (IOException e) {
					Log.w(Prefs.TAG, "Error when closing license file io: " + e.getMessage());
				}  

			}

		}
		else {
			Log.i(Prefs.TAG, "Not coping license");
		}
	
	}
	
	public static String getValidFileNameFromPath(String path) {
		int startIndex = path.lastIndexOf("/") + 1;
		int endIndex = path.lastIndexOf(".");
		
		String name = path.substring(startIndex, endIndex);
		String ext = path.substring(endIndex + 1);
		Log.d(Prefs.TAG, "name: " + name + " ext: " + ext);
		String validName = (name.replaceAll("\\Q.\\E", "_")).replaceAll(" ", "_");
		return validName + "." + ext;
	}
	
	public static String copyFileToFolder(String filePath, String folderPath) {
		//Log.i(Prefs.TAG, "Coping file: " + filePath + " to: " + folderPath);
		String validFilePathStr = filePath;
		try {
			FileInputStream is = new FileInputStream(filePath); 
			BufferedOutputStream o = null;
			String validFileName = getValidFileNameFromPath(filePath);
			validFilePathStr = folderPath + validFileName;
			File destFile = new File(validFilePathStr);
			try {
				byte[] buff = new byte[10000];
				int read = -1;
				o = new BufferedOutputStream(new FileOutputStream(destFile), 10000);
				while ((read = is.read(buff)) > -1) { 
					o.write(buff, 0, read);
				}
			} finally {
				is.close();
				if (o != null) o.close();  

			}
		} catch (FileNotFoundException e) {
			Log.w(Prefs.TAG, e.getMessage());
		} catch (IOException e) {
			Log.w(Prefs.TAG, e.getMessage());
		}
		return validFilePathStr;
	}
	
	public static boolean checkIfFolderExists(String fullFileName) {
		File f = new File(fullFileName);
		//Log.d(Prefs.TAG,"Checking if : " +  fullFileName + " exists" );
		if (f.exists() && f.isDirectory()) {
			//Log.d(Prefs.TAG,"Direcory: " +  fullFileName + " exists" );
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean createFolder(String folderPath) {
		File f = new File(folderPath);
		return f.mkdirs();
	}
	
	public static void copyDemoVideoFromAssetsToSDIfNeeded(Context context, String destinationFolderPath) {
		File destVid = null;
		try {
			if (!GeneralUtils.checkIfFolderExists(destinationFolderPath)) {

				boolean isFolderCreated = GeneralUtils.createFolder(destinationFolderPath);
				Log.i(Prefs.TAG, destinationFolderPath + " created? " + isFolderCreated);
				if (isFolderCreated) {
					destVid = new File(destinationFolderPath + "in.mp4");
					Log.i(Prefs.TAG, "Adding vid file at " + destVid.getAbsolutePath());
					InputStream is = context.getAssets().open("in.mp4");
					BufferedOutputStream o = null;
					try {
						byte[] buff = new byte[10000];
						int read = -1;
						o = new BufferedOutputStream(new FileOutputStream(destVid), 10000);
						while ((read = is.read(buff)) > -1) { 
							o.write(buff, 0, read);
						}
						Log.i(Prefs.TAG, "Copy " + destVid.getAbsolutePath() + " from assets to SDCARD finished succesfully");
					} 
					catch (Exception e) {
						Log.w(Prefs.TAG, "Failed copying: " + destVid.getAbsolutePath());
					}
					finally {
						is.close();
						if (o != null) o.close(); 

					}

				}
				else {
					Log.w(Prefs.TAG, "Demo videos folder was not created.");
				}

			}
			else {
				Log.d(Prefs.TAG, "demo videos directory exists, not copying demo video)");

			}

		} catch (FileNotFoundException e) {
			Log.e(Prefs.TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(Prefs.TAG, e.getMessage());
		}
	}
	
	public static String getVersionName(Context ctx) {
		/*
		String versionName = "";
		try {
			versionName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.w(Prefs.TAG, "No version code found, returning -1");
		}
		
		return versionName;
		*/
		return Prefs.version;
	}
	
	
	
	public static int isLicenseValid(Context ctx, String workingFolder) {
		 LicenseCheckJNI lm = new LicenseCheckJNI();
		  int rc = lm.licenseCheck(workingFolder, ctx);
		  if (rc >= 0) {
			  if (rc == 1) {
				  //Toast.makeText(ctx, "Permanent license.", Toast.LENGTH_SHORT).show();
			  }
			  else if (rc == 2 || rc == 0) {
				  //Toast.makeText(ctx, "Trial license.", Toast.LENGTH_SHORT).show();
			  }
		  }
		  else if (rc < 0) {
			  if (rc == -1)
				  Toast.makeText(ctx, "Trail Expired. contact support.", Toast.LENGTH_LONG).show();
			  else if (rc == -2) 
				  Toast.makeText(ctx, "License invalid contact support", Toast.LENGTH_LONG).show();
			  else 
				  Toast.makeText(ctx, "License check failed. contact support." + rc, Toast.LENGTH_LONG).show();
		  }
		  return rc;
	}


}
