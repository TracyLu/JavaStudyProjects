package net.madz.download.service.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.madz.download.service.IService;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.requests.CreateTaskRequest;
import net.madz.download.service.responses.CreateTaskResponse;

@Command(arguments = {
		@Arg(name = "url", description = "the address of remote file."),
		@Arg(name = "folder", description = "where to store the file"),
		@Arg(name = "filename", description = "new file name.") }, commandName = "create-task", options = { @Option(description = "thread number", fullName = "--threadNumber", shortName = "-n") }, request = CreateTaskRequest.class, description = "This command is responsible for downloding specified url resource.")
public class CreateTaskService implements IService<CreateTaskRequest> {
	private ExecutorService pool; // We use thread pool
	private Lock poolLock = new ReentrantLock();
	private Condition allDone = poolLock.newCondition();
	private int doneNumber = 0;
	private static File file; // Point to the storage file

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public IServiceResponse processRequest(CreateTaskRequest request) {
		URL url = null;
		try {
			url = new URL(request.getUrl());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String folder = request.getFolder();
		String filename = request.getFilename();
		int threadNumber = request.getThreadNumber();
		this.pool = Executors.newFixedThreadPool(threadNumber);
		file = new File(folder, filename);
		download(url, threadNumber);
		CreateTaskResponse downloadResponse = new CreateTaskResponse();
		downloadResponse.setMessage("You task is downloading");
		return downloadResponse;
	}

	public boolean download(URL url, int threadNumber) {
		URLConnection openConnection = null;
		int totalLength = 0;
		int partLength = 0;
		try {
			openConnection = url.openConnection();
			openConnection.connect();
			totalLength = openConnection.getContentLength();
			partLength = totalLength / threadNumber;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		for (int i = 0; i < threadNumber; i++) {
			final int seq = i;
			final int finalPartLength = partLength;
			final File logfile = new File(file.getAbsolutePath() + "_log");
			final URL finalURL = url;
			if (i < threadNumber - 1) {
				pool.execute(new Runnable() {

					@Override
					public void run() {
						download(finalURL, finalPartLength * seq,
								finalPartLength * (seq + 1) - 1, logfile, seq);

					}
				});
			} else {
				final int finalTotalLength = totalLength;
				pool.execute(new Runnable() {
					@Override
					public void run() {
						download(finalURL, finalPartLength * seq,
								finalTotalLength - 1, logfile, seq);
					}
				});
			}
		}

		poolLock.lock();
		try {
			while (doneNumber < threadNumber) {
				try {
					allDone.await();
					if (doneNumber == threadNumber) {
						pool.shutdown();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} finally {
			poolLock.unlock();
		}
		return true;

	}

	private void download(URL url, int start, int end, File logfile, int seq) {
		HttpURLConnection openConnection = null;
		InputStream bs = null;
		RandomAccessFile fs = null;
		byte[] buf = new byte[8096];
		int size = 0;
		// long count = DownloadHelper.readOffset(seq);
		try {
			openConnection = (HttpURLConnection) url.openConnection();
			openConnection.setRequestProperty("RANGE", "bytes=" + start + "-"
					+ end);
			openConnection.connect();
			bs = openConnection.getInputStream();
			fs = new RandomAccessFile(file, "rw");
			int off = start;
			RandomAccessFile fos = new RandomAccessFile(logfile, "rw");
			while ((size = bs.read(buf)) != -1) {
				fs.seek(off);
				fs.write(buf, 0, size);
				// count += size;
				// DownloadHelper.writeOffSet(seq, count);
				off += size;
			}
			poolLock.lock();
			try {
				doneNumber++;
				allDone.signalAll();
			} finally {
				poolLock.unlock();
				fos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bs.close();
				fs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
