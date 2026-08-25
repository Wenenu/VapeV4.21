package gg.vape.api;

import gg.vape.config.RefreshableSettingsPayload;
import gg.vape.config.SettingsDataType;
import gg.vape.config.SettingsPayload;

public final class SettingsApi {
    public <T> T saveSettings(SettingsDataType type, SettingsPayload payload) throws Exception {
        if (payload instanceof RefreshableSettingsPayload) {
            ((RefreshableSettingsPayload) payload).refreshFromCurrentSettings();
        }
        throw new UnsupportedOperationException("Online services are disabled in this build");
    }

    public <T> ApiResponse<T> loadSettings(SettingsDataType type) throws Exception {
        throw new UnsupportedOperationException("Online services are disabled in this build");
    }
}
