package net.madz.download.service.metadata;

public class Consts {

	/***
	 * For below resources length, they are measured in bytes.
	 */
	// For URL, ReferURL, Folder: 1000 characters, 1 character is 3 bytes.
	//
	public static final int URL_LENGTH = 1000 * 3;
	public static final int REFER_URL_LENGTH = 1000 * 3;
	public static final int FOLDER_LENGTH = 1000 * 3;
	public static final int FILE_NAME_LENGTH = 128 * 3;
	public static final int TOTAL_LENGTH = 8;
	public static final int SEGMENTS_NUMBER_LENGTH = 4;
	public static final int RESUMABLE_FLAG_LENGTH = 1;
	public static final int THREAD_NUMBER_LENGTH = 1;
	public static final int STATE_LENGTH = 1;
	
	

}
