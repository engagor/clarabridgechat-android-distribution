package com.clarabridge.core.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import javax.inject.Inject;

/**
 * A helper class that provides information about the host app.
 */
public final class ApplicationInfo {

    private final PackageManager packageManager;
    private final android.content.pm.ApplicationInfo applicationInfo;
    private final String packageName;

    @Inject
    public ApplicationInfo(Context context) {
        this.packageManager = context.getPackageManager();
        this.applicationInfo = context.getApplicationInfo();
        this.packageName = context.getPackageName();
    }

    /**
     * @return the label associated with the host application
     */
    public String getName() {
        return StringUtils.emptyIfNull(packageManager.getApplicationLabel(applicationInfo));
    }

    /**
     * @return the package name of the host application
     */
    public String getPackageName() {
        return StringUtils.emptyIfNull(packageName);
    }

    /**
     * @return the version name of the host application
     */
    public String getVersion() {
        String versionName = null;

        try {
            final PackageInfo info = packageManager.getPackageInfo(packageName, 0);

            if (info != null) {
                versionName = info.versionName;
            }
        } catch (final PackageManager.NameNotFoundException ignored) {
            // Intentionally empty
        }

        return StringUtils.emptyIfNull(versionName);
    }

    /**
     * @return the package name of the application that installed a package. This identifies
     * which market the package came from
     */
    public String getInstallerPackageName() {
        String packageInstallerName = packageManager.getInstallerPackageName(packageName);

        return StringUtils.emptyIfNull(packageInstallerName);
    }
}

