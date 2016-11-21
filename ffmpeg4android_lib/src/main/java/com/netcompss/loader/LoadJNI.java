package com.netcompss.loader;

import java.io.File;

import com.netcompss.ffmpeg4android.CommandValidationException;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.ffmpeg4android.Prefs;

import android.content.Context;
import android.util.Log;

public final class LoadJNI {

	static {
		System.loadLibrary("loader-jni");
	}
	
	/**
	 * 
	 * @param args ffmpeg command
	 * @param workFolder working directory 
	 * @param ctx Android context
	 * @param isValidate apply validation to the command
	 * @throws CommandValidationException
	 */
	public void run(String[] args, String workFolder, Context ctx, boolean isValidate) throws CommandValidationException {
		Log.i(Prefs.TAG, "running ffmpeg4android_lib: " + Prefs.version);
		// delete previous log: this is essential for correct progress calculation
		String vkLogPath = workFolder + "vk.log";
		GeneralUtils.deleteFileUtil(vkLogPath);
		GeneralUtils.printCommand(args);
		
		//printInternalDirStructure(ctx);
		
		if (isValidate) {
			if (GeneralUtils.isValidCommand(args))
				load(args, workFolder, getVideokitLibPath(ctx), true);
			else
				throw new CommandValidationException();
		}
		else {
			load(args, workFolder, getVideokitLibPath(ctx), true);
		}
		
	}
	
	/**
	 * 
	 * @param args ffmpeg command
	 * @param workFolder working directory
	 * @param ctx Android context
	 * @throws CommandValidationException
	 */
	public void run(String[] args, String workFolder, Context ctx) throws CommandValidationException {
		run(args, workFolder, ctx, true);
	}
	
	
	private static void printInternalDirStructure(Context ctx) {
		Log.d(Prefs.TAG, "=printInternalDirStructure=");
		Log.d(Prefs.TAG, "==============================");
		File file = new File(ctx.getFilesDir().getParent());
		analyzeDir(file);
		Log.d(Prefs.TAG, "==============================");
	}
	
	private static void analyzeDir(File path) {
		if (path.isDirectory()) {
			Log.d(Prefs.TAG,"Scanning dir: " + path.getAbsolutePath());
			File[] files1 = path.listFiles();
			for (int i = 0; i < files1.length; i++) {
				analyzeDir(files1[i]);
			}
			Log.d(Prefs.TAG, "==========");
		}
		else {
			Log.d(Prefs.TAG, path.getAbsolutePath());
			
		}
	}
	
	private static String getVideokitLibPath(Context ctx) {
		
		//File file = new File(ctx.getFilesDir().getParent() + "/lib/");
		//analyzeDir(file);
		
		String videokitLibPath = ctx.getFilesDir().getParent()  + "/lib/libvideokit.so";
		
		File file = new File(videokitLibPath);
		if(file.exists())  {     
		  Log.i(Prefs.TAG, "videokitLibPath exits");
		}
		else {
			Log.w(Prefs.TAG, "videokitLibPath not exits: " + videokitLibPath);
			videokitLibPath = ctx.getFilesDir().getParent()  + "/lib/arm64/libvideokit.so";
			Log.i(Prefs.TAG, "trying videokitLibPath: " + videokitLibPath);
			file = new File(videokitLibPath);
			if(file.exists())  {
				Log.i(Prefs.TAG, "videokitLibPath exits: " + videokitLibPath);
			}
			else {
				Log.w(Prefs.TAG, "videokitLibPath not exits: " + videokitLibPath);
				videokitLibPath = "/data/app/com.examples.ffmpeg4android_demo-1/lib/arm64/libvideokit.so";
				Log.i(Prefs.TAG, "trying videokitLibPath: " + videokitLibPath);
				file = new File(videokitLibPath);
				if(file.exists())  {
					Log.i(Prefs.TAG, "videokitLibPath exits: " + videokitLibPath);
				}
				else {
					Log.w(Prefs.TAG, "videokitLibPath not exits: " + videokitLibPath);
					videokitLibPath = "/data/app/com.examples.ffmpeg4android_demo-2/lib/arm64/libvideokit.so";
					Log.i(Prefs.TAG, "trying videokitLibPath: " + videokitLibPath);
					if(file.exists())  {
						Log.i(Prefs.TAG, "videokitLibPath exits: " + videokitLibPath);
					}
					else {
						Log.e(Prefs.TAG, "can't find path of lib");
					}
				}
			}
		}
		
		
		
		
		
		//String videokitLibPath = ctx.getFilesDir().getParent()  + "/lib/arm64/libvideokit.so";
		
		// only this works on Android M, and the number changes (demo-2, demo-1)
		//String videokitLibPath = "/data/app/com.examples.ffmpeg4android_demo-1/lib/arm64/libvideokit.so";
		
		
		//Log.i(Prefs.TAG, "videokitLibPath: " + videokitLibPath);
		return videokitLibPath;
		
	}
	
	
	
	public void fExit( Context ctx) {
		fexit(getVideokitLibPath(ctx));
	}
	
	public native String fexit(String videokitLibPath);
	
	public native String unload();

	public native String load(String[] args, String videokitSdcardPath, String videokitLibPath, boolean isComplex);
}
