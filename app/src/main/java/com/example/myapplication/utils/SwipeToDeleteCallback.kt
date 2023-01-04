package com.example.myapplication.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.utils.adapters.PathAdapter
import com.example.myapplication.viewModels.HomeViewModel

class SwipeToDeleteCallback(val context: Context, val adapter: PathAdapter, val viewModel: HomeViewModel)
    : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {


    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        //non necessary, should never be called
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        AlertDialog.Builder(context).setTitle(R.string.delete_path_title).setMessage(R.string.delete_path_message)
                .setOnCancelListener { adapter.notifyDataSetChanged() }.setOnDismissListener { adapter.notifyDataSetChanged() }
                .apply {
                    setPositiveButton(R.string.delete) { dialog, _ ->
                        adapter.deleteItem(position, viewModel, { operationSuccess() }, { operationFailure() })
                        dialog.dismiss()
                    }
                    setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                    }

                }.show()
    }

    private fun operationSuccess() {
        Toast.makeText(context, "Done!", Toast.LENGTH_LONG).show()
        adapter.notifyDataSetChanged()
    }

    private fun operationFailure() {
        Toast.makeText(context, "Database error", Toast.LENGTH_LONG).show()
        adapter.notifyDataSetChanged()
    }
}