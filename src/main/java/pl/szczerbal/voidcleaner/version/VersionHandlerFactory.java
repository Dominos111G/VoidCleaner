package pl.szczerbal.voidcleaner.version;

import org.bukkit.Bukkit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory for creating version-specific handlers.
 * Detects the server's Minecraft version and returns appropriate handler.
 */
public class VersionHandlerFactory {

    private static final Pattern VERSION_PATTERN = Pattern.compile("\\(MC: (\\d+)\\.(\\d+)(?:\\.(\\d+))?");

    /**
     * Create appropriate version handler for current server
     */
    public static VersionHandler create() {
        String version = detectVersion();

        // Extract major and minor version
        String[] parts = version.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

        // Route to appropriate handler
        if (major == 1) {
            return switch (minor) {
                case 21 -> new Handler121();
                case 20 -> new Handler120();
                // Fallback for unknown 1.x versions
                default -> {
                    if (minor > 21) {
                        yield new Handler121(); // Use latest for future versions
                    } else {
                        throw new IllegalStateException(
                            "Unsupported Minecraft version: " + version +
                            ". Supported: 1.20.x, 1.21.x"
                        );
                    }
                }
            };
        }

        throw new IllegalStateException(
            "Unsupported Minecraft major version: " + major +
            ". Expected version 1.x"
        );
    }

    /**
     * Detect Minecraft version from Bukkit
     * Example: "version git-Paper-XXX (MC: 1.20.4)"
     */
    private static String detectVersion() {
        String bukkitVersion = Bukkit.getVersion();
        Matcher matcher = VERSION_PATTERN.matcher(bukkitVersion);

        if (matcher.find()) {
            String major = matcher.group(1);
            String minor = matcher.group(2);
            String patch = matcher.group(3);

            if (patch != null) {
                return major + "." + minor + "." + patch;
            } else {
                // Default to .0 if patch not specified
                return major + "." + minor + ".0";
            }
        }

        throw new IllegalStateException(
            "Could not detect Minecraft version from: " + bukkitVersion
        );
    }

    /**
     * Check if server version is supported
     */
    public static boolean isSupported() {
        try {
            create();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
