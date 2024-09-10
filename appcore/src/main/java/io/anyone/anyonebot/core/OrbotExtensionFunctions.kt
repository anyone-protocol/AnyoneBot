package io.anyone.anyonebot.core

import android.content.Intent
import org.torproject.anyonebot.service.AnyoneBotConstants

/**
 * Used to build Intents in Orbot, annoyingly we have to set this when passing Intents to
 * OrbotService to distinguish between Intents that are triggered from this codebase VS
 * Intents that the system sends to the VPNService on boot...
 */
fun Intent.putNotSystem(): Intent = this.putExtra(AnyoneBotConstants.EXTRA_NOT_SYSTEM, true)
