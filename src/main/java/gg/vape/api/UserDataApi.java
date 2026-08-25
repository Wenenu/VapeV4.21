package gg.vape.api;

import com.google.gson.JsonObject;
import gg.vape.api.ApiResponse;
import gg.vape.api.UserDataResponse;
import gg.vape.sync.RemoteProfileDataMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class UserDataApi {
    public CompletableFuture<ApiResponse<Boolean>> saveUserData(JsonObject ignoredData) {
        return failedFuture();
    }

    public CompletableFuture<ApiResponse<RemoteProfileDataMap>> saveProfileData(JsonObject ignoredData) {
        return failedFuture();
    }

    public CompletableFuture<ApiResponse<UserDataResponse>> getUserData() {
        return failedFuture();
    }

    public CompletableFuture<ApiResponse<UUID>> reserveProfileId() {
        return failedFuture();
    }

    private static <T> CompletableFuture<T> failedFuture() {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(new UnsupportedOperationException(
                "Online services are disabled in this build"));
        return future;
    }
}
