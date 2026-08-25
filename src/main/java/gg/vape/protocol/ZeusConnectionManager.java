package gg.vape.protocol;

import gg.vape.ui.click.component.GuiComponent;
import java.io.IOException;

/** Offline compatibility manager; it never creates a channel or starts a connection. */
public final class ZeusConnectionManager {
    private static final ZeusConnectionManager INSTANCE = new ZeusConnectionManager();
    private static GuiComponent[] guiComponents;
    private final ZeusClient client = new ZeusClient();

    private ZeusConnectionManager() {
    }

    public static ZeusConnectionManager T() {
        return INSTANCE;
    }

    public ZeusClient u() {
        return client;
    }

    public void V(Runnable connected, Runnable disconnected) throws InterruptedException, IOException {
        // Deliberately do not invoke callbacks: no transport exists in this build.
    }

    void connect(String ignoredAddress, Runnable connected, Runnable disconnected)
            throws InterruptedException, IOException {
        // Deliberately empty.
    }

    public static void d(GuiComponent[] components) {
        guiComponents = components;
    }

    public static GuiComponent[] r() {
        return guiComponents;
    }

    public static ZeusClient m(ZeusConnectionManager ignoredManager) {
        return INSTANCE.client;
    }

    static {
        d(new GuiComponent[3]);
    }
}
