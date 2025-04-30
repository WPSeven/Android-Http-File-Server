package com.waiphyo.androidhttpfileserver;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.nanohttpd.fileupload.NanoFileUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by waiphyo.com on 2025/4/30.
 */

public class HttpServer extends NanoHTTPD {
    private static final String TAG = "HttpServer";
    private NanoFileUpload mFileUpload;
    private OnStatusUpdateListener mStatusUpdateListener;
    private Context mContext;

    interface OnStatusUpdateListener {
        void onUploadingProgressUpdate(int progress);
        void onUploadingFile(File file, boolean done);
        void onDownloadingFile(File file, boolean done);
    }

    class DownloadResponse extends Response {
        private File downloadFile;

        DownloadResponse(File downloadFile, InputStream stream) {
            super(Status.OK, "application/octet-stream", stream, downloadFile.length());
            this.downloadFile = downloadFile;
        }

        @Override
        protected void send(OutputStream outputStream) {
            super.send(outputStream);
            if (mStatusUpdateListener != null) {
                mStatusUpdateListener.onDownloadingFile(downloadFile, true);
            }
        }
    }

    public HttpServer(Context context, int port) {
        super(port);
        mContext = context;


        // Configure for large files
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(1024 * 1024); // 1MB buffer
        factory.setRepository(mContext.getCacheDir()); // Temporary file location

        mFileUpload = new NanoFileUpload(factory);
        // Allow files up to 10GB
        mFileUpload.setFileSizeMax(10L * 1024 * 1024 * 1024);
        mFileUpload.setSizeMax(10L * 1024 * 1024 * 1024);


        mFileUpload.setProgressListener(new ProgressListener() {
            int progress = 0;
            @Override
            public void update(long pBytesRead, long pContentLength, int pItems) {
                //Log.d(TAG, pBytesRead + " bytes has been read, totol " + pContentLength + " bytes");
                if (mStatusUpdateListener != null) {
                    int p = (int) (pBytesRead * 100 / pContentLength);
                    if (p != progress) {
                        progress = p;
                        mStatusUpdateListener.onUploadingProgressUpdate(progress);
                    }
                }
            }
        });
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();
        Map<String, String> header = session.getHeaders();
        Map<String, String> parms = session.getParms();
        String answer = "Success!";
        Log.d(TAG, "uri=" + uri);
        Log.d(TAG, "method=" + method);
        Log.d(TAG, "header=" + header);
        Log.d(TAG, "params=" + parms);

        // for file upload
        if (NanoFileUpload.isMultipartContent(session)) {
            try {
                File appFolder = mContext.getFilesDir();

                // Set chunked transfer to true for progress updates
                Response response = newChunkedResponse(Response.Status.OK, "text/html", null);
                response.addHeader("Cache-Control", "no-cache");
                response.addHeader("Connection", "keep-alive");

                FileItemIterator iterator = mFileUpload.getItemIterator(session);
                while (iterator.hasNext()) {
                    FileItemStream item = iterator.next();

                    if (!item.isFormField()) {
                        String fileName = item.getName();
                        if (fileName != null && !fileName.isEmpty()) {
                            File file = new File(appFolder, fileName);
                            Log.d(TAG, "Saving file to " + file.getAbsolutePath());

                            if (mStatusUpdateListener != null) {
                                mStatusUpdateListener.onUploadingFile(file, false);
                            }

                            // Get content length from headers
                            String contentLength = session.getHeaders().get("content-length");
                            long fileSize = contentLength != null ? Long.parseLong(contentLength) : -1;

                            // Copy with progress using the helper method
                            copyInputStreamToFile(item.openStream(), file, fileSize);

                            if (mStatusUpdateListener != null) {
                                mStatusUpdateListener.onUploadingFile(file, true);
                            }

                            // Return success response with redirect
                            String successHtml = "<html><head>" +
                                    "<meta http-equiv='refresh' content='2;url=/'>" +
                                    "</head><body>" +
                                    "<h2>Upload Successful!</h2>" +
                                    "<p>File: " + fileName + " uploaded successfully.</p>" +
                                    "<p>Redirecting back to upload page...</p>" +
                                    "</body></html>";
                            return newFixedLengthResponse(successHtml);
                        }
                    }
                }

                return response;

            } catch (Exception e) {
                e.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR,
                        "text/html",
                        "<html><body><h2>Upload Error</h2>" +
                                "<p>Error: " + e.getMessage() + "</p>" +
                                "<p><a href='/'>Back to Upload Form</a></p>" +
                                "</body></html>");
            }
        } else if (method.equals(Method.GET)) {

            // In HttpServer.java, modify the upload form HTML
            if (uri.equals("/")) {
                answer = "<html><head><title>File Upload</title>" +
                        "<style>" +
                        ".progress { width: 100%; max-width: 500px; border: 1px solid #ccc; }" +
                        ".progress-bar { width: 0%; height: 20px; background-color: #4CAF50; text-align: center; line-height: 20px; color: white; }" +
                        "</style>" +
                        "<script>" +
                        "function uploadFile() {" +
                        "    var file = document.getElementById('fileInput').files[0];" +
                        "    var formData = new FormData();" +
                        "    formData.append('file', file);" +
                        "    var xhr = new XMLHttpRequest();" +
                        "    xhr.open('POST', '/upload', true);" +
                        "    xhr.upload.onprogress = function(e) {" +
                        "        if (e.lengthComputable) {" +
                        "            var percentComplete = (e.loaded / e.total) * 100;" +
                        "            document.getElementById('progressBar').style.width = percentComplete + '%';" +
                        "            document.getElementById('progressBar').textContent = Math.round(percentComplete) + '%';" +
                        "        }" +
                        "    };" +
                        "    xhr.onload = function() {" +
                        "        if (xhr.status === 200) {" +
                        "            document.getElementById('status').innerHTML = 'Upload complete!';" +
                        "        } else {" +
                        "            document.getElementById('status').innerHTML = 'Upload failed!';" +
                        "        }" +
                        "    };" +
                        "    document.getElementById('status').innerHTML = 'Uploading...';" +
                        "    xhr.send(formData);" +
                        "    return false;" +
                        "}" +
                        "</script></head><body>" +
                        "<h2>HTTP File Upload</h2>" +
                        "<form onsubmit='return uploadFile();'>" +
                        "<input type='file' id='fileInput' name='file'><br><br>" +
                        "<input type='submit' value='Upload File'>" +
                        "</form><br>" +
                        "<div class='progress'>" +
                        "<div id='progressBar' class='progress-bar'></div>" +
                        "</div>" +
                        "<p id='status'></p>" +
                        "<p><a href='/files'>View Uploaded Files</a></p>" +
                        "</body></html>";
                return newFixedLengthResponse(answer);
            }

            // In the GET handler, add this case
            if (uri.equals("/files")) {
                File appFolder = mContext.getFilesDir();
                File[] files = appFolder.listFiles();

                answer = "<html><head><title>Uploaded Files</title></head><body>" +
                        "<h2>Uploaded Files</h2>";

                if (files != null && files.length > 0) {
                    for (File file : files) {
                        answer += "<a href='/download/" + file.getName() + "'>" +
                                file.getName() + "</a><br>";
                    }
                } else {
                    answer += "<p>No files uploaded yet.</p>";
                }

                answer += "<br><a href='/'>Upload More Files</a></body></html>";
                return newFixedLengthResponse(answer);
            }

            File rootFile = Environment.getExternalStorageDirectory();
            String requestedPath = uri;

            // Remove any leading slash
            if (requestedPath.startsWith("/")) {
                requestedPath = requestedPath.substring(1);
            }

            // Clean up the requested path
            if (requestedPath.startsWith("sdcard/")) {
                requestedPath = requestedPath.substring("sdcard/".length());
            } else if (requestedPath.startsWith("storage/emulated/0/")) {
                requestedPath = requestedPath.substring("storage/emulated/0/".length());
            }

            File targetFile = new File(rootFile, requestedPath);
            Log.d(TAG, "Accessing path: " + targetFile.getAbsolutePath());

            if (!targetFile.exists()) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND,
                        "text/plain",
                        "Error! No such file or directory: " + targetFile.getPath());
            }


            if (targetFile.isDirectory()) {
                // list directory files
                Log.d(TAG, "list " + targetFile.getPath());
                File[] files = targetFile.listFiles();
                answer = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; " +
                        "charset=utf-8\"><title> HTTP File Browser</title>";


                if (files != null) {
                    for (File file : files) {
                        answer += "<a href=\"" + file.getAbsolutePath()
                                + "\" alt = \"\">" + file.getAbsolutePath()
                                + "</a><br>";
                    }
                } else {
                    answer += "Unable to list directory contents. Check permissions.";
                }

                answer += "</head></html>";
            } else {
                // serve file download
                InputStream inputStream;
                Response response = null;
                Log.d(TAG, "downloading file " + rootFile.getAbsolutePath());
                if (mStatusUpdateListener != null) {
                    mStatusUpdateListener.onDownloadingFile(rootFile, false);
                }

                try {
                    inputStream = new FileInputStream(rootFile);
                    response = new DownloadResponse(rootFile, inputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (response != null) {
                    response.addHeader(
                            "Content-Disposition", "attachment; filename=" + rootFile.getName());
                    return response;
                } else {
                    return newFixedLengthResponse("Error downloading file!");
                }
            }
        }

        return newFixedLengthResponse(answer);
    }


    public void setOnStatusUpdateListener(OnStatusUpdateListener listener) {
        mStatusUpdateListener = listener;
    }



    // In HttpServer.java, update the file upload section
    private static final int BUFFER_SIZE = 8192;

    private void copyInputStreamToFile(InputStream input, File file, long contentLength) throws IOException {
        try (FileOutputStream output = new FileOutputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            long count = 0;
            int n;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;

                // Calculate progress
                int progress = (int)((count * 100) / contentLength);
                if (mStatusUpdateListener != null) {
                    mStatusUpdateListener.onUploadingProgressUpdate(progress);
                }
            }
        }
    }

}
