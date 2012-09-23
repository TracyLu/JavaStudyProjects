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
    public static final long ONE_SEGEMENT = 1024 * 1024; // measured in bytes.
    public static long URL_POSITION = 0;
    public static int SEGMENT_ID_LENGTH = 4;
    public static int SEGMENT_START_BYTES_LENGTH = 8;
    public static int SEGMENT_END_BYTES_LENGTH = 8;
    public static int SEGMENT_CURRENT_BYTES_LENGTH = 8;
    public static int SEGMENT_STATE_LENGTH = 1;
    public static int SEGMENT_LENGTH = SEGMENT_ID_LENGTH + SEGMENT_START_BYTES_LENGTH + SEGMENT_END_BYTES_LENGTH + SEGMENT_CURRENT_BYTES_LENGTH
            + SEGMENT_STATE_LENGTH;
    public static long REFER_URL_POSITION = URL_POSITION + URL_LENGTH;
    public static long FOLDER_POSITION = REFER_URL_POSITION + REFER_URL_LENGTH;
    public static long FILE_NAME_POSITION = FOLDER_POSITION + FOLDER_LENGTH;
    public static long TOTAL_LENGTH_POSITION = FILE_NAME_POSITION + FILE_NAME_LENGTH;
    public static long SEGMENTS_NUMBER_POSITION = TOTAL_LENGTH_POSITION + TOTAL_LENGTH;
    public static long RESUMABLE_FLAG_POSITION = SEGMENTS_NUMBER_POSITION + SEGMENTS_NUMBER_LENGTH;
    public static long THREAD_NUMBER_POSITION = RESUMABLE_FLAG_POSITION + RESUMABLE_FLAG_LENGTH;
    public static long STATE_POSTION = THREAD_NUMBER_POSITION + THREAD_NUMBER_LENGTH;
    public static long FIRST_SEGMENT_POSITION = STATE_POSTION + STATE_LENGTH;
}
