package se.allco.githubbrowser.app.main.repos

import androidx.lifecycle.ViewModel
import se.allco.githubbrowser.common.ui.recyclerview.DataBoundAdapter

class ReposViewModel : ViewModel() {
    val listItems: List<DataBoundAdapter.Item> = emptyList()
}
