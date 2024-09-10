package io.anyone.anyonebot

import android.app.Application
import android.content.res.Configuration
import io.anyone.anyonebot.core.Languages
import io.anyone.anyonebot.core.LocaleHelper
import io.anyone.anyonebot.service.AnyoneBotConstants
import io.anyone.anyonebot.service.util.Prefs
import java.util.Locale

class AnyoneBotApp : Application(),
    AnyoneBotConstants {


    override fun onCreate() {
        super.onCreate()

//      useful for finding unclosed sockets...
//        StrictMode.setVmPolicy(
//            VmPolicy.Builder()
//                .detectLeakedClosableObjects()
//                .penaltyLog()
//                .build()
//        )

        Prefs.setContext(applicationContext)
        LocaleHelper.onAttach(applicationContext)

        Languages.setup(AnyoneBotActivity::class.java, R.string.menu_settings)

        if (Prefs.getDefaultLocale() != Locale.getDefault().language) {
            Languages.setLanguage(this, Prefs.getDefaultLocale(), true)
        }

        // this code only runs on first install and app updates
        if (Prefs.getCurrentVersionForUpdate() < BuildConfig.VERSION_CODE) {
            Prefs.setCurrentVersionForUpdate(BuildConfig.VERSION_CODE)
            // don't do anything resource intensive here, instead set a flag to do the task later

            // tell AnyoneBotService it needs to reinstall geoip
            Prefs.setIsGeoIpReinstallNeeded(true)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (Prefs.getDefaultLocale() != Locale.getDefault().language) {
            Languages.setLanguage(this, Prefs.getDefaultLocale(), true)
        }
    }

    fun setLocale() {
        val appLocale = Prefs.getDefaultLocale()
        val systemLoc = Locale.getDefault().language

        if (appLocale != systemLoc) {
            Languages.setLanguage(this, appLocale, true)
        }
    }
}
