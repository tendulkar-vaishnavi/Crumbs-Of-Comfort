package com.example.crumbsofcomfort.Vendor.Helper;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudinaryHelper {

    private static boolean isInitialized = false;

    public interface CloudinaryCallback {
        void onSuccess(String imageUrl);
        void onError(Exception e);
    }

    public static void initCloudinary(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dr2bfrllm"); // ✅ REPLACE THIS
            config.put("api_key", "794786254878996");       // ✅ REPLACE THIS
            config.put("api_secret", "xa39INgg1EPGOMlMeglEOvMNT6o"); // ✅ REPLACE THIS
            MediaManager.init(context.getApplicationContext(), config);
            isInitialized = true;
        }
    }

    public static void uploadImage(Context context, Uri imageUri, CloudinaryCallback callback) {
        MediaManager.get().upload(imageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        callback.onSuccess(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        callback.onError(new Exception(error.getDescription()));
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    public static void uploadMultipleImages(Context context, List<Uri> imageUris, CloudinaryCallback callback) {
        for (Uri uri : imageUris) {
            uploadImage(context, uri, callback);
        }
    }
}
