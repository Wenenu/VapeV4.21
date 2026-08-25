package gg.vape.api;

import gg.vape.account.AccountInfoResponse;
import gg.vape.ui.click.component.GuiComponent;
import java.util.concurrent.CompletableFuture;

/** Compatibility registry for an offline build; no remote service is configured. */
public final class ApiServices {
    private static final ApiServices INSTANCE = new ApiServices();
    private static GuiComponent[] legacyComponentState;
    private final UserDataApi userDataApi = new UserDataApi();
    private final PublicProfileApi publicProfileApi = new PublicProfileApi();
    private final SettingsApi settingsApi = new SettingsApi();

    public static ApiServices getInstance() {
        return INSTANCE;
    }

    public UserDataApi getUserDataApi() {
        return this.userDataApi;
    }

    public PublicProfileApi getPublicProfileApi() {
        return this.publicProfileApi;
    }

    public SettingsApi getSettingsApi() {
        return this.settingsApi;
    }

    public CompletableFuture<ApiResponse<AccountInfoResponse>> getAccountInfo() {
        return failedFuture();
    }

    public CompletableFuture<ApiResponse<Boolean>> registerOnlineAccount(String ignored) {
        return failedFuture();
    }

    public static GuiComponent[] getLegacyComponentState() {
        return legacyComponentState;
    }

    public static void setLegacyComponentState(GuiComponent[] state) {
        legacyComponentState = state;
    }

    private static <T> CompletableFuture<T> failedFuture() {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(new UnsupportedOperationException(
                "Online services are disabled in this build"));
        return future;
    }

    static {
        setLegacyComponentState(new GuiComponent[4]);
    }
}
