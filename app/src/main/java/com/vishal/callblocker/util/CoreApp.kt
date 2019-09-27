package com.vishal.callblocker.util

import android.app.Application
import com.osquare.support.utils.sharedPreference.SharedPrefsUtil

class CoreApp : Application() {

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     *
     * <p>Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.</p>
     *
     * <p>If you override this method, be sure to call {@code super.onCreate()}.</p>
     *
     * <p class="note">Be aware that direct boot may also affect callback order on
     * Android {@link android.os.Build.VERSION_CODES#N} and later devices.
     * Until the user unlocks the device, only direct boot aware components are
     * allowed to run. You should consider that all direct boot unaware
     * components, including such {@link android.content.ContentProvider}, are
     * disabled until user unlock happens, especially when component callback
     * order matters.</p>
     */
    override fun onCreate() {
        super.onCreate()
        SharedPrefsUtil.init(this)
    }

    /**
     * This method is for use in emulated process environments.  It will
     * never be called on a production Android device, where processes are
     * removed by simply killing them; no user code (including this callback)
     * is executed when doing so.
     */
    override fun onTerminate() {
        super.onTerminate()
    }

    companion object {
        @get:Synchronized
        var instance: CoreApp? = null
            private set
    }
}

