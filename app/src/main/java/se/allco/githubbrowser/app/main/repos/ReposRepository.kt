package se.allco.githubbrowser.app.main.repos

import android.net.Uri
import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.http.GET
import retrofit2.http.Query
import se.allco.githubbrowser.app.user.http.RetrofitFactory
import javax.inject.Inject

class ReposRepository @Inject constructor(private val retrofitFactory: RetrofitFactory) {

    data class Repo(
        val id: Long,
        val name: String?,
        val nameFull: String?,
        val description: String?,
        val uri: Uri?
    )

    /**
     * Provides repositories available for the user.
     * https://developer.github.com/v3/repos/#list-your-repositories
     */
    interface GetRepos {

        data class Repo(
            val id: Long,
            val name: String?,
            @SerializedName("full_name")
            val nameFull: String?,
            val description: String?,
            val url: String
        )

        @GET("/user/repos")
        fun call(@Query("sort") sortOrder: String = "updated"): Single<List<Repo>>
    }

    fun getRepos(): Single<List<Repo>> =
        retrofitFactory
            .create()
            .create(GetRepos::class.java)
            .call()
            .subscribeOn(Schedulers.io())
            .map { list -> list.map { it.asRepo() } }
}

fun ReposRepository.GetRepos.Repo.asRepo() =
    ReposRepository.Repo(
        id = id,
        name = name?.takeIf { it.isNotBlank() },
        nameFull = nameFull?.takeIf { it.isNotBlank() },
        description = description?.takeIf { it.isNotBlank() },
        uri = url.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
    )
