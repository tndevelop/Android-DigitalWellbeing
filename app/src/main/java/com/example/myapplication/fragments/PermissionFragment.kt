package com.example.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class PermissionFragment: Fragment() {

    private lateinit var textViewTitle: TextView
    private lateinit var textViewDescription: TextView
    private lateinit var permissionButton: Button

    companion object {
        var onGivePermission: View.OnClickListener? = null
        lateinit var title: String
        lateinit var description: String

        fun newInstance(titles: String, descriptions: String, onClickListener: View.OnClickListener?): PermissionFragment {
            val fragment = PermissionFragment()
            onGivePermission = onClickListener!!
            title = titles
            description = descriptions
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_permission, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textViewTitle = view.findViewById(R.id.title)
        textViewDescription = view.findViewById(R.id.description)
        permissionButton = view.findViewById(R.id.button_give_permission)

        textViewTitle.text = title
        textViewDescription.text = description

        if (onGivePermission != null) {
            permissionButton.setOnClickListener(onGivePermission)
        }
    }
}