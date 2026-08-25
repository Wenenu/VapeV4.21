package gg.vape.utils.render;

import gg.vape.Vape;
import gg.vape.unmap.ImageParser$Format;
import java.io.ByteArrayInputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

class RemoteImageTextureCache {
    private final int imageSize;
    private final ConcurrentLinkedQueue<String> pendingUsernames = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<String, GlImageTexture> textures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, byte[]> downloadedImages = new ConcurrentHashMap<>();

    void processPendingDownloads() {
        pendingUsernames.clear();
    }

    RemoteImageTextureCache(int imageSize) {
        this.imageSize = imageSize;
    }

    byte[] getDownloadedImage(String username) {
        return downloadedImages.get(username);
    }

    GlImageTexture getTexture(String username) {
        GlImageTexture cached = textures.get(username);
        if (cached != null) {
            return cached;
        }
        byte[] imageData = downloadedImages.get(username);
        if (imageData == null) {
            return null;
        }
        try {
            GlImageTexture texture = new GlImageTexture(
                    new ByteArrayInputStream(imageData), 9729, ImageParser$Format.RGBA);
            textures.put(username, texture);
            return texture;
        } catch (Exception exception) {
            Vape.logThrowable(exception);
            return ImageRenderer.loadResource("default_user", false, false);
        }
    }

    void download(String ignoredUsername) {
        // Remote avatar downloads are intentionally unavailable offline.
    }
}
