package net.madz.download.service.responses;

import java.util.List;

import net.madz.download.engine.DownloadTask;
import net.madz.download.service.IServiceResponse;

public class ListTasksResponse implements IServiceResponse {

    private StringBuilder result = new StringBuilder();
    private int count;

    public ListTasksResponse(List<DownloadTask> tasks) {
        if ( null == tasks ) {
            count = 0;
            return;
        }
        for ( DownloadTask task : tasks ) {
            count++;
            result.append(task.getId() + "\t");
            result.append(task.getUrl() + "\t");
            result.append(task.getFolder() + "\t");
            result.append(task.getFileName() + "\t");
            result.append(task.getState());
            result.append("\n");
        }
    }

    @Override
    public String toString() {
        if ( 0 == count ) {
            return "There is no result satisfied your requirement.";
        } else {
            return count + " tasks returned.\n" + result;
        }
    }
}
