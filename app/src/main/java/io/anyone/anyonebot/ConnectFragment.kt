package io.anyone.anyonebot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import io.anyone.anyonebot.core.NetworkUtils.isNetworkAvailable
import io.anyone.anyonebot.core.putNotSystem
import io.anyone.anyonebot.service.AnyoneBotConstants
import io.anyone.anyonebot.service.AnyoneBotService
import io.anyone.anyonebot.service.util.Prefs
import io.anyone.anyonebot.ui.AppManagerActivity
import io.anyone.anyonebot.ui.MenuAction
import io.anyone.anyonebot.ui.MenuActionAdapter
import io.anyone.jni.AnonControlCommands


class ConnectFragment : Fragment(), ConnectionHelperCallbacks,
    ExitNodeDialogFragment.ExitNodeSelectedCallback {

    // main screen UI
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var btnStartVpn: Button
    private lateinit var ivOnion: ImageView
    private lateinit var ivOnionShadow: ImageView
    lateinit var progressBar: ProgressBar
    private lateinit var lvConnectedActions: ListView

    private var lastStatus: String? = ""

    @Deprecated("Deprecated in Java")
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)

        (activity as AnyoneBotActivity).fragConnect = this
        lastStatus = activity.previousReceivedTorStatus

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_connect, container, false)
        view?.let {

            tvTitle = it.findViewById(R.id.tvTitle)
            tvSubtitle = it.findViewById(R.id.tvSubtitle)
            btnStartVpn = it.findViewById(R.id.btnStart)
            ivOnion = it.findViewById(R.id.ivStatus)
            ivOnionShadow = it.findViewById(R.id.ivShadow)
            progressBar = it.findViewById(R.id.progressBar)
            lvConnectedActions = it.findViewById(R.id.lvConnected)

            if (Prefs.isPowerUserMode()) {
                btnStartVpn.text = getString(R.string.connect)
            }

            if (!isNetworkAvailable(requireContext())) {
                doLayoutNoInternet()
            } else {
                when (lastStatus) {
                    AnyoneBotConstants.STATUS_OFF -> doLayoutOff()
                    AnyoneBotConstants.STATUS_STARTING -> doLayoutStarting(requireContext())
                    AnyoneBotConstants.STATUS_ON -> doLayoutOn(requireContext())
                    AnyoneBotConstants.STATUS_STOPPING -> {}
                    else -> {
                        doLayoutOff()
                    }
                }
            }


        }

        return view
    }

    private fun stopTorAndVpn() {
        sendIntentToService(AnyoneBotConstants.ACTION_STOP)
        sendIntentToService(AnyoneBotConstants.ACTION_STOP_VPN)
    }

    private fun stopAnimations() {
        ivOnion.clearAnimation()
        ivOnionShadow.clearAnimation()
    }

    private fun sendNewnymSignal() {
        sendIntentToService(AnonControlCommands.SIGNAL_NEWNYM)
        ivOnion.animate().alpha(0f).duration = 500
        Handler().postDelayed({ ivOnion.animate().alpha(1f).duration = 500 }, 600)
    }

    private fun openExitNodeDialog() {
        ExitNodeDialogFragment(this).show(
            requireActivity().supportFragmentManager, "ExitNodeDialogFragment"
        )
    }

    private fun startAnonAndVpnDelay(@Suppress("SameParameterValue") ms: Long) =
        Handler(Looper.getMainLooper()).postDelayed({ startAnonAndVpn() }, ms)


    fun startAnonAndVpn() {
        val vpnIntent = VpnService.prepare(requireActivity())?.putNotSystem()
        if (vpnIntent != null && (!Prefs.isPowerUserMode())) {
            startActivityForResult(vpnIntent, AnyoneBotActivity.REQUEST_CODE_VPN)
        } else {
            // todo we need to add a power user mode for users to start the VPN without tor
            Prefs.putUseVpn(!Prefs.isPowerUserMode())
            sendIntentToService(AnyoneBotConstants.ACTION_START)

            if (!Prefs.isPowerUserMode()) sendIntentToService(AnyoneBotConstants.ACTION_START_VPN)
        }
    }

    fun refreshMenuList(context: Context) {
        val listItems =
            arrayListOf(MenuAction(R.string.btn_change_exit, 0) { openExitNodeDialog() },
                MenuAction(R.string.btn_refresh, R.drawable.ic_refresh) { sendNewnymSignal() },
                MenuAction(R.string.btn_tor_off, R.drawable.ic_power) { stopTorAndVpn() })
        if (!Prefs.isPowerUserMode()) listItems.add(0,
            MenuAction(R.string.btn_choose_apps, R.drawable.ic_choose_apps) {
                startActivityForResult(
                    Intent(requireActivity(), AppManagerActivity::class.java),
                    AnyoneBotActivity.REQUEST_VPN_APP_SELECT
                )
            })
        lvConnectedActions.adapter = MenuActionAdapter(context, listItems)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AnyoneBotActivity.REQUEST_CODE_VPN && resultCode == AppCompatActivity.RESULT_OK) {
            startAnonAndVpn()
        } else if (requestCode == AnyoneBotActivity.REQUEST_CODE_SETTINGS && resultCode == AppCompatActivity.RESULT_OK) {
            // todo respond to language change extra data here...
        } else if (requestCode == AnyoneBotActivity.REQUEST_VPN_APP_SELECT && resultCode == AppCompatActivity.RESULT_OK) {
            sendIntentToService(AnyoneBotConstants.ACTION_RESTART_VPN) // is this enough todo?
            refreshMenuList(requireContext())
        }
    }

    private fun doLayoutNoInternet() {
        ivOnion.setImageResource(R.drawable.nointernet)

        stopAnimations()

        tvSubtitle.visibility = View.VISIBLE

        progressBar.visibility = View.INVISIBLE
        tvTitle.text = getString(R.string.no_internet_title)
        tvSubtitle.text = getString(R.string.no_internet_subtitle)

        btnStartVpn.visibility = View.GONE
        lvConnectedActions.visibility = View.GONE
    }

    fun doLayoutOn(context: Context) {

        ivOnion.setImageResource(R.drawable.toron)

        tvSubtitle.visibility = View.GONE
        progressBar.visibility = View.INVISIBLE
        tvTitle.text = context.getString(R.string.connected_title)
        btnStartVpn.visibility = View.GONE
        lvConnectedActions.visibility = View.VISIBLE

        refreshMenuList(context)

        ivOnion.setOnClickListener {}
    }

    fun doLayoutOff() {

        ivOnion.setImageResource(R.drawable.toroff)
        stopAnimations()
        tvSubtitle.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
        lvConnectedActions.visibility = View.GONE
        tvTitle.text = getString(R.string.secure_your_connection_title)
        tvSubtitle.text = getString(R.string.secure_your_connection_subtitle)

        with(btnStartVpn) {
            visibility = View.VISIBLE

            val connectStr = context.getString(R.string.action_use)

            text = if (Prefs.isPowerUserMode()) getString(R.string.connect)
            else Html.fromHtml(
                "<big>${getString(R.string.btn_start_vpn)}</big><br/><small>${connectStr}</small>",
                Html.FROM_HTML_MODE_LEGACY
            )


            isEnabled = true
            backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(), R.color.btn_enabled_purple
                )
            )
            setOnClickListener { startAnonAndVpn() }
        }

        ivOnion.setOnClickListener {
            startAnonAndVpn()
        }
    }


    fun doLayoutStarting(context: Context) {

        // torStatsGroup.visibility = View.VISIBLE
        tvSubtitle.visibility = View.GONE
        with(progressBar) {
            progress = 0
            visibility = View.VISIBLE
        }
        ivOnion.setImageResource(R.drawable.torstarting)
        val animHover = AnimationUtils.loadAnimation(context, R.anim.hover)
        animHover.repeatCount = 7
        animHover.repeatMode = Animation.REVERSE
        ivOnion.animation = animHover
        animHover.start()
        val animShadow = AnimationUtils.loadAnimation(context, R.anim.shadow)
        animShadow.repeatCount = 7
        animShadow.repeatMode = Animation.REVERSE
        ivOnionShadow.animation = animShadow
        animShadow.start()

        tvTitle.text = context.getString(R.string.trying_to_connect_title)
        with(btnStartVpn) {
            text = context.getString(android.R.string.cancel)
            isEnabled = true
            backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    context, R.color.btn_enabled_purple
                )
            )
            setOnClickListener {
                stopTorAndVpn()
            }
        }
    }

    override fun tryConnecting() {
        startAnonAndVpn() // TODO for now just start tor and VPN, we need to decouple this down the line
    }

    override fun onExitNodeSelected(countryCode: String, displayCountryName: String) {

        //tor format expects "{" for country code
        Prefs.setExitNodes("{$countryCode}")

        sendIntentToService(
            Intent(
                requireActivity(),
                AnyoneBotService::class.java
            ).setAction(AnyoneBotConstants.CMD_SET_EXIT).putExtra("exit", countryCode)
        )

        refreshMenuList(requireContext())
    }


    /** Sends intent to service, first modifying it to indicate it is not from the system */
    private fun sendIntentToService(intent: Intent) =
        ContextCompat.startForegroundService(requireActivity(), intent.putNotSystem())

    private fun sendIntentToService(action: String) {
        sendIntentToService(Intent(requireActivity(), AnyoneBotService::class.java).apply {
            this.action = action
        })
    }
}
