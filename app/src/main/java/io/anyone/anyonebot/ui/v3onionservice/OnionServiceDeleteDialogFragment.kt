package io.anyone.anyonebot.ui.v3onionservice

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import io.anyone.anyonebot.R

import io.anyone.anyonebot.core.DiskUtils.recursivelyDeleteDirectory
import org.torproject.anyonebot.service.AnyoneBotConstants

import java.io.File

class OnionServiceDeleteDialogFragment internal constructor(arguments: Bundle?) : DialogFragment() {
    init {
        setArguments(arguments)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_service_deletion)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int -> doDelete(arguments, requireContext()) }
            .setNegativeButton(android.R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .create()
    }

    private fun doDelete(arguments: Bundle?, context: Context) {
        context.contentResolver.delete(
            OnionServiceContentProvider.CONTENT_URI,
            OnionServiceContentProvider.OnionService._ID + '=' + requireArguments().getInt(
                OnionServiceActivity.BUNDLE_KEY_ID
            ),
            null
        )
        val base = context.filesDir.absolutePath + "/" + AnyoneBotConstants.ONION_SERVICES_DIR
        val localPath = arguments?.getString(OnionServiceActivity.BUNDLE_KEY_PATH)
        localPath?.let { recursivelyDeleteDirectory(File(base, localPath)) }
        (requireActivity() as OnionServiceActivity).showBatteryOptimizationsMessageIfAppropriate()
    }
}
