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

import kotlinx.android.synthetic.main.fragment_d.*


@ArgumentedFragment(alias = "FragmentNotD")
class FragmentD : Fragment() {
    @Argument
    internal var mArgumentString: String? = null

    @Argument
    internal var mArgumentInt = 0

    @Argument
    lateinit var mArgumentEnum: Arg


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        FrargBinder.bind(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_d, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv.text = mArgumentString + "\n" + mArgumentInt + "\n" + mArgumentEnum.name
    }

    enum class Arg {
        FIRST, SECOND
    }
}