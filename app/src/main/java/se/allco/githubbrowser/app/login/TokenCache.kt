package se.allco.githubbrowser.app.login

import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import io.reactivex.Completable
import io.reactivex.Maybe
import se.allco.githubbrowser.app.user.GithubToken
import se.allco.githubbrowser.app.user.User
import javax.inject.Inject
import javax.inject.Provider

class TokenCache @Inject constructor(
    private val prefs: SharedPreferences,
    private val gsonBuilderProvider: Provider<GsonBuilder>
) {

    companion object {
        private val KEY = TokenCache::class.java.name + ".User"
    }

    fun read(): Maybe<GithubToken> =
        Maybe.create { emitter ->
            prefs.getString(KEY, null).takeUnless { it.isNullOrBlank() }
                ?.let { userInJson ->
                    emitter.onSuccess(
                        gsonBuilderProvider
                            .get()
                            .create()
                            .fromJson(userInJson, GithubToken::class.java)
                    )
                }
                ?: emitter.onComplete()
        }

    fun write(user: User.Valid): Completable =
        Completable.fromAction {
            prefs.edit().apply { putString(KEY, gsonBuilderProvider.get().create().toJson(user.token)) }.apply()
        }
}
