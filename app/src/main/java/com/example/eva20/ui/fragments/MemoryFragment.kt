package com.example.eva20.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eva20.databinding.FragmentMemoryBinding
import com.example.eva20.ui.adapters.MemoryAdapter
import com.example.eva20.ui.viewmodels.MemoryViewModel
import com.example.eva20.utils.Logger

class MemoryFragment : Fragment() {

    private var _binding: FragmentMemoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var memoryViewModel: MemoryViewModel
    private lateinit var memoryAdapter: MemoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.d("MemoryFragment", "Fragment created")

        setupViewModel()
        setupRecyclerView()
        setupSwipeRefresh()
        observeData()
    }

    private fun setupViewModel() {
        memoryViewModel = ViewModelProvider(this)[MemoryViewModel::class.java]
    }

    private fun setupRecyclerView() {
        memoryAdapter = MemoryAdapter { memory ->
            showDeleteConfirmationDialog(memory.id)
        }

        binding.recyclerViewMemories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = memoryAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            memoryViewModel.syncWithCloud()
        }
    }

    private fun observeData() {
        memoryViewModel.memories.observe(viewLifecycleOwner) { memories ->
            memoryAdapter.submitList(memories)
            binding.emptyView.visibility = if (memories.isEmpty()) View.VISIBLE else View.GONE
        }

        memoryViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        memoryViewModel.syncStatus.observe(viewLifecycleOwner) { status ->
            Logger.d("MemoryFragment", "Sync status: $status")
            // Could show a toast here
        }
    }

    private fun showDeleteConfirmationDialog(memoryId: String) {
        // Show alert dialog to confirm deletion
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Memory")
            .setMessage("Are you sure you want to delete this memory? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                memoryViewModel.deleteMemory(memoryId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}