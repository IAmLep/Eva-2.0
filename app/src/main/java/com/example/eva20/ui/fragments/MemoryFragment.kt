package com.example.eva20.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eva20.databinding.FragmentMemoryBinding
import com.example.eva20.ui.adapters.MemoryAdapter
import com.example.eva20.ui.viewmodels.MemoryViewModel
import com.example.eva20.utils.Logger
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoryFragment : Fragment() {
    private val tag = "MemoryFragment"
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

        Logger.d(tag, "Fragment created")

        setupViewModel()
        setupRecyclerView()
        setupSwipeRefresh()
        setupAddButton()
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

    private fun setupAddButton() {
        // Make sure this FAB exists in your layout
        binding.addMemoryButton.setOnClickListener {
            showAddMemoryDialog()
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
            Logger.d(tag, "Sync status: $status")
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog(memoryId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Memory")
            .setMessage("Are you sure you want to delete this memory? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                memoryViewModel.deleteMemory(memoryId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddMemoryDialog() {
        // This would typically be a custom dialog with form fields
        // For now, we'll create a test memory
        val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        memoryViewModel.createMemory(
            title = "Memory created on $currentDateTime",
            text = "This is a test memory created from the Memory Fragment",
            importance = 3,
            category = "Test"
        )

        Toast.makeText(context, "Test memory created", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}