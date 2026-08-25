package gg.vape.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/** Local JSON/date helpers retained for offline model parsing. */
public final class ApiHttpClient {
    private static final DateFormat API_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    public static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private ApiHttpClient() {
    }

    @Nullable
    @Contract(value = "null -> null")
    public static Date parseApiDate(@Nullable String dateText) throws ParseException {
        return dateText == null ? null : API_DATE_FORMAT.parse(dateText);
    }

    public static boolean opaquePredicate() {
        return false;
    }

    public static boolean getOpaqueState() {
        return false;
    }

    public static void setOpaqueState(boolean ignored) {
    }

    public static <R> ApiResponse<R> getApiResponse(
            String ignored, Function<JsonElement, R> ignoredParser) {
        throw offline();
    }

    public static <R> ApiResponse<R> postApiResponse(
            String ignored, Object ignoredBody, Function<JsonElement, R> ignoredParser) {
        throw offline();
    }

    public static <R> ApiResponse<R> deleteApiResponse(
            String ignored, Object ignoredBody, Function<JsonElement, R> ignoredParser) {
        throw offline();
    }

    public static <R> R get(String ignored, Class<R> ignoredType) {
        throw offline();
    }

    public static <R> R post(String ignored, Class<R> ignoredType, Object ignoredBody) {
        throw offline();
    }

    public static <R> R delete(String ignored, Class<R> ignoredType) {
        throw offline();
    }

    public static <R> R delete(String ignored, Class<R> ignoredType, Object ignoredBody) {
        throw offline();
    }

    private static UnsupportedOperationException offline() {
        return new UnsupportedOperationException("Online services are disabled in this build");
    }
}
