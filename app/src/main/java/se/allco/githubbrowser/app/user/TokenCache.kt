package se.allco.githubbrowser.app.user

import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import io.reactivex.Completable
import io.reactivex.Maybe
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * The Github token storage.
 * Uses SharedPreferences under hood.
 * All the methods return reactive streams which is not necessary for the current implementation since all the actions
 * are pretty quick and can be be invoked synchronously. But I think it is better to have it like this since it makes
 * the app ready for using asynchronous storage (other then SharedPreferences) under hood here later in future.
 */

@Singleton
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

    fun erase(): Completable = Completable.fromAction {
        prefs.edit().apply { remove(KEY) }.apply()
    }
}
