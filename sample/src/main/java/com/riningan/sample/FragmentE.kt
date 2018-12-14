package com.riningan.sample

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.riningan.frarg.FrargBinder
import com.riningan.frarg.annotations.Argument
import com.riningan.frarg.annotations.ArgumentedFragment
import java.util.*
import kotlinx.android.synthetic.main.fragment_e.*


@ArgumentedFragment
class FragmentE : Fragment() {
    @Argument
    private lateinit var mArgDate: Date

    @Argument(optional = true)
    private var mArgString: String = ""

    @Argument(optional = true)
    private var mArgInt1 = 0

    @Argument(optional = true)
    private var mArgInt2 = 0


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        FrargBinder.bind(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_e, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv.text = mArgDate.toString() + "\n" + mArgString + "\n" + mArgInt1 + "\n" + mArgInt2
    }
}