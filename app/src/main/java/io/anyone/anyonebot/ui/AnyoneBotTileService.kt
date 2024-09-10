package io.anyone.anyonebot.ui

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.service.quicksettings.TileService
import io.anyone.anyonebot.AnyoneBotActivity

class AnyoneBotTileService: TileService() {

    // Called when the user taps on your tile in an active or inactive state.
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()

        val intent = Intent(this, AnyoneBotActivity::class.java)
            .addFlags(FLAG_ACTIVITY_NEW_TASK)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            startActivityAndCollapse(pi)
        }
        else {
            startActivityAndCollapse(intent)
        }
    }
}