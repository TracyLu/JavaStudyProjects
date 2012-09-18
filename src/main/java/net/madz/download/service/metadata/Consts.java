package net.madz.download.service.metadata;

public class Consts {

	public static final int URL_LENGTH = 1000 * 3;
	public static final int REFER_URL_LENGTH = 1000 * 3;
	public static final int FOLDER_LENGTH = 1000 * 3;
	public static final int FILE_NAME_LENGTH = 128 * 3;
	public static final int TOTAL_LENGTH = 8;
	public static final int SEGMENTS_NUMBER_LENGTH = 4;
	public static final int RESUMABLE_FLAG_LENGTH = 1;
	public static final int THREAD_NUMBER_LENGTH = 1;
	public static final int STATE_LENGTH = 1;
	public static int URL_POSITION = 0;
	public static int REFER_URL_POSITION = URL_POSITION + URL_LENGTH;
	public static int FOLDER_POSITION = REFER_URL_POSITION + REFER_URL_LENGTH;
	public static int FILE_NAME_POSITION = FOLDER_POSITION + FOLDER_LENGTH;
	public static int TOTAL_LENGTH_POSITION = FILE_NAME_POSITION
			+ FILE_NAME_LENGTH;
	public static int SEGMENTS_NUMBER_POSITION = TOTAL_LENGTH_POSITION
			+ TOTAL_LENGTH;
	public static int RESUMABLE_FLAG_POSITION = SEGMENTS_NUMBER_POSITION
			+ SEGMENTS_NUMBER_LENGTH;
	public static int THREAD_NUMBER_POSITION = RESUMABLE_FLAG_POSITION
			+ RESUMABLE_FLAG_LENGTH;
	public static int STATE_POSTION = THREAD_NUMBER_POSITION
			+ THREAD_NUMBER_LENGTH;
	public static int FIRST_SEGMENT_POSITION = STATE_POSTION + STATE_LENGTH;

}
