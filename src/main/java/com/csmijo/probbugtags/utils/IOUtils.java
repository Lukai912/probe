package com.csmijo.probbugtags.utils;

/**
 * Created by chengqianqian-xy on 2016/10/14.
 */

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.android.internal.util.Predicate;
import com.csmijo.probbugtags.collector.BoundedLinkedList;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by chengqianqian-xy on 2016/8/18.
 */
public class IOUtils {
    private static final Predicate<String> DEFAULT_FILTER = new Predicate<String>() {
        @Override
        public boolean apply(String s) {
            return true;
        }
    };

    private static final int NO_LIMIT = -1;

    public IOUtils() {
    }

    /**
     * Safe closes a Closeable
     *
     * @param closeable
     */
    public static void safeClose(@Nullable Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Reads an inputStream into a String
     *
     * @param input
     * @return
     */
    public static String streamToString(@NonNull InputStream input) throws IOException {
        return streamToString(input, DEFAULT_FILTER, NO_LIMIT);
    }


    /**
     * Reads an inputStream into a string
     *
     * @param input  the stream
     * @param filter should return false for lines which should be excluded
     * @return the read string
     * @throws IOException
     */
    public static String streamToString(@NonNull InputStream input, Predicate<String> filter) throws IOException {
        return streamToString(input, filter, NO_LIMIT);
    }


    /**
     * Reads an InputStream into a string
     *
     * @param input the stream
     * @param limit the maximum number of lines to read (the last x lines are kept)
     * @return the read string
     * @throws IOException
     */
    public static String streamToString(@NonNull InputStream input, int limit) throws IOException {
        return streamToString(input, DEFAULT_FILTER, limit);
    }


    /**
     * Reads an InputStream into a string
     *
     * @param input  the stream
     * @param filter should return false for lines which should be exclued
     * @param limit  the maximum number of lines to read (the last x lines to kept)
     * @return the read string
     * @throws IOException
     */
    public static String streamToString(@NonNull InputStream input, Predicate<String> filter, int
            limit) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input), Constants.DEFAULT_BUFFER_SIZE_IN_BYTES);
        try {
            String line;
            List<String> buffer = limit == NO_LIMIT ? new LinkedList<String>() : new
                    BoundedLinkedList<String>(limit);
            while ((line = reader.readLine()) != null) {
                if (filter.apply(line)) {
                    buffer.add(line);
                }
            }
            //Returns a string containing the tokens joined by delimiters
            return TextUtils.join("\n", buffer);
        } finally {
            safeClose(reader);
        }
    }
}

