package gg.vape.runtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Loads payload classes from a local JAR without URL-based class loading. */
public final class ForgePayloadClassLoader extends ClassLoader {
    private static final String PAYLOAD_CLASS_PREFIX = "gg.vape.";
    private static final String AUXILIARY_CLASS_PREFIX = "func.skidline.";
    private final File payload;

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public ForgePayloadClassLoader(String payloadPath, ClassLoader parent) {
        super(parent);
        this.payload = new File(payloadPath);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null && isPayloadClass(name)) {
                loaded = findClass(name);
            }
            if (loaded == null) {
                loaded = super.loadClass(name, false);
            }
            if (resolve) {
                resolveClass(loaded);
            }
            return loaded;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String entryName = name.replace('.', '/') + ".class";
        try (JarFile jar = new JarFile(payload)) {
            JarEntry entry = jar.getJarEntry(entryName);
            if (entry == null) {
                throw new ClassNotFoundException(name);
            }
            try (InputStream input = jar.getInputStream(entry)) {
                java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }
                byte[] bytes = output.toByteArray();
                return defineClass(name, bytes, 0, bytes.length);
            }
        } catch (IOException exception) {
            throw new ClassNotFoundException(name, exception);
        }
    }

    public Map<String, ClassLoader> buildPackageRoutingMap(
            Map<String, ClassLoader> currentRoutes) throws IOException {
        Map<String, ClassLoader> routes = new HashMap<>();
        if (currentRoutes != null) {
            routes.putAll(currentRoutes);
        }
        Set<String> payloadPackages = new HashSet<>();
        try (JarFile jar = new JarFile(payload)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement().getName();
                if (!entryName.endsWith(".class")) {
                    continue;
                }
                int separator = entryName.lastIndexOf('/');
                if (separator > 0 && (entryName.startsWith("gg/vape/")
                        || entryName.startsWith("func/skidline/"))) {
                    payloadPackages.add(entryName.substring(0, separator).replace('/', '.'));
                }
            }
        }
        if (payloadPackages.isEmpty()) {
            throw new IOException("payload JAR contains no product classes");
        }
        for (String packageName : payloadPackages) {
            routes.put(packageName, this);
        }
        return routes;
    }

    private static boolean isPayloadClass(String name) {
        return name.startsWith(PAYLOAD_CLASS_PREFIX)
                || name.startsWith(AUXILIARY_CLASS_PREFIX);
    }
}
