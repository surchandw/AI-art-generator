package antsearth.com.imagegenerator.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import antsearth.com.imagegenerator.Constants
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

private const val TAG = "RewardedAdManager"

class RewardedAdManager(private val context: Context) {
    private var rewardedAd: RewardedAd? = null
    private var secondRewardedAd: RewardedAd? = null

    fun loadRewardedAd(onAdLoaded: () -> Unit = {},
                       onAdFailedToLoad: (LoadAdError) -> Unit = {}
    ) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, Constants.REWARDED_AD_ID, adRequest,
            object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                onAdLoaded()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
                onAdFailedToLoad(adError)
            }
        })
    }

    fun loadSecondRewardedAd(onAdLoaded: () -> Unit = {},
                       onAdFailedToLoad: (LoadAdError) -> Unit = {}
    ) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, Constants.SECOND_REWARDED_AD_ID, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    secondRewardedAd = ad
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    secondRewardedAd = null
                    onAdFailedToLoad(adError)
                }
            })
    }

    fun showRewardedAd(activity: Activity, onAdShown: () -> Unit = {},
                       onAdClosed: () -> Unit = {}) {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
                //onAdShown()
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed fullscreen content.")
                onAdClosed()
                rewardedAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show fullscreen content.")
                rewardedAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
                rewardedAd = null
                //onAdShown()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
                rewardedAd = null
                //onAdShown()
            }
        }
        rewardedAd?.show(activity) {
            Log.d(TAG, "Rewarded Ad shown completely. Earned reward")
            onAdShown()
        }
    }

    fun showSecondRewardedAd(activity: Activity, onAdShown: () -> Unit = {}) {
        secondRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Second rewardedAd was clicked.")
                //onAdShown()
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Second rewardedAd dismissed fullscreen content.")
                secondRewardedAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Second rewardedAd failed to show fullscreen content.")
                secondRewardedAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "second rewardedAd recorded an impression.")
                secondRewardedAd = null
                //onAdShown()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "second rewardedAd showed fullscreen content.")
                secondRewardedAd = null
                //onAdShown()
            }
        }
        secondRewardedAd?.show(activity) {
            Log.d(TAG, "Second rewardedAd shown completely. Earned reward")
            onAdShown()
        }
    }
    fun isSecondRewardedAdLoaded() = secondRewardedAd != null

}
