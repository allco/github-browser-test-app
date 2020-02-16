package se.allco.githubbrowser.app.main.repos

import se.allco.githubbrowser.R
import se.allco.githubbrowser.common.ui.recyclerview.DataBoundAdapter
import javax.inject.Inject

class ReposItemViewModel(val repo: ReposRepository.Repo) : DataBoundAdapter.Item {

    class Factory @Inject constructor() {
        fun create(repo: ReposRepository.Repo): ReposItemViewModel =
            ReposItemViewModel(repo)
    }

    override fun areItemsTheSame(item: DataBoundAdapter.Item): Boolean =
        (item as ReposItemViewModel).repo.id == repo.id

    override fun areContentsTheSame(item: DataBoundAdapter.Item): Boolean =
        (item as ReposItemViewModel).repo == repo

    override fun getLayoutId(): Int = R.layout.main_repos_item
}
