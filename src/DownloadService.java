public class DownloadService {
	public static void main(String[] args) {
		String path = "http://dl_dir.qq.com/qqfile/qq/QQforMac/QQ_V2.1.3.dmg";
		DownloadTask task = new DownloadTask(path, "QQ.dmg", 20);
//		DownloadHelper helper = new DownloadHelper(task);
		long start = System.currentTimeMillis();
		task.download();
		long end = System.currentTimeMillis();
		System.out.println("total time:" + (end - start));
		
		DownloadTask task2 = new DownloadTask(path, "QQ2.dmg");
		start = System.currentTimeMillis();
		task2.download();
		end = System.currentTimeMillis();
		System.out.println("total time 2:" + (end - start));
	}
}
