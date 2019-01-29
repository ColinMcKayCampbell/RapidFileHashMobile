package colin.mckay.campbell.rfh_mobile;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Colin on 28/04/2018.
 */

public interface AsyncResponse {
    void process(ConcurrentLinkedQueue<myFile> fileQueue);
}
