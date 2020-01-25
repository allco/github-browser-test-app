package se.allco.githubbrowser.app.main.repos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import se.allco.githubbrowser.databinding.FragmentReposBinding
import javax.inject.Inject

class ReposFragment @Inject constructor() : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        FragmentReposBinding.inflate(inflater, container, false).root
}
