package net.madz.download.engine;

import net.madz.download.ILifeCycle;

public interface IDownloadEngine extends ILifeCycle {

    DownloadTask findById(int id);

    DownloadTask[] findByUrl(String url);
}
