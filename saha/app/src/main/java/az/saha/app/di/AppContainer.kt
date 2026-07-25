package az.saha.app.di

import android.content.Context
import az.saha.app.R
import az.saha.app.data.local.SahaDatabase
import az.saha.app.data.remote.FirebaseMeasurementSource
import az.saha.app.data.repository.AuthRepository
import az.saha.app.data.repository.MeasurementRepository
import az.saha.app.location.LocationTracker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val db = SahaDatabase.get(appContext)
    private val remote = FirebaseMeasurementSource()

    val measurementRepository = MeasurementRepository(db.measurementDao(), remote)
    val locationTracker = LocationTracker(appContext)

    private val googleOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(appContext.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    private val googleClient = GoogleSignIn.getClient(appContext, googleOptions)

    val authRepository = AuthRepository(googleClient = googleClient)
}
