package tr.main.rearth

import android.app.Application
import androidx.lifecycle.*
import tr.main.rearth.ViewModels.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.onesignal.OneSignal


const val ONESIGNAL_APP_ID = "05ee3b90-dd7f-45dc-82cf-1af6212e4cd4"
class MyApplication : Application(), LifecycleObserver {
    private lateinit var profileViewModels: ProfileViewModel
    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate() {
        super.onCreate()
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth?.currentUser != null) {
            profileViewModels = ViewModelProvider.AndroidViewModelFactory.getInstance(this).create(
                ProfileViewModel::class.java)
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Logging set to help debug issues, remove before releasing your app.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onAppForegrounded() {
        // code to be executed when the app is brought to the foreground
        if (firebaseAuth?.currentUser != null) {
            profileViewModels.updateStatus("online")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onAppBackgrounded() {
        // code to be executed when the app is moved to the background
        firebaseAuth?.currentUser?.let {
            if (!::profileViewModels.isInitialized) {
                profileViewModels = ViewModelProvider.AndroidViewModelFactory.getInstance(this).create(
                    ProfileViewModel::class.java)
            }
            profileViewModels.updateStatus("offline")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onAppDestroyed() {
        // code to be executed when the app is fully closed
        if (firebaseAuth?.currentUser != null) {
            profileViewModels.updateStatus("offline")
        }
    }
}

