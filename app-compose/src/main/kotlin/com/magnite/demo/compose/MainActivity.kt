package com.magnite.demo.compose

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.magnite.sdk.ads.banner.BannerCreator
import com.magnite.sdk.ads.banner.BannerFormat
import com.magnite.sdk.ads.banner.BannerListener
import com.magnite.sdk.ads.banner.BannerRequest
import com.magnite.sdk.ads.nativead.MagniteNativeAd
import com.magnite.sdk.ads.nativead.NativeAdDetails
import com.magnite.sdk.ads.nativead.NativeAdPreferences
import com.magnite.sdk.adsbase.Ad
import com.magnite.sdk.adsbase.MagniteInterstitialAd
import com.magnite.sdk.adsbase.MagniteRewardedAd
import com.magnite.sdk.adsbase.MagniteSDK
import com.magnite.sdk.adsbase.adlisteners.AdDisplayListener
import com.magnite.sdk.adsbase.adlisteners.AdEventListener
import com.magnite.sdk.adsbase.model.AdPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Suppress("SameParameterValue")
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private companion object {
        val LOG_TAG: String = MainActivity::class.java.simpleName

        const val APP_ID = "123"
        const val INTERSTITIAL_AD_TAG = ""
        const val REWARDED_AD_TAG = ""
        const val BANNER_AD_TAG = ""
        const val MREC_AD_TAG = ""
        const val NATIVE_AD_TAG = ""

        val spaceNormal = 8.dp
        val spaceHalf = 4.dp
        val initializedFlow = MutableStateFlow<Boolean?>(null)
    }

    internal enum class AdState {
        IDLE,
        LOADING,
        VISIBLE,
    }

    private val interstitialAdFlow = MutableStateFlow<Pair<MagniteInterstitialAd?, AdState>?>(null)
    private val rewardedAdFlow = MutableStateFlow<Pair<MagniteRewardedAd?, AdState>?>(null)
    private val bannerAdFlow = MutableStateFlow<Pair<View?, AdState>?>(null)
    private val mrecAdFlow = MutableStateFlow<Pair<View?, AdState>?>(null)
    private val nativeAdFlow = MutableStateFlow<Pair<View?, AdState>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                initializedFlow.collect {
                    if (it == null) {
                        initializedFlow.value = false

                        MagniteSDK.initParams(applicationContext, APP_ID)
                            .setCallback { initializedFlow.value = true }
                            .init()
                    }
                }
            }
        }

        // region Compose layout

        setContent {
            val helper = helper()

            Scaffold(
                topBar = {
                    Surface(shadowElevation = spaceHalf) {
                        TopAppBar(
                            title = {
                                SingleLineText("Magnite SDK ${MagniteSDK.getVersion()}")
                            }
                        )
                    }
                },
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(spaceHalf),
                ) {
                    Spacer(modifier = Modifier.height(spaceHalf))

                    LoadShowRow(
                        helper = helper,
                        state = helper.interstitialAd,
                        onLoad = { loadInterstitial(INTERSTITIAL_AD_TAG) },
                        onShow = ::showInterstitial,
                        text = "Interstitial",
                    )

                    LoadShowRow(
                        helper = helper,
                        state = helper.rewardedAd,
                        onLoad = { loadRewarded(REWARDED_AD_TAG) },
                        onShow = ::showRewarded,
                        text = "Rewarded",
                    )

                    LoadShowHideRow(
                        helper = helper,
                        state = helper.bannerAd,
                        onLoad = { loadBanner(BANNER_AD_TAG) },
                        onShow = { showBanner(it) },
                        onHide = { hideBanner(it) },
                        text = "Banner",
                    )

                    LoadShowHideRow(
                        helper = helper,
                        state = helper.mrecAd,
                        onLoad = { loadMrec(MREC_AD_TAG) },
                        onShow = { showMrec(it) },
                        onHide = { hideMrec(it) },
                        text = "Mrec",
                    )

                    LoadShowHideRow(
                        helper = helper,
                        state = helper.nativeAd,
                        onLoad = { loadNative(NATIVE_AD_TAG) },
                        onShow = { showNative(it) },
                        onHide = { hideNative(it) },
                        text = "Native",
                    )

                    if (helper.isAdVisible(helper.bannerAd)) {
                        helper.bannerAd.value?.first?.let { adView ->
                            AndroidView(
                                modifier = Modifier.fillMaxWidth(),
                                factory = { adView },
                            )
                        }
                    }

                    if (helper.isAdVisible(helper.mrecAd)) {
                        helper.mrecAd.value?.first?.let { adView ->
                            AndroidView(
                                modifier = Modifier.fillMaxWidth(),
                                factory = { adView },
                            )
                        }
                    }

                    if (helper.isAdVisible(helper.nativeAd)) {
                        helper.nativeAd.value?.first?.let { adView ->
                            val adDetails = adView.tag as? NativeAdDetails

                            NativeAdLayout(
                                title = adDetails?.title ?: "",
                                description = adDetails?.description ?: "",
                                rating = adDetails?.rating,
                                category = adDetails?.category,
                                callToAction = adDetails?.callToAction,
                                bitmap = adDetails?.imageBitmap?.asImageBitmap(),
                                factory = { context -> adView },
                            )
                        }
                    }
                }
            }
        }

        // endregion
    }

    // region Helper

    @Composable
    private fun helper(): Helper = Helper(
        initialized = initializedFlow.collectAsState().let { remember { it } },
        interstitialAd = interstitialAdFlow.collectAsState().let { remember { it } },
        rewardedAd = rewardedAdFlow.collectAsState().let { remember { it } },
        bannerAd = bannerAdFlow.collectAsState().let { remember { it } },
        mrecAd = mrecAdFlow.collectAsState().let { remember { it } },
        nativeAd = nativeAdFlow.collectAsState().let { remember { it } },
    )

    internal class Helper(
        val initialized: State<Boolean?>,
        val interstitialAd: State<Pair<MagniteInterstitialAd?, AdState>?>,
        val rewardedAd: State<Pair<MagniteRewardedAd?, AdState>?>,
        val bannerAd: State<Pair<View?, AdState>?>,
        val mrecAd: State<Pair<View?, AdState>?>,
        val nativeAd: State<Pair<View?, AdState>?>,
    ) {
        private fun isInitialized(): Boolean = initialized.value ?: false

        fun isLoadButtonEnabled(state: State<Pair<Any?, AdState>?>): Boolean {
            val pair = state.value
            return (pair == null || pair.first == null && pair.second != AdState.LOADING) && isInitialized()
        }

        fun isShowButtonEnabled(state: State<Pair<Any?, AdState>?>): Boolean {
            val pair = state.value
            return pair?.first != null && pair.second != AdState.VISIBLE
        }

        fun isHideButtonEnabled(state: State<Pair<Any?, AdState>?>): Boolean {
            val pair = state.value
            return pair != null && pair.second == AdState.VISIBLE
        }

        // simple helper method created for clarity
        fun isAdVisible(state: State<Pair<Any?, AdState>?>): Boolean = isHideButtonEnabled(state)
    }

    // endregion

    // region Interstitial

    private fun loadInterstitial(adTag: String?) {
        val adPreferences = AdPreferences()
        adPreferences.adTag = adTag

        MagniteInterstitialAd(this).let {
            it.loadAd(adPreferences, object : AdEventListener {
                override fun onReceiveAd(ad: Ad) {
                    Log.v(LOG_TAG, "loadInterstitial: onReceiveAd")

                    interstitialAdFlow.value = Pair(it, AdState.IDLE)
                }

                override fun onFailedToReceiveAd(ad: Ad?) {
                    Log.v(LOG_TAG, "loadInterstitial: onFailedToReceiveAd: ${ad?.errorMessage}")

                    interstitialAdFlow.value = null
                }
            })
        }
    }

    private fun showInterstitial(ad: MagniteInterstitialAd?) {
        ad?.let {
            interstitialAdFlow.value = null

            it.showAd(object : AdDisplayListener {
                override fun adHidden(ad: Ad) {
                    Log.v(LOG_TAG, "showInterstitial: adHidden")
                }

                override fun adDisplayed(ad: Ad) {
                    Log.v(LOG_TAG, "showInterstitial: adDisplayed")
                }

                override fun adClicked(ad: Ad) {
                    Log.v(LOG_TAG, "showInterstitial: adClicked")
                }

                override fun adNotDisplayed(ad: Ad) {
                    Log.v(LOG_TAG, "showInterstitial: adNotDisplayed")
                }
            })
        } ?: run {
            Toast.makeText(this, "Interstitial is not ready", Toast.LENGTH_SHORT).show()
        }
    }

    // endregion

    // region Rewarded

    private fun loadRewarded(adTag: String?) {
        val adPreferences = AdPreferences()
        adPreferences.adTag = adTag

        MagniteRewardedAd(this).let {
            it.loadAd(adPreferences, object : AdEventListener {
                override fun onReceiveAd(ad: Ad) {
                    Log.v(LOG_TAG, "loadRewarded: onReceiveAd")

                    rewardedAdFlow.value = Pair(it, AdState.IDLE)
                }

                override fun onFailedToReceiveAd(ad: Ad?) {
                    Log.v(LOG_TAG, "loadRewarded: onFailedToReceiveAd: ${ad?.errorMessage}")

                    rewardedAdFlow.value = null
                }
            })
        }
    }

    private fun showRewarded(ad: MagniteRewardedAd?) {
        ad?.let {
            rewardedAdFlow.value = null

            it.setOnUserRewardedListener {
                Toast.makeText(this, "User gained a reward", Toast.LENGTH_SHORT).show()
            }

            it.showAd(object : AdDisplayListener {
                override fun adHidden(ad: Ad) {
                    Log.v(LOG_TAG, "showRewarded: adHidden")
                }

                override fun adDisplayed(ad: Ad) {
                    Log.v(LOG_TAG, "showRewarded: adDisplayed")
                }

                override fun adClicked(ad: Ad) {
                    Log.v(LOG_TAG, "showRewarded: adClicked")
                }

                override fun adNotDisplayed(ad: Ad) {
                    Log.v(LOG_TAG, "showRewarded: adNotDisplayed")
                }
            })
        } ?: run {
            Toast.makeText(this, "Rewarded is not ready", Toast.LENGTH_SHORT).show()
        }
    }

    // endregion

    // region Banner & Mrec

    private fun loadBanner(adTag: String) {
        loadAdView(
            adTag = adTag,
            format = BannerFormat.BANNER,
            flow = bannerAdFlow,
        )
    }

    private fun loadMrec(adTag: String) {
        loadAdView(
            adTag = adTag,
            format = BannerFormat.MREC,
            flow = mrecAdFlow,
        )
    }

    @Suppress("SameParameterValue")
    private fun loadAdView(adTag: String, format: BannerFormat, flow: MutableStateFlow<Pair<View?, AdState>?>) {
        val adPreferences = AdPreferences()
        adPreferences.adTag = adTag

        BannerRequest(applicationContext)
            .setAdFormat(format)
            .setAdPreferences(adPreferences)
            .load { creator: BannerCreator?, error: String? ->
                if (creator != null) {
                    val adView = creator.create(applicationContext, object : BannerListener {
                        override fun onReceiveAd(banner: View) {
                            Log.v(LOG_TAG, "loadAdView: onReceiveAd")
                        }

                        override fun onFailedToReceiveAd(banner: View) {
                            Log.v(LOG_TAG, "loadAdView: onFailedToReceiveAd")
                        }

                        override fun onImpression(banner: View) {
                            Log.v(LOG_TAG, "loadAdView: onImpression")
                        }

                        override fun onClick(banner: View) {
                            Log.v(LOG_TAG, "loadAdView: onClick")
                        }
                    })

                    flow.value = Pair(adView, AdState.IDLE)
                } else {
                    Log.e(LOG_TAG, "loadAdView: error: $error")

                    flow.value = null
                }
            }

        flow.value = Pair(null, AdState.LOADING)
    }

    private fun showBanner(adView: View?) {
        showAdView(adView, bannerAdFlow)
    }

    private fun showMrec(adView: View?) {
        showAdView(adView, mrecAdFlow)
    }

    private fun showAdView(adView: View?, flow: MutableStateFlow<Pair<View?, AdState>?>) {
        adView?.let {
            flow.value = Pair(it, AdState.VISIBLE)
        }
    }

    private fun hideBanner(adView: View?) {
        hideAdView(adView, bannerAdFlow)
    }

    private fun hideMrec(adView: View?) {
        hideAdView(adView, mrecAdFlow)
    }

    private fun hideAdView(adView: View?, flow: MutableStateFlow<Pair<View?, AdState>?>) {
        adView?.let { v ->
            v.parent?.let { p ->
                if (p is ViewGroup) {
                    p.removeView(v)
                }
            }
        }

        flow.value = null
    }

    // endregion

    // region Native

    private fun loadNative(adTag: String?) {
        nativeAdFlow.value = Pair(null, AdState.LOADING)

        val adPreferences = NativeAdPreferences()
        adPreferences.adTag = adTag
        adPreferences.isAutoBitmapDownload = true

        val nativeAd = MagniteNativeAd(applicationContext)
        nativeAd.setPreferences(adPreferences)
        nativeAd.loadAd(object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                Log.v(LOG_TAG, "loadNative: onReceiveAd")

                val nativeAds = nativeAd.nativeAds
                if (nativeAds != null && nativeAds.isNotEmpty()) {
                    val nativeAdDetails = nativeAds[0]
                    if (nativeAdDetails != null) {
                        nativeAdFlow.value = Pair(View(this@MainActivity).apply {
                            tag = nativeAdDetails
                        }, AdState.IDLE)
                    } else {
                        nativeAdFlow.value = null
                    }
                } else {
                    nativeAdFlow.value = null
                }
            }

            override fun onFailedToReceiveAd(ad: Ad?) {
                Log.v(LOG_TAG, "loadNative: onFailedToReceiveAd: " + (ad?.errorMessage))

                nativeAdFlow.value = null
            }
        })
    }

    private fun showNative(adView: View?) {
        adView?.let {
            nativeAdFlow.value = Pair(it, AdState.VISIBLE)
        }
    }

    private fun hideNative(adView: View?) {
        adView?.tag?.let {
            if (it is NativeAdDetails) {
                it.unregisterView()
            }
        }

        nativeAdFlow.value = null
    }

    // endregion

    // region Auxiliary Composable functions

    @Composable
    private fun <T> LoadShowRow(
        helper: Helper,
        state: State<Pair<T?, AdState>?>,
        onLoad: () -> Unit,
        onShow: (ad: T?) -> Unit,
        text: String,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = spaceNormal)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onLoad,
                enabled = helper.isLoadButtonEnabled(state),
            ) {
                SingleLineText("Load $text")
            }
            Spacer(modifier = Modifier.width(spaceNormal))
            Button(
                modifier = Modifier.weight(1f),
                onClick = { onShow(state.value?.first) },
                enabled = helper.isShowButtonEnabled(state),
            ) {
                SingleLineText("Show $text")
            }
        }
    }

    @Composable
    private fun <T> LoadShowHideRow(
        helper: Helper,
        state: State<Pair<T?, AdState>?>,
        onLoad: () -> Unit,
        onShow: (ad: T?) -> Unit,
        onHide: (ad: T?) -> Unit,
        text: String,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = spaceNormal)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onLoad,
                enabled = helper.isLoadButtonEnabled(state),
            ) {
                SingleLineText("Load $text")
            }
            Spacer(modifier = Modifier.width(spaceNormal))
            Button(
                modifier = Modifier.weight(1f),
                onClick = { onShow(state.value?.first) },
                enabled = helper.isShowButtonEnabled(state),
            ) {
                SingleLineText("Show $text")
            }
            Spacer(modifier = Modifier.width(spaceNormal))
            Button(
                modifier = Modifier.weight(1f),
                onClick = { onHide(state.value?.first) },
                enabled = helper.isHideButtonEnabled(state),
            ) {
                SingleLineText("Hide $text")
            }
        }
    }

    @Composable
    fun SingleLineText(text: String) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    // endregion
}
