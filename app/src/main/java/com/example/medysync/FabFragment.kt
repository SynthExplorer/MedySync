package com.example.medysync

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FabFragment : Fragment() {

    private var isFabOpen = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_fab, container, false)

        val fabPrincipal = view.findViewById<FloatingActionButton>(R.id.fabPrincipal)
        val fabOpcion1 = view.findViewById<FloatingActionButton>(R.id.fabOpcion1)
        val fabOpcion2 = view.findViewById<FloatingActionButton>(R.id.fabOpcion2)
        val fabHome = view.findViewById<FloatingActionButton>(R.id.fabHome)
        val fabSeetings = view.findViewById<FloatingActionButton>(R.id.fabSeetings)

        fabPrincipal.setOnClickListener { toggleFabMenu(fabOpcion1, fabOpcion2, fabHome, fabSeetings) }

        fabOpcion1.setOnClickListener {
            if (requireActivity() !is Actividad1) {
                startActivity(Intent(requireContext(), Actividad1::class.java))
            }else {
                Toast.makeText(requireContext(), "Ya estás en esta vista", Toast.LENGTH_SHORT).show()
            }
        }

        fabOpcion2.setOnClickListener {
            if (requireActivity() !is Actividad2) {
                startActivity(Intent(requireContext(), Actividad2::class.java))
            }else {
                Toast.makeText(requireContext(), "Ya estás en esta vista", Toast.LENGTH_SHORT).show()
            }
        }

        fabHome.setOnClickListener {
            if (requireActivity() !is MainActivity) {
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }else {
                Toast.makeText(requireContext(), "Ya estás en esta vista", Toast.LENGTH_SHORT).show()
            }
        }

        fabSeetings.setOnClickListener {
            if (requireActivity() !is SeetingsActivity) {
                startActivity(Intent(requireContext(), SeetingsActivity::class.java))
            }else {
                Toast.makeText(requireContext(), "Ya estás en esta vista", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }

    private fun toggleFabMenu(fabOpcion1: FloatingActionButton, fabOpcion2: FloatingActionButton, fabHome: FloatingActionButton, fabSeetings: FloatingActionButton) {
        if (isFabOpen) {
            fabOpcion1.animate().translationY(0f).alpha(0f).setDuration(1300).withEndAction {
                fabOpcion1.visibility = View.GONE
            }
            fabOpcion2.animate().translationY(0f).alpha(0f).setDuration(900).withEndAction {
                fabOpcion2.visibility = View.GONE
            }
            fabHome.animate().translationY(0f).alpha(0f).setDuration(500).withEndAction {
                fabHome.visibility = View.GONE
            }
            fabSeetings.animate().translationY(0f).alpha(0f).setDuration(300).withEndAction {
                fabSeetings.visibility = View.GONE
            }

        } else {
            fabOpcion1.visibility = View.VISIBLE
            fabOpcion1.animate().translationY(-200f).alpha(1f).setDuration(200).start()

            fabOpcion2.visibility = View.VISIBLE
            fabOpcion2.animate().translationY(-400f).alpha(1f).setDuration(600).start()

            fabHome.visibility = View.VISIBLE
            fabHome.animate().translationY(-600f).alpha(1f).setDuration(800).start()

            fabSeetings.visibility = View.VISIBLE
            fabSeetings.animate().translationY(-800f).alpha(1f).setDuration(1200).start()
        }

        isFabOpen = !isFabOpen
    }
}
