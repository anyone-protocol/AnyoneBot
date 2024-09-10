package io.anyone.anyonebot.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import io.anyone.anyonebot.BuildConfig
import io.anyone.anyonebot.R
import io.anyone.anyonebot.core.DiskUtils
import io.anyone.anyonebot.service.AnyoneBotService
import java.io.IOException

class AboutDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "AboutDialogFragment"
        const val VERSION = "${BuildConfig.VERSION_NAME} (Tor ${AnyoneBotService.BINARY_TOR_VERSION})"
        private const val BUNDLE_KEY_TV_ABOUT_TEXT = "about_tv_txt"
        private const val ABOUT_LICENSE_EQUALSIGN =
            "==============================================================================="
    }

    private lateinit var tvAbout: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view: View? = activity?.layoutInflater?.inflate(R.layout.layout_about, null)

        val versionName = view?.findViewById<TextView>(R.id.versionName)
        versionName?.text = VERSION

        tvAbout = view?.findViewById(R.id.aboutother) as TextView

        var buildAboutText = true

        savedInstanceState?.getString(BUNDLE_KEY_TV_ABOUT_TEXT)?.let {
            buildAboutText = false
            tvAbout.text = it
        }

        if (buildAboutText) {
            try {
                var aboutText = DiskUtils.readFileFromAssets("LICENSE", requireContext())
                aboutText =
                    aboutText.replace(ABOUT_LICENSE_EQUALSIGN, "\n").replace("\n\n", "<br/><br/>")
                        .replace("\n", "")
                tvAbout.text = Html.fromHtml(aboutText, Html.FROM_HTML_MODE_LEGACY)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return AlertDialog.Builder(context, R.style.AnyoneBotDialogTheme)
            .setTitle(getString(R.string.button_about))
            .setView(view)
            .create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BUNDLE_KEY_TV_ABOUT_TEXT, tvAbout.text.toString())
    }
}
