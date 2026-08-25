package gg.vape.api;

import com.google.gson.JsonObject;
import gg.vape.config.PublicProfile;
import gg.vape.config.PublicProfileReview;
import gg.vape.config.PublicProfileReviewResponse;
import gg.vape.config.PublicProfileShareInfo;
import gg.vape.config.PublicProfileSortMode;
import gg.vape.config.PublicProfileSummary;
import gg.vape.sync.RemoteProfileData;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Nullable;

public final class PublicProfileApi {
    private static boolean opaqueState;

    public CompletableFuture<ApiResponse<RemoteProfileData>> downloadProfile(long ignoredProfileId) { return failedFuture(); }
    public CompletableFuture<ApiResponse<Boolean>> markAllReviewsRead(PublicProfile ignoredProfile) { return failedFuture(); }
    public CompletableFuture<ApiResponse<PublicProfile>> createProfile(JsonObject ignoredPayload) { return failedFuture(); }
    public CompletableFuture<ApiResponse<Boolean>> deleteReviewResponse(PublicProfileReviewResponse ignoredResponse) { return failedFuture(); }
    public CompletableFuture<ApiResponse<Boolean>> deleteProfile(long ignoredProfileId) { return failedFuture(); }
    public CompletableFuture<ApiResponse<PagedResult<PublicProfileReview>>> getReviewPage(long ignoredProfileId, long ignoredPage) { return failedFuture(); }
    public CompletableFuture<ApiResponse<Boolean>> reportReviewResponse(long ignoredResponseId, String ignoredReason) { return failedFuture(); }
    public CompletableFuture<ApiResponse<Boolean>> markAllReviewsRead(long ignoredProfileId) { return failedFuture(); }
    public CompletableFuture<ApiResponse<PublicProfileReview>> createReview(PublicProfile ignoredProfile, boolean ignoredLiked, @Nullable String ignoredReason) { return failedFuture(); }
    public CompletableFuture<ApiResponse<Boolean>> markReviewsRead(long ignoredProfileId, List<Long> ignoredReviewIds) { return failedFuture(); }
    public CompletableFuture<ApiResponse<PublicProfileReviewResponse>> respondToReview(PublicProfileReview ignoredReview, String ignoredMessage) { return failedFuture(); }
    public CompletableFuture<ApiResponse<Boolean>> deleteReview(PublicProfileReview ignoredReview) { return failedFuture(); }
    public CompletableFuture<ApiResponse<RemoteProfileData>> downloadProfileUpdate(long ignoredProfileId) { return failedFuture(); }
    public CompletableFuture<ApiResponse<PagedResult<PublicProfileReview>>> getDelayedReviewPage(long ignoredProfileId, long ignoredPage) { return failedFuture(); }
    public CompletableFuture<ApiResponse<Boolean>> deleteReviewResponse(long ignoredResponseId) { return failedFuture(); }
    public CompletableFuture<ApiResponse<PagedResult<PublicProfileReview>>> getDelayedReviewPage(PublicProfile ignoredProfile, long ignoredPage) { return failedFuture(); }
    public CompletableFuture<ApiResponse<PublicProfile>> viewProfile(long ignoredProfileId) { return failedFuture(); }
    public CompletableFuture<ApiResponse<PublicProfileReviewResponse>> respondToReview(long ignoredReviewId, String ignoredMessage) { return failedFuture(); }
    public CompletableFuture<ApiResponse<Boolean>> deleteReview(long ignoredReviewId) { return failedFuture(); }
    public CompletableFuture<ApiResponse<PublicProfileReview>> createReview(long ignoredProfileId, boolean ignoredLiked, @Nullable String ignoredReason) { return failedFuture(); }
    public CompletableFuture<ApiResponse<Boolean>> markReviewsRead(PublicProfile ignoredProfile, List<Long> ignoredReviewIds) { return failedFuture(); }
    public CompletableFuture<ApiResponse<PublicProfileShareInfo>> regenerateShareCode(long ignoredProfileId) { return failedFuture(); }
    public CompletableFuture<ApiResponse<PagedResult<PublicProfileSummary>>> listProfiles(PublicProfileSortMode ignoredSortMode, long ignoredPage, @Nullable String ignoredSearch, @Nullable List<String> ignoredTags) { return failedFuture(); }
    public CompletableFuture<ApiResponse<Boolean>> reportReview(long ignoredReviewId, String ignoredReason) { return failedFuture(); }
    public CompletableFuture<ApiResponse<List<String>>> getMostPopularTags() { return failedFuture(); }
    public CompletableFuture<ApiResponse<PublicProfile>> editProfile(JsonObject ignoredPayload) { return failedFuture(); }

    public PublicProfileApi() {
    }

    public static boolean getOpaqueState() { return opaqueState; }
    public static void setOpaqueState(boolean state) { opaqueState = state; }
    public static boolean opaquePredicate() { return false; }

    private static <T> CompletableFuture<T> failedFuture() {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(new UnsupportedOperationException(
                "Online services are disabled in this build"));
        return future;
    }
}
