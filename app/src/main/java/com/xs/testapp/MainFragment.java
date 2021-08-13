package com.xs.testapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        getView().findViewById(R.id.btn_go1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getChildFragmentManager();
                NavHostFragment nhf = (NavHostFragment) fm.findFragmentById(R.id.nav_host);
                nhf.getNavController().navigate(R.id.oneFragment);
//                Navigation.findNavController(getView()).navigate(R.id.oneFragment);
//                NavHostFragment.findNavController(getView()).navigate(R.id.oneFragment);
            }
        });
        getView().findViewById(R.id.btn_go2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getChildFragmentManager();
                NavHostFragment nhf = (NavHostFragment) fm.findFragmentById(R.id.nav_host);
                nhf.getNavController().navigate(R.id.twoFragment);
//                Navigation.findNavController(getView()).navigate(R.id.twoFragment);
//                NavHostFragment.findNavController(mFragment).navigate(R.id.twoFragment);
            }
        });
        getView().findViewById(R.id.btn_go3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getChildFragmentManager();
                NavHostFragment nhf = (NavHostFragment) fm.findFragmentById(R.id.nav_host);
                nhf.getNavController().navigate(R.id.threeFragment);
//                Navigation.findNavController(getView()).navigate(R.id.threeFragment);
//                NavHostFragment.findNavController(mFragment).navigate(R.id.threeFragment);
            }
        });
    }
}