package com.csmijo.probbugtags.http;


import com.csmijo.probbugtags.utils.Constants;

import java.util.Map;

/**
 * Created by lukai1 on 2018/2/8.
 */

public class HttpConfig {
    private int mConnectionTimeout;
    private int mSocketTimeout;
    private Map<String, String> mHttpHeaders;
    private boolean isMuiltyPart;

    public HttpConfig() {
        this.mConnectionTimeout = Constants.DEFAULT_CONNECTION_TIMEOUT;
        this.mSocketTimeout = Constants.DEFAULT_SOCKET_TIMEOUT;
        this.mHttpHeaders = null;
        this.isMuiltyPart = false;
    }

    public HttpConfig(int connectionTimeout, int socketTimeout, Map<String, String> httpHeaders, boolean isMuiltyPart) {
        this.mConnectionTimeout = connectionTimeout;
        this.mSocketTimeout = connectionTimeout;
        this.mHttpHeaders = httpHeaders;
        this.isMuiltyPart = isMuiltyPart;
    }

    public int getConnectionTimeout() {
        return mConnectionTimeout;
    }

    public int getSocketTimeout() {
        return mSocketTimeout;
    }

    public Map<String, String> getHttpHeaders() {
        return mHttpHeaders;
    }

    public boolean isMuiltyPart() {
        return isMuiltyPart;
    }
}
